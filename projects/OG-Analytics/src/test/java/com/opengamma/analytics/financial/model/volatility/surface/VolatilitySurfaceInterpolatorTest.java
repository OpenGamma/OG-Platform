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

/**
 * 
 */
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

  private static final String[] TENORS = new String[] {"1W", "2W", "3W", "1M", "3M", "6M", "9M", "1Y", "5Y", "10Y" };
  private static final double[] DELTAS = new double[] {0.15, 0.25 };
  private static final double[] FORWARDS = new double[] {1.34, 1.35, 1.36, 1.38, 1.4, 1.43, 1.45, 1.48, 1.5, 1.52 };
  private static final double[] EXPIRIES = new double[] {7. / 365, 14 / 365., 21 / 365., 1 / 12., 3 / 12., 0.5, 0.75, 1, 5, 10 };
  private static final double[] ATM = new double[] {0.17045, 0.1688, 0.167425, 0.1697, 0.1641, 0.1642, 0.1641, 0.1642, 0.138, 0.12515 };
  private static final double[][] RR = new double[][] { {-0.0168, -0.02935, -0.039125, -0.047325, -0.058325, -0.06055, -0.0621, -0.063, -0.032775, -0.023925 },
      {-0.012025, -0.02015, -0.026, -0.0314, -0.0377, -0.03905, -0.0396, -0.0402, -0.02085, -0.015175 } };
  private static final double[][] BUTT = new double[][] { {0.00665, 0.00725, 0.00835, 0.009075, 0.013175, 0.01505, 0.01565, 0.0163, 0.009275, 0.007075, },
      {0.002725, 0.00335, 0.0038, 0.004, 0.0056, 0.0061, 0.00615, 0.00635, 0.00385, 0.002575 } };
  private static final double FLAT_VOL = 0.11;
  private static final SmileSurfaceDataBundle MARKET_DATA = new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, ATM, RR, BUTT, true, EXTRAPOLATOR_1D);
  private static final SmileSurfaceDataBundle FLAT_MARKET_DATA;
  private static final int N = EXPIRIES.length;

  static {
    double[] atm = new double[N];
    Arrays.fill(atm, FLAT_VOL);
    double[][] rr = new double[2][N];
    double[][] butt = new double[2][N];
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
    BlackVolatilitySurfaceMoneyness blackVol = SURFACE_INTERPOLATOR.getVolatilitySurface(FLAT_MARKET_DATA);
    double tStart = Math.log(0.01);
    double tEnd = Math.log(10.0);
    double xL = 0.3;
    double xH = 3.5;
    for (int i = 0; i < 20; i++) {
      double t = Math.exp(tStart + (tEnd - tStart) * i / 19.);
      for (int j = 0; j < 20; j++) {
        double x = xL + (xH - xL) * j / 19.;
        double vol = blackVol.getVolatilityForMoneyness(t, x);
        assertEquals("time: " + t + " moneyness: " + x, FLAT_VOL, vol, 1e-7);
      }
    }
  }
}
