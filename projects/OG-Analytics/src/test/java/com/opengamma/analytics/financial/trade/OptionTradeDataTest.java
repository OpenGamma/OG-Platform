/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.trade;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class OptionTradeDataTest {
  private static final double N1 = 100;
  private static final double N2 = 200;
  private static final double PV1 = 25;
  private static final double PV2 = 10;

  @Test(expectedExceptions = IllegalArgumentException.class)
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
