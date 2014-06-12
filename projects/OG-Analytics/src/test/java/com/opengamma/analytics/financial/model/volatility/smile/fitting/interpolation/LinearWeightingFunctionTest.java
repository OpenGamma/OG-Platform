/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class LinearWeightingFunctionTest extends WeightingFunctionTestCase {

  @Override
  protected LinearWeightingFunction getInstance() {
    return WeightingFunctionFactory.LINEAR_WEIGHTING_FUNCTION;
  }

  @Test
  public void testWeighting() {
    assertEquals(0.55, getInstance().getWeight(STRIKES, STRIKE), EPS);
    assertEquals(0.55, getInstance().getWeight(STRIKES, INDEX, STRIKE), EPS);
    assertEquals(1, getInstance().getWeight(STRIKES, STRIKES[3]), EPS);
    assertEquals(1, getInstance().getWeight(STRIKES, INDEX, STRIKES[3]), EPS);
  }
}
