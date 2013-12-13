/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class FixedIncomeCalculatorFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadName() {
    FixedIncomeCalculatorFactory.getCalculator("A");
  }

  @Test
  public void testNullCalculator() {
    assertNull(FixedIncomeCalculatorFactory.getCalculatorName(null));
  }

  @Test
  public void test() {
    assertEquals(FixedIncomeCalculatorFactory.PAR_RATE, FixedIncomeCalculatorFactory.getCalculatorName(FixedIncomeCalculatorFactory
        .getCalculator(FixedIncomeCalculatorFactory.PAR_RATE)));
    assertEquals(FixedIncomeCalculatorFactory.PAR_RATE_CURVE_SENSITIVITY, FixedIncomeCalculatorFactory.getCalculatorName(FixedIncomeCalculatorFactory
        .getCalculator(FixedIncomeCalculatorFactory.PAR_RATE_CURVE_SENSITIVITY)));
    assertEquals(FixedIncomeCalculatorFactory.PRESENT_VALUE, FixedIncomeCalculatorFactory.getCalculatorName(FixedIncomeCalculatorFactory
        .getCalculator(FixedIncomeCalculatorFactory.PRESENT_VALUE)));
    assertEquals(FixedIncomeCalculatorFactory.PRESENT_VALUE_COUPON_SENSITIVITY, FixedIncomeCalculatorFactory.getCalculatorName(FixedIncomeCalculatorFactory
        .getCalculator(FixedIncomeCalculatorFactory.PRESENT_VALUE_COUPON_SENSITIVITY)));
    assertEquals(FixedIncomeCalculatorFactory.PRESENT_VALUE_SENSITIVITY, FixedIncomeCalculatorFactory.getCalculatorName(FixedIncomeCalculatorFactory
        .getCalculator(FixedIncomeCalculatorFactory.PRESENT_VALUE_SENSITIVITY)));
    assertEquals(FixedIncomeCalculatorFactory.PAR_RATE, FixedIncomeCalculatorFactory.getCalculatorName(FixedIncomeCalculatorFactory
        .getCalculator(FixedIncomeCalculatorFactory.PAR_RATE)));
  }
}
