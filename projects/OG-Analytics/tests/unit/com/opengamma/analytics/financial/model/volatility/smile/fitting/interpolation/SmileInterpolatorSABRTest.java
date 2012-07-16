/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 */
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
    INTERPOLATOR.getVolatilityFunction(FORWARD, null, EXPIRY, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullImpliedVols() {
    INTERPOLATOR.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrikeLength() {
    INTERPOLATOR.getVolatilityFunction(FORWARD, new double[] {1000, 1100 }, EXPIRY, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength() {
    INTERPOLATOR.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, new double[] {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDecreasingStrikes() {
    INTERPOLATOR.getVolatilityFunction(FORWARD, new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1500 }, EXPIRY, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEqualStrikes() {
    INTERPOLATOR.getVolatilityFunction(FORWARD, new double[] {782.9777301, 982.3904005, 1547.184937, 1547.184937, 1854.305534 }, EXPIRY, VOLS);
  }

  //failing numbers
  @Test(enabled = false)
  public void debugTest() {
    double f = 1.0;
    double t = 1. / 52;
    double[] k = new double[] {0.74056752, 0.824319729, 0.908071938, 0.991824147, 1.075576356, 1.159328565, 1.243080775 };
    double[] vols = new double[] {1.818267184, 1.464232108, 1.137345745, 0.778552231, 0.399669545, 0.318172862, 0.420392212 };

    Function1D<Double, Double> func = INTERPOLATOR.getVolatilityFunction(f, k, t, vols);
    final GeneralSmileInterpolator inter = new SmileInterpolatorSpline();
    Function1D<Double, Double> func2 = inter.getVolatilityFunction(f, k, t, vols);
    for (int i = 0; i < 101; i++) {
      double x = 0.7 + 0.6 * i / 100.;
      System.out.println(x + "\t" + func.evaluate(x) + "\t" + func2.evaluate(x));
    }
    System.out.println();

    //    final int n = k.length;
    //    for (int i = 0; i < n; i++) {
    //      System.out.println(k[i] + "\t" + vols[i] + "\t" + func.evaluate(k[i]));
    //    }

    List<SABRFormulaData> parms = ((SmileInterpolatorSABR) INTERPOLATOR).getFittedModelParameters(f, k, t, vols);
    int m = parms.size();
    for (int i = 0; i < m; i++) {
      System.out.println(parms.get(i));
    }

  }

}
