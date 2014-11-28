/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
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
 * Tests the interest rate future option with margin security description.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureOptionMarginSecurityDefinitionTest {
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Ibor");
  // Future option mid-curve 1Y
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final double STRIKE = 0.9895;
  private static final InterestRateFutureSecurityDefinition ERU2 = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurityDefinition OPTION_ERU2 = new InterestRateFutureOptionMarginSecurityDefinition(ERU2, EXPIRATION_DATE, STRIKE, IS_CALL);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAMES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new InterestRateFutureOptionMarginSecurityDefinition(null, EXPIRATION_DATE, STRIKE, IS_CALL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiration() {
    new InterestRateFutureOptionMarginSecurityDefinition(ERU2, null, STRIKE, IS_CALL);
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals(ERU2, OPTION_ERU2.getUnderlyingFuture());
    assertEquals(EXPIRATION_DATE, OPTION_ERU2.getExpirationDate());
    assertEquals(STRIKE, OPTION_ERU2.getStrike());
    assertEquals(IS_CALL, OPTION_ERU2.isCall());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    final InterestRateFutureOptionMarginSecurityDefinition other = new InterestRateFutureOptionMarginSecurityDefinition(ERU2, EXPIRATION_DATE, STRIKE, IS_CALL);
    assertTrue(OPTION_ERU2.equals(other));
    assertTrue(OPTION_ERU2.hashCode() == other.hashCode());
    InterestRateFutureOptionMarginSecurityDefinition modifiedFuture;
    modifiedFuture = new InterestRateFutureOptionMarginSecurityDefinition(ERU2, LAST_TRADING_DATE, STRIKE, IS_CALL);
    assertFalse(OPTION_ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureOptionMarginSecurityDefinition(ERU2, EXPIRATION_DATE, STRIKE + 0.001, IS_CALL);
    assertFalse(OPTION_ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureOptionMarginSecurityDefinition(ERU2, EXPIRATION_DATE, STRIKE, !IS_CALL);
    assertFalse(OPTION_ERU2.equals(modifiedFuture));
  }

  @Test
  public void toDerivative() {
    final double expirationTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRATION_DATE);
    final InterestRateFutureSecurity underlyingFuture = ERU2.toDerivative(REFERENCE_DATE);
    final InterestRateFutureOptionMarginSecurity security = new InterestRateFutureOptionMarginSecurity(underlyingFuture, expirationTime, STRIKE, IS_CALL);
    final InterestRateFutureOptionMarginSecurity convertedSecurity = OPTION_ERU2.toDerivative(REFERENCE_DATE);
    assertTrue("Rate future option with margining security converter", security.equals(convertedSecurity));
  }

}
