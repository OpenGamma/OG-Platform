/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.PSplineFitter;
import com.opengamma.analytics.math.statistics.leastsquare.GeneralizedLeastSquareResults;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SmileInterpolatorSABRTest extends SmileInterpolatorTestCase {

  private static final double BETA = 0.75;

  private static final GeneralSmileInterpolator INTERPOLATOR = new SmileInterpolatorSABR();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new SmileInterpolatorSABR(null, BETA, WeightingFunctionFactory.LINEAR_WEIGHTING_FUNCTION);
  }

  @Override
  public GeneralSmileInterpolator getSmileInterpolator() {
    return INTERPOLATOR;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowBeta() {
    new SmileInterpolatorSABR(-1.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighBeta() {
    new SmileInterpolatorSABR(1 + 1e-15);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeightingFunction() {
    new SmileInterpolatorSABR(BETA, null);
  }

  @Override
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrikes() {
    INTERPOLATOR.getVolatilityFunction(getForward(), null, getExpiry(), getVols());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullImpliedVols() {
    INTERPOLATOR.getVolatilityFunction(getForward(), getStrikes(), getExpiry(), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrikeLength() {
    INTERPOLATOR.getVolatilityFunction(getForward(), new double[] {1000, 1100 }, getExpiry(), getVols());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength() {
    INTERPOLATOR.getVolatilityFunction(getForward(), getStrikes(), getExpiry(), new double[] {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDecreasingStrikes() {
    INTERPOLATOR.getVolatilityFunction(getForward(), new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1500 }, getExpiry(), getVols());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEqualStrikes() {
    INTERPOLATOR.getVolatilityFunction(getForward(), new double[] {782.9777301, 982.3904005, 1547.184937, 1547.184937, 1854.305534 }, getExpiry(), getVols());
  }

  //failing numbers
  @Test(enabled = false)
  public void debugTest() {
    final double f = 1.0;
    final double t = 0.038461538;
    final double[] k = new double[] {0.739440845, 0.823080386, 0.906719928, 0.99035947, 1.073999011, 1.157638553, 1.241278094 };
    final double[] vols = new double[] {0.540271125, 0.478068011, 0.388601315, 0.308990013, 0.394024968, 0.543202368, 0.656213382 };

    final Function1D<Double, Double> func = INTERPOLATOR.getVolatilityFunction(f, k, t, vols);
    final GeneralSmileInterpolator inter = new SmileInterpolatorSpline();
    final Function1D<Double, Double> func2 = inter.getVolatilityFunction(f, k, t, vols);

    final int n = k.length;
    final List<Double> x = new ArrayList<>(n);
    final List<Double> y = new ArrayList<>(n);
    final List<Double> sigma = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      x.add(k[i]);
      y.add(vols[i]);
      sigma.add(1e-5);
    }
    final PSplineFitter pfitter = new PSplineFitter();
    final GeneralizedLeastSquareResults<Double> temp = pfitter.solve(x, y, sigma, 0.5, 1.7, 50, 3, 100000000, 2);
    final Function1D<Double, Double> func3 = temp.getFunction();

    for (int i = 0; i < 101; i++) {
      final double strike = 0.5 + 1.0 * i / 100.;
      System.out.println(strike + "\t" + func.evaluate(strike) + "\t" + func2.evaluate(strike) + "\t" + func3.evaluate(strike));
    }
    System.out.println();

    //    final int n = k.length;
    //    for (int i = 0; i < n; i++) {
    //      System.out.println(k[i] + "\t" + vols[i] + "\t" + func.evaluate(k[i]));
    //    }

    final List<SABRFormulaData> parms = ((SmileInterpolatorSABR) INTERPOLATOR).getFittedModelParameters(f, k, t, vols);
    final int m = parms.size();
    for (int i = 0; i < m; i++) {
      System.out.println(parms.get(i));
    }

  }

}
