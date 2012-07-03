/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

public class ParameterSensitivityCalculator2Test extends ParameterSensitivityCalculator1Test {

  private static PresentValueCalculator VALUE_CALCULATOR = PresentValueCalculator.getInstance();
  //  private static PresentValueCurveSensitivityCalculator SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  private static PresentValueCurveSensitivityIRSCalculator SENSITIVITY_IRS_CALCULATOR = PresentValueCurveSensitivityIRSCalculator.getInstance();
  private static ParameterSensitivityCalculator NODE_CALCULATOR = new ParameterSensitivityCalculator(SENSITIVITY_IRS_CALCULATOR);

  @Override
  protected ParameterSensitivityCalculator getCalculator() {
    return NODE_CALCULATOR;
  }

  //  @Override
  //  protected PresentValueCurveSensitivityCalculator getSensitivityCalculator() {
  //    return SENSITIVITY_CALCULATOR;
  //  }

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
    ParameterSensitivityCalculator other = new ParameterSensitivityCalculator(SENSITIVITY_IRS_CALCULATOR);
    assertEquals(NODE_CALCULATOR, other);
    assertEquals(NODE_CALCULATOR.hashCode(), other.hashCode());
    //    other = new ParameterSensitivityCalculator(PresentValueCurveSensitivitySABRCalculator.getInstance());
    //    assertFalse(other.equals(NODE_CALCULATOR));
  }

  @Test
  public void testPresentValue() {
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = PresentValueCalculator.getInstance();
    final DoubleMatrix1D result = NODE_CALCULATOR.calculateSensitivity(SWAP, null, CURVE_BUNDLE_YIELD);
    final DoubleMatrix1D fdresult = finiteDiffNodeSensitivitiesYield(SWAP, valueCalculator, null, CURVE_BUNDLE_YIELD);
    assertArrayEquals(result.getData(), fdresult.getData(), TOLERANCE_SENSI);
  }

  //  @Test
  //  /**
  //   * Tests the present value node sensitivity for a swaption.
  //   */
  //  public void presentValueSwaption() {
  //    final String[] curveNames = TestsDataSetsSABR.curves2Names();
  //    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2009, 3, 28);
  //    final SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionInstrumentsDescriptionDataSet.createSwaptionPhysicalFixedIborDefinition(); // USD - Expiry date: 2011-Mar-28
  //    final SwaptionPhysicalFixedIbor swaption = swaptionDefinition.toDerivative(referenceDate, curveNames);
  //    final PresentValueSABRCalculator pvc = PresentValueSABRCalculator.getInstance();
  //    final PresentValueCurveSensitivitySABRCalculator pvcsc = PresentValueCurveSensitivitySABRCalculator.getInstance();
  //    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves2();
  //    final SABRInterestRateParameters sabrParam = TestsDataSetsSABR.createSABR2();
  //    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParam, curves);
  //    final DoubleMatrix1D resultCalculator = NODE_CALCULATOR.calculateSensitivities(swaption, pvcsc, null, sabrBundle);
  //    final DoubleMatrix1D resultFiniteDifference = finiteDiffNodeSensitivitiesYield(swaption, pvc, null, sabrBundle);
  //    double notional = Math.abs(swaption.getUnderlyingSwap().getFirstLeg().getNthPayment(0).getNotional());
  //    assertArrayEquals("Present Value Node Sensitivity", resultFiniteDifference.getData(), resultCalculator.getData(), notional * TOLERANCE_SENSI);
  //
  //    final SwaptionPhysicalFixedIborSABRMethod method = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  //    final InterestRateCurveSensitivity pvcsMethod = method.presentValueCurveSensitivity(swaption, sabrBundle);
  //    final DoubleMatrix1D resultMethod = NODE_CALCULATOR.curveToNodeSensitivities(pvcsMethod, sabrBundle);
  //    final DoubleMatrix1D resultMethod2 = NODE_CALCULATOR.curveToNodeSensitivities(pvcsMethod, curves);
  //    assertArrayEquals("Present Value Node Sensitivity", resultCalculator.getData(), resultMethod.getData(), notional * TOLERANCE_SENSI);
  //    assertArrayEquals("Present Value Node Sensitivity", resultCalculator.getData(), resultMethod2.getData(), notional * TOLERANCE_SENSI);
  //  }
}
