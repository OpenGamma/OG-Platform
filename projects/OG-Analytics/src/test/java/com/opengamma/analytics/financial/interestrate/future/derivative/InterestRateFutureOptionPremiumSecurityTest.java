/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
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
 * Tests on the construction of interest rate future option with up-front payment.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureOptionPremiumSecurityTest {
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "EDU2";
  private static final double STRIKE = 0.9850;
  private static final InterestRateFutureSecurityDefinition EDU2_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final InterestRateFutureSecurity EDU2 = EDU2_DEFINITION.toDerivative(REFERENCE_DATE);
  // Option
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final double EXPIRATION_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, EXPIRATION_DATE);
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurity OPTION_EDU2 = new InterestRateFutureOptionMarginSecurity(EDU2, EXPIRATION_TIME, STRIKE, IS_CALL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new InterestRateFutureOptionPremiumSecurity(null, EXPIRATION_TIME, STRIKE, IS_CALL);
  }

  @Test
  public void getter() {
    assertEquals(EDU2, OPTION_EDU2.getUnderlyingFuture());
    assertEquals(EXPIRATION_TIME, OPTION_EDU2.getExpirationTime());
    assertEquals(STRIKE, OPTION_EDU2.getStrike());
    assertEquals(IS_CALL, OPTION_EDU2.isCall());
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    final InterestRateFutureOptionMarginSecurity newOption = new InterestRateFutureOptionMarginSecurity(EDU2, EXPIRATION_TIME, STRIKE, IS_CALL);
    assertTrue(OPTION_EDU2.equals(newOption));
    assertEquals(OPTION_EDU2.hashCode(), newOption.hashCode());
    InterestRateFutureOptionMarginSecurity modifiedOption;
    modifiedOption = new InterestRateFutureOptionMarginSecurity(EDU2, EXPIRATION_TIME - 0.01, STRIKE, IS_CALL);
    assertFalse(OPTION_EDU2.equals(modifiedOption));
    modifiedOption = new InterestRateFutureOptionMarginSecurity(EDU2, EXPIRATION_TIME, STRIKE + 0.01, IS_CALL);
    assertFalse(OPTION_EDU2.equals(modifiedOption));
    modifiedOption = new InterestRateFutureOptionMarginSecurity(EDU2, EXPIRATION_TIME, STRIKE, !IS_CALL);
    assertFalse(OPTION_EDU2.equals(modifiedOption));
  }

}
