/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;

import com.opengamma.financial.pnl.UnderlyingType;

public class NthOrderUnderlyingTest {

  @Test(expected = IllegalArgumentException.class)
  public void testOrder() {
    new NthOrderUnderlying(-2, UnderlyingType.SPOT_PRICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnderlyingType() {
    new NthOrderUnderlying(1, null);
  }

  @Test
  public void test() {
    final int order = 3;
    final UnderlyingType type = UnderlyingType.SPOT_PRICE;
    final Underlying underlying = new NthOrderUnderlying(order, type);
    assertEquals(underlying.getOrder(), order);
    assertEquals(underlying.getUnderlyings(), Arrays.asList(type));
    final Underlying other = new NthOrderUnderlying(order, type);
    assertFalse(other.equals(type));
  }
}
