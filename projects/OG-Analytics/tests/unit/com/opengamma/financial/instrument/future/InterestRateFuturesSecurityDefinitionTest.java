/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

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
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

/**
 * Tests the interest rate future security description.
 */
public class InterestRateFuturesSecurityDefinitionTest {
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
  private static final double REFERENCE_PRICE = 0.0; // TODO - CASE - Future refactor - 0.0 Refence Price here
  private static final String NAME = "ERU2";
  private static final InterestRateFutureDefinition ERU2_DEFINITION = new InterestRateFutureDefinition(LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");

  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLastTradeDate() {
    new InterestRateFutureDefinition(null, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new InterestRateFutureDefinition(LAST_TRADING_DATE, null, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new InterestRateFutureDefinition(LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, null);
  }

  @Test
  public void getter() {
    assertEquals(LAST_TRADING_DATE, ERU2_DEFINITION.getLastTradingDate());
    assertEquals(IBOR_INDEX, ERU2_DEFINITION.getIborIndex());
    assertEquals(NOTIONAL, ERU2_DEFINITION.getNotional());
    assertEquals(FUTURE_FACTOR, ERU2_DEFINITION.getPaymentAccrualFactor());
    assertEquals(NAME, ERU2_DEFINITION.getName());
    assertEquals(SPOT_LAST_TRADING_DATE, ERU2_DEFINITION.getFixingPeriodStartDate());
    assertEquals(ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, TENOR, BUSINESS_DAY, CALENDAR, IS_EOM), ERU2_DEFINITION.getFixingPeriodEndDate());
    assertEquals(DAY_COUNT_INDEX.getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE), ERU2_DEFINITION.getFixingPeriodAccrualFactor());
    String description = "IRFuture Security: " + NAME + " Last trading date: " + LAST_TRADING_DATE.toString() + " Ibor Index: " + IBOR_INDEX.getName() + " Notional: " + NOTIONAL;
    assertEquals(description, ERU2_DEFINITION.toString());
  }

  @Test
  public void constructor() {
    InterestRateFutureDefinition constructor1 = new InterestRateFutureDefinition(LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, "RateFuture " +
        IBOR_INDEX.getName());
    InterestRateFutureDefinition constructor2 = new InterestRateFutureDefinition(LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR);
    assertTrue(constructor1.equals(constructor2));
  }

  @Test
  public void equalHash() {
    InterestRateFutureDefinition other = new InterestRateFutureDefinition(LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME);
    assertTrue(ERU2_DEFINITION.equals(other));
    assertTrue(ERU2_DEFINITION.hashCode() == other.hashCode());
    InterestRateFutureDefinition modifiedFuture;
    modifiedFuture = new InterestRateFutureDefinition(SPOT_LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureDefinition(LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL + 1.0, FUTURE_FACTOR, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureDefinition(LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR * 2.0, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureDefinition(LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME + "+");
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    IborIndex otherIndex = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, !IS_EOM);
    modifiedFuture = new InterestRateFutureDefinition(LAST_TRADING_DATE, otherIndex, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    assertFalse(ERU2_DEFINITION.equals(IBOR_INDEX));
    assertFalse(ERU2_DEFINITION.equals(null));
  }

  @Test
  public void toDerivative() {
    double LAST_TRADING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, LAST_TRADING_DATE);
    double FIXING_START_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, SPOT_LAST_TRADING_DATE);
    double FIXING_END_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE, FIXING_END_DATE);
    double FIXING_ACCRUAL = DAY_COUNT_INDEX.getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
    InterestRateFuture ERU2 = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR,
        NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    InterestRateFuture convertedERU2 = ERU2_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE, CURVES);
    assertTrue("Rate future security converter", ERU2.equals(convertedERU2));
  }
}
