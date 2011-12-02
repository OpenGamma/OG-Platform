/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class PresentValueNodeSensitivityCalculatorTest extends NodeSensitivityCalculatorTest {
  private static PresentValueCalculator VALUE_CALCULATOR = PresentValueCalculator.getInstance();
  private static PresentValueCurveSensitivityCalculator SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  private static PresentValueNodeSensitivityCalculator CALCULATOR = PresentValueNodeSensitivityCalculator.getDefaultInstance();

  @Override
  protected NodeSensitivityCalculator getCalculator() {
    return CALCULATOR;
  }

  @Override
  protected PresentValueCurveSensitivityCalculator getSensitivityCalculator() {
    return SENSITIVITY_CALCULATOR;
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
    PresentValueNodeSensitivityCalculator other = new PresentValueNodeSensitivityCalculator();
    assertEquals(CALCULATOR, other);
    assertEquals(CALCULATOR.hashCode(), other.hashCode());
    other = PresentValueNodeSensitivityCalculator.getDefaultInstance();
    assertEquals(CALCULATOR, other);
    assertEquals(CALCULATOR.hashCode(), other.hashCode());
    other = new PresentValueNodeSensitivityCalculator(PresentValueCurveSensitivitySABRCalculator.getInstance());
    assertFalse(other.equals(CALCULATOR));
  }

  @Test
  public void testPresentValue() {
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = PresentValueCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> senseCalculator = PresentValueCurveSensitivityCalculator.getInstance();
    final DoubleMatrix1D result = CALCULATOR.calculateSensitivities(IRD, senseCalculator, null, INTERPOLATED_CURVES);
    final DoubleMatrix1D fdresult = finiteDiffNodeSensitivities(IRD, valueCalculator, null, INTERPOLATED_CURVES);
    assertArrayEquals(result.getData(), fdresult.getData(), 1e-8);
  }
}
