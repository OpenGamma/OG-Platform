/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * 
 */
public class CosineWeightingFunctionTest extends WeightingFunctionTestCase {

  @Override
  protected CosineWeightingFunction getInstance() {
    return CosineWeightingFunction.getInstance();
  }

  @Test
  public void testWeighting() {
    final double x = Math.cos(Math.PI * 0.225);
    assertEquals(x * x, getInstance().getWeight(STRIKES, STRIKE), EPS);
    assertEquals(x * x, getInstance().getWeight(STRIKES, INDEX, STRIKE), EPS);
    assertEquals(1, getInstance().getWeight(STRIKES, STRIKES[3]), EPS);
    assertEquals(1, getInstance().getWeight(STRIKES, INDEX, STRIKES[3]), EPS);
  }
}
