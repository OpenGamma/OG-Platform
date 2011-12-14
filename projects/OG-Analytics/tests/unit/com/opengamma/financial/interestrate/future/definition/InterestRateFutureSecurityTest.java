/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * Tests related to the construction of interest rate future security.
 */
public class InterestRateFutureSecurityTest {
  //EURIBOR 3M Index
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -SETTLEMENT_DAYS, CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final double REFERENCE_PRICE = 0.0;
  private static final String NAME = "ERU2";
  //  private static final InterestRateFutureSecurityDefinition ERU2 = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME);

  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 8, 18);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
  private static final double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, LAST_TRADING_DATE);
  private static final double FIXING_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, SPOT_LAST_TRADING_DATE);
  private static final double FIXING_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_END_DATE);
  private static final double FIXING_ACCRUAL = DAY_COUNT_INDEX.getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final InterestRateFuture ERU2 = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL,
      FUTURE_FACTOR, NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new InterestRateFuture(LAST_TRADING_TIME, null, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, null, DISCOUNTING_CURVE_NAME,
        FORWARD_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDscCurve() {
    new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME, null, FORWARD_CURVE_NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwardCurve() {
    new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME, DISCOUNTING_CURVE_NAME, null);
  }

  @Test
  public void getter() {
    assertEquals(LAST_TRADING_TIME, ERU2.getLastTradingTime());
    assertEquals(IBOR_INDEX, ERU2.getIborIndex());
    assertEquals(NOTIONAL, ERU2.getNotional());
    assertEquals(FUTURE_FACTOR, ERU2.getPaymentAccrualFactor());
    assertEquals(DISCOUNTING_CURVE_NAME, ERU2.getDiscountingCurveName());
    assertEquals(FORWARD_CURVE_NAME, ERU2.getForwardCurveName());
    assertEquals(NAME, ERU2.getName());
    assertEquals(FIXING_START_TIME, ERU2.getFixingPeriodStartTime());
    assertEquals(FIXING_END_TIME, ERU2.getFixingPeriodEndTime());
    assertEquals(FIXING_ACCRUAL, ERU2.getFixingPeriodAccrualFactor());
  }

  @Test
  public void equalHash() {
    assertTrue(ERU2.equals(ERU2));
    InterestRateFuture other = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR,
        NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    assertTrue(ERU2.equals(other));
    assertTrue(ERU2.hashCode() == other.hashCode());
    assertEquals(ERU2.toString(), other.toString());
    InterestRateFuture modifiedFuture;
    modifiedFuture = new InterestRateFuture(LAST_TRADING_TIME - 0.01, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME + 0.01, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME + 0.01, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL + 0.01, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL + 1.0, FUTURE_FACTOR, NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR + 0.25, NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME,
        DISCOUNTING_CURVE_NAME + "NO", FORWARD_CURVE_NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME + "NO");
    assertFalse(ERU2.equals(modifiedFuture));
    modifiedFuture = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME + NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    IborIndex otherIndex = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM);
    modifiedFuture = new InterestRateFuture(LAST_TRADING_TIME, otherIndex, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    assertFalse(ERU2.equals(modifiedFuture));
    assertFalse(ERU2.equals(LAST_TRADING_DATE));
    assertFalse(ERU2.equals(null));
  }
}
