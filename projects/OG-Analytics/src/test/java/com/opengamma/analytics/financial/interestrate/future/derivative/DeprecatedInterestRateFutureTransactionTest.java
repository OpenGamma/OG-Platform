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
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

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
 * Tests related to the construction of interest rate future security.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedInterestRateFutureTransactionTest {
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final double REFERENCE_PRICE = 0.0;
  private static final String NAME = "ERU2";
  private static final int QUANTITY = 123;

  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 8, 18);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE, LocalTime.MIDNIGHT), ZoneOffset.UTC);
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, LAST_TRADING_DATE);
  private static final double FIXING_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, SPOT_LAST_TRADING_DATE);
  private static final double FIXING_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL = DAY_COUNT_INDEX.getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final InterestRateFutureSecurity ERU2 = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL,
      NOTIONAL, FUTURE_FACTOR, NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
  private static final InterestRateFutureTransaction ERU2_TRA = new InterestRateFutureTransaction(ERU2, REFERENCE_PRICE, QUANTITY);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSecurity() {
    new InterestRateFutureTransaction(null, REFERENCE_PRICE, QUANTITY);
  }

  @Test
  public void getter() {
    assertEquals(ERU2, ERU2_TRA.getUnderlying());
    assertEquals(LAST_TRADING_TIME, ERU2_TRA.getLastTradingTime());
    assertEquals(IBOR_INDEX, ERU2_TRA.getIborIndex());
    assertEquals(NOTIONAL, ERU2_TRA.getNotional());
    assertEquals(FUTURE_FACTOR, ERU2_TRA.getPaymentAccrualFactor());
    assertEquals(DISCOUNTING_CURVE_NAME, ERU2_TRA.getDiscountingCurveName());
    assertEquals(FORWARD_CURVE_NAME, ERU2_TRA.getForwardCurveName());
    assertEquals(NAME, ERU2_TRA.getName());
    assertEquals(FIXING_START_TIME, ERU2_TRA.getFixingPeriodStartTime());
    assertEquals(FIXING_END_TIME, ERU2_TRA.getFixingPeriodEndTime());
    assertEquals(FIXING_ACCRUAL, ERU2_TRA.getFixingPeriodAccrualFactor());
  }

  @Test
  public void equalHash() {
    assertTrue(ERU2_TRA.equals(ERU2_TRA));
    final InterestRateFutureTransaction other = new InterestRateFutureTransaction(ERU2, REFERENCE_PRICE, QUANTITY);
    assertTrue(ERU2_TRA.equals(other));
    assertTrue(ERU2_TRA.hashCode() == other.hashCode());
    assertEquals(ERU2_TRA.toString(), other.toString());
    InterestRateFutureTransaction modifiedFuture;
    final InterestRateFutureSecurity modifiedSec = new InterestRateFutureSecurity(LAST_TRADING_TIME - 0.1, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL,
        NOTIONAL, FUTURE_FACTOR, NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    modifiedFuture = new InterestRateFutureTransaction(modifiedSec, REFERENCE_PRICE, QUANTITY);
    assertFalse(ERU2_TRA.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransaction(ERU2, REFERENCE_PRICE + 0.01, QUANTITY);
    assertFalse(ERU2_TRA.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransaction(ERU2, REFERENCE_PRICE, QUANTITY + 1);
    assertFalse(ERU2_TRA.equals(modifiedFuture));
    assertFalse(ERU2_TRA.equals(LAST_TRADING_DATE));
    assertFalse(ERU2_TRA.equals(null));
  }
}
