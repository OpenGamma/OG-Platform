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
public class CosineWeightingFunctionTest extends WeightingFunctionTestCase {

  @Override
  protected CosineWeightingFunction getInstance() {
    return WeightingFunctionFactory.COSINE_WEIGHTING_FUNCTION;
  }

  @Test
  public void testWeighting() {
    assertEquals(1.0, getInstance().getWeight(STRIKES, STRIKES[3]), EPS);
    assertEquals(1.0, getInstance().getWeight(STRIKES, INDEX, STRIKES[3]), EPS);
    assertEquals(0.0, getInstance().getWeight(STRIKES, STRIKES[4] - EPS), 100 * EPS);
    //    assertEquals(0.5, getInstance().getWeight(STRIKES, 0.5 * (STRIKES[3] + STRIKES[4])), 10 * EPS);
  }
}
