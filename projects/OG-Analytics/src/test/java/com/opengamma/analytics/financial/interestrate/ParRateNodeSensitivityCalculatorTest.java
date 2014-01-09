/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ParRateNodeSensitivityCalculatorTest extends NodeSensitivityCalculatorTestBase {

  private static ParRateCalculator VALUE_CALCULATOR = ParRateCalculator.getInstance();
  private static ParRateCurveSensitivityCalculator SENSITIVITY_CALCULATOR = ParRateCurveSensitivityCalculator.getInstance();
  private static ParRateNodeSensitivityCalculator CALCULATOR = ParRateNodeSensitivityCalculator.getDefaultInstance();

  @Override
  protected NodeYieldSensitivityCalculator getCalculator() {
    return CALCULATOR;
  }

  @Override
  protected ParRateCurveSensitivityCalculator getSensitivityCalculator() {
    return SENSITIVITY_CALCULATOR;
  }

  @Override
  protected ParRateCalculator getValueCalculator() {
    return VALUE_CALCULATOR;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator1() {
    new ParRateNodeSensitivityCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator2() {
    ParRateNodeSensitivityCalculator.using(null);
  }

  @Test
  public void testParRateValue() {
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = ParRateCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> senseCalculator = ParRateCurveSensitivityCalculator.getInstance();
    final DoubleMatrix1D result = CALCULATOR.calculateSensitivities(getSwap(), senseCalculator, null, getYieldCurve());
    final DoubleMatrix1D fdresult = finiteDiffNodeSensitivitiesYield(getSwap(), valueCalculator, null, getYieldCurve());
    assertArrayEquals(result.getData(), fdresult.getData(), 1e-8);
  }
}
