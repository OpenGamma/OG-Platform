/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import org.testng.annotations.Test;

import com.opengamma.financial.model.volatility.smile.fitting.interpolation.LinearWeightingFunction;


/**
 * 
 */
public class PiecewiseSABRFitter1Test {
  private static final double BETA = 0.75;
  private static final PiecewiseSABRFitter FITTER = new PiecewiseSABRFitter();
  private static final double FORWARD = 1172.011012;
  private static final double EXPIRY = 1.5;
  private static final double[] STRIKES = new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1854.305534 };
  private static final double[] VOLS = new double[] {0.311, 0.288, 0.267, 0.271, 0.276 };

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
    FITTER.getVolatilityFunction(FORWARD, new double[]{1000, 1100}, EXPIRY, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength() {
    FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, new double[] {0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDecreasingStrikes() {
    FITTER.getVolatilityFunction(FORWARD, new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1500}, EXPIRY, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEqualStrikes() {
    FITTER.getVolatilityFunction(FORWARD, new double[] {782.9777301, 982.3904005, 1547.184937, 1547.184937, 1854.305534 }, EXPIRY, VOLS);
  }
}
