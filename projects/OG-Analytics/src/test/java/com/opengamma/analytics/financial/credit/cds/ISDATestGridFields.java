package com.opengamma.analytics.financial.credit.cds;

/**
 * The fields we expect in the ISDA CDS test grid files.
 *
 * @author Niels Stchedroff
 */
public enum ISDATestGridFields {
	START_DATE("Start Date"),
	CASH_SETTLE("Cash Settle"),
	ACCRUED_PREMIUM("Accrued Premium"),
	DAYS_ACCRUED("Days Accrued"),
	CLEAN_PRICE("Clean Price"),
	UPFRONT("Upfront"),
	QUOTED_SPREAD("Quoted Spread"),
	COUPON("Coupon"),
	CURRENCY("Currency"),
	MATURITY_DATE("Maturity Date"),
	TRADE_DATE("Trade Date"),
	RECOVERY("Recovery");

	/**
	 * The heading name for the column in the Excel file.
	 */
	private final String _heading;

	public String getHeading() {
		return _heading;
	}

	private ISDATestGridFields(final String heading)
	{
		this._heading = heading;
	}
}
