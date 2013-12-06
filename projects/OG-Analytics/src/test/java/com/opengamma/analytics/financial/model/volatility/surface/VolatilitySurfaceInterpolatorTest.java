/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSpline;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.ForexSmileDeltaSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.LinearExtrapolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceInterpolatorTest {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final CombinedInterpolatorExtrapolator EXTRAPOLATOR_1D = new CombinedInterpolatorExtrapolator(INTERPOLATOR_1D, new LinearExtrapolator1D(INTERPOLATOR_1D));
  private static final GeneralSmileInterpolator SMILE_INTERPOLATOR = new SmileInterpolatorSpline();
  private static final Interpolator1D TIME_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final boolean USE_LOG_TIME = true;
  private static final boolean USE_INTEGRATED_VARIANCE = true;
  private static final boolean USE_LOG_VALUE = true;
  private static final VolatilitySurfaceInterpolator SURFACE_INTERPOLATOR = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, TIME_INTERPOLATOR, USE_LOG_TIME,
      USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);

  private static final double[] DELTAS = new double[] {0.15, 0.25 };
  private static final double[] FORWARDS = new double[] {1.34, 1.35, 1.36, 1.38, 1.4, 1.43, 1.45, 1.48, 1.5, 1.52 };
  private static final double[] EXPIRIES = new double[] {7. / 365, 14 / 365., 21 / 365., 1 / 12., 3 / 12., 0.5, 0.75, 1, 5, 10 };
  private static final double FLAT_VOL = 0.11;
  private static final SmileSurfaceDataBundle FLAT_MARKET_DATA;
  private static final int N = EXPIRIES.length;

  static {
    final double[] atm = new double[N];
    Arrays.fill(atm, FLAT_VOL);
    final double[][] rr = new double[2][N];
    final double[][] butt = new double[2][N];
    FLAT_MARKET_DATA = new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, atm, rr, butt, true, EXTRAPOLATOR_1D);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSmileInterpolator() {
    new VolatilitySurfaceInterpolator(null, TIME_INTERPOLATOR, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimeInterpolator() {
    new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, null, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData1() {
    SURFACE_INTERPOLATOR.getIndependentSmileFits(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData2() {
    SURFACE_INTERPOLATOR.getBumpedVolatilitySurface(null, 0, 0, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData3() {
    SURFACE_INTERPOLATOR.getVolatilitySurface(null);
  }

  @Test
  public void testGetters() {
    assertEquals(SURFACE_INTERPOLATOR.useIntegratedVariance(), USE_INTEGRATED_VARIANCE);
    assertEquals(SURFACE_INTERPOLATOR.useLogTime(), USE_LOG_TIME);
    assertEquals(SURFACE_INTERPOLATOR.useLogValue(), USE_LOG_VALUE);
    assertEquals(SURFACE_INTERPOLATOR.getSmileInterpolator(), SMILE_INTERPOLATOR);
    assertEquals(SURFACE_INTERPOLATOR.getTimeInterpolator(), TIME_INTERPOLATOR);
  }

  @Test
  public void testHashCodeEquals() {
    VolatilitySurfaceInterpolator other = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, TIME_INTERPOLATOR, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
    assertEquals(SURFACE_INTERPOLATOR, other);
    assertEquals(SURFACE_INTERPOLATOR.hashCode(), other.hashCode());
    other = new VolatilitySurfaceInterpolator(new SmileInterpolatorSABR(), TIME_INTERPOLATOR, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
    assertFalse(other.equals(SURFACE_INTERPOLATOR));
    other = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, Interpolator1DFactory.LINEAR_INSTANCE, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
    assertFalse(other.equals(SURFACE_INTERPOLATOR));
    other = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, Interpolator1DFactory.LINEAR_INSTANCE, !USE_LOG_TIME, USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
    assertFalse(other.equals(SURFACE_INTERPOLATOR));
    other = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, Interpolator1DFactory.LINEAR_INSTANCE, USE_LOG_TIME, !USE_INTEGRATED_VARIANCE, USE_LOG_VALUE);
    assertFalse(other.equals(SURFACE_INTERPOLATOR));
    other = new VolatilitySurfaceInterpolator(SMILE_INTERPOLATOR, Interpolator1DFactory.LINEAR_INSTANCE, USE_LOG_TIME, USE_INTEGRATED_VARIANCE, !USE_LOG_VALUE);
    assertFalse(other.equals(SURFACE_INTERPOLATOR));
  }

  @Test
  public void testFlatSurface() {
    final BlackVolatilitySurfaceMoneyness blackVol = SURFACE_INTERPOLATOR.getVolatilitySurface(FLAT_MARKET_DATA);
    final double tStart = Math.log(0.01);
    final double tEnd = Math.log(10.0);
    final double xL = 0.3;
    final double xH = 3.5;
    for (int i = 0; i < 20; i++) {
      final double t = Math.exp(tStart + (tEnd - tStart) * i / 19.);
      for (int j = 0; j < 20; j++) {
        final double x = xL + (xH - xL) * j / 19.;
        final double vol = blackVol.getVolatilityForMoneyness(t, x);
        assertEquals("time: " + t + " moneyness: " + x, FLAT_VOL, vol, 1e-7);
      }
    }
  }
}
