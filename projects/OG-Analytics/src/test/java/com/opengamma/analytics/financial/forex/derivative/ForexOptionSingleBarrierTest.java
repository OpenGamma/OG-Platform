/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ForexOptionSingleBarrierTest {
  private static final Currency CCY1 = Currency.AUD;
  private static final Currency CCY2 = Currency.CAD;
  private static final double PAYMENT_TIME = 0.5;
  private static final double AMOUNT1 = 1000;
  private static final double AMOUNT2 = -1500;
  private static final Forex FOREX = new Forex(new PaymentFixed(CCY1, PAYMENT_TIME, AMOUNT1), new PaymentFixed(CCY2, PAYMENT_TIME, AMOUNT2));
  private static final double EXPIRY_TIME = 0.49;
  private static final boolean IS_CALL = true;
  private static final boolean IS_LONG = true;
  private static final double REBATE = 0.5;
  private static final ForexOptionVanilla UNDERLYING = new ForexOptionVanilla(FOREX, EXPIRY_TIME, IS_CALL, IS_LONG);
  private static final Barrier BARRIER = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CLOSE, 1);
  private static final ForexOptionSingleBarrier OPTION = new ForexOptionSingleBarrier(UNDERLYING, BARRIER);
  private static final ForexOptionSingleBarrier OPTION_REBATE = new ForexOptionSingleBarrier(UNDERLYING, BARRIER, REBATE);

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
    assertEquals(0.0, OPTION.getRebate(), 1.0E-10);
    assertEquals(REBATE, OPTION_REBATE.getRebate(), 1.0E-10);
    assertEquals(OPTION, OPTION);
    ForexOptionSingleBarrier other = new ForexOptionSingleBarrier(UNDERLYING, BARRIER);
    assertEquals(OPTION, other);
    assertEquals(OPTION.hashCode(), other.hashCode());
    final ForexOptionSingleBarrier otherRebate = new ForexOptionSingleBarrier(UNDERLYING, BARRIER, REBATE);
    assertEquals(OPTION_REBATE, otherRebate);
    assertEquals(OPTION_REBATE.hashCode(), otherRebate.hashCode());
    other = new ForexOptionSingleBarrier(new ForexOptionVanilla(FOREX, EXPIRY_TIME, !IS_CALL, IS_LONG), BARRIER);
    assertFalse(other.equals(OPTION));
    other = new ForexOptionSingleBarrier(UNDERLYING, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CLOSE, 1));
    assertFalse(other.equals(OPTION));
    assertFalse(OPTION_REBATE.equals(OPTION));
    assertFalse(other.equals(CCY1));
    assertFalse(other.equals(null));
  }
}
