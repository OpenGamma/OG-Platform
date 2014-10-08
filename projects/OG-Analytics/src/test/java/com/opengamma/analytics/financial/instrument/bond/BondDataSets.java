/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Examples of bond and bills to be used in tests.
 */
public class BondDataSets {

  private static final String UK_GOVT_NAME = IssuerProviderDiscountDataSets.getIssuerNames()[3];
  private static final String REPO_TYPE = "General collateral";
  private static final Currency GBP = Currency.GBP;
  private static final double DEFAULT_NOTIONAL = 1.0d;
  private static final Calendar CALENDAR_GILT = new CalendarGBP("LON");

  private static final Period PAYMENT_TENOR_GILT = Period.ofMonths(6);
  private static final DayCount DAY_COUNT_GILT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY_GILT = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_GILT = false;
  private static final int SETTLEMENT_DAYS_GILT = 1; // See http://www.dmo.gov.uk/?page=Links/Glossary
  private static final int EX_DIVIDEND_DAYS_GILT = 7;
  private static final YieldConvention YIELD_CONVENTION_GILT = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");

  //UKT 5 09/07/14 - ISIN-GB0031829509
  private static final Period BOND_TENOR_UK14 = Period.ofYears(12);
  private static final ZonedDateTime START_ACCRUAL_DATE_UK14 = DateUtils.getUTCDate(2002, 9, 7);
  private static final ZonedDateTime MATURITY_DATE_UK14 = START_ACCRUAL_DATE_UK14.plus(BOND_TENOR_UK14);
  private static final double RATE_UK14 = 0.0500;
  //UKT 2 2016-01-22 - ISIN-GB00B3QCG246
  private static final Period BOND_TENOR_UK16 = Period.ofYears(5);
  private static final ZonedDateTime START_ACCRUAL_DATE_UK16 = DateUtils.getUTCDate(2011, 1, 22);
  private static final ZonedDateTime MATURITY_DATE_UK16 = START_ACCRUAL_DATE_UK16.plus(BOND_TENOR_UK16);
  private static final double RATE_UK16 = 0.0200;
  //UKT 1 3/4 2019-07-22 - ISIN-GB00BDV0F150
  private static final Period BOND_TENOR_UK19 = Period.ofYears(6);
  private static final ZonedDateTime START_ACCRUAL_DATE_UK19 = DateUtils.getUTCDate(2013, 7, 22);
  private static final ZonedDateTime MATURITY_DATE_UK19 = START_ACCRUAL_DATE_UK19.plus(BOND_TENOR_UK19);
  private static final double RATE_UK19 = 0.0175;
  //UKT 8 2021-06-07 - ISIN-GB0009997999
  private static final Period BOND_TENOR_UK21 = Period.ofYears(10);
  private static final ZonedDateTime START_ACCRUAL_DATE_UK21 = DateUtils.getUTCDate(2011, 6, 7);
  private static final ZonedDateTime MATURITY_DATE_UK21 = START_ACCRUAL_DATE_UK21.plus(BOND_TENOR_UK21);
  private static final double RATE_UK21 = 0.0800;
  //UKT 2 1/4 2023-09-07 - ISIN-GB00B7Z53659
  private static final Period BOND_TENOR_UK23 = Period.ofYears(10);
  private static final ZonedDateTime START_ACCRUAL_DATE_UK23 = DateUtils.getUTCDate(2013, 9, 7);
  private static final ZonedDateTime MATURITY_DATE_UK23 = START_ACCRUAL_DATE_UK23.plus(BOND_TENOR_UK23);
  private static final double RATE_UK23 = 0.0225;

  /**
   * Returns the definition of the UKT 5 09/07/14 - ISIN-GB0031829509 bond security.
   * Default notional of 1.
   * @return The bond.
   */
  public static BondFixedSecurityDefinition bondUKT5_20140907() {
    return BondFixedSecurityDefinition.from(GBP, MATURITY_DATE_UK14, START_ACCRUAL_DATE_UK14, PAYMENT_TENOR_GILT,
        RATE_UK14, SETTLEMENT_DAYS_GILT, DEFAULT_NOTIONAL, EX_DIVIDEND_DAYS_GILT, CALENDAR_GILT, DAY_COUNT_GILT,
        BUSINESS_DAY_GILT, YIELD_CONVENTION_GILT, IS_EOM_GILT, UK_GOVT_NAME, REPO_TYPE);
  }

  /**
   * Returns the definition of the UKT 5 09/07/14 - ISIN-GB0031829509 bond security.
   * @param notional The bond notional.
   * @return The bond.
   */
  public static BondFixedSecurityDefinition bondUKT5_20140907(double notional) {
    return BondFixedSecurityDefinition.from(GBP, MATURITY_DATE_UK14, START_ACCRUAL_DATE_UK14, PAYMENT_TENOR_GILT, RATE_UK14, SETTLEMENT_DAYS_GILT,
        notional, EX_DIVIDEND_DAYS_GILT, CALENDAR_GILT, DAY_COUNT_GILT, BUSINESS_DAY_GILT, YIELD_CONVENTION_GILT, IS_EOM_GILT, UK_GOVT_NAME, REPO_TYPE);
  }

  /**
   * Returns the definition of the UKT 2 2016-01-22 - ISIN-GB00B3QCG246 bond security.
   * @param notional The bond notional.
   * @return The bond.
   */
  public static BondFixedSecurityDefinition bondUKT2_20160122(double notional) {
    return BondFixedSecurityDefinition.from(GBP, MATURITY_DATE_UK16, START_ACCRUAL_DATE_UK16, PAYMENT_TENOR_GILT, RATE_UK16, SETTLEMENT_DAYS_GILT,
        notional, EX_DIVIDEND_DAYS_GILT, CALENDAR_GILT, DAY_COUNT_GILT, BUSINESS_DAY_GILT, YIELD_CONVENTION_GILT, IS_EOM_GILT, UK_GOVT_NAME, REPO_TYPE);
  }

  /**
   * Returns the definition of the UKT 8 2021-06-07 - ISIN-GB0009997999 bond security.
   * @param notional The bond notional.
   * @return The bond.
   */
  public static BondFixedSecurityDefinition bondUKT800_20210607(double notional) {
    return BondFixedSecurityDefinition.from(GBP, MATURITY_DATE_UK21, START_ACCRUAL_DATE_UK21, PAYMENT_TENOR_GILT, RATE_UK21, SETTLEMENT_DAYS_GILT,
        notional, EX_DIVIDEND_DAYS_GILT, CALENDAR_GILT, DAY_COUNT_GILT, BUSINESS_DAY_GILT, YIELD_CONVENTION_GILT, IS_EOM_GILT, UK_GOVT_NAME, REPO_TYPE);
  }

  /**
   * Returns the definition of the UKT 1 3/4 2019-07-22 - ISIN-GB00BDV0F150 bond security.
   * @param notional The bond notional.
   * @return The bond.
   */
  public static BondFixedSecurityDefinition bondUKT175_20190722(double notional) {
    return BondFixedSecurityDefinition.from(GBP, MATURITY_DATE_UK19, START_ACCRUAL_DATE_UK19, PAYMENT_TENOR_GILT, RATE_UK19, SETTLEMENT_DAYS_GILT,
        notional, EX_DIVIDEND_DAYS_GILT, CALENDAR_GILT, DAY_COUNT_GILT, BUSINESS_DAY_GILT, YIELD_CONVENTION_GILT, IS_EOM_GILT, UK_GOVT_NAME, REPO_TYPE);
  }

  /**
   * Returns the definition of the UKT 2 1/4 2023-09-07 - ISIN-GB00B7Z53659 bond security.
   * @param notional The bond notional.
   * @return The bond.
   */
  public static BondFixedSecurityDefinition bondUKT225_20230907(double notional) {
    return BondFixedSecurityDefinition.from(GBP, MATURITY_DATE_UK23, START_ACCRUAL_DATE_UK23, PAYMENT_TENOR_GILT, RATE_UK23, SETTLEMENT_DAYS_GILT,
        notional, EX_DIVIDEND_DAYS_GILT, CALENDAR_GILT, DAY_COUNT_GILT, BUSINESS_DAY_GILT, YIELD_CONVENTION_GILT, IS_EOM_GILT, UK_GOVT_NAME, REPO_TYPE);
  }

  // Generic bill
  private static final YieldConvention YIELD_BILL_UK = YieldConventionFactory.INSTANCE.getYieldConvention("INTEREST@MTY");
  private static final DayCount DAY_COUNT_BILL_UK = DayCounts.ACT_365;
  private static final int SPOT_LAG_BILL_UK = 1;

  /**
   * Returns the definition of a UK Treasury bill. 
   * @param notional The bill notional.
   * @param maturityDate The bill maturity date.
   * @return
   */
  public static BillSecurityDefinition billUK(double notional, ZonedDateTime maturityDate) {
    return new BillSecurityDefinition(GBP, maturityDate, notional, SPOT_LAG_BILL_UK, CALENDAR_GILT, YIELD_BILL_UK, DAY_COUNT_BILL_UK, UK_GOVT_NAME);
  }

}
