package com.opengamma.analytics.financial.credit.cds;

import org.threeten.bp.LocalDate;

/**
 * Represents a single test (one row) in the ISDA test grids
 * 
 * @author Niels Stchedroff
 */
public class ISDATestGridRow extends ISDATestGrid {

	private Object[] _rowData;

	public ISDATestGridRow(Object[] rowData) {
		_rowData = rowData;
	}
	
	public boolean isTestValid() {
	  
	  return _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.TRADE_DATE)] != null;
	}
	
	/**
	 * Get the Trade Date
	 */
	public LocalDate getTradeDate()
	{
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.TRADE_DATE)];
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
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.MATURITY_DATE)];
		if(data instanceof LocalDate)
		{
			return (LocalDate) data;
		}
		
		throw new RuntimeException("Maturity Date " + data + " is not a date");
	}
	
	public String getCurrency()
	{
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.CURRENCY)];
		return data.toString();	
	}
	
	public double getCoupon()
	{
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.COUPON)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Coupon " + data + " is not a double");
	}
	
	public double getQuotedSpread()
	{
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.QUOTED_SPREAD)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Quoted spread " + data + " is not a double");
	}
	
	public double getRecoveryRate()
	{
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.RECOVERY)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Recovery " + data + " is not a double");
	}
	
	public double getUpfront()
	{
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.UPFRONT)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Upfront " + data + " is not a double");
	}
	
	public double getCleanPrice()
	{
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.CLEAN_PRICE)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Clean price " + data + " is not a double");
	}
	
	public double getDaysAccrued()
	{
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.DAYS_ACCRUED)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Days accrued " + data + " is not a double");
	}
	
	public double getAccruedPremium()
	{
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.ACCRUED_PREMIUM)];
		if(data instanceof Double)
		{
			return (Double) data;
		}
		
		throw new RuntimeException("Days accrued " + data + " is not a double");
	}
	
	public LocalDate getCashSettle()
	{
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.CASH_SETTLE)];
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
		Object data = _rowData[ISDATestGrid.getHeadingIndex().get(ISDATestGridFields.START_DATE)];
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