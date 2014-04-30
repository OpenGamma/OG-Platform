/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveYieldImplied;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.PCHIPYieldCurveInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * This relates to [PLAT-4316]
 */
@Test(groups = TestGroup.UNIT, enabled = false)
public class FailingSABRInterpolatorTest {

  private final static double[] datearray = {9.0 / 365, 44.0 / 365, 72.0 / 365, 135.0 / 365, 226.0 / 365, 317.0 / 365, 499.0 / 365, 681.0 / 365, 863.0 / 365, 1045.0 / 365, 1227. / 365 };

  private final static double[][] strikematrix = { {2550, 2650, 2700, 2725, 2750, 2775, 2800, 2825, 2850, 2900, 3000 },
    {2550, 2600, 2650, 2675, 2700, 2725, 2750, 2775, 2800, 2825, 2850, 2900, 3000 }, {2550, 2600, 2650, 2675, 2700, 2725, 2750, 2775, 2800, 2825, 2850, 2900, 3000 },
    {2550, 2600, 2650, 2675, 2700, 2725, 2750, 2775, 2800, 2825, 2850, 2900, 3000 }, {2550, 2600, 2650, 2700, 2750, 2800, 2850, 2900, 3000 }, {2550, 2600, 2650, 2700, 2750, 2800, 2850, 2900, 3000 },
    {2550, 2600, 2650, 2700, 2750, 2800, 2850, 2900, 3000 }, {2550, 2600, 2650, 2700, 2750, 2800, 2850, 2900, 3000 }, {2550, 2600, 2650, 2700, 2750, 2800, 2850, 2900, 3000 },
    {2550, 2600, 2650, 2700, 2750, 2800, 2850, 2900, 3000 }, {2550, 2600, 2650, 2700, 2750, 2800, 2900, 3000 } };

  private final static double[][] volsmatrix = { {0.54257, 0.4121, 0.1627, 0.2012, 0.1439, 0.1302, 0.1354, 0.1292, 0.1308, 0.13788, 0.17734 },
    {0.2912, 0.20091, 0.2445, 0.2283, 0.1559, 0.2078, 0.1518, 0.1474, 0.1512, 0.1412, 0.1445, 0.13692, 0.13659 },
    {0.2623, 0.24416, 0.2274, 0.2198, 0.2127, 0.2052, 0.1920, 0.1921, 0.1768, 0.1825, 0.1607, 0.15436, 0.15298 },
    {0.23854, 0.228, 0.1880, 0.2133, 0.2063, 0.2042, 0.1796, 0.1958, 0.1707, 0.1886, 0.1692, 0.1644, 0.15784 }, {0.23308, 0.22634, 0.2184, 0.2116, 0.2051, 0.1982, 0.1932, 0.19005, 0.17623 },
    {0.23166, 0.22543, 0.2195, 0.2140, 0.2114, 0.2040, 0.1989, 0.19461, 0.18698 }, {0.23650, 0.23183, 0.2273, 0.2231, 0.2191, 0.2153, 0.2117, 0.20836, 0.20182 },
    {0.23893, 0.23485, 0.2309, 0.2271, 0.2235, 0.2201, 0.2169, 0.21382, 0.20825 }, {0.24215, 0.23877, 0.2353, 0.2321, 0.2291, 0.2263, 0.2234, 0.22084, 0.21582 },
    {0.24631, 0.24288, 0.2396, 0.2366, 0.2336, 0.2309, 0.2282, 0.22565, 0.22086 }, {0.24997, 0.24663, 0.2435, 0.2405, 0.2377, 0.2321, 0.22983, 0.22535 } };

  private final static double[] nodepoints1 = {1. / 365, 1.0 / 12, 3.0 / 12, 6.0 / 12, 9.0 / 12, 1, 18.0 / 12, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30 };
  private final static double[] yields1 = {0.0007, 0.002647, 0.003185, 0.00342, 0.00414, 0.00501, 0.006465, 0.005805, 0.007665, 0.00997, 0.01227, 0.014275, 0.016050, 0.01763, 0.01906, 0.02034,
    0.02239, 0.02437, 0.025445, 0.02557, 0.025477 };

  private final static GeneralSmileInterpolator DEFAULT_SMILE_INTERPOLATOR = new SmileInterpolatorSABR(0.5);
  private final static Interpolator1D PCHIP = new PCHIPYieldCurveInterpolator1D();
  private final static DoublesCurve intercurve = InterpolatedDoublesCurve.from(nodepoints1, yields1, PCHIP);
  private final static YieldCurve TestKurve2 = new YieldCurve("testkurve", intercurve);
  private final static ConstantDoublesCurve div = new ConstantDoublesCurve(0.0383);
  private final static YieldCurve div1 = new YieldCurve("test", div);
  private final static ForwardCurve fwd = new ForwardCurveYieldImplied(2791.0, TestKurve2, div1);

  public static void SABRSurface() {

    final SmileSurfaceDataBundle surfdatabundle = new StandardSmileSurfaceDataBundle(fwd, datearray, strikematrix, volsmatrix);

    final VolatilitySurfaceInterpolator surf = new VolatilitySurfaceInterpolator(DEFAULT_SMILE_INTERPOLATOR);
    final BlackVolatilitySurfaceMoneynessFcnBackedByGrid surf1 = surf.getVolatilitySurface(surfdatabundle);

    for (int i = 0; i < 11; i++) {
      System.out.println(fwd.getForward(datearray[i]));
      for (int j = 0; j < 13; j++) {
        System.out.println("Expiry = " + datearray[i] + ", Strike = " + strikematrix[2][j] + ", " + "Vol = " + surf1.getVolatility(datearray[i], strikematrix[2][j]));
      }
    }
  }

  //TODO get this working
  @Test(enabled = false)
  public void test() {

    SABRSurface();
  }

}
