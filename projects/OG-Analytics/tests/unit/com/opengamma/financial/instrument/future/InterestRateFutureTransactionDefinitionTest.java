/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the interest rate future transaction construction.
 */
public class InterestRateFutureTransactionDefinitionTest { //EURIBOR 3M Index
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
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, CALENDAR, -SETTLEMENT_DAYS);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final InterestRateFutureSecurityDefinition ERU2_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME);

  private static final LocalDate REFERENCE_DATE = LocalDate.of(2011, 5, 16);
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
  // Transaction
  private static final int QUANTITY = -123;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 5, 12);
  private static final double TRADE_PRICE = 0.985;
  private static final InterestRateFutureTransactionDefinition FUTURE_TRANSACTION_DEFINITION = new InterestRateFutureTransactionDefinition(ERU2_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);

  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullunderlying() {
    new InterestRateFutureTransactionDefinition(null, QUANTITY, TRADE_DATE, TRADE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTradeDate() {
    new InterestRateFutureTransactionDefinition(ERU2_DEFINITION, QUANTITY, null, TRADE_PRICE);
  }

  @Test
  public void getter() {
    assertEquals(ERU2_DEFINITION, FUTURE_TRANSACTION_DEFINITION.getUnderlyingFuture());
    assertEquals(QUANTITY, FUTURE_TRANSACTION_DEFINITION.getQuantity());
    assertEquals(TRADE_DATE, FUTURE_TRANSACTION_DEFINITION.getTradeDate());
    assertEquals(TRADE_PRICE, FUTURE_TRANSACTION_DEFINITION.getTradePrice());
    String description = "IRFuture Transaction:  Underlying: " + ERU2_DEFINITION.toString() + " Trade date: " + TRADE_DATE.toString() + " Trade price: " + TRADE_PRICE;
    assertEquals(description, FUTURE_TRANSACTION_DEFINITION.toString());
  }

  @Test
  public void equalHash() {
    assertTrue(FUTURE_TRANSACTION_DEFINITION.equals(FUTURE_TRANSACTION_DEFINITION));
    InterestRateFutureTransactionDefinition other = new InterestRateFutureTransactionDefinition(ERU2_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertTrue(FUTURE_TRANSACTION_DEFINITION.equals(other));
    assertTrue(FUTURE_TRANSACTION_DEFINITION.hashCode() == other.hashCode());
    InterestRateFutureTransactionDefinition modifiedFuture;
    modifiedFuture = new InterestRateFutureTransactionDefinition(ERU2_DEFINITION, QUANTITY + 1, TRADE_DATE, TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransactionDefinition(ERU2_DEFINITION, QUANTITY, LAST_TRADING_DATE, TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new InterestRateFutureTransactionDefinition(ERU2_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE + 0.001);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    InterestRateFutureSecurityDefinition otherUnderlying = new InterestRateFutureSecurityDefinition(SPOT_LAST_TRADING_DATE, IBOR_INDEX, NOTIONAL, FUTURE_FACTOR, NAME);
    modifiedFuture = new InterestRateFutureTransactionDefinition(otherUnderlying, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(QUANTITY));
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(null));
  }

  @Test
  public void toDerivativeTradeInPast() {
    InterestRateFutureSecurity ERU2 = ERU2_DEFINITION.toDerivative(REFERENCE_DATE_ZONED, CURVES);
    double lastMarginPrice = 0.99;
    InterestRateFutureTransaction futureTransactionConverted = FUTURE_TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE_ZONED, lastMarginPrice, CURVES);
    InterestRateFutureTransaction futureTransaction = new InterestRateFutureTransaction(ERU2, QUANTITY, lastMarginPrice);
    assertTrue(futureTransactionConverted.equals(futureTransaction));
  }

  @Test
  public void toDerivativeTradeToday() {
    ZonedDateTime referenceDate = TRADE_DATE;
    InterestRateFutureSecurity ERU2 = ERU2_DEFINITION.toDerivative(referenceDate, CURVES);
    double lastMarginPrice = 0.99;
    InterestRateFutureTransaction futureTransactionConverted = FUTURE_TRANSACTION_DEFINITION.toDerivative(referenceDate, lastMarginPrice, CURVES);
    InterestRateFutureTransaction futureTransaction = new InterestRateFutureTransaction(ERU2, QUANTITY, TRADE_PRICE);
    assertTrue(futureTransactionConverted.equals(futureTransaction));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void toDerivativeTradeFuture() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 5, 11);
    double lastMarginPrice = 0.99;
    FUTURE_TRANSACTION_DEFINITION.toDerivative(referenceDate, lastMarginPrice, CURVES);
  }

  @Test
  /**
   * Tests that the toDerivative without reference price returns the correct exception.
   */
  public void toDerivativeNoReferencePrice() {
    try {
      FUTURE_TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE_ZONED, CURVES);
      assertTrue(false);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

}
