package com.ims.fees.api;

import com.ims.fees.model.BatchFee;
import com.ims.fees.utils.ApiUtility;
import com.ims.fees.utils.TextUtils;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The call the page makes when a centre is chosen: POST /update-cart/ with action=getBatchesNew.
 * Response with batches: {"batch":[{"TaxAmount":17100,"BaseFees":95000,"code":null,"batch":"...","description":"..."}]}
 * Response without batches: {"Message1":"No Record Found"} (also HTTP 200, so the body is what we check).
 */
public class BatchApi {

	public static List<BatchFee> getBatches(String baseUrl, String centreCode, String variantCode) {
		String body = "action=getBatchesNew" + "&txtcentre=" + URLEncoder.encode(centreCode, StandardCharsets.UTF_8)
				+ "&txtvariant=" + URLEncoder.encode(variantCode, StandardCharsets.UTF_8);
		Map<String, String> headers = Map.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8",
				"X-Requested-With", "XMLHttpRequest", "User-Agent", "Mozilla/5.0");
		return parse(ApiUtility.sendPost(baseUrl + "/update-cart/", body, headers));
	}

	static List<BatchFee> parse(String response) {
		JSONObject json = new JSONObject(response);
		List<BatchFee> fees = new ArrayList<>();
		if (!json.has("batch")) {
			if ("No Record Found".equalsIgnoreCase(json.optString("Message1"))) {
				return fees; // no batch for this centre
			}
			throw new IllegalStateException("Unexpected API response: " + response);
		}
		JSONArray batches = json.getJSONArray("batch");
		for (int i = 0; i < batches.length(); i++) {
			JSONObject b = batches.getJSONObject(i);
			String code = b.isNull("code") ? "" : b.get("code").toString();
			int base = b.optInt("BaseFees", -1);
			int tax = b.optInt("TaxAmount", -1);
			// description is "Batch Start Date: 5-2-2026<BR>Batch Type: Weekday"
			String description = b.optString("description", "").replaceAll("(?i)<br\\s*/?>", "\n");
			fees.add(new BatchFee(code, b.optString("batch", ""), base, effectiveRate(base, tax), tax,
					TextUtils.find(description, "Batch Start Date:\\s*(\\S+)"),
					TextUtils.find(description, "Batch Type:[ \\t]*(.*)")));
		}
		return fees;
	}

	/** The API gives no GST rate, so it is worked out from its own numbers: 17100 on 95000 is 18. */
	private static int effectiveRate(int base, int tax) {
		return base > 0 && tax >= 0 ? (int) Math.round(tax * 100.0 / base) : -1;
	}
}
