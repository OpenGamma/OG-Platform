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
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the interest rate future security description.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFuturesTransactionDefinitionTest {

  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final IborIndex IBOR_INDEX = IndexIborMaster.getInstance().getIndex("EURIBOR3M");

  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -IBOR_INDEX.getSpotLag(), CALENDAR);
  private static final ZonedDateTime FIXING_END_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, IBOR_INDEX.getTenor(), IBOR_INDEX.getBusinessDayConvention(), CALENDAR,
      IBOR_INDEX.isEndOfMonth());
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final int QUANTITY = 123;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2012, 2, 29);
  private static final double TRADE_PRICE = 0.9925;

  private static final InterestRateFutureSecurityDefinition ERU2_SEC_DEFINITION =
      new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
  private static final InterestRateFutureTransactionDefinition ERU2_TRA_DEFINITION =
      new InterestRateFutureTransactionDefinition(ERU2_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);

  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSecurity() {
    new InterestRateFutureTransactionDefinition(null, QUANTITY, TRADE_DATE, TRADE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTradeDate() {
    new InterestRateFutureTransactionDefinition(ERU2_SEC_DEFINITION, QUANTITY, null, TRADE_PRICE);
  }

  @Test
  public void getter() {
    assertEquals("InterestRateFuturesTransactionDefinition: getter", ERU2_SEC_DEFINITION, ERU2_TRA_DEFINITION.getUnderlyingFuture());
    assertEquals("InterestRateFuturesTransactionDefinition: getter", QUANTITY, ERU2_TRA_DEFINITION.getQuantity());
    assertEquals("InterestRateFuturesTransactionDefinition: getter", TRADE_DATE, ERU2_TRA_DEFINITION.getTradeDate());
    assertEquals("InterestRateFuturesTransactionDefinition: getter", TRADE_PRICE, ERU2_TRA_DEFINITION.getTradePrice());
  }

  @Test
  public void equalHash() {
    assertTrue("InterestRateFuturesTransactionDefinition: equal-hash", ERU2_TRA_DEFINITION.equals(ERU2_TRA_DEFINITION));
    final InterestRateFutureTransactionDefinition other = new InterestRateFutureTransactionDefinition(ERU2_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertTrue("InterestRateFuturesTransactionDefinition: equal-hash", ERU2_TRA_DEFINITION.equals(other));
    assertTrue("InterestRateFuturesTransactionDefinition: equal-hash", ERU2_TRA_DEFINITION.hashCode() == other.hashCode());
    InterestRateFutureTransactionDefinition modifiedFuture;
    final InterestRateFutureSecurityDefinition modifiedSec =
        new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE.minusDays(1), IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
    modifiedFuture = new InterestRateFutureTransactionDefinition(modifiedSec, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertFalse(ERU2_TRA_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransactionDefinition(ERU2_SEC_DEFINITION, QUANTITY + 1, TRADE_DATE, TRADE_PRICE);
    assertFalse(ERU2_TRA_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransactionDefinition(ERU2_SEC_DEFINITION, QUANTITY, TRADE_DATE.plusDays(1), TRADE_PRICE);
    assertFalse(ERU2_TRA_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransactionDefinition(ERU2_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE + 0.01);
    assertFalse(ERU2_TRA_DEFINITION.equals(modifiedFuture));
    assertFalse(ERU2_TRA_DEFINITION.equals(IBOR_INDEX));
    assertFalse(ERU2_TRA_DEFINITION.equals(null));
  }

  @SuppressWarnings("deprecation")
  @Test
  public void toDerivativeDeprecated() {
    final double lastTradingTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_TRADING_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, SPOT_LAST_TRADING_DATE);
    final double fixingEndTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_END_DATE);
    final double fixingAccrual = IBOR_INDEX.getDayCount().getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
    final InterestRateFutureSecurity ERU2 = new InterestRateFutureSecurity(lastTradingTime, IBOR_INDEX, fixingStartTime, fixingEndTime, fixingAccrual, NOTIONAL, FUTURE_FACTOR,
        NAME, DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME);
    final InterestRateFutureTransaction ERU2_TRA = new InterestRateFutureTransaction(ERU2, TRADE_PRICE, QUANTITY);
    final InterestRateFutureTransaction convertedERU2 = ERU2_TRA_DEFINITION.toDerivative(REFERENCE_DATE, TRADE_PRICE, CURVES);
    assertTrue("Rate future security converter", ERU2_TRA.equals(convertedERU2));
  }

  @Test
  public void toDerivative() {
    final double lastTradingTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_TRADING_DATE);
    final double fixingStartTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, SPOT_LAST_TRADING_DATE);
    final double fixingEndTime = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_END_DATE);
    final double fixingAccrual = IBOR_INDEX.getDayCount().getDayCountFraction(SPOT_LAST_TRADING_DATE, FIXING_END_DATE);
    final InterestRateFutureSecurity ERU2 = new InterestRateFutureSecurity(lastTradingTime, IBOR_INDEX, fixingStartTime, fixingEndTime, fixingAccrual, NOTIONAL, FUTURE_FACTOR, NAME);
    final InterestRateFutureTransaction ERU2_TRA = new InterestRateFutureTransaction(ERU2, TRADE_PRICE, QUANTITY);
    final InterestRateFutureTransaction convertedERU2 = ERU2_TRA_DEFINITION.toDerivative(REFERENCE_DATE, TRADE_PRICE);
    assertTrue("Rate future security converter", ERU2_TRA.equals(convertedERU2));
  }
}
