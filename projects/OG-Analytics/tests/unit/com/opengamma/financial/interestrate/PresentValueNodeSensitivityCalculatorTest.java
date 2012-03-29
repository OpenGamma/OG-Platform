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

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.forex.calculator.ForexInstrumentsDescriptionDataSet;
import com.opengamma.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionInstrumentsDescriptionDataSet;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.time.DateUtils;
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

  private static final ForexSwapDefinition FX_SWAP = ForexInstrumentsDescriptionDataSet.createForexSwapDefinition(); // EUR/USD - Near date: 2011-May26
  private static final double TOLERANCE_DELTA = 1.0E-0; // 1.0E+2 = 0.01 unit by bp

  @Test
  /**
   * Tests the present value node sensitivity for a swaption.
   */
  public void presentValueSwaption() {
    String[] curveNames = TestsDataSetsSABR.curves2Names();
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2009, 3, 28);
    SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionInstrumentsDescriptionDataSet.createSwaptionPhysicalFixedIborDefinition(); // USD - Expiry date: 2011-Mar-28
    SwaptionPhysicalFixedIbor swaption = swaptionDefinition.toDerivative(referenceDate, curveNames);
    PresentValueSABRCalculator pvc = PresentValueSABRCalculator.getInstance();
    PresentValueCurveSensitivitySABRCalculator pvcsc = PresentValueCurveSensitivitySABRCalculator.getInstance();
    YieldCurveBundle curves = TestsDataSetsSABR.createCurves2();
    SABRInterestRateParameters sabrParam = TestsDataSetsSABR.createSABR2();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParam, curves);
    final DoubleMatrix1D resultCalculator = CALCULATOR.calculateSensitivities(swaption, pvcsc, null, sabrBundle);
    final DoubleMatrix1D resultFiniteDifference = finiteDiffNodeSensitivities(swaption, pvc, null, sabrBundle);
    assertArrayEquals("Present Value Node Sensitivity", resultFiniteDifference.getData(), resultCalculator.getData(), TOLERANCE_DELTA);

    SwaptionPhysicalFixedIborSABRMethod method = SwaptionPhysicalFixedIborSABRMethod.getInstance();
    InterestRateCurveSensitivity pvcsMethod = method.presentValueCurveSensitivity(swaption, sabrBundle);
    final DoubleMatrix1D resultMethod = CALCULATOR.curveToNodeSensitivities(pvcsMethod, sabrBundle);
    final DoubleMatrix1D resultMethod2 = CALCULATOR.curveToNodeSensitivities(pvcsMethod, curves);
    assertArrayEquals("Present Value Node Sensitivity", resultCalculator.getData(), resultMethod.getData(), TOLERANCE_DELTA);
    assertArrayEquals("Present Value Node Sensitivity", resultCalculator.getData(), resultMethod2.getData(), TOLERANCE_DELTA);
  }

}
