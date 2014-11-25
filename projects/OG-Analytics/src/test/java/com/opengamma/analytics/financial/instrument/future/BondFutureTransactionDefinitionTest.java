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

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to bond futures transaction Definition construction.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureTransactionDefinitionTest {

  private static final BondFuturesSecurityDefinition FUTURE_DEFINITION = BondFuturesDataSets.FVU1Definition();
  // Transaction
  private static final int QUANTITY = 4321;
  private static final double TRADE_PRICE = 1.0987;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2011, 6, 21);
  private static final BondFuturesTransactionDefinition FUTURE_TRANSACTION_DEFINITION = new BondFuturesTransactionDefinition(FUTURE_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFuture() {
    new BondFuturesTransactionDefinition(null, QUANTITY, TRADE_DATE, TRADE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTradeDate() {
    new BondFuturesTransactionDefinition(FUTURE_DEFINITION, QUANTITY, null, TRADE_PRICE);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("Bond future transaction definition: underlying", FUTURE_DEFINITION, FUTURE_TRANSACTION_DEFINITION.getUnderlyingSecurity());
    assertEquals("Bond future transaction definition: quantity", QUANTITY, FUTURE_TRANSACTION_DEFINITION.getQuantity());
    assertEquals("Bond future transaction definition: trade date", TRADE_DATE, FUTURE_TRANSACTION_DEFINITION.getTradeDate());
    assertEquals("Bond future transaction definition: trade price", TRADE_PRICE, FUTURE_TRANSACTION_DEFINITION.getTradePrice());
  }

  @Test
  /**
   * Tests the equal and hash code methods.
   */
  public void equalHash() {
    assertTrue(FUTURE_TRANSACTION_DEFINITION.equals(FUTURE_TRANSACTION_DEFINITION));
    final BondFuturesTransactionDefinition other = new BondFuturesTransactionDefinition(FUTURE_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertTrue(FUTURE_TRANSACTION_DEFINITION.equals(other));
    assertTrue(FUTURE_TRANSACTION_DEFINITION.hashCode() == other.hashCode());
    BondFuturesTransactionDefinition modifiedFuture;
    modifiedFuture = new BondFuturesTransactionDefinition(FUTURE_DEFINITION, QUANTITY + 1, TRADE_DATE, TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new BondFuturesTransactionDefinition(FUTURE_DEFINITION, QUANTITY, TRADE_DATE.plusDays(1), TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new BondFuturesTransactionDefinition(FUTURE_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE + 0.001);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    final BondFuturesSecurityDefinition otherUnderlying = new BondFuturesSecurityDefinition(FUTURE_DEFINITION.getLastTradingDate(), FUTURE_DEFINITION.getNoticeFirstDate(),
        FUTURE_DEFINITION.getNoticeLastDate(), 2 * FUTURE_DEFINITION.getNotional(), FUTURE_DEFINITION.getDeliveryBasket(), FUTURE_DEFINITION.getConversionFactor());
    modifiedFuture = new BondFuturesTransactionDefinition(otherUnderlying, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(QUANTITY));
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(null));
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeOnTradeDate() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 21);
    final double lastMarginPrice = 1.0234;
    final BondFuturesTransaction futureConverted = FUTURE_TRANSACTION_DEFINITION.toDerivative(referenceDate, lastMarginPrice);
    final BondFuturesSecurity security = FUTURE_DEFINITION.toDerivative(referenceDate);
    final BondFuturesTransaction futureConstructed = new BondFuturesTransaction(security, QUANTITY, TRADE_PRICE);
    assertEquals("Bond future transaction definition: to derivative", futureConstructed, futureConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeAfterTradeDate() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 22);
    final double lastMarginPrice = 1.0234;
    final BondFuturesTransaction futureConverted = FUTURE_TRANSACTION_DEFINITION.toDerivative(referenceDate, lastMarginPrice);
    final BondFuturesSecurity security = FUTURE_DEFINITION.toDerivative(referenceDate);
    final BondFuturesTransaction futureConstructed = new BondFuturesTransaction(security, QUANTITY, lastMarginPrice);
    assertEquals("Bond future transaction definition: to derivative", futureConstructed, futureConverted);
  }

  /**
   * Tests the exception of to derivative method when no reference price is provided.
   */
  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void toDerivativeNoReferencePrice() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 22);
    FUTURE_TRANSACTION_DEFINITION.toDerivative(referenceDate);
  }
}
