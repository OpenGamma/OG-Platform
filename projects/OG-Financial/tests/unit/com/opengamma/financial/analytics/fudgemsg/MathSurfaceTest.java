/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.MoneynessPiecewiseSABRSurfaceFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.math.function.Function;
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
    Surface<Double, Double, Double> s1 = InterpolatedDoublesSurface.from(new double[] {1, 2, 3, 4}, new double[] {4, 5, 6, 7}, new double[] {8, 9, 10, 11}, interpolator);
    Surface<Double, Double, Double> s2 = cycleObject(Surface.class, s1);
    assertEquals(s1, s2);
    s1 = InterpolatedDoublesSurface.from(new double[] {1, 2, 3, 4}, new double[] {4, 5, 6, 7}, new double[] {8, 9, 10, 11}, interpolator, "NAME");
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
    final StandardSmileSurfaceDataBundle data = new StandardSmileSurfaceDataBundle(new double[] {1, 2, 3, 4}, new double[] {4, 5, 6, 7},
        new double[][] {new double[] {1, 2, 3, 4}, new double[] {1, 2, 3, 4}, new double[] {1, 2, 3, 4}, new double[] {1, 2, 3, 4}},
        new double[][] {new double[] {0.1, 0.1, 0.1, 0.1}, new double[] {0.1, 0.1, 0.1, 0.1}, new double[] {0.1, 0.1, 0.1, 0.1}, new double[] {0.1, 0.1, 0.1, 0.1}}, true,
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR));
    final BlackVolatilitySurfaceMoneyness s1 = new MoneynessPiecewiseSABRSurfaceFitter(true, true, true).getVolatilitySurface(data);
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
