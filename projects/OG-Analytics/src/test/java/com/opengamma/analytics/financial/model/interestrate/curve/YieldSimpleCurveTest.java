/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link YieldSimpleCurve}
 */
@Test(groups = TestGroup.UNIT)
public class YieldSimpleCurveTest {

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] TIME = new double[] {1.0, 2.0, 2.5, 3};
  private static final int NB_TIME = TIME.length;
  private static final double[] YIELD = new double[] {0.01, 0.02, 0.02, 0.01};
  private static final InterpolatedDoublesCurve RATE = InterpolatedDoublesCurve.from(TIME, YIELD, INTERPOLATOR_LINEAR);
  private static final String NAME = "SimpleRate";
  private static final YieldAndDiscountCurve CURVE = new YieldSimpleCurve(NAME, RATE);
  private static final double CST = 0.01;
  private static final YieldAndDiscountCurve CURVE_CST =
      new YieldSimpleCurve(NAME, ConstantDoublesCurve.from(CST, NAME));
  private static final double[] TIME_TEST = {0.5, 1.0, 1.0001, 2.5, 2.70, 3.0, 4.0 };
  private static final int NB_TEST = TIME_TEST.length;
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_SENSI = 1.0E-8;


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void curve() {
    new YieldSimpleCurve(NAME, null);
  }
  
  @Test
  public void discountFactor() {
    for (int loopt = 0; loopt < NB_TEST; loopt++) {
      double r = RATE.getYValue(TIME_TEST[loopt]);
      double df = 1.0d / (1.0d + TIME_TEST[loopt] * r);
      assertEquals("YieldSimpleCurve: discount factor", df, CURVE.getDiscountFactor(TIME_TEST[loopt]), TOLERANCE_RATE);
    }
  }

  @Test
  public void continouslyCoompoundedRate() {
    for (int loopt = 0; loopt < NB_TEST; loopt++) {
      double df = CURVE.getDiscountFactor(TIME_TEST[loopt]);
      double rcc = -1.0d / TIME_TEST[loopt] * Math.log(df);
      assertEquals("YieldSimpleCurve: discount factor", rcc, CURVE.getInterestRate(TIME_TEST[loopt]), TOLERANCE_RATE);
    }
  }

  @Test
  public void numberParameters() {
    assertEquals("YieldSimpleCurve: parameters", CURVE.getNumberOfParameters(), RATE.size());    
  }
  
  @Test
  public void interestRateParameterSensitivity() {
    double shift = 1.0E-6;
    for (int looptest = 0; looptest < NB_TEST; looptest++) {
      double[] sensiComputed = CURVE.getInterestRateParameterSensitivity(TIME_TEST[looptest]);
      for (int loopnode = 0; loopnode < NB_TIME; loopnode++) {
        double[] rateBumpedPlus = YIELD.clone();
        rateBumpedPlus[loopnode] += shift;
        InterpolatedDoublesCurve interpPlus = InterpolatedDoublesCurve.from(TIME, rateBumpedPlus, INTERPOLATOR_LINEAR);
        YieldAndDiscountCurve curvePlus = new YieldSimpleCurve(NAME, interpPlus);
        double rccPlus = curvePlus.getInterestRate(TIME_TEST[looptest]);
        double[] rateBumpedMinus = YIELD.clone();
        rateBumpedMinus[loopnode] -= shift;
        InterpolatedDoublesCurve interpMinus = InterpolatedDoublesCurve
            .from(TIME, rateBumpedMinus, INTERPOLATOR_LINEAR);
        YieldAndDiscountCurve curveMinus = new YieldSimpleCurve(NAME, interpMinus);
        double rccMinus = curveMinus.getInterestRate(TIME_TEST[looptest]);
        double sensiFD = (rccPlus - rccMinus) / (2 * shift);
        assertEquals("YieldSimpleCurve: ParameterSensitivity", sensiFD, sensiComputed[loopnode], TOLERANCE_SENSI);
      }
    }
  }
    
  @Test
  public void interestRateParameterSensitivityCst() {
    double shift = 1.0E-6;
    for (int looptest = 0; looptest < NB_TEST; looptest++) {
      double[] sensiComputed = CURVE_CST.getInterestRateParameterSensitivity(TIME_TEST[looptest]);
      assertEquals("YieldSimpleCurve: ParameterSensitivity", 1, sensiComputed.length);
      YieldAndDiscountCurve curvePlus = new YieldSimpleCurve(NAME, ConstantDoublesCurve.from(CST + shift));
      double rccPlus = curvePlus.getInterestRate(TIME_TEST[looptest]);
      YieldAndDiscountCurve curveMinus = new YieldSimpleCurve(NAME, ConstantDoublesCurve.from(CST - shift));
      double rccMinus = curveMinus.getInterestRate(TIME_TEST[looptest]);
      double sensiFD = (rccPlus - rccMinus) / (2 * shift);
      assertEquals("YieldSimpleCurve: ParameterSensitivity", sensiFD, sensiComputed[0], TOLERANCE_SENSI);
    }    
  }
  
}
