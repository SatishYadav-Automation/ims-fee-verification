package com.ims.fees.tests;

import com.ims.fees.api.BatchApi;
import com.ims.fees.core.ConfigReader;
import com.ims.fees.data.ExpectedFeeData;
import com.ims.fees.model.BatchFee;
import com.ims.fees.model.Scenario;
import com.ims.fees.validation.FeeComparer;
import java.io.IOException;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FeeApiTest {

	@DataProvider(name = "apiScenarios", parallel = true)
	public Object[][] apiScenarios() throws IOException {
		List<Scenario> scenarios = ExpectedFeeData.scenarios().stream().filter(Scenario::hasApiData).toList();

		Object[][] data = new Object[scenarios.size()][1];
		for (int row = 0; row < scenarios.size(); row++) {
			data[row][0] = scenarios.get(row);
		}
		return data;
	}

	@Test(groups = "api", dataProvider = "apiScenarios")
	public void apiFeesMatchExcel(Scenario scenario) throws IOException {
		List<BatchFee> expected = ExpectedFeeData.batches(scenario);
		List<BatchFee> actual = BatchApi.getBatches(ConfigReader.baseUrl(), scenario.centreCode(),
				scenario.variantCode());
		FeeComparer.verify("API " + scenario, expected, actual);
	}
}