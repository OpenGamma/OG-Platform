/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.model.option.definition.Barrier;
import com.opengamma.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexOptionSingleBarrierTest {
  private static final Currency CCY1 = Currency.AUD;
  private static final Currency CCY2 = Currency.CAD;
  private static final double PAYMENT_TIME = 0.5;
  private static final double AMOUNT1 = 1000;
  private static final double AMOUNT2 = -1500;
  private static final Forex FOREX = new Forex(new PaymentFixed(CCY1, PAYMENT_TIME, AMOUNT1, "USD"), new PaymentFixed(CCY2, PAYMENT_TIME, AMOUNT2, "EUR"));
  private static final double EXPIRY_TIME = 0.49;
  private static final boolean IS_CALL = true;
  private static final ForexOptionVanilla UNDERLYING = new ForexOptionVanilla(FOREX, EXPIRY_TIME, IS_CALL);
  private static final Barrier BARRIER = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CLOSE, 1);
  private static final ForexOptionSingleBarrier OPTION = new ForexOptionSingleBarrier(UNDERLYING, BARRIER);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new ForexOptionSingleBarrier(null, BARRIER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBarrier() {
    new ForexOptionSingleBarrier(UNDERLYING, null);
  }

  @Test
  public void testObject() {
    assertEquals(UNDERLYING, OPTION.getUnderlyingOption());
    assertEquals(BARRIER, OPTION.getBarrier());
    ForexOptionSingleBarrier other = new ForexOptionSingleBarrier(UNDERLYING, BARRIER);
    assertEquals(OPTION, other);
    assertEquals(OPTION.hashCode(), other.hashCode());
    other = new ForexOptionSingleBarrier(new ForexOptionVanilla(FOREX, EXPIRY_TIME, !IS_CALL), BARRIER);
    assertFalse(other.equals(OPTION));
    other = new ForexOptionSingleBarrier(UNDERLYING, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CLOSE, 1));
    assertFalse(other.equals(OPTION));
  }
}
