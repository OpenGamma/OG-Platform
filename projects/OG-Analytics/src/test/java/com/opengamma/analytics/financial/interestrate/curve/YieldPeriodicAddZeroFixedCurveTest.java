/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicAddZeroFixedCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldPeriodicCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link YieldPeriodicAddZeroFixedCurve}.
 */
@Test(groups = TestGroup.UNIT)
public class YieldPeriodicAddZeroFixedCurveTest {
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  
  private static final double[] TIME = new double[] {0.0, 0.5, 1.0, 2.0, 5.0, 10.0, 30.00 };
  private static final double[] RATE = new double[] {0.0250, 0.0225, 0.0250, 0.0275, 0.0250, 0.0250, 0.0250 };
  private static final String CURVE_NAME = "Curve base";
  /* Curve with annually compounded rates. */
  private static final int FREQ = 1;
  private static final DoublesCurve CURVE_INT = new InterpolatedDoublesCurve(TIME, RATE, LINEAR_FLAT, true, CURVE_NAME);
  private static final YieldPeriodicCurve CURVE_ANNUAL = new YieldPeriodicCurve(CURVE_NAME, FREQ,CURVE_INT);
  private static final double CST = 0.0123;
  private static final DoublesCurve CURVE_CST = new ConstantDoublesCurve(CST, "Spread");
  private static final YieldAndDiscountCurve CURVE_ANNUAL_SPREAD = 
      new YieldPeriodicAddZeroFixedCurve("Total", false, CURVE_ANNUAL, CURVE_CST);  

  private static final double[] TEST_TIME = new double[] {-0.01, 0.0, 0.01, 1.0, 2.5, 31.00 };
  private static final int NB_TEST = TEST_TIME.length;
  private static final Double TOLERANCE_RATE = 1.0E-8;
  
  @Test
  public void interestRateCC() {
    for (int i = 0; i < NB_TEST; i++) {
      double rateCCComputed = CURVE_ANNUAL_SPREAD.getInterestRate(TEST_TIME[i]);
      double rateP = CURVE_INT.getYValue(TEST_TIME[i]) + CST;
      double rateCCExpected = FREQ * Math.log(1.0d + rateP / FREQ);
      assertEquals("YieldPeriodicAddZeroFixedCurve: rate continously compounded",
          rateCCExpected, rateCCComputed, TOLERANCE_RATE);
      double dfExpected = Math.exp(-rateCCComputed * TEST_TIME[i]);
      double dfComputed = CURVE_ANNUAL_SPREAD.getDiscountFactor(TEST_TIME[i]);
      assertEquals("YieldPeriodicAddZeroFixedCurve: consitency",
          dfExpected, dfComputed, TOLERANCE_RATE);
    }
  }

  @Test
  public void discountFactor() {
    for (int i = 0; i < NB_TEST; i++) {
      double dfComputed = CURVE_ANNUAL_SPREAD.getDiscountFactor(TEST_TIME[i]);
      double rateP = CURVE_INT.getYValue(TEST_TIME[i]) + CST;
      double dfExpected = Math.pow(1.0d + rateP / FREQ, -FREQ * TEST_TIME[i]);
      assertEquals("YieldPeriodicAddZeroFixedCurve: discount factor",
          dfExpected, dfComputed, TOLERANCE_RATE);
    }
  }
  
}
