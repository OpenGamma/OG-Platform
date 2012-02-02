/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * 
 */
public class ModelForwardCurveTest extends AnalyticsTestBase {
  private static final double[] EXPIRIES = new double[] {1, 2, 3, 4, 5};
  private static final double[] FORWARD = new double[] {100, 101, 102, 103, 104};
  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double EPS = 1e-12;

  //Test in so that it will break when the drift curve is serialized
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testDriftCurveNotSerialized1() {
    final double spot = 100;
    final Curve<Double, Double> driftCurve = InterpolatedDoublesCurve.from(EXPIRIES, FORWARD, INTERPOLATOR);
    final ForwardCurve curve = new ForwardCurve(spot, driftCurve);
    cycleObject(ForwardCurve.class, curve);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testDriftCurveNotSerialized2() {
    final double spot = 100;
    final double drift = 1.5;
    final ForwardCurve curve = new ForwardCurve(spot, drift);
    cycleObject(ForwardCurve.class, curve);
  }

  @Test
  public void testCurve1() {
    final double spot = 100;
    final ForwardCurve curve1 = new ForwardCurve(spot);
    final ForwardCurve curve2 = cycleObject(ForwardCurve.class, curve1);
    assertEquals(curve1.getSpot(), curve2.getSpot(), EPS);
    assertTrue(curve2.getForwardCurve() instanceof ConstantDoublesCurve);
    assertTrue(curve2.getDriftCurve() instanceof FunctionalDoublesCurve);
    assertCurveEquals(curve1.getForwardCurve(), curve2.getForwardCurve());
    assertCurveEquals(curve1.getDriftCurve(), curve2.getDriftCurve());
  }

  @Test
  public void testCurve2() {
    final ForwardCurve curve1 = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARD, INTERPOLATOR));
    final ForwardCurve curve2 = cycleObject(ForwardCurve.class, curve1);
    assertEquals(curve1.getSpot(), curve2.getSpot(), EPS);
    assertTrue(curve2.getDriftCurve() instanceof FunctionalDoublesCurve);
    assertCurveEquals(curve1.getForwardCurve(), curve2.getForwardCurve());
    assertCurveEquals(curve1.getDriftCurve(), curve2.getDriftCurve());
  }

  private void assertCurveEquals(final Curve<Double, Double> c1, final Curve<Double, Double> c2) {
    if (c1 != c2) {
      for (double x = 0.1; x < 100.0; x += 5.00000001) {
        assertEquals(c1.getYValue(x), c2.getYValue(x));
      }
    }
  }
}
