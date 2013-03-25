/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.ForexSmileDeltaSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FittedVolatilitySurfaceBuilderTest extends AnalyticsTestBase {

  private static final double[] EXPIRIES = new double[] {7. / 365, 14 / 365., 21 / 365., 1 / 12., 3 / 12., 0.5, 0.75, 1, 5, 10 };
  private static final double[] ATM = new double[] {0.17045, 0.1688, 0.167425, 0.1697, 0.1641, 0.1642, 0.1641, 0.1642, 0.138, 0.12515 };
  private static final double[][] RR = new double[][] { {-0.0168, -0.02935, -0.039125, -0.047325, -0.058325, -0.06055, -0.0621, -0.063, -0.032775, -0.023925 },
    {-0.012025, -0.02015, -0.026, -0.0314, -0.0377, -0.03905, -0.0396, -0.0402, -0.02085, -0.015175 } };
  private static final double[][] BUTT = new double[][] { {0.00665, 0.00725, 0.00835, 0.009075, 0.013175, 0.01505, 0.01565, 0.0163, 0.009275, 0.007075, },
    {0.002725, 0.00335, 0.0038, 0.004, 0.0056, 0.0061, 0.00615, 0.00635, 0.00385, 0.002575 } };
  private static final double[] DELTAS = new double[] {0.15, 0.25 };
  private static final double[] FORWARDS = new double[] {1.34, 1.35, 1.36, 1.38, 1.4, 1.43, 1.45, 1.48, 1.5, 1.52 };
  private static final CombinedInterpolatorExtrapolator EXTRAPOLATOR_1D =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final ForexSmileDeltaSurfaceDataBundle FOREX_DATA = new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, ATM, RR, BUTT, true, EXTRAPOLATOR_1D);
  private static final StandardSmileSurfaceDataBundle STANDARD_DATA = new StandardSmileSurfaceDataBundle(FOREX_DATA.getForwardCurve(), FOREX_DATA.getExpiries(), FOREX_DATA.getStrikes(),
      FOREX_DATA.getVolatilities());

  @Test
  public void testStandardData() {
    final StandardSmileSurfaceDataBundle data = cycleObject(StandardSmileSurfaceDataBundle.class, STANDARD_DATA);
    assertArrayEquals(STANDARD_DATA.getExpiries(), data.getExpiries(), 0);
    assertTrue(Arrays.deepEquals(STANDARD_DATA.getStrikes(), data.getStrikes()));
    assertTrue(Arrays.deepEquals(STANDARD_DATA.getVolatilities(), data.getVolatilities()));
    assertCurveEquals(STANDARD_DATA.getForwardCurve().getForwardCurve(), data.getForwardCurve().getForwardCurve());
    assertCurveEquals(STANDARD_DATA.getForwardCurve().getDriftCurve(), data.getForwardCurve().getDriftCurve());
    assertEquals(STANDARD_DATA.getForwardCurve().getSpot(), data.getForwardCurve().getSpot(), 1e-12);
  }

  @Test
  public void testForexData() {
    final ForexSmileDeltaSurfaceDataBundle data = cycleObject(ForexSmileDeltaSurfaceDataBundle.class, FOREX_DATA);
    assertArrayEquals(FOREX_DATA.getExpiries(), data.getExpiries(), 0);
    assertTrue(Arrays.deepEquals(FOREX_DATA.getStrikes(), data.getStrikes()));
    assertTrue(Arrays.deepEquals(FOREX_DATA.getVolatilities(), data.getVolatilities()));
    assertCurveEquals(FOREX_DATA.getForwardCurve().getForwardCurve(), data.getForwardCurve().getForwardCurve());
    assertCurveEquals(FOREX_DATA.getForwardCurve().getDriftCurve(), data.getForwardCurve().getDriftCurve());
    assertEquals(FOREX_DATA.getForwardCurve().getSpot(), data.getForwardCurve().getSpot(), 1e-12);
  }

  private void assertCurveEquals(final Curve<Double, Double> c1, final Curve<Double, Double> c2) {
    if (c1 != c2) {
      for (double x = 0.1; x < 100.0; x += 5.00000001) {
        assertEquals(c1.getYValue(x), c2.getYValue(x));
      }
    }
  }
}
