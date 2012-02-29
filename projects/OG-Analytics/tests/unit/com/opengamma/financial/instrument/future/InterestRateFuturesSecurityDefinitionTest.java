/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.index.iborindex.EURIBOR3M;
import com.opengamma.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.TimeCalculator;

/**
 * Tests the interest rate future security description.
 */
public class InterestRateFuturesSecurityDefinitionTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final IborIndex IBOR_INDEX = new EURIBOR3M(CALENDAR);

  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -IBOR_INDEX.getSpotLag(), CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, IBOR_INDEX.getTenor(), IBOR_INDEX.getBusinessDayConvention(), CALENDAR,
      IBOR_INDEX.isEndOfMonth());
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final double REFERENCE_PRICE = 0.0; // TODO - CASE - Future refactor - 0.0 Refence Price here
  private static final String NAME = "ERU2";
  private static final int QUANTITY = 123;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2012, 2, 29);
  private static final double TRADE_PRICE = 0.9925;

  private static final InterestRateFutureDefinition ERU2_DEFINITION = new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL,
      FUTURE_FACTOR, QUANTITY, NAME);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);

  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLastTradeDate() {
    new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, null, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, QUANTITY, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex() {
    new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, LAST_TRADING_DATE, null, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, QUANTITY, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, QUANTITY, null);
  }

  @Test
  public void getter() {
    assertEquals(LAST_TRADING_DATE, ERU2_DEFINITION.getLastTradingDate());
    assertEquals(IBOR_INDEX, ERU2_DEFINITION.getIborIndex());
    assertEquals(NOTIONAL, ERU2_DEFINITION.getNotional());
    assertEquals(FUTURE_FACTOR, ERU2_DEFINITION.getPaymentAccrualFactor());
    assertEquals(NAME, ERU2_DEFINITION.getName());
    assertEquals(SPOT_LAST_TRADING_DATE, ERU2_DEFINITION.getFixingPeriodStartDate());
    assertEquals(ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, IBOR_INDEX.getTenor(), IBOR_INDEX.getBusinessDayConvention(), CALENDAR, IBOR_INDEX.isEndOfMonth()),
        ERU2_DEFINITION.getFixingPeriodEndDate());
    assertEquals(IBOR_INDEX.getDayCount().getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE), ERU2_DEFINITION.getFixingPeriodAccrualFactor());
    String description = "IRFuture Security: " + NAME + " Last trading date: " + LAST_TRADING_DATE.toString() + " Ibor Index: " + IBOR_INDEX.getName() + " Notional: " + NOTIONAL;
    assertEquals(description, ERU2_DEFINITION.toString());
  }

  @Test
  public void constructor() {
    InterestRateFutureDefinition constructor1 = new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, QUANTITY,
        "RateFuture " + IBOR_INDEX.getName());
    InterestRateFutureDefinition constructor2 = new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, QUANTITY);
    assertTrue(constructor1.equals(constructor2));
  }

  @Test
  public void equalHash() {
    InterestRateFutureDefinition other = new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, QUANTITY, NAME);
    assertTrue(ERU2_DEFINITION.equals(other));
    assertTrue(ERU2_DEFINITION.hashCode() == other.hashCode());
    InterestRateFutureDefinition modifiedFuture;
    modifiedFuture = new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, SPOT_LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, QUANTITY, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL + 1.0, FUTURE_FACTOR, QUANTITY, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR * 2.0, QUANTITY, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, LAST_TRADING_DATE, IBOR_INDEX, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, QUANTITY, NAME + "+");
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    IborIndex otherIndex = new IborIndex(IBOR_INDEX.getCurrency(), IBOR_INDEX.getTenor(), IBOR_INDEX.getSpotLag(), CALENDAR, IBOR_INDEX.getDayCount(), IBOR_INDEX.getBusinessDayConvention(),
        !IBOR_INDEX.isEndOfMonth());
    modifiedFuture = new InterestRateFutureDefinition(TRADE_DATE, TRADE_PRICE, LAST_TRADING_DATE, otherIndex, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, QUANTITY, NAME);
    assertFalse(ERU2_DEFINITION.equals(modifiedFuture));
    assertFalse(ERU2_DEFINITION.equals(IBOR_INDEX));
    assertFalse(ERU2_DEFINITION.equals(null));
  }

  @Test
  public void toDerivative() {
    double LAST_TRADING_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_TRADING_DATE);
    double FIXING_START_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, SPOT_LAST_TRADING_DATE);
    double FIXING_END_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_END_DATE);
    double FIXING_ACCRUAL = IBOR_INDEX.getDayCount().getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
    InterestRateFuture ERU2 = new InterestRateFuture(LAST_TRADING_TIME, IBOR_INDEX, FIXING_START_TIME, FIXING_END_TIME, FIXING_ACCRUAL, REFERENCE_PRICE, NOTIONAL, FUTURE_FACTOR, QUANTITY, NAME,
        DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    InterestRateFuture convertedERU2 = ERU2_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE, CURVES);
    assertTrue("Rate future security converter", ERU2.equals(convertedERU2));
  }
}
