package com.opengamma.analytics.financial.credit.cds;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;
import javax.xml.bind.JAXBException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the ISDA test grids
 */
@SuppressWarnings("restriction")
public abstract class TestGridManager {

	private static final Logger s_logger = LoggerFactory.getLogger(TestGridManager.class);

	/**
	 * The sub directory in resources where the test grids are found
	 */
	public static final String s_testGridFileLocation = "resources" + File.separator + "isda_test_grids" + File.separator;

	/**
	 * The sub directory in the resources directory where the ISDA CDS model
	 * generated IR curves are found
	 */
	public static final String s_isdaIrDataFileLocation = "IsdaIrData"
			+ File.separator;

	/**
	 * The names of the grid files for the benchmark tests
	 * 
	 * @see<a href="http://www.cdsmodel.com/cdsmodel/test-grids.page?">http://www.cdsmodel.com/cdsmodel/test-grids.page?</a>
	 */
	public static final String[] testGridsBenchMark = { "USD_20090907.xls",
			"USD_20090908.xls", "USD_20090909.xls", "USD_20090910.xls",
			"USD_20090911.xls", "AUD_20090908.xls", "AUD_20090909.xls",
			"AUD_20090910.xls", "AUD_20090911.xls", "JPY_20090907.xls",
			"JPY_20090908.xls", "JPY_20090909.xls", "JPY_20090910.xls",
			"JPY_20090911.xls", "NZD_20090908.xls", "NZD_20090909.xls",
			"NZD_20090910.xls", "NZD_20090911.xls", "SGD_20090908.xls",
			"SGD_20090909.xls", "SGD_20090910.xls", "SGD_20090911.xls",
			"HKD_20090908.xls", "HKD_20090909.xls", "HKD_20090910.xls",
			"HKD_20090911.xls", };

	/**
	 * The names of the grid files for the "Standard North American Corporates Currencies Test Grids"
	 * 
	 * @see<a href="http://www.cdsmodel.com/cdsmodel/test-grids.page?">http://www.cdsmodel.com/cdsmodel/test-grids.page?</a>
	 */
	public static final String[] testGrids = { 
			"CAD_20090428.xls",
			"CAD_20090429.xls", "CAD_20090430.xls",
			"CAD_20090501.xls",
			"CAD_20090504.xls", "CAD_20090505.xls", "CAD_20090506.xls",
			"CAD_20090507.xls", "CAD_20090508.xls", "CAD_20090511.xls",
			"CAD_20090512.xls", "CAD_20090513.xls", "CAD_20090514.xls",
			"CAD_20090515.xls", "CAD_20090518.xls", "CAD_20090519.xls",
			"CAD_20090521.xls", "CAD_20090522.xls", "CAD_20090525.xls",
			"CAD_20090526.xls", "CAD_20090527.xls", "CAD_20090528.xls",
			"CAD_20090529.xls", "CAD_20090601.xls", "CHF_20090428.xls",
			"CHF_20090429.xls", "CHF_20090430.xls", "CHF_20090501.xls",
			"CHF_20090504.xls", "CHF_20090505.xls", "CHF_20090506.xls",
			"CHF_20090507.xls", "CHF_20090508.xls", "CHF_20090511.xls",
			"CHF_20090512.xls", "CHF_20090513.xls", "CHF_20090514.xls",
			"CHF_20090515.xls", "CHF_20090518.xls", "CHF_20090519.xls",
			"CHF_20090521.xls", "CHF_20090522.xls", "CHF_20090525.xls",
			"CHF_20090526.xls", "CHF_20090527.xls", "CHF_20090528.xls",
			"CHF_20090529.xls", "CHF_20090601.xls", "EUR_20090422.xls",
			"EUR_20090424.xls", "EUR_20090428.xls", "EUR_20090429.xls",
			"EUR_20090430.xls", "EUR_20090501.xls", "EUR_20090504.xls",
			"EUR_20090505.xls", "EUR_20090506.xls", "EUR_20090507.xls",
			"EUR_20090508.xls", "EUR_20090511.xls", "EUR_20090512.xls",
			"EUR_20090513.xls", "EUR_20090514.xls", "EUR_20090515.xls",
			"EUR_20090518.xls", "EUR_20090519.xls", "EUR_20090521.xls",
			"EUR_20090522.xls", "EUR_20090525.xls", "EUR_20090526.xls",
			"EUR_20090527.xls", "EUR_20090528.xls", "EUR_20090529.xls",
			"EUR_20090601.xls", "GBP_20090422.xls", "GBP_20090424.xls",
			"GBP_20090428.xls", "GBP_20090429.xls", "GBP_20090430.xls",
			"GBP_20090501.xls", "GBP_20090505.xls", "GBP_20090506.xls",
			"GBP_20090507.xls", "GBP_20090508.xls", "GBP_20090511.xls",
			"GBP_20090512.xls", "GBP_20090513.xls", "GBP_20090514.xls",
			"GBP_20090515.xls", "GBP_20090518.xls", "GBP_20090519.xls",
			"GBP_20090521.xls", "GBP_20090522.xls", "GBP_20090525.xls",
			"GBP_20090526.xls", "GBP_20090527.xls", "GBP_20090528.xls",
			"GBP_20090529.xls", "GBP_20090601.xls", "JPY_20090428.xls",
			"JPY_20090429.xls", "JPY_20090430.xls", "JPY_20090501.xls",
			"JPY_20090504.xls", "JPY_20090505.xls", "JPY_20090506.xls",
			"JPY_20090507.xls", "JPY_20090508.xls", "JPY_20090511.xls",
			"JPY_20090512.xls", "JPY_20090513.xls", "JPY_20090514.xls",
			"JPY_20090515.xls", "JPY_20090518.xls", "JPY_20090519.xls",
			"JPY_20090521.xls", "JPY_20090522.xls", "JPY_20090525.xls",
			"JPY_20090526.xls", "JPY_20090527.xls", "JPY_20090528.xls",
			"JPY_20090529.xls", "JPY_20090601.xls", "USD_20090511.xls",
			"USD_20090512.xls", "USD_20090513.xls", "USD_20090514.xls",
			"USD_20090515.xls", "USD_20090518.xls", "USD_20090519.xls",
			"USD_20090521.xls", "USD_20090522.xls", "USD_20090525.xls",
			"USD_20090526.xls", "USD_20090527.xls", "USD_20090528.xls",
			"USD_20090529.xls", "USD_20090601.xls"};

	/**
	 * TRhe format used for dates in the xls files
	 */
	private static DateTimeFormatter formatter = DateTimeFormatters
			.pattern("yyyy_MM_dd");

	public static void checkAll() throws IOException, InterruptedException,
			JAXBException {

		for (int i = 0; i < TestGridManager.testGrids.length; i++) {
			String fileName = s_testGridFileLocation
					+ TestGridManager.testGrids[i];
			s_logger.info("Finding file " + fileName);
			InputStream is = TestGridManager.getFileInputStream(fileName);

			try {
				s_logger.info("Reading test grid file " + fileName);

				// Yes, ancient format excel :-)
				Workbook wb = new HSSFWorkbook(is);
				TestGrid testGrid = TestGridManager.getTestGrid(wb);
				s_logger.info(testGrid.getCurrencies().toString() + " "
						+ testGrid.getTradeDate());
			} catch (IOException e) {
				continue;
			}
		}
	}

	public static InputStream getFileInputStream(String fileName) {
		return TestGridManager.class.getClassLoader().getResourceAsStream(
				fileName);
	}
	
	public static TestGrid getTestGrid(String fileName) throws IOException, InterruptedException, JAXBException {
	  InputStream is = TestGridManager.getFileInputStream(s_testGridFileLocation + fileName);
	  Workbook wb = new HSSFWorkbook(is);
	  return TestGridManager.getTestGrid(wb);
	}

	public static TestGrid getTestGrid(Workbook wb)
			throws InterruptedException, JAXBException {
		TestGrid testGrid = new TestGrid();
		// Assuming the first sheet
		Sheet sheet = wb.getSheetAt(0);
		testGrid.process(sheet);
		return testGrid;
	}
}
