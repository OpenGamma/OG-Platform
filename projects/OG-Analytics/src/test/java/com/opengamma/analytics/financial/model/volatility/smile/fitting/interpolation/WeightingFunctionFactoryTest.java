/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;
import static com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunctionFactory.getWeightingFunction;
import static com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunctionFactory.getWeightingFunctionName;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class WeightingFunctionFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadName() {
    getWeightingFunction("Random");
  }

  @Test
  public void test() {
    assertEquals(WeightingFunctionFactory.COSINE_WEIGHTING_FUNCTION_NAME, getWeightingFunctionName(getWeightingFunction(WeightingFunctionFactory.COSINE_WEIGHTING_FUNCTION_NAME)));
    assertEquals(WeightingFunctionFactory.LINEAR_WEIGHTING_FUNCTION_NAME, getWeightingFunctionName(getWeightingFunction(WeightingFunctionFactory.LINEAR_WEIGHTING_FUNCTION_NAME)));
    assertEquals(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME, getWeightingFunctionName(getWeightingFunction(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME)));
  }
}
