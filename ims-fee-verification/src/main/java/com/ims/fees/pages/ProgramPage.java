package com.ims.fees.pages;

import com.ims.fees.model.BatchFee;
import com.ims.fees.utils.NavigationUtils;
import com.ims.fees.utils.TextUtils;
import com.ims.fees.utils.WaitUtils;
import com.ims.fees.wrappers.Element;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProgramPage extends BasePage {

	/** The whole page. */
	private static final By ROOT = By.tagName("main");
	/**
	 * The "Select Batch" block that holds the cards (this is what we scroll to).
	 */
	private static final By BATCH_SECTION = By.className("select_batch");
	/** One card per batch. */
	private static final By CARDS = By.cssSelector(".select_batch .form-check");

	// Inside one card, each of these finds exactly one element:
	private static final By RADIO = By.cssSelector("input[type='radio']");
	private static final By NAME = By.cssSelector(".batch-details label");
	private static final By DETAILS = By.cssSelector(".batch-details p"); // start date and batch type
	private static final By BASE_FEE = By.className("batch-total-price");
	private static final By GST = By.className("batch-price-details");

	public ProgramPage(WebDriver driver) {
		super(driver);
	}

	@Override
	protected By pageRootLocator() {
		return ROOT;
	}

	/**
	 * Reads every batch card as the user sees it. Cards are only read, never
	 * selected. Waits for the batch section to appear (on classroom pages it
	 * appears after the centre is chosen). Returns an empty list when the page says
	 * "Batch not available!".
	 */
	public List<BatchFee> readBatchCards() {
		WebElement batchSection;
		try {
			assertPageLoaded(); // the page root (main) is present and visible
			batchSection = WaitUtils.waitForVisibility(BATCH_SECTION); // the "Select Batch" block
		} catch (TimeoutException e) {
			throw new AssertionError("Batch section did not appear on " + NavigationUtils.getCurrentUrl()
					+ " (page title: '" + driver.getTitle() + "'). Check that the page exists and is not a 404, "
					+ "and on classroom pages that the chosen centre returned a result.");
		}
		// Bring the batch section to the middle of the screen, so the cards (and any
		// screenshot) are visible
		Element.wrap(batchSection).scrollIntoView();
		if (batchSection.getText().contains("Batch not available")) {
			return new ArrayList<>();
		}

		List<WebElement> cards;
		try {
			cards = WaitUtils.waitForVisibilityOfAllElements(CARDS);
		} catch (TimeoutException e) {
			throw new AssertionError("Batch cards not visible on " + NavigationUtils.getCurrentUrl());
		}

		List<BatchFee> fees = new ArrayList<>();
		for (WebElement card : cards) {
		
			String code = WaitUtils.waitForPresence(card, RADIO).getDomAttribute("value").split("@@")[0];
			String name = WaitUtils.waitForPresence(card, NAME).getDomProperty("textContent").trim();
			String details = visibleText(card, DETAILS);
			String gst = visibleText(card, GST);
			fees.add(new BatchFee("null".equals(code) ? "" : code, name, rupees(visibleText(card, BASE_FEE)),
					percent(gst), rupees(gst), TextUtils.find(details, "Batch Start Date:\\s*(\\S+)"),
					TextUtils.find(details, "Batch Type:[ \\t]*(.*)")));
		}
		return fees;
	}

	/** Text of an element inside a card, read after it is present and visible. */
	private String visibleText(WebElement card, By child) {
		return WaitUtils.waitForVisibility(WaitUtils.waitForPresence(card, child)).getText();
	}

	/** "₹300000/-" -> 300000 ; "+ GST(18%): ₹54000" -> 54000 ; 0 if no amount. */
	static int rupees(String text) {
		String amount = TextUtils.find(text, "\u20B9\\s*([0-9,]+)").replace(",", "");
		return amount.isEmpty() ? 0 : Integer.parseInt(amount);
	}

	/** "+ GST(18%): ₹54000" -> 18 ; 0 if no rate. */
	static int percent(String text) {
		String value = TextUtils.find(text, "\\((\\d+)\\s*%\\)");
		return value.isEmpty() ? 0 : Integer.parseInt(value);
	}
}