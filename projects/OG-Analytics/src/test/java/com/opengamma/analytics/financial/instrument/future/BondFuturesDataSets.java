/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Examples of bond futures to be used in tests.
 */
public class BondFuturesDataSets {

  // Bond futures: Bobl June 14
  private static final Currency EUR = Currency.EUR;
  private static final Period PAYMENT_TENOR_EUR = Period.ofYears(1);
  private static final Calendar TARGET = new CalendarTarget("TARGET");
  private static final DayCount DAY_COUNT_EUR = DayCounts.ACT_ACT_ICMA;
  private static final BusinessDayConvention BUSINESS_DAY_EUR = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM_EUR = false;
  private static final int SETTLEMENT_DAYS_EUR = 3;
  private static final YieldConvention YIELD_CONVENTION_EUR = YieldConventionFactory.INSTANCE.getYieldConvention("GERMAN BONDS");
  private static final int NB_BOND_EUR = 3;
  private static final Period[] BOND_TENOR_EUR = new Period[] {Period.ofYears(6), Period.ofYears(6), Period.ofYears(6) };
  private static final ZonedDateTime[] START_ACCRUAL_DATE_EUR = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 1, 4),
    DateUtils.getUTCDate(2013, 7, 4), DateUtils.getUTCDate(2013, 2, 22) };
  private static final double[] RATE_EUR = new double[] {0.0375, 0.0350, 0.0100 };
  private static final double[] CONVERSION_FACTOR_EUR = new double[] {0.912067, 0.893437, 0.800111 };
  private static final String DE_GOVT = "GERMANY GOVT";
  private static final ZonedDateTime[] MATURITY_DATE_EUR = new ZonedDateTime[NB_BOND_EUR];
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION_EUR = new BondFixedSecurityDefinition[NB_BOND_EUR];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND_EUR; loopbasket++) {
      MATURITY_DATE_EUR[loopbasket] = START_ACCRUAL_DATE_EUR[loopbasket].plus(BOND_TENOR_EUR[loopbasket]);
      BASKET_DEFINITION_EUR[loopbasket] = BondFixedSecurityDefinition.from(EUR, MATURITY_DATE_EUR[loopbasket], START_ACCRUAL_DATE_EUR[loopbasket],
          PAYMENT_TENOR_EUR, RATE_EUR[loopbasket], SETTLEMENT_DAYS_EUR, TARGET, DAY_COUNT_EUR, BUSINESS_DAY_EUR, YIELD_CONVENTION_EUR, IS_EOM_EUR, DE_GOVT);
    }
  }
  private static final ZonedDateTime LAST_TRADING_DATE_EUR = DateUtils.getUTCDate(2014, 6, 6);
  private static final ZonedDateTime FIRST_NOTICE_DATE_EUR = DateUtils.getUTCDate(2014, 6, 6);
  private static final ZonedDateTime LAST_NOTICE_DATE_EUR = DateUtils.getUTCDate(2014, 6, 6);
  private static final double NOTIONAL_EUR = 100000;
  private static final BondFuturesSecurityDefinition BOBLM4_DEFINITION = new BondFuturesSecurityDefinition(LAST_TRADING_DATE_EUR, FIRST_NOTICE_DATE_EUR,
      LAST_NOTICE_DATE_EUR, NOTIONAL_EUR, BASKET_DEFINITION_EUR, CONVERSION_FACTOR_EUR);

  /**
   * Returns the definition of the June 14 Bobl bond futures.
   * @return The bond futures.
   */
  public static BondFuturesSecurityDefinition boblM4Definition() {
    return BOBLM4_DEFINITION;
  }

  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final Currency USD = Currency.USD;
  private static final Period PAYMENT_TENOR_USD = Period.ofMonths(6);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ISDA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final int SETTLEMENT_DAYS = 1;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final int NB_BOND = 7;
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5), Period.ofYears(5) };
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
    DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31) };
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175 };
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292 };
  private static final String US_GOVT = "US GOVT";
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(USD, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], PAYMENT_TENOR_USD, RATE[loopbasket], SETTLEMENT_DAYS, CALENDAR,
          DAY_COUNT, BUSINESS_DAY, YIELD_CONVENTION, IS_EOM, US_GOVT);
    }
  }
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private static final ZonedDateTime FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 9, 29);
  private static final double NOTIONAL = 100000;
  private static final BondFuturesSecurityDefinition FVU1_DEFINITION = new BondFuturesSecurityDefinition(LAST_TRADING_DATE, FIRST_NOTICE_DATE, LAST_NOTICE_DATE, NOTIONAL, BASKET_DEFINITION,
      CONVERSION_FACTOR);

  /**
   * Returns the definition of the September 11 US 5Y-note futures.
   * @return The bond futures.
   */
  public static BondFuturesSecurityDefinition FVU1Definition() {
    return FVU1_DEFINITION;
  }

}
