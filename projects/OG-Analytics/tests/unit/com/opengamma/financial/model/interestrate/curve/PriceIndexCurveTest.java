/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;

public class PriceIndexCurveTest {

  private static double[] INDEX_VALUE = new double[] {108.23, 108.64, 111.0, 115.0};
  private static double[] TIME_VALUE = new double[] {-3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 2.0 + 9.0 / 12.0};
  private static final InterpolatedDoublesCurve CURVE = InterpolatedDoublesCurve.from(TIME_VALUE, INDEX_VALUE, new LinearInterpolator1D());
  private static final PriceIndexCurve PRICE_INDEX_CURVE = new PriceIndexCurve(CURVE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve() {
    new PriceIndexCurve(null);
  }

  @Test
  /**
   * Tests the getter.
   */
  public void getter() {
    assertEquals(CURVE, PRICE_INDEX_CURVE.getCurve());
  }

  @Test
  /**
   * Tests price index.
   */
  public void priceIndex() {
    assertEquals(INDEX_VALUE[0], PRICE_INDEX_CURVE.getPriceIndex(TIME_VALUE[0]), 1.0E-10);
    assertEquals(INDEX_VALUE[2], PRICE_INDEX_CURVE.getPriceIndex(TIME_VALUE[2]), 1.0E-10);
    assertEquals((INDEX_VALUE[2] + INDEX_VALUE[3]) / 2.0, PRICE_INDEX_CURVE.getPriceIndex((TIME_VALUE[2] + TIME_VALUE[3]) / 2.0), 1.0E-10);
  }
}
