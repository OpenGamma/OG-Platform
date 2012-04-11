/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import org.testng.annotations.Test;

/**
 * 
 */
public class SmileInterpolatorSABRTest extends SmileInterpolatorTestCase {

  private static final double BETA = 0.75;
  private static final double FORWARD = 1172.011012;
  private static final double EXPIRY = 1.5;
  private static final double[] STRIKES = new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1854.305534 };
  private static final double[] VOLS = new double[] {0.311, 0.288, 0.267, 0.271, 0.276 };

  private static final GeneralSmileInterpolator INTERPOLATOR = new SmileInterpolatorSABR();

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

}
