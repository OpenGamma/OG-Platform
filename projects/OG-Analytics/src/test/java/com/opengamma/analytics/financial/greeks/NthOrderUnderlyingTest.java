/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NthOrderUnderlyingTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOrder() {
    new NthOrderUnderlying(-2, UnderlyingType.SPOT_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
