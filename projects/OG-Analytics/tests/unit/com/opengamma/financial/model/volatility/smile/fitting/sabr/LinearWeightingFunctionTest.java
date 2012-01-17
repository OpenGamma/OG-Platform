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
public class LinearWeightingFunctionTest extends WeightingFunctionTestCase {

  @Override
  protected LinearWeightingFunction getInstance() {
    return LinearWeightingFunction.getInstance();
  }

  @Test
  public void testWeighting1() {
    assertEquals(0.55, getInstance().getWeight(STRIKES, STRIKE), EPS);
  }

  @Test
  public void testWeighting2() {
    assertEquals(0.55, getInstance().getWeight(STRIKES, INDEX, STRIKE), EPS);
  }
}
