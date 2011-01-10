/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.trade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class OptionTradeDataTest {
  private static final double N1 = 100;
  private static final double N2 = 200;
  private static final double PV1 = 25;
  private static final double PV2 = 10;

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePointValue() {
    new OptionTradeData(N1, -PV1);
  }

  @Test
  public void test() {
    final OptionTradeData data = new OptionTradeData(N1, PV1);
    OptionTradeData other = new OptionTradeData(N1, PV1);
    assertEquals(data, other);
    assertEquals(data.hashCode(), other.hashCode());
    other = new OptionTradeData(N2, PV1);
    assertFalse(data.equals(other));
    other = new OptionTradeData(N1, PV2);
    assertFalse(data.equals(other));
    assertEquals(data.getNumberOfContracts(), N1, 0);
    assertEquals(data.getPointValue(), PV1, 0);
  }
}
