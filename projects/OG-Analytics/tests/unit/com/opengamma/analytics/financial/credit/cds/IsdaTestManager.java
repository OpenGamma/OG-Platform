package com.opengamma.analytics.financial.credit.cds;

import java.io.File;
import java.io.InputStream;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

@SuppressWarnings("restriction")
public abstract class IsdaTestManager {
	
	private static DateTimeFormatter formatter = DateTimeFormatters
			.pattern("dd_MM_yyyy");
	
	private static DateTimeFormatter formatter2 = DateTimeFormatters
			.pattern("yyyyMMdd");
	
	public static final String[][] tests = { {"CAD","01_05_2009"},
			{"CAD","01_06_2009"},
			{"CAD","04_05_2009"},
			{"CAD","05_05_2009"},
			{"CAD","06_05_2009"},
			{"CAD","07_05_2009"},
			{"CAD","08_05_2009"},
			{"CAD","11_05_2009"},
			{"CAD","12_05_2009"},
			{"CAD","13_05_2009"},
			{"CAD","14_05_2009"},
			{"CAD","15_05_2009"},
			{"CAD","18_05_2009"},
			{"CAD","19_05_2009"},
			{"CAD","21_05_2009"},
			{"CAD","22_05_2009"},
			{"CAD","25_05_2009"},
			{"CAD","26_05_2009"},
			{"CAD","27_05_2009"},
			{"CAD","28_04_2009"},
			{"CAD","28_05_2009"},
			{"CAD","29_04_2009"},
			{"CAD","29_05_2009"},
			{"CAD","30_04_2009"},
			{"CHF","01_05_2009"},
			{"CHF","01_06_2009"},
			{"CHF","04_05_2009"},
			{"CHF","05_05_2009"},
			{"CHF","06_05_2009"},
			{"CHF","07_05_2009"},
			{"CHF","08_05_2009"},
			{"CHF","11_05_2009"},
			{"CHF","12_05_2009"},
			{"CHF","13_05_2009"},
			{"CHF","14_05_2009"},
			{"CHF","15_05_2009"},
			{"CHF","18_05_2009"},
			{"CHF","19_05_2009"},
			{"CHF","21_05_2009"},
			{"CHF","22_05_2009"},
			{"CHF","25_05_2009"},
			{"CHF","26_05_2009"},
			{"CHF","27_05_2009"},
			{"CHF","28_04_2009"},
			{"CHF","28_05_2009"},
			{"CHF","29_04_2009"},
			{"CHF","29_05_2009"},
			{"CHF","30_04_2009"},
			{"EUR","01_05_2009"},
			{"EUR","01_06_2009"},
			{"EUR","04_05_2009"},
			{"EUR","05_05_2009"},
			{"EUR","06_05_2009"},
			{"EUR","07_05_2009"},
			{"EUR","08_05_2009"},
			{"EUR","11_05_2009"},
			{"EUR","12_05_2009"},
			{"EUR","13_05_2009"},
			{"EUR","14_05_2009"},
			{"EUR","15_05_2009"},
			{"EUR","18_05_2009"},
			{"EUR","19_05_2009"},
			{"EUR","21_05_2009"},
			{"EUR","22_04_2009"},
			{"EUR","22_05_2009"},
			{"EUR","24_04_2009"},
			{"EUR","25_05_2009"},
			{"EUR","26_05_2009"},
			{"EUR","27_05_2009"},
			{"EUR","28_04_2009"},
			{"EUR","28_05_2009"},
			{"EUR","29_04_2009"},
			{"EUR","29_05_2009"},
			{"EUR","30_04_2009"},
			{"GBP","01_05_2009"},
			{"GBP","01_06_2009"},
			{"GBP","05_05_2009"},
			{"GBP","06_05_2009"},
			{"GBP","07_05_2009"},
			{"GBP","08_05_2009"},
			{"GBP","11_05_2009"},
			{"GBP","12_05_2009"},
			{"GBP","13_05_2009"},
			{"GBP","14_05_2009"},
			{"GBP","15_05_2009"},
			{"GBP","18_05_2009"},
			{"GBP","19_05_2009"},
			{"GBP","21_05_2009"},
			{"GBP","22_04_2009"},
			{"GBP","22_05_2009"},
			{"GBP","24_04_2009"},
			{"GBP","25_05_2009"},
			{"GBP","26_05_2009"},
			{"GBP","27_05_2009"},
			{"GBP","28_04_2009"},
			{"GBP","28_05_2009"},
			{"GBP","29_04_2009"},
			{"GBP","29_05_2009"},
			{"GBP","30_04_2009"},
			{"JPY","01_05_2009"},
			{"JPY","01_06_2009"},
			{"JPY","04_05_2009"},
			{"JPY","05_05_2009"},
			{"JPY","06_05_2009"},
			{"JPY","07_05_2009"},
			{"JPY","08_05_2009"},
			{"JPY","11_05_2009"},
			{"JPY","12_05_2009"},
			{"JPY","13_05_2009"},
			{"JPY","14_05_2009"},
			{"JPY","15_05_2009"},
			{"JPY","18_05_2009"},
			{"JPY","19_05_2009"},
			{"JPY","21_05_2009"},
			{"JPY","22_05_2009"},
			{"JPY","25_05_2009"},
			{"JPY","26_05_2009"},
			{"JPY","27_05_2009"},
			{"JPY","28_04_2009"},
			{"JPY","28_05_2009"},
			{"JPY","29_04_2009"},
			{"JPY","29_05_2009"},
			{"JPY","30_04_2009"},
			{"USD","01_06_2009"},
			{"USD","11_05_2009"},
			{"USD","12_05_2009"},
			{"USD","13_05_2009"},
			{"USD","14_05_2009"},
			{"USD","15_05_2009"},
			{"USD","18_05_2009"},
			{"USD","19_05_2009"},
			{"USD","21_05_2009"},
			{"USD","22_05_2009"},
			{"USD","25_05_2009"},
			{"USD","26_05_2009"},
			{"USD","27_05_2009"},
			{"USD","28_05_2009"},
			{"USD","29_05_2009"} };
	
	
	public static IsdaTest[] getAllTests() throws JAXBException
	{
		IsdaTest[] result = new IsdaTest[tests.length];
		for (int i = 0; i < tests.length; i++) {

			InputStream is = getFileInputStream(getTestFileName(i));
			result[i] = getGridTest(is);
		}
		return result;
	}
	
	public static String getTestCurrency(int i)
	{
		return tests[i][0];
	}
	
	public static String getTestDate(int i)
	{
		return tests[i][1];
	}
	
	public static String getTestFileName(int i) {
		String currency = getTestCurrency(i);
		String date = getTestDate(i);
		return "resources" + File.separator + "isda_test_grids_xml" + File.separator + "IsdaTestGrid_" + currency + "_" + date + ".xml";
	}

	public static String getNameOfExcelTestGridFile(int i)
	{
		String currency = getTestCurrency(i);
		String dateStr = getTestDate(i);
		LocalDate date = LocalDate.parse(dateStr, formatter);
		return currency + "_" + date.toString(formatter2) + ".xls";
	}
	
	/**
	 * Get a Test object
	 * 
	 * @param is
	 *            The incoming input stream
	 * @return The Interest Rate Curve object
	 * @throws JAXBException
	 */
	public static IsdaTest getGridTest(InputStream is)
			throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(IsdaTest.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		IsdaTest isdaTest = (IsdaTest) jaxbUnmarshaller
				.unmarshal(is);
		return isdaTest;
	}

	/**
	 * Find a given file in the classpath
	 * @param fileName
	 * @return The input stream to the file
	 */
	public static InputStream getFileInputStream(String fileName) {
		return IsdaTestManager.class.getClassLoader().getResourceAsStream(
				fileName);
	}

}
