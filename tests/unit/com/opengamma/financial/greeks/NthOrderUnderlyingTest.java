/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

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
    assertEquals(underlying.getUnderlyings(), Collections.singleton(type));
  }
}
