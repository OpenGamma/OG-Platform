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
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureSecurityTest {
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
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final double NOTIONAL = 1000000;
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";

  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 8, 18);
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE, LocalTime.MIDNIGHT), ZoneOffset.UTC);
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, LAST_TRADING_DATE);
  private static final double FIXING_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, SPOT_LAST_TRADING_DATE);
  private static final double FIXING_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL = DAY_COUNT_INDEX.getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
  private static final InterestRateFutureSecurity ERU2 = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR,
      NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new InterestRateFutureSecurity(LAST_TRADING_TIME, null, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR, null);
  }

  @Test
  public void getter() {
    assertEquals(LAST_TRADING_TIME, ERU2.getTradingLastTime());
    assertEquals(IBOR_INDEX, ERU2.getIborIndex());
    assertEquals(NOTIONAL, ERU2.getNotional());
    assertEquals(FUTURE_FACTOR, ERU2.getPaymentAccrualFactor());
    assertEquals(NAME, ERU2.getName());
    assertEquals(FIXING_START_TIME, ERU2.getFixingPeriodStartTime());
    assertEquals(FIXING_END_TIME, ERU2.getFixingPeriodEndTime());
    assertEquals(FIXING_ACCRUAL, ERU2.getFixingPeriodAccrualFactor());
  }

  @Test
  public void equalHash() {
    assertTrue(ERU2.equals(ERU2));
    final InterestRateFutureSecurity other = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR, NAME);
    assertTrue(ERU2.equals(other));
    assertTrue(ERU2.hashCode() == other.hashCode());
    assertEquals(ERU2.toString(), other.toString());
    InterestRateFutureSecurity modifiedFuture;
    modifiedFuture = new InterestRateFutureSecurity(LAST_TRADING_TIME - 0.01, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR, NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME + 0.01, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR, NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME + 0.01, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR, NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL + 0.01, NOTIONAL, FUTURE_FACTOR, NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL + 1.0, FUTURE_FACTOR, NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR + 0.25, NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureSecurity(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR, NAME + NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    final IborIndex otherIndex = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM, "Ibor");
    modifiedFuture = new InterestRateFutureSecurity(LAST_TRADING_TIME, otherIndex, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, NOTIONAL, FUTURE_FACTOR, NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    assertFalse(ERU2.equals(LAST_TRADING_DATE));
    assertFalse(ERU2.equals(null));
  }
}
