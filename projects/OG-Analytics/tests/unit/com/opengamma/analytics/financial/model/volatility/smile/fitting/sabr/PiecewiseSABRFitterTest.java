/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.LinearWeightingFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 */
public class PiecewiseSABRFitterTest {
  private static final double BETA = 0.75;
  private static final double FORWARD = 1172.011012;
  private static final double EXPIRY = 1.5;
  private static final double[] STRIKES = new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1854.305534 };
  private static final double[] VOLS = new double[] {0.311, 0.288, 0.267, 0.271, 0.276 };
  private static final PiecewiseSABRFitter FITTER = new PiecewiseSABRFitter();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowBeta() {
    new PiecewiseSABRFitter(-1, LinearWeightingFunction.getInstance());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighBeta() {
    new PiecewiseSABRFitter(1 + 1e-15, LinearWeightingFunction.getInstance());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullWeightingFunction() {
    new PiecewiseSABRFitter(BETA, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrikes() {
    FITTER.getVolatilityFunction(FORWARD, null, EXPIRY, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullImpliedVols() {
    FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrikeLength() {
    FITTER.getVolatilityFunction(FORWARD, new double[] {1000, 1100 }, EXPIRY, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength() {
    FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, new double[] {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDecreasingStrikes() {
    FITTER.getVolatilityFunction(FORWARD, new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1500 }, EXPIRY, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEqualStrikes() {
    FITTER.getVolatilityFunction(FORWARD, new double[] {782.9777301, 982.3904005, 1547.184937, 1547.184937, 1854.305534 }, EXPIRY, VOLS);
  }

  @Test
      (enabled = false)
      public void test() {

    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, VOLS);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test
      (enabled = false)
      public void bumpTest() {
    double bump = 1e-2;
    int index = 1;
    double[] vols = Arrays.copyOf(VOLS, VOLS.length);
    vols[index] += bump;

    SABRFormulaData[] parms = FITTER.getFittedModelParameters(FORWARD, STRIKES, EXPIRY, vols);
    for (int i = 0; i < parms.length; i++) {
      System.out.println(parms[i].toString());
    }
    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test
  public void FlatTest() {
    final int n = STRIKES.length;
    final double[] vols = new double[n];
    Arrays.fill(vols, 0.2);

    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      assertEquals(0.2, vol, 1e-8);
    }
  }

  @Test(enabled = false)
  public void FlatBumpTest() {
    final int n = STRIKES.length;
    final double[] vols = new double[n];
    Arrays.fill(vols, 0.2);
    double bump = 1e-3;
    int index = 2;
    vols[index] += bump;

    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test(enabled = false)
  public void badFitTest() {
    final double forward = 1.30276013603506;
    final double[] strikes = new double[] {1.080256504787705, 1.161299691076151, 1.329077636516407, 1.5210230159922162, 1.635211041136184 };
    final double expiry = 1.0;
    final double[] impVols = new double[] {0.2, 0.2, 0.2, 0.2, 0.2 };
    double bump = 1e-3;
    int index = 2;
    impVols[index] += bump;
    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(forward, strikes, expiry, impVols);

    for (int i = 0; i < 200; i++) {
      final double k = 0.8 + 1.2 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

}
