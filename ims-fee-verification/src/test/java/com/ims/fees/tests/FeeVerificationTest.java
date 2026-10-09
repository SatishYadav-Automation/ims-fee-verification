package com.ims.fees.tests;

import com.ims.fees.base.TestBase;
import com.ims.fees.core.ConfigReader;
import com.ims.fees.data.ExpectedFeeData;
import com.ims.fees.model.BatchFee;
import com.ims.fees.model.Scenario;
import com.ims.fees.pages.ClassroomProgramPage;
import com.ims.fees.pages.ProgramPage;
import com.ims.fees.utils.NavigationUtils;
import com.ims.fees.validation.FeeComparer;
import java.io.IOException;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FeeVerificationTest extends TestBase {

	@DataProvider(name = "scenarios", parallel = true)
	public Object[][] scenarios() throws IOException {
		List<Scenario> scenarios = ExpectedFeeData.scenarios();

		Object[][] data = new Object[scenarios.size()][1];
		for (int row = 0; row < scenarios.size(); row++) {
			data[row][0] = scenarios.get(row); // row 0 = first scenario, row 1 = second, ...
		}
		return data;
	}

	@Test(groups = "ui", dataProvider = "scenarios", description = "Batch card fees match the expected values in Excel")
	public void batchCardFeesMatchExcel(Scenario scenario) throws IOException {
		List<BatchFee> expected = ExpectedFeeData.batches(scenario);

		NavigationUtils.goTo(ConfigReader.baseUrl() + scenario.pagePath());
		List<BatchFee> actual;
		if (scenario.hasCentre()) {
			ClassroomProgramPage page = new ClassroomProgramPage(getDriver());
			page.selectCityAndCentre(scenario.city(), scenario.centre());
			actual = page.readBatchCards();
		} else {
			actual = new ProgramPage(getDriver()).readBatchCards();
		}
		FeeComparer.verify(scenario.toString(), expected, actual);
	}
}