/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class YieldCurveNodeSensitivityDataBundleTest {

  private static final Double[] T = new Double[] {1., 2., 3., 4.};
  private static final Object[] LABELS = new Object[] {"1Y", "2Y", "3Y", "4Y"};
  private static final double[] X = new double[] {5, 6, 7, 8};
  private static final DoubleLabelledMatrix1D M = new DoubleLabelledMatrix1D(T, X);
  private static final Currency CCY = Currency.USD;
  private static final String NAME = "FUNDING";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMatrix() {
    new YieldCurveNodeSensitivityDataBundle(CCY, null, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new YieldCurveNodeSensitivityDataBundle(null, M, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new YieldCurveNodeSensitivityDataBundle(CCY, M, null);
  }

  @Test
  public void test() {
    final YieldCurveNodeSensitivityDataBundle data = new YieldCurveNodeSensitivityDataBundle(CCY, M, NAME);
    YieldCurveNodeSensitivityDataBundle other = new YieldCurveNodeSensitivityDataBundle(CCY, M, NAME);
    assertEquals(data, other);
    assertEquals(data.hashCode(), other.hashCode());
    assertEquals(data.getCurrency(), CCY);
    assertEquals(data.getLabelledMatrix(), M);
    assertEquals(data.getYieldCurveName(), NAME);
    other = new YieldCurveNodeSensitivityDataBundle(CCY, new DoubleLabelledMatrix1D(T, LABELS, X), NAME);
    assertFalse(other.equals(data));
    other = new YieldCurveNodeSensitivityDataBundle(Currency.GBP, M, NAME);
    assertFalse(other.equals(data));
    other = new YieldCurveNodeSensitivityDataBundle(CCY, M, "PPP");
    assertFalse(other.equals(data));
  }
}
