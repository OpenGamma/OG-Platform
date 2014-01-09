/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.HashSet;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.curve.interestrate.sensitivity.ParameterSensitivityCalculator;
import com.opengamma.analytics.financial.curve.interestrate.sensitivity.ParameterUnderlyingSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityIRSCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Test(groups = TestGroup.UNIT)
@Deprecated
public class PresentValueParameterUnderlyingSensitivityCalculatorTest extends ParameterUnderlyingSensitivityCalculatorTest {

  private static PresentValueCalculator VALUE_CALCULATOR = PresentValueCalculator.getInstance();
  private static PresentValueCurveSensitivityIRSCalculator SENSITIVITY_IRS_CALCULATOR = PresentValueCurveSensitivityIRSCalculator.getInstance();
  private static ParameterUnderlyingSensitivityCalculator PARAMETER_UNDERLYING_CALCULATOR = new ParameterUnderlyingSensitivityCalculator(SENSITIVITY_IRS_CALCULATOR);
  private static ParameterSensitivityCalculator PARAMETER_CALCULATOR = new ParameterSensitivityCalculator(SENSITIVITY_IRS_CALCULATOR);

  @Override
  protected ParameterUnderlyingSensitivityCalculator getCalculator() {
    return PARAMETER_UNDERLYING_CALCULATOR;
  }

  @Override
  protected ParameterSensitivityCalculator getNoUnderlyingCalculator() {
    return PARAMETER_CALCULATOR;
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
    final ParameterUnderlyingSensitivityCalculator other = new ParameterUnderlyingSensitivityCalculator(SENSITIVITY_IRS_CALCULATOR);
    assertEquals(PARAMETER_UNDERLYING_CALCULATOR, other);
    assertEquals(PARAMETER_UNDERLYING_CALCULATOR.hashCode(), other.hashCode());
  }

  @Test
  public void curveWithSpreadAllCurves() {
    final DoubleMatrix1D resultCleanComputed = PARAMETER_UNDERLYING_CALCULATOR.calculateSensitivity(getSwap(), new HashSet<String>(), getCurveBundleSpread());
    final DoubleMatrix1D resultDirty = PARAMETER_CALCULATOR.calculateSensitivity(getSwap(), new HashSet<String>(), getCurveBundleSpread());
    final int nbNode = 9;
    final int nbSpreadParam = 1;
    final double[] resultCleanExpected = new double[nbNode + nbSpreadParam];
    System.arraycopy(resultDirty.getData(), nbNode, resultCleanExpected, 0, nbNode + nbSpreadParam);
    for (int loops = 0; loops < nbNode; loops++) {
      resultCleanExpected[loops] += resultDirty.getEntry(loops);
    }
    assertArrayEquals("Sensitivity to rates: Spread curve", resultCleanComputed.getData(), resultCleanExpected, getTolerance());
  }

}
