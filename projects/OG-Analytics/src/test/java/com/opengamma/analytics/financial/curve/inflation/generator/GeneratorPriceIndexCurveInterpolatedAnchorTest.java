/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.inflation.generator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.calculator.generic.LastFixingEndTimeCalculator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the generator of price index curves interpolated with an "anchor" node.
 */
public class GeneratorPriceIndexCurveInterpolatedAnchorTest {

  private static final Interpolator1D INTERPOLATOR_LINEAR = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final LastFixingEndTimeCalculator LAST_FIXING_END_CALCULATOR = LastFixingEndTimeCalculator.getInstance();
  private static final double ANCHOR_NODE = 0.5;
  private static final double ANCHOR_VALUE = 1.0;
  
  private static final GeneratorPriceIndexCurveInterpolatedAnchor GENERATOR =
      new GeneratorPriceIndexCurveInterpolatedAnchor(LAST_FIXING_END_CALCULATOR, INTERPOLATOR_LINEAR, 
          ANCHOR_NODE, ANCHOR_VALUE);
  
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex(IndexIborMaster.USDLIBOR3M);
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final ZonedDateTime DATE_0 = DateUtils.getUTCDate(2013, 9, 30);
  private static final ZonedDateTime DATE_1 = DateUtils.getUTCDate(2013, 12, 31);
  private static final ZonedDateTime DATE_2 = DateUtils.getUTCDate(2014, 3, 31);
  private static final ZonedDateTime DATE_3 = DateUtils.getUTCDate(2014, 6, 30);
  private static final ForwardRateAgreementDefinition FRA_1_DEFINITION = 
      ForwardRateAgreementDefinition.from(DATE_1, DATE_2, 100.0, USDLIBOR3M, 0.0100, NYC);
  private static final ForwardRateAgreementDefinition FRA_2_DEFINITION = 
      ForwardRateAgreementDefinition.from(DATE_2, DATE_3, 100.0, USDLIBOR3M, 0.0150, NYC);
  private static final Payment FRA_1 = FRA_1_DEFINITION.toDerivative(DATE_0);
  private static final Payment FRA_2 = FRA_2_DEFINITION.toDerivative(DATE_0);
  
  private static final double TOLERANCE_NODE = 1.0E-6;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullLastTime() {
    new GeneratorPriceIndexCurveInterpolatedAnchor(null, INTERPOLATOR_LINEAR, ANCHOR_NODE, ANCHOR_VALUE);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullInterpolator() {
    new GeneratorPriceIndexCurveInterpolatedAnchor(LAST_FIXING_END_CALCULATOR, null, ANCHOR_NODE, ANCHOR_VALUE);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongData() {
    GENERATOR.finalGenerator(new double[2]);
  }
  
  @Test
  public void getter() {
    assertEquals("GeneratorPriceIndexCurveInterpolatedAnchor: getter", ANCHOR_NODE, GENERATOR.getAnchorNode());
    assertEquals("GeneratorPriceIndexCurveInterpolatedAnchor: getter", ANCHOR_VALUE, GENERATOR.getAnchorValue());
  }
  
  @Test
  public void finalGenerator() {
    GeneratorPriceIndexCurve finalGen = GENERATOR.finalGenerator(new InstrumentDerivative[] {FRA_1, FRA_2});
    assertTrue("GeneratorPriceIndexCurveInterpolatedAnchor: final generator", 
        finalGen instanceof GeneratorPriceIndexCurveInterpolatedAnchorNode);
    GeneratorPriceIndexCurveInterpolatedAnchorNode genNode = (GeneratorPriceIndexCurveInterpolatedAnchorNode) finalGen;
    assertTrue("GeneratorPriceIndexCurveInterpolatedAnchor: final generator", 
        genNode.getNumberOfParameter() == 2);
    assertEquals("GeneratorPriceIndexCurveInterpolatedAnchor: final generator", ANCHOR_NODE, genNode.getAnchorNode());
    assertEquals("GeneratorPriceIndexCurveInterpolatedAnchor: final generator", ANCHOR_VALUE, genNode.getAnchorValue());
    double[] nodePointsExpected = new double[2];
    nodePointsExpected[0] = FRA_1.accept(LAST_FIXING_END_CALCULATOR);
    nodePointsExpected[1] = FRA_2.accept(LAST_FIXING_END_CALCULATOR);
    ArrayAsserts.assertArrayEquals(nodePointsExpected, genNode.getNodePoints(), TOLERANCE_NODE);
  }
  
}
