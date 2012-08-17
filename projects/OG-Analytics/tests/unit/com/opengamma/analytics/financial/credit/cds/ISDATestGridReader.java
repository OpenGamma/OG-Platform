package com.opengamma.analytics.financial.credit.cds;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ISDATestGridReader {

	private static void readTheTests() throws InterruptedException, IOException {
		for (int i = 0; i < testGrids.length; i++) {
			String fileName = testGrids[i];
			s_logger.info("Finding file " + fileName);
			InputStream is = getFileInputStream(fileName);
			try {
				s_logger.info("Reading test grid file " + fileName);

				// Yes, ancient format excel :-)
				Workbook wb = new HSSFWorkbook(is);
				ISDATestGrid testGrid = getTestGrid(wb);

			} catch (IOException e) {
			}
		}
	}

	private static ISDATestGrid getTestGrid(Workbook wb)
			throws InterruptedException {
		ISDATestGrid testGrid = new ISDATestGrid();
		// Assuming the first sheet
		Sheet sheet = wb.getSheetAt(0);
		testGrid.process(sheet);
		return testGrid;
	}

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException,
			IOException {
		readTheTests();
	}

	/**
	 * TRhe format used for dates in the xls files
	 */
	private static DateTimeFormatter formatter = DateTimeFormatters
			.pattern("yyyyMMdd");
	private static final Logger s_logger = LoggerFactory
			.getLogger(ISDATestGridReader.class);
	public static final String[] testGrids = { "AUD_20090908.xls",
			"AUD_20090909.xls", "AUD_20090910.xls", "AUD_20090911.xls",
			"CAD_20090428.xls", "CAD_20090429.xls", "CAD_20090430.xls",
			"CAD_20090501.xls", "CAD_20090504.xls", "CAD_20090505.xls",
			"CAD_20090506.xls", "CAD_20090507.xls", "CAD_20090508.xls",
			"CAD_20090511.xls", "CAD_20090512.xls", "CAD_20090513.xls",
			"CAD_20090514.xls", "CAD_20090515.xls", "CAD_20090518.xls",
			"CAD_20090519.xls", "CAD_20090521.xls", "CAD_20090522.xls",
			"CAD_20090525.xls", "CAD_20090526.xls", "CAD_20090527.xls",
			"CAD_20090528.xls", "CAD_20090529.xls", "CAD_20090601.xls",
			"CHF_20090428.xls", "CHF_20090429.xls", "CHF_20090430.xls",
			"CHF_20090501.xls", "CHF_20090504.xls", "CHF_20090505.xls",
			"CHF_20090506.xls", "CHF_20090507.xls", "CHF_20090508.xls",
			"CHF_20090511.xls", "CHF_20090512.xls", "CHF_20090513.xls",
			"CHF_20090514.xls", "CHF_20090515.xls", "CHF_20090518.xls",
			"CHF_20090519.xls", "CHF_20090521.xls", "CHF_20090522.xls",
			"CHF_20090525.xls", "CHF_20090526.xls", "CHF_20090527.xls",
			"CHF_20090528.xls", "CHF_20090529.xls", "CHF_20090601.xls",
			"EUR_20090422.xls", "EUR_20090424.xls", "EUR_20090428.xls",
			"EUR_20090429.xls", "EUR_20090430.xls", "EUR_20090501.xls",
			"EUR_20090504.xls", "EUR_20090505.xls", "EUR_20090506.xls",
			"EUR_20090507.xls", "EUR_20090508.xls", "EUR_20090511.xls",
			"EUR_20090512.xls", "EUR_20090513.xls", "EUR_20090514.xls",
			"EUR_20090515.xls", "EUR_20090518.xls", "EUR_20090519.xls",
			"EUR_20090521.xls", "EUR_20090522.xls", "EUR_20090525.xls",
			"EUR_20090526.xls", "EUR_20090527.xls", "EUR_20090528.xls",
			"EUR_20090529.xls", "EUR_20090601.xls", "GBP_20090422.xls",
			"GBP_20090424.xls", "GBP_20090428.xls", "GBP_20090429.xls",
			"GBP_20090430.xls", "GBP_20090501.xls", "GBP_20090505.xls",
			"GBP_20090506.xls", "GBP_20090507.xls", "GBP_20090508.xls",
			"GBP_20090511.xls", "GBP_20090512.xls", "GBP_20090513.xls",
			"GBP_20090514.xls", "GBP_20090515.xls", "GBP_20090518.xls",
			"GBP_20090519.xls", "GBP_20090521.xls", "GBP_20090522.xls",
			"GBP_20090525.xls", "GBP_20090526.xls", "GBP_20090527.xls",
			"GBP_20090528.xls", "GBP_20090529.xls", "GBP_20090601.xls",
			"HKD_20090908.xls", "HKD_20090909.xls", "HKD_20090910.xls",
			"HKD_20090911.xls", "JPY_20090428.xls", "JPY_20090429.xls",
			"JPY_20090430.xls", "JPY_20090501.xls", "JPY_20090504.xls",
			"JPY_20090505.xls", "JPY_20090506.xls", "JPY_20090507.xls",
			"JPY_20090508.xls", "JPY_20090511.xls", "JPY_20090512.xls",
			"JPY_20090513.xls", "JPY_20090514.xls", "JPY_20090515.xls",
			"JPY_20090518.xls", "JPY_20090519.xls", "JPY_20090521.xls",
			"JPY_20090522.xls", "JPY_20090525.xls", "JPY_20090526.xls",
			"JPY_20090527.xls", "JPY_20090528.xls", "JPY_20090529.xls",
			"JPY_20090601.xls", "JPY_20090907.xls", "JPY_20090908.xls",
			"JPY_20090909.xls", "JPY_20090910.xls", "JPY_20090911.xls",
			"NZD_20090908.xls", "NZD_20090909.xls", "NZD_20090910.xls",
			"NZD_20090911.xls", "SGD_20090908.xls", "SGD_20090909.xls",
			"SGD_20090910.xls", "SGD_20090911.xls", "USD_20090511.xls",
			"USD_20090512.xls", "USD_20090513.xls", "USD_20090514.xls",
			"USD_20090515.xls", "USD_20090518.xls", "USD_20090519.xls",
			"USD_20090521.xls", "USD_20090522.xls", "USD_20090525.xls",
			"USD_20090526.xls", "USD_20090527.xls", "USD_20090528.xls",
			"USD_20090529.xls", "USD_20090601.xls", "USD_20090907.xls",
			"USD_20090908.xls", "USD_20090909.xls", "USD_20090910.xls",
			"USD_20090911.xls" };

	private static InputStream getFileInputStream(String fileName) {
		return ISDATestGridReader.class.getClassLoader().getResourceAsStream(
				fileName);
	}

}
