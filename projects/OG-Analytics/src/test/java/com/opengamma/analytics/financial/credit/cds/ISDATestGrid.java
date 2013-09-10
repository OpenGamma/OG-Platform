package com.opengamma.analytics.financial.credit.cds;

import static org.threeten.bp.DayOfWeek.SATURDAY;
import static org.threeten.bp.DayOfWeek.SUNDAY;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * The class holds the data for one of the test grids and the associated
 * interest rate curve
 */
public class ISDATestGrid {

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

		DataTypes(final int type) {
			_poiType = type;
		}

		public int getPoiType() {
			return _poiType;
		}
	}

	private static final Logger s_logger = LoggerFactory
			.getLogger(ISDATestGrid.class);

	public static final ISDATestGridFields[] headingNames = { ISDATestGridFields.TRADE_DATE, ISDATestGridFields.MATURITY_DATE,
		ISDATestGridFields.CURRENCY, ISDATestGridFields.COUPON, ISDATestGridFields.QUOTED_SPREAD, ISDATestGridFields.RECOVERY, ISDATestGridFields.UPFRONT,
		ISDATestGridFields.CLEAN_PRICE, ISDATestGridFields.DAYS_ACCRUED, ISDATestGridFields.ACCRUED_PREMIUM, ISDATestGridFields.CASH_SETTLE,
		ISDATestGridFields.START_DATE };
	public static final DataTypes[] headingTypes = { DataTypes.DATE, DataTypes.DATE,
			DataTypes.STRING, DataTypes.NUMBER, DataTypes.NUMBER,
			DataTypes.NUMBER, DataTypes.NUMBER, DataTypes.NUMBER,
			DataTypes.NUMBER, DataTypes.NUMBER, DataTypes.DATE, DataTypes.DATE };

	/**
	 * The test data itself
	 */
	private final List<ISDATestGridRow> _data = new ArrayList<>();

	private final Set<String> _currencies = new TreeSet<>();

	private LocalDate _tradeDate;

	private String _currency;
	private static TreeMap<ISDATestGridFields, Integer> headingIndex;

	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

	static TreeMap<ISDATestGridFields, Integer> getHeadingIndex()
	{
		if(headingIndex == null)
		{
			headingIndex = new TreeMap<>();
			for (int i = 0; i < headingNames.length; i++) {
				headingIndex.put(headingNames[i], i);
			}
		}

		return headingIndex;
	}

	private void checkHeader(final Row row) {
		for (final Cell cell : row) {
			final int index = cell.getColumnIndex();
			final String colName = cell.getStringCellValue();
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
			for (final String currency : _currencies) {
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
		if (dt.getDayOfWeek() == SATURDAY) {
			dt = dt.minusDays(1);
		} else if (dt.getDayOfWeek() == SUNDAY) {
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
	public void process(final Sheet sheet) {
		for (final Row row : sheet) {
			final int rowNum = row.getRowNum();

			if (rowNum == 0) {
				checkHeader(row);
			} else {
				processRow(row);
			}
		}
	}

	private void processRow(final Row row) {
		final Object[] rowData = new Object[headingNames.length];
		for (final Cell cell : row) {
			final int colIndex = cell.getColumnIndex();
			final int type = cell.getCellType();
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
				final Integer originalDate = (int) cell.getNumericCellValue();
				final LocalDate date = LocalDate.parse(originalDate.toString(),
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
		getData().add(new ISDATestGridRow(rowData));
	}

	public List<ISDATestGridRow> getData() {
		return _data;
	}
}
