/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.model.option.definition.Barrier;
import com.opengamma.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class ForexOptionSingleBarrierDefinitionTest {
  private static final Currency CCY1 = Currency.AUD;
  private static final Currency CCY2 = Currency.CAD;
  private static final ZonedDateTime EXCHANGE = DateUtil.getUTCDate(2011, 12, 5);
  private static final double AMOUNT = 1000;
  private static final double RATE = 1.5;
  private static final ForexDefinition FOREX = new ForexDefinition(CCY1, CCY2, EXCHANGE, AMOUNT, RATE);
  private static final ZonedDateTime EXPIRY = DateUtil.getUTCDate(2011, 12, 1);
  private static final boolean IS_CALL = true;
  private static final ForexOptionVanillaDefinition UNDERLYING = new ForexOptionVanillaDefinition(FOREX, EXPIRY, IS_CALL);
  private static final Barrier BARRIER = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CLOSE, 1);
  private static final ForexOptionSingleBarrierDefinition OPTION = new ForexOptionSingleBarrierDefinition(UNDERLYING, BARRIER);
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2011, 7, 1);
  private static final String[] NAMES = new String[] {"USD", "EUR"};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new ForexOptionSingleBarrierDefinition(null, BARRIER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBarrier() {
    new ForexOptionSingleBarrierDefinition(UNDERLYING, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    OPTION.toDerivative(null, NAMES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNames() {
    OPTION.toDerivative(DATE, (String[]) null);
  }

  @Test
  public void testObject() {
    assertEquals(UNDERLYING, OPTION.getUnderlyingOption());
    assertEquals(BARRIER, OPTION.getBarrier());
    ForexOptionSingleBarrierDefinition other = new ForexOptionSingleBarrierDefinition(UNDERLYING, BARRIER);
    assertEquals(OPTION, other);
    assertEquals(OPTION.hashCode(), other.hashCode());
    other = new ForexOptionSingleBarrierDefinition(new ForexOptionVanillaDefinition(FOREX, EXPIRY, !IS_CALL), BARRIER);
    assertFalse(other.equals(OPTION));
    other = new ForexOptionSingleBarrierDefinition(UNDERLYING, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CLOSE, 1));
    assertFalse(other.equals(OPTION));
  }

  @Test
  public void testConversion() {
    final ForexOptionSingleBarrier derivative = OPTION.toDerivative(DATE, NAMES);
    assertEquals(derivative.getUnderlyingOption(), UNDERLYING.toDerivative(DATE, NAMES));
    assertEquals(derivative.getBarrier(), BARRIER);
  }
}
