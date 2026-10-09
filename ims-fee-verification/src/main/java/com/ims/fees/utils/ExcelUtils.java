package com.ims.fees.utils;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils implements AutoCloseable {

	private final Workbook workbook;
	private final DataFormatter formatter = new DataFormatter();

	public ExcelUtils(String filePath) throws IOException {
		workbook = new XSSFWorkbook(new FileInputStream(filePath));
	}

	public Sheet getSheet(String sheetName) {
		return workbook.getSheet(sheetName);
	}

	public int getRowCount(String sheetName) {
		return getSheet(sheetName).getLastRowNum() + 1;
	}

	public Row getRow(String sheetName, int rowNumber) {
		return getSheet(sheetName).getRow(rowNumber);
	}

	public String getCellValue(String sheetName, int rowNumber, int cellNumber) {
		Row row = getRow(sheetName, rowNumber);
		return getCellValue(row, cellNumber);
	}

	public String getCellValue(Row row, int cellNumber) {
		if (row == null)
			return "";
		Cell cell = row.getCell(cellNumber);
		if (cell == null)
			return "";
		return formatter.formatCellValue(cell).trim();
	}

	public int getColumnIndex(String sheetName, String columnName) {
		Row headerRow = getRow(sheetName, 0);
		for (Cell cell : headerRow) {
			String header = formatter.formatCellValue(cell).trim();
			if (header.equals(columnName))
				return cell.getColumnIndex();
		}
		throw new IllegalArgumentException("Column '" + columnName + "' not found in sheet '" + sheetName + "'");
	}

	public int getColumnIndexContains(String sheetName, String columnName) {
		Row headerRow = getRow(sheetName, 0);
		for (Cell cell : headerRow) {
			String header = formatter.formatCellValue(cell).trim();
			if (header.contains(columnName))
				return cell.getColumnIndex();
		}
		throw new IllegalArgumentException(
				"No column containing '" + columnName + "' found in sheet '" + sheetName + "'");
	}

	public boolean hasColumn(String sheetName, String columnName) {
		Row headerRow = getRow(sheetName, 0);
		for (Cell cell : headerRow) {
			if (formatter.formatCellValue(cell).trim().equals(columnName))
				return true;
		}
		return false;
	}

	public void close() throws IOException {
		workbook.close();
	}
}