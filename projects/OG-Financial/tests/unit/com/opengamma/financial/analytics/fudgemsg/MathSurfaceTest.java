/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.ForexSmileDeltaSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public class MathSurfaceTest extends AnalyticsTestBase {

  @SuppressWarnings("unchecked")
  @Test
  public void testConstantSurface() {
    Surface<Double, Double, Double> s1 = ConstantDoublesSurface.from(4.);
    Surface<Double, Double, Double> s2 = cycleObject(Surface.class, s1);
    assertEquals(s1, s2);
    s1 = ConstantDoublesSurface.from(4., "NAME");
    s2 = cycleObject(Surface.class, s1);
    assertEquals(s1, s2);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testInterpolatedSurface() {
    final LinearInterpolator1D linear = new LinearInterpolator1D();
    final GridInterpolator2D interpolator = new GridInterpolator2D(linear, linear);
    Surface<Double, Double, Double> s1 = InterpolatedDoublesSurface.from(new double[] {1, 2, 3, 4 }, new double[] {4, 5, 6, 7 }, new double[] {8, 9, 10, 11 }, interpolator);
    Surface<Double, Double, Double> s2 = cycleObject(Surface.class, s1);
    assertEquals(s1, s2);
    s1 = InterpolatedDoublesSurface.from(new double[] {1, 2, 3, 4 }, new double[] {4, 5, 6, 7 }, new double[] {8, 9, 10, 11 }, interpolator, "NAME");
    s2 = cycleObject(Surface.class, s1);
    assertEquals(s1, s2);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testFunctionalSurfaceUnserialized() {
    final Function<Double, Double> f = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        return 9.;
      }

    };
    final Surface<Double, Double, Double> s = FunctionalDoublesSurface.from(f);
    cycleObject(Surface.class, s);
  }

  @Test
  public void testFunctionalSurface1() {
    final FunctionalDoublesSurface s1 = FunctionalDoublesSurface.from(new TestStatelessFunction().getSurface(3., 4.));
    @SuppressWarnings("unchecked")
    final Surface<Double, Double, Double> s2 = cycleObject(Surface.class, s1);
    assertSurfaceEquals(s1, s2);
  }

  @Test
  public void testFunctionalSurface2() {
    final FunctionalDoublesSurface s1 = FunctionalDoublesSurface.from(new TestStatefulFunction(true, 3).getSurface(5., 6.));
    @SuppressWarnings("unchecked")
    final Surface<Double, Double, Double> s2 = cycleObject(Surface.class, s1);
    assertSurfaceEquals(s1, s2);
  }

  @Test
  public void testFunctionalSurface3() {
    final double[] expiries = new double[] {7. / 365, 14 / 365., 21 / 365., 1 / 12., 3 / 12., 0.5, 0.75, 1, 5, 10 };
    final double[] atm = new double[] {0.17045, 0.1688, 0.167425, 0.1697, 0.1641, 0.1642, 0.1641, 0.1642, 0.138, 0.12515 };
    final double[][] rr = new double[][] { {-0.0168, -0.02935, -0.039125, -0.047325, -0.058325, -0.06055, -0.0621, -0.063, -0.032775, -0.023925 },
        {-0.012025, -0.02015, -0.026, -0.0314, -0.0377, -0.03905, -0.0396, -0.0402, -0.02085, -0.015175 } };
    final double[][] butterfly = new double[][] { {0.00665, 0.00725, 0.00835, 0.009075, 0.013175, 0.01505, 0.01565, 0.0163, 0.009275, 0.007075, },
        {0.002725, 0.00335, 0.0038, 0.004, 0.0056, 0.0061, 0.00615, 0.00635, 0.00385, 0.002575 } };
    final double[] deltas = new double[] {0.15, 0.25 };
    final double[] forwards = new double[] {1.34, 1.35, 1.36, 1.38, 1.4, 1.43, 1.45, 1.48, 1.5, 1.52 };
    final CombinedInterpolatorExtrapolator extrapolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final ForexSmileDeltaSurfaceDataBundle data = new ForexSmileDeltaSurfaceDataBundle(forwards, expiries, deltas, atm, rr, butterfly, true, extrapolator);
    final BlackVolatilitySurface<?> s1 = new VolatilitySurfaceInterpolator(true, true, true).getVolatilitySurface(data);
    @SuppressWarnings("unchecked")
    final Surface<Double, Double, Double> s2 = cycleObject(Surface.class, s1.getSurface());
    assertSurfaceEquals(s1.getSurface(), s2);
  }

  private void assertSurfaceEquals(final Surface<Double, Double, Double> s1, final Surface<Double, Double, Double> s2) {
    if (s1 != s2) {
      for (double x = 0.1; x < 100.0; x += 5.00000001) {
        for (double y = 0.11; y < 100; y += 5.00000001) {
          assertEquals(s1.getZValue(x, y), s2.getZValue(x, y));
        }
      }
    }
  }
}
