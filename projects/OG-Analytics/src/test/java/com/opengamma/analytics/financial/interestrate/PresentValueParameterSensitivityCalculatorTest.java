/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.HashSet;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.curve.interestrate.sensitivity.ParameterSensitivityCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class PresentValueParameterSensitivityCalculatorTest extends ParameterSensitivityCalculatorTestBase {

  private static PresentValueCalculator VALUE_CALCULATOR = PresentValueCalculator.getInstance();
  private static PresentValueCurveSensitivityIRSCalculator SENSITIVITY_IRS_CALCULATOR = PresentValueCurveSensitivityIRSCalculator.getInstance();
  private static ParameterSensitivityCalculator NODE_CALCULATOR = new ParameterSensitivityCalculator(SENSITIVITY_IRS_CALCULATOR);

  @Override
  protected ParameterSensitivityCalculator getCalculator() {
    return NODE_CALCULATOR;
  }

  @Override
  protected PresentValueCalculator getValueCalculator() {
    return VALUE_CALCULATOR;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new PresentValueNodeSensitivityCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    PresentValueNodeSensitivityCalculator.using(null);
  }

  @Test
  public void testObject() {
    final ParameterSensitivityCalculator other = new ParameterSensitivityCalculator(SENSITIVITY_IRS_CALCULATOR);
    assertEquals(NODE_CALCULATOR, other);
    assertEquals(NODE_CALCULATOR.hashCode(), other.hashCode());
  }

  @Test
  public void testPresentValue() {
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = PresentValueCalculator.getInstance();
    final DoubleMatrix1D result = NODE_CALCULATOR.calculateSensitivity(SWAP, new HashSet<String>(), CURVE_BUNDLE_YIELD);
    final DoubleMatrix1D fdresult = finiteDiffNodeSensitivitiesYield(SWAP, valueCalculator, null, CURVE_BUNDLE_YIELD);
    assertArrayEquals(result.getData(), fdresult.getData(), TOLERANCE_SENSI);
  }

}
