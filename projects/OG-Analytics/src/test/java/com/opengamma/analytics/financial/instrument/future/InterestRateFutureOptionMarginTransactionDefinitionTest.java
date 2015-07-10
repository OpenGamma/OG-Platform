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
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
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
 * Tests the interest rate future option with margin transaction description.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureOptionMarginTransactionDefinitionTest {
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
  // Transaction
  private static final int QUANTITY = -123;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 5, 12);
  private static final double TRADE_PRICE = 0.0050;
  private static final InterestRateFutureOptionMarginTransactionDefinition OPTION_TRANSACTION = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_ERU2, QUANTITY, TRADE_DATE, TRADE_PRICE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 13);
  private static final String DISCOUNTING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAMES = {DISCOUNTING_CURVE_NAME, FORWARD_CURVE_NAME };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new InterestRateFutureOptionMarginTransactionDefinition(null, QUANTITY, TRADE_DATE, TRADE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTradeDate() {
    new InterestRateFutureOptionMarginTransactionDefinition(OPTION_ERU2, QUANTITY, null, TRADE_PRICE);
  }

  @Test
  /**
   * Tests the class getters.
   */
  public void getter() {
    assertEquals(OPTION_ERU2, OPTION_TRANSACTION.getUnderlyingSecurity());
    assertEquals(QUANTITY, OPTION_TRANSACTION.getQuantity());
    assertEquals(TRADE_DATE, OPTION_TRANSACTION.getTradeDate());
    assertEquals(TRADE_PRICE, OPTION_TRANSACTION.getTradePrice());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    final InterestRateFutureOptionMarginTransactionDefinition other = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_ERU2, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertTrue(OPTION_TRANSACTION.equals(other));
    assertTrue(OPTION_TRANSACTION.hashCode() == other.hashCode());
    InterestRateFutureOptionMarginTransactionDefinition modifidOption;
    modifidOption = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_ERU2, QUANTITY + 1, TRADE_DATE, TRADE_PRICE);
    assertFalse(OPTION_TRANSACTION.equals(modifidOption));
    modifidOption = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_ERU2, QUANTITY, LAST_TRADING_DATE, TRADE_PRICE);
    assertFalse(OPTION_TRANSACTION.equals(modifidOption));
    modifidOption = new InterestRateFutureOptionMarginTransactionDefinition(OPTION_ERU2, QUANTITY, TRADE_DATE, TRADE_PRICE - 0.00001);
    assertFalse(OPTION_TRANSACTION.equals(modifidOption));
  }

  @Test
  public void toDerivativeTradeInPast() {
    final InterestRateFutureOptionMarginSecurity securityConverted = OPTION_ERU2.toDerivative(REFERENCE_DATE);
    final double lastMarginPrice = 0.99;
    final InterestRateFutureOptionMarginTransaction transactionConverted = OPTION_TRANSACTION.toDerivative(REFERENCE_DATE, lastMarginPrice);
    final InterestRateFutureOptionMarginTransaction transaction = new InterestRateFutureOptionMarginTransaction(securityConverted, QUANTITY, lastMarginPrice);
    assertTrue("Conversion with trade date in the past", transactionConverted.equals(transaction));
  }

  @Test
  public void toDerivativeTradeToday() {
    final ZonedDateTime referenceDate = TRADE_DATE;
    final InterestRateFutureOptionMarginSecurity securityConverted = OPTION_ERU2.toDerivative(referenceDate);
    final double lastMarginPrice = 0.99;
    final InterestRateFutureOptionMarginTransaction transactionConverted = OPTION_TRANSACTION.toDerivative(referenceDate, lastMarginPrice);
    final InterestRateFutureOptionMarginTransaction transaction = new InterestRateFutureOptionMarginTransaction(securityConverted, QUANTITY, TRADE_PRICE);
    assertTrue("Conversion with trade date in the past", transactionConverted.equals(transaction));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void toDerivativeTradeFuture() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, -1, CALENDAR);
    final double lastMarginPrice = 0.99;
    OPTION_TRANSACTION.toDerivative(referenceDate, lastMarginPrice);
  }

}
