package com.opengamma.analytics.financial.credit.cds;

import javax.time.calendar.LocalDate;

/**
 * Represents a single test (one row) in the ISDA test grids
 * 
 * @author Niels Stchedroff
 */
public class TestGridRow extends TestGrid {

	private Object[] _rowData;

	public TestGridRow(Object[] rowData) {
		_rowData = rowData;
	}
	
	/**
	 * Get the Trade Date
	 */
	public LocalDate getTradeDate()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.TRADE_DATE)];
		if(data instanceof LocalDate)
		{
			return (LocalDate) data;
		}
		
		throw new RuntimeException("Trade Date " + data + " is not a date");
	}
	
	/**
	 * Get the Maturity Date
	 * @return
	 */
	public LocalDate getMaturityDate()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.MATURITY_DATE)];
		if(data instanceof LocalDate)
		{
			return (LocalDate) data;
		}
		
		throw new RuntimeException("Maturity Date " + data + " is not a date");
	}
	
	public String getCurrency()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.CURRENCY)];
		return data.toString();	
	}
	
	public double getCoupon()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.COUPON)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Coupon " + data + " is not a double");
	}
	
	public double getQuotedSpread()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.QUOTED_SPREAD)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Quoted spread " + data + " is not a double");
	}
	
	public double getRecoveryRate()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.RECOVERY)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Recovery " + data + " is not a double");
	}
	
	public double getUpfront()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.UPFRONT)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Upfront " + data + " is not a double");
	}
	
	public double getCleanPrice()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.CLEAN_PRICE)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Clean price " + data + " is not a double");
	}
	
	public double getDaysAccrued()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.DAYS_ACCRUED)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Days accrued " + data + " is not a double");
	}
	
	public double getAccruedPremium()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.ACCRUED_PREMIUM)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Days accrued " + data + " is not a double");
	}
	
	public LocalDate getCashSettle()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.CASH_SETTLE)];
		if(data instanceof LocalDate)
		{
			return (LocalDate) data;
		}
		
		if (data == null) {
		  return null;
		}
		
		throw new RuntimeException("Cash settlement " + data + " is not a date");
	}
	
	public LocalDate getStartDate()
	{
		Object data = _rowData[TestGrid.getHeadingIndex().get(TestGridFields.START_DATE)];
		if(data instanceof LocalDate)
		{
			return (LocalDate) data;
		}
		
		if(data == null)
		{
			return null;
		}
		throw new RuntimeException("Start date " + data + " is not a date");
	}
}