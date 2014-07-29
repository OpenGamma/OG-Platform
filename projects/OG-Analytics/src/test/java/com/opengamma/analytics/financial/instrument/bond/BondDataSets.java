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
 * Examples of bond futures to be used in tests.
 */
public class BondDataSets {

  //UKT 5 09/07/14 - ISIN-GB0031829509
  private static final String UK_GOVT_NAME = IssuerProviderDiscountDataSets.getIssuerNames()[3];
  private static final String REPO_TYPE = "General collateral";
  private static final Currency GBP = Currency.GBP;
  private static final Period PAYMENT_TENOR_GILT = Period.ofMonths(6);
  private static final Calendar CALENDAR_GILT = new CalendarGBP("LON");
  private static final DayCount DAY_COUNT_GILT = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY_GILT = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_GILT = false;
  private static final int SETTLEMENT_DAYS_GILT = 2;
  private static final int EX_DIVIDEND_DAYS_GILT = 7;
  private static final YieldConvention YIELD_CONVENTION_GILT = YieldConventionFactory.INSTANCE.getYieldConvention("UK:BUMP/DMO METHOD");
  private static final Period BOND_TENOR_UK14 = Period.ofYears(12);
  private static final ZonedDateTime START_ACCRUAL_DATE_UK14 = DateUtils.getUTCDate(2002, 9, 7);
  private static final ZonedDateTime MATURITY_DATE_UK14 = START_ACCRUAL_DATE_UK14.plus(BOND_TENOR_UK14);
  private static final double RATE_UK14 = 0.0500;
  private static final double DEFAULT_NOTIONAL_UK14 = 1.0d;
  private static final BondFixedSecurityDefinition BOND_SECURITY_DEFINITION_UK14 = BondFixedSecurityDefinition.from(GBP, MATURITY_DATE_UK14, START_ACCRUAL_DATE_UK14,
      PAYMENT_TENOR_GILT, RATE_UK14, SETTLEMENT_DAYS_GILT, DEFAULT_NOTIONAL_UK14, EX_DIVIDEND_DAYS_GILT, CALENDAR_GILT, DAY_COUNT_GILT, BUSINESS_DAY_GILT, YIELD_CONVENTION_GILT, IS_EOM_GILT,
      UK_GOVT_NAME, REPO_TYPE);

  /**
   * Returns the definition of the UKT 5 09/07/14 - ISIN-GB0031829509 bond security.
   * Default notional of 1.
   * @return The bond.
   */
  public static BondFixedSecurityDefinition bondUKT5_20140907() {
    return BOND_SECURITY_DEFINITION_UK14;
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

}
