package com.opengamma.analytics.financial.credit.cds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.DateTimeFormatter;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class holds the data for one of the test grids and the associated
 * interest rate curve
 */
public class TestGrid {

	/**
	 * The data types we expect in the spreadsheet mapped to the POI structures
	 */
	private enum DataTypes {
		STRING(Cell.CELL_TYPE_STRING), DATE(Cell.CELL_TYPE_NUMERIC), NUMBER(
				Cell.CELL_TYPE_NUMERIC);

		/**
		 * The poi cell type
		 */
		private final int _poiType;

		DataTypes(int type) {
			_poiType = type;
		}

		public int getPoiType() {
			return _poiType;
		}
	}

	private static final Logger s_logger = LoggerFactory
			.getLogger(TestGrid.class);

	public static final TestGridFields[] headingNames = { TestGridFields.TRADE_DATE, TestGridFields.MATURITY_DATE,
		TestGridFields.CURRENCY, TestGridFields.COUPON, TestGridFields.QUOTED_SPREAD, TestGridFields.RECOVERY, TestGridFields.UPFRONT,
		TestGridFields.CLEAN_PRICE, TestGridFields.DAYS_ACCRUED, TestGridFields.ACCRUED_PREMIUM, TestGridFields.CASH_SETTLE,
		TestGridFields.START_DATE };
	public static final DataTypes[] headingTypes = { DataTypes.DATE, DataTypes.DATE,
			DataTypes.STRING, DataTypes.NUMBER, DataTypes.NUMBER,
			DataTypes.NUMBER, DataTypes.NUMBER, DataTypes.NUMBER,
			DataTypes.NUMBER, DataTypes.NUMBER, DataTypes.DATE, DataTypes.DATE };

	/**
	 * The test data itself
	 */
	private List<TestGridRow> _data = new ArrayList<TestGridRow>();

	private Set<String> _currencies = new TreeSet<String>();

	private LocalDate _tradeDate;

	private String _currency;
	private static TreeMap<TestGridFields, Integer> headingIndex;

	private static DateTimeFormatter formatter = DateTimeFormatters
			.pattern("yyyyMMdd");

	static TreeMap<TestGridFields, Integer> getHeadingIndex()
	{
		if(headingIndex == null)
		{
			headingIndex = new TreeMap<TestGridFields, Integer>();
			for (int i = 0; i < headingNames.length; i++) {
				headingIndex.put(headingNames[i], i);
			}
		}
		
		return headingIndex;
	}

	private void checkHeader(Row row) {
		for (Cell cell : row) {
			int index = cell.getColumnIndex();
			String colName = cell.getStringCellValue();
			if (!colName.trim().startsWith(headingNames[index].getHeading())) {
				throw new IllegalArgumentException("Heading name " + colName
						+ " does not match column name in the expected header "
						+ headingNames[index]);
			}
		}
	}

	/**
	 * Get the currency(ies) used in the test grid
	 * 
	 * @return
	 */
	public Set<String> getCurrencies() {
		return _currencies;
	}

	public String getCurrency()
	{
		if(_currency == null)
		{
			for (String currency : _currencies) {
				_currency = currency;
				break;
			}
		}
		return _currency;
	}
	

	/**
	 * Get the interest curve date for the Test Grid - one business day before
	 * the trade date
	 * 
	 * @return
	 */
	public LocalDate getInterestRateCurveDate() {
		// Interest rate curve is for the day before
		LocalDate dt = getTradeDate().minusDays(1);
		
		// deal with weekends.
		if (dt.getDayOfWeek().isSaturday()) {
			dt = dt.minusDays(1);
		} else if (dt.getDayOfWeek().isSunday()) {
			dt = dt.minusDays(2);
		}

		s_logger.info("Trade date = " + getTradeDate()
				+ ", interest rate curve date = " + dt);

		return dt;

	}

	/**
	 * Get the list of trade dates
	 * 
	 * @return
	 */
	public LocalDate getTradeDate() {
		return _tradeDate;
	}

	/**
	 * Process a the incoming XLS data to get the values and build the test grid
	 * 
	 * @param sheet
	 */
	public void process(Sheet sheet) {
		for (Row row : sheet) {
			int rowNum = row.getRowNum();

			if (rowNum == 0) {
				checkHeader(row);
			} else {
				processRow(row);
			}
		}
	}

	private void processRow(Row row) {
		Object[] rowData = new Object[headingNames.length];
		for (Cell cell : row) {
			int colIndex = cell.getColumnIndex();
			int type = cell.getCellType();
			if (type == Cell.CELL_TYPE_BLANK) {
				return;
			}

			if (type != headingTypes[colIndex].getPoiType()) {
				throw new IllegalArgumentException("Row type for "
						+ headingNames[colIndex] + " was wrong on row "
						+ row.getRowNum() + " type is " + type);
			}

			switch (headingTypes[colIndex]) {
			case DATE:
				Integer originalDate = (int) cell.getNumericCellValue();
				LocalDate date = LocalDate.parse(originalDate.toString(),
						formatter);
				rowData[colIndex] = date;
				if (colIndex == 0) {
					_tradeDate = date;
				}
				break;

			case NUMBER:
				rowData[colIndex] = cell.getNumericCellValue();
				break;

			case STRING:
				rowData[colIndex] = cell.getStringCellValue().trim();
				break;

			default:
				break;
			}

			if (colIndex == 2) {
				_currencies.add((String) rowData[colIndex]);
			}
		}
		getData().add(new TestGridRow(rowData));
	}

	public List<TestGridRow> getData() {
		return _data;
	}

	private void setData(List<TestGridRow> _data) {
		this._data = _data;
	}
}
