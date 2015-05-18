/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Contains a set of Future instruments that can be used in tests.
 */
@Test(groups = TestGroup.UNIT)
public class FutureInstrumentsDescriptionDataSet {

  //EURIBOR 3M Index
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final Currency CUR = Currency.EUR;
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2012, 2, 29);
  // Future option
  private static final ZonedDateTime OPTION_EXPIRY = DateUtils.getUTCDate(2012, 6, 19);
  private static final double OPTION_STRIKE = 0.97;
  private static final boolean IS_CALL = false;
  // Derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 13);

  public static InterestRateFutureSecurityDefinition createInterestRateFutureSecurityDefinition() {
    final InterestRateFutureSecurityDefinition sec = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
    return sec;
  }

  public static InterestRateFutureSecurity createInterestRateFutureSecurity() {
    return createInterestRateFutureSecurityDefinition().toDerivative(REFERENCE_DATE);
  }

  public static InterestRateFutureOptionMarginSecurityDefinition createInterestRateFutureOptionMarginSecurityDefinition() {
    final InterestRateFutureSecurityDefinition underlying = createInterestRateFutureSecurityDefinition();
    return new InterestRateFutureOptionMarginSecurityDefinition(underlying, OPTION_EXPIRY, OPTION_STRIKE, IS_CALL);
  }

  public static InterestRateFutureOptionMarginTransactionDefinition createInterestRateFutureOptionMarginTransactionDefinition() {
    final InterestRateFutureSecurityDefinition underlying = createInterestRateFutureSecurityDefinition();
    final InterestRateFutureOptionMarginSecurityDefinition option = new InterestRateFutureOptionMarginSecurityDefinition(underlying, OPTION_EXPIRY, OPTION_STRIKE, IS_CALL);
    return new InterestRateFutureOptionMarginTransactionDefinition(option, 1, TRADE_DATE, .99);
  }

  public static InterestRateFutureOptionPremiumSecurityDefinition createInterestRateFutureOptionPremiumSecurityDefinition() {
    final InterestRateFutureSecurityDefinition underlying = createInterestRateFutureSecurityDefinition();
    return new InterestRateFutureOptionPremiumSecurityDefinition(underlying, OPTION_EXPIRY, OPTION_STRIKE, IS_CALL);
  }

  public static InterestRateFutureOptionPremiumTransactionDefinition createInterestRateFutureOptionPremiumTransactionDefinition() {
    final InterestRateFutureSecurityDefinition underlying = createInterestRateFutureSecurityDefinition();
    final InterestRateFutureOptionPremiumSecurityDefinition option = new InterestRateFutureOptionPremiumSecurityDefinition(underlying, OPTION_EXPIRY, OPTION_STRIKE, IS_CALL);
    return new InterestRateFutureOptionPremiumTransactionDefinition(option, 1, TRADE_DATE, .99);
  }

}
