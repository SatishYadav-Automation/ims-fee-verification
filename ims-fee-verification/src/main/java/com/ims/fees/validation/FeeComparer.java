package com.ims.fees.validation;

import com.ims.fees.model.BatchFee;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;

public class FeeComparer {

	/**
	 * Passes silently when the page shows exactly the expected batches, in any
	 * order. Otherwise attaches the difference to Allure and fails the test. (A fee
	 * of -1 means the page value could not be read.)
	 */
	public static void verify(String title, List<BatchFee> expected, List<BatchFee> actual) {
		List<BatchFee> notShown = new ArrayList<>(expected);
		notShown.removeAll(actual); // expected, but not shown exactly like that

		List<BatchFee> notExpected = new ArrayList<>(actual);
		notExpected.removeAll(expected); // shown, but not expected exactly like that

		if (notShown.isEmpty() && notExpected.isEmpty() && expected.size() == actual.size()) {
			return;
		}
		StringBuilder report = new StringBuilder("Batches expected: " + expected.size() + ", shown: " + actual.size());
		for (BatchFee batch : notShown) {
			report.append("\n  EXPECTED: ").append(batch);
		}
		for (BatchFee batch : notExpected) {
			report.append("\n  SHOWN:    ").append(batch);
		}
		Assert.fail("Fee differences for " + title + ":\n" + report);
	}
}