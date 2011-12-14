/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.future.definition.BondFuture;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

/**
 * Contains a set of Future instruments that can be used in tests.
 */
public class FutureInstrumentsDescriptionDataSet {
  //EURIBOR 3M Index
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final Currency CUR = Currency.EUR;
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final double REFERENCE_PRICE = 0.0; // TODO - CASE - Future refactor - 0.0 Reference Price here
  private static final String NAME = "ERU2";

  // Derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 13);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME };

  public static InterestRateFutureDefinition createInterestRateFutureSecurityDefinition() {
    return new InterestRateFutureDefinition(LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME);
  }

  public static InterestRateFuture createInterestRateFutureSecurity() {
    return createInterestRateFutureSecurityDefinition().toDerivative(REFERENCE_DATE, REFERENCE_PRICE, CURVES);
  }

  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final Currency BNDFUT_CUR = Currency.EUR;
  private static final Period BNDFUT_PAYMENT_TENOR = Period.ofMonths(6);
  private static final DayCount BNDFUT_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final BusinessDayConvention BNDFUT_BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final boolean BNDFUT_IS_EOM = false;
  private static final int BNDFUT_SETTLEMENT_DAYS = 1;
  private static final YieldConvention YIELD_CONVENTION = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
  private static final int NB_BOND = 7;
  private static final Period[] BOND_TENOR = new Period[] {Period.ofYears(5), Period.ofYears(5), Period.ofYears(5), Period.ofYears(8), Period.ofYears(5), Period.ofYears(5), Period.ofYears(5) };
  private static final ZonedDateTime[] START_ACCRUAL_DATE = new ZonedDateTime[] {DateUtils.getUTCDate(2010, 11, 30), DateUtils.getUTCDate(2010, 12, 31), DateUtils.getUTCDate(2011, 1, 31),
      DateUtils.getUTCDate(2008, 2, 29), DateUtils.getUTCDate(2011, 3, 31), DateUtils.getUTCDate(2011, 4, 30), DateUtils.getUTCDate(2011, 5, 31) };
  private static final double[] RATE = new double[] {0.01375, 0.02125, 0.0200, 0.02125, 0.0225, 0.0200, 0.0175 };
  private static final double[] CONVERSION_FACTOR = new double[] {.8317, .8565, .8493, .8516, .8540, .8417, .8292 };
  private static final ZonedDateTime[] MATURITY_DATE = new ZonedDateTime[NB_BOND];
  private static final BondFixedSecurityDefinition[] BASKET_DEFINITION = new BondFixedSecurityDefinition[NB_BOND];
  static {
    for (int loopbasket = 0; loopbasket < NB_BOND; loopbasket++) {
      MATURITY_DATE[loopbasket] = START_ACCRUAL_DATE[loopbasket].plus(BOND_TENOR[loopbasket]);
      BASKET_DEFINITION[loopbasket] = BondFixedSecurityDefinition.from(BNDFUT_CUR, MATURITY_DATE[loopbasket], START_ACCRUAL_DATE[loopbasket], BNDFUT_PAYMENT_TENOR, RATE[loopbasket],
          BNDFUT_SETTLEMENT_DAYS, CALENDAR, BNDFUT_DAY_COUNT, BNDFUT_BUSINESS_DAY, YIELD_CONVENTION, BNDFUT_IS_EOM);
    }
  }
  private static final ZonedDateTime BNDFUT_LAST_TRADING_DATE = DateUtils.getUTCDate(2011, 9, 21);
  private static final ZonedDateTime BNDFUT_FIRST_NOTICE_DATE = DateUtils.getUTCDate(2011, 8, 31);
  private static final ZonedDateTime BNDFUT_LAST_NOTICE_DATE = DateUtils.getUTCDate(2011, 9, 29);
  private static final double BNDFUT_NOTIONAL = 100000;
  private static final double BNDFUT_REFERENCE_PRICE = 0.0; // FIXME CASE Confirm BNDFUT_REFERENCE_PRICE. Was 1.23 
  private static final ZonedDateTime BNDFUT_REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 21);
  private static final String CREDIT_CURVE_NAME = "Credit";
  private static final String REPO_CURVE_NAME = "Repo";
  private static final String[] CURVES_NAME = {CREDIT_CURVE_NAME, REPO_CURVE_NAME };

  public static BondFutureDefinition createBondFutureSecurityDefinition() {
    return new BondFutureDefinition(BNDFUT_LAST_TRADING_DATE, BNDFUT_FIRST_NOTICE_DATE, BNDFUT_LAST_NOTICE_DATE, BNDFUT_NOTIONAL, BASKET_DEFINITION, CONVERSION_FACTOR);
  }

  public static BondFuture createBondFutureSecurity() {
    return createBondFutureSecurityDefinition().toDerivative(BNDFUT_REFERENCE_DATE, BNDFUT_REFERENCE_PRICE, CURVES_NAME);
  }

}
