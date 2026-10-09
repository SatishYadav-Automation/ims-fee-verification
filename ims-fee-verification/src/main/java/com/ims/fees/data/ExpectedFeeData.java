package com.ims.fees.data;

import com.ims.fees.core.ConfigReader;
import com.ims.fees.model.BatchFee;
import com.ims.fees.model.Scenario;
import com.ims.fees.utils.ExcelUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the expected values from the "Batches" sheet of ExpectedFees.xlsx. One
 * row = one batch.
 */
public class ExpectedFeeData {

	private static final String EXCEL = ConfigReader.get("excelPath");
	private static final String SHEET = "Batches";

	/**
	 * The different things to test (a page, or a page + city + centre), each listed
	 * once.
	 */
	public static List<Scenario> scenarios() throws IOException {
		List<Scenario> scenarios = new ArrayList<>();
		try (ExcelUtils excel = new ExcelUtils(EXCEL)) {
			for (int row = 1; row < excel.getRowCount(SHEET); row++) {
				Scenario scenario = scenarioOf(excel, row);
				if (!scenario.pagePath().isEmpty() && !scenarios.contains(scenario)) {
					scenarios.add(scenario);
				}
			}
		}
		if (scenarios.isEmpty()) {
			throw new IllegalStateException("No rows found in sheet '" + SHEET + "' of " + EXCEL);
		}
		return scenarios;
	}

	/**
	 * The batches expected for one scenario. Empty when Excel says "no batch
	 * expected" (batch columns blank).
	 */
	public static List<BatchFee> batches(Scenario scenario) throws IOException {
		List<BatchFee> batches = new ArrayList<>();
		try (ExcelUtils excel = new ExcelUtils(EXCEL)) {
			for (int row = 1; row < excel.getRowCount(SHEET); row++) {
				if (scenarioOf(excel, row).equals(scenario) && !cell(excel, row, "Batch Name").isEmpty()) {
					int baseFee = Integer.parseInt(cell(excel, row, "Base Fee (INR)").replace(",", ""));
					int gstPercent = Integer.parseInt(cell(excel, row, "GST Rate (%)"));
					int gstAmount = (int) Math.round(baseFee * gstPercent / 100.0);
					batches.add(new BatchFee(cell(excel, row, "Batch Code"), cell(excel, row, "Batch Name"), baseFee,
							gstPercent, gstAmount, cell(excel, row, "Start Date (d-m-yyyy)"),
							cell(excel, row, "Batch Type")));
				}
			}
		}
		return batches;
	}

	/** Which scenario does this row belong to? */
	private static Scenario scenarioOf(ExcelUtils excel, int row) {
		return new Scenario(cell(excel, row, "Page Path"), cell(excel, row, "City"), cell(excel, row, "Centre"),
				cell(excel, row, "Centre Code"), cell(excel, row, "Variant Code"));
	}

	/** The text in a cell, found by its column header. */
	private static String cell(ExcelUtils excel, int row, String header) {
		return excel.getCellValue(SHEET, row, excel.getColumnIndex(SHEET, header));
	}
}