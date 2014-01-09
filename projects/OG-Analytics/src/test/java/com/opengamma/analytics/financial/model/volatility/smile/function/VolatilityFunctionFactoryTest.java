/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilityFunctionFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongName() {
    VolatilityFunctionFactory.getCalculator("Other");
  }

  @Test
  public void testWrongCalculator() {
    assertNull(VolatilityFunctionFactory.getCalculatorName(new SVIVolatilityFunction()));
  }

  @Test
  public void test() {
    assertEquals(VolatilityFunctionFactory.HAGAN, VolatilityFunctionFactory.getCalculatorName(VolatilityFunctionFactory.getCalculator(VolatilityFunctionFactory.HAGAN)));
    assertEquals(VolatilityFunctionFactory.ALTERNATIVE_HAGAN, VolatilityFunctionFactory.getCalculatorName(VolatilityFunctionFactory.getCalculator(VolatilityFunctionFactory.ALTERNATIVE_HAGAN)));
    assertEquals(VolatilityFunctionFactory.BERESTYCKI, VolatilityFunctionFactory.getCalculatorName(VolatilityFunctionFactory.getCalculator(VolatilityFunctionFactory.BERESTYCKI)));
    assertEquals(VolatilityFunctionFactory.JOHNSON, VolatilityFunctionFactory.getCalculatorName(VolatilityFunctionFactory.getCalculator(VolatilityFunctionFactory.JOHNSON)));
    assertEquals(VolatilityFunctionFactory.PAULOT, VolatilityFunctionFactory.getCalculatorName(VolatilityFunctionFactory.getCalculator(VolatilityFunctionFactory.PAULOT)));
    assertEquals(VolatilityFunctionFactory.HAGAN_FORMULA, VolatilityFunctionFactory.getCalculator(VolatilityFunctionFactory.getCalculatorName(VolatilityFunctionFactory.HAGAN_FORMULA)));
    assertEquals(VolatilityFunctionFactory.ALTERNATIVE_HAGAN_FORMULA,
        VolatilityFunctionFactory.getCalculator(VolatilityFunctionFactory.getCalculatorName(VolatilityFunctionFactory.ALTERNATIVE_HAGAN_FORMULA)));
    assertEquals(VolatilityFunctionFactory.BERESTYCKI_FORMULA, VolatilityFunctionFactory.getCalculator(VolatilityFunctionFactory.getCalculatorName(VolatilityFunctionFactory.BERESTYCKI_FORMULA)));
    assertEquals(VolatilityFunctionFactory.JOHNSON_FORMULA, VolatilityFunctionFactory.getCalculator(VolatilityFunctionFactory.getCalculatorName(VolatilityFunctionFactory.JOHNSON_FORMULA)));
    assertEquals(VolatilityFunctionFactory.PAULOT_FORMULA, VolatilityFunctionFactory.getCalculator(VolatilityFunctionFactory.getCalculatorName(VolatilityFunctionFactory.PAULOT_FORMULA)));
  }
}
