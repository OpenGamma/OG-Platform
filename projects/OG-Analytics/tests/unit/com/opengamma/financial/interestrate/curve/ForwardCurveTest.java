/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.FlatExtrapolator1D;

/**
 * 
 */
public class ForwardCurveTest {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final CombinedInterpolatorExtrapolator EXTRAPOLATOR_1D = new CombinedInterpolatorExtrapolator(INTERPOLATOR_1D, new FlatExtrapolator1D());
  private static double DRIFT = 0.05;
  private static final double[] EXPIRIES = new double[] {0.0, 7. / 365, 14 / 365., 21 / 365., 1 / 12., 3 / 12., 0.5, 0.75, 1, 5, 10 };
  private static final double SPOT = 1.34;
  private static final double[] FORWARDS;
  private static final ForwardCurve FORWARD_CURVE;

  static {
    int n = EXPIRIES.length;
    FORWARDS = new double[n];
    for (int i = 0; i < n; i++) {
      FORWARDS[i] = SPOT * Math.exp(DRIFT * EXPIRIES[i]);
    }
    FORWARD_CURVE = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, EXTRAPOLATOR_1D));
  }

  @Test
  public void testShift() {
    final double shift = 0.1;
    ForwardCurve shifedCurve = FORWARD_CURVE.withShiftedSpot(shift);
    assertEquals(SPOT + shift, shifedCurve.getSpot(), 0.0);

    for (int i = 0; i < EXPIRIES.length - 1; i++) {
      double t = EXPIRIES[i];
      assertEquals("time " + i, (SPOT + shift) * Math.exp(DRIFT * t), shifedCurve.getForward(t), 1e-9);
    }

    Function1D<Double, Double> func = shifedCurve.getForwardCurve().toFunction1D();
    for (int i = 0; i < EXPIRIES.length - 1; i++) {
      double t = EXPIRIES[i];
      assertEquals("time " + i, (SPOT + shift) * Math.exp(DRIFT * t), func.evaluate(t), 1e-9);
    }
  }
}
