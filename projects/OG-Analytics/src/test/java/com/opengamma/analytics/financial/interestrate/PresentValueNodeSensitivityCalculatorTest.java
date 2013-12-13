/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.swaption.SwaptionInstrumentsDescriptionDataSet;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class PresentValueNodeSensitivityCalculatorTest extends NodeSensitivityCalculatorTestBase {

  private static PresentValueCalculator VALUE_CALCULATOR = PresentValueCalculator.getInstance();
  private static PresentValueCurveSensitivityCalculator SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  private static PresentValueNodeSensitivityCalculator NODE_CALCULATOR = PresentValueNodeSensitivityCalculator.getDefaultInstance();

  @Override
  protected NodeYieldSensitivityCalculator getCalculator() {
    return NODE_CALCULATOR;
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
    assertEquals(NODE_CALCULATOR, other);
    assertEquals(NODE_CALCULATOR.hashCode(), other.hashCode());
    other = PresentValueNodeSensitivityCalculator.getDefaultInstance();
    assertEquals(NODE_CALCULATOR, other);
    assertEquals(NODE_CALCULATOR.hashCode(), other.hashCode());
    other = new PresentValueNodeSensitivityCalculator(PresentValueCurveSensitivitySABRCalculator.getInstance());
    assertFalse(other.equals(NODE_CALCULATOR));
  }

  @Test
  public void presentValueYieldCurve() {
    final InstrumentDerivativeVisitor<YieldCurveBundle, Double> valueCalculator = PresentValueCalculator.getInstance();
    final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> senseCalculator = PresentValueCurveSensitivityCalculator.getInstance();
    final DoubleMatrix1D result = NODE_CALCULATOR.calculateSensitivities(getSwap(), senseCalculator, null, getYieldCurve());
    final DoubleMatrix1D fdresult = finiteDiffNodeSensitivitiesYield(getSwap(), valueCalculator, null, getYieldCurve());
    assertArrayEquals(result.getData(), fdresult.getData(), getTolerance());
  }

  @Test
  /**
   * Tests the present value node sensitivity for a swaption.
   */
  public void presentValueSwaption() {
    final String[] curveNames = TestsDataSetsSABR.curves2Names();
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2009, 3, 28);
    final SwaptionPhysicalFixedIborDefinition swaptionDefinition = SwaptionInstrumentsDescriptionDataSet.createSwaptionPhysicalFixedIborDefinition(); // USD - Expiry date: 2011-Mar-28
    final SwaptionPhysicalFixedIbor swaption = swaptionDefinition.toDerivative(referenceDate, curveNames);
    final PresentValueSABRCalculator pvc = PresentValueSABRCalculator.getInstance();
    final PresentValueCurveSensitivitySABRCalculator pvcsc = PresentValueCurveSensitivitySABRCalculator.getInstance();
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves2();
    final SABRInterestRateParameters sabrParam = TestsDataSetsSABR.createSABR2();
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParam, curves);
    final DoubleMatrix1D resultCalculator = NODE_CALCULATOR.calculateSensitivities(swaption, pvcsc, null, sabrBundle);
    final DoubleMatrix1D resultFiniteDifference = finiteDiffNodeSensitivitiesYield(swaption, pvc, null, sabrBundle);
    final double notional = Math.abs(swaption.getUnderlyingSwap().getFirstLeg().getNthPayment(0).getNotional());
    assertArrayEquals("Present Value Node Sensitivity", resultFiniteDifference.getData(), resultCalculator.getData(), notional * getTolerance());

    final SwaptionPhysicalFixedIborSABRMethod method = SwaptionPhysicalFixedIborSABRMethod.getInstance();
    final InterestRateCurveSensitivity pvcsMethod = method.presentValueCurveSensitivity(swaption, sabrBundle);
    final DoubleMatrix1D resultMethod = NODE_CALCULATOR.curveToNodeSensitivities(pvcsMethod, sabrBundle);
    final DoubleMatrix1D resultMethod2 = NODE_CALCULATOR.curveToNodeSensitivities(pvcsMethod, curves);
    assertArrayEquals("Present Value Node Sensitivity", resultCalculator.getData(), resultMethod.getData(), notional * getTolerance());
    assertArrayEquals("Present Value Node Sensitivity", resultCalculator.getData(), resultMethod2.getData(), notional * getTolerance());
  }

}
