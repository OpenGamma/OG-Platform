/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 */
public class MathCurveTest extends AnalyticsTestBase {

  @SuppressWarnings("unchecked")
  @Test
  public void testConstantCurve() {
    Curve<Double, Double> c1 = ConstantDoublesCurve.from(4.);
    Curve<Double, Double> c2 = cycleObject(Curve.class, c1);
    assertEquals(c1, c2);
    c1 = ConstantDoublesCurve.from(4., "NAME");
    c2 = cycleObject(Curve.class, c1);
    assertEquals(c1, c2);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testInterpolatedCurve() {
    Curve<Double, Double> c1 = InterpolatedDoublesCurve.from(new double[] {1, 2, 3, 4}, new double[] {4, 5, 6, 7}, new LinearInterpolator1D());
    Curve<Double, Double> c2 = cycleObject(Curve.class, c1);
    assertEquals(c1, c2);
    c1 = InterpolatedDoublesCurve.from(new double[] {1, 2, 3, 4}, new double[] {4, 5, 6, 7}, new LinearInterpolator1D(), "NAME");
    c2 = cycleObject(Curve.class, c1);
    assertEquals(c1, c2);
  }
}
