/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.pnl.UnderlyingType;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GreekTest {
  private static final String NAME = "GREEK";
  private static final Underlying UNDERLYING = new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE);

  @Test
  public void test() {
    final Greek greek = new MyGreek(UNDERLYING, NAME);
    assertEquals(greek.toString(), NAME);
    assertEquals(greek.getUnderlying(), UNDERLYING);
    Greek other = new MyGreek(UNDERLYING, NAME);
    assertEquals(other, greek);
    assertEquals(other.hashCode(), greek.hashCode());
    other = new MyGreek(new NthOrderUnderlying(2, UnderlyingType.SPOT_PRICE), NAME);
    assertFalse(other.equals(greek));
    other = new MyGreek(UNDERLYING, "OTHER");
    assertFalse(other.equals(greek));
  }

  private static class MyGreek extends Greek {

    public MyGreek(final Underlying underlying, final String name) {
      super(underlying, name);
    }

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return null;
    }

  }
}
