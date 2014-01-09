/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.SABRDataBundle;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SABRBlackEquivalentVolatilitySurfaceModelTest {
  private static final double K = 70;
  private static final double T = 0.5;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 8, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, T));
  private static final double ALPHA = 0.3;
  private static final double BETA = 1;
  private static final double RHO = -0.4;
  private static final double VOL_OF_VOL = 0.5;
  private static final double R = 0.05;
  private static final YieldCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final double B = R;
  private static final double S = 100 * Math.exp(-B * T);
  private static final VolatilitySurface ATM_VOL = new VolatilitySurface(ConstantDoublesSurface.from(0.3));
  private static final OptionDefinition OPTION = new EuropeanVanillaOptionDefinition(K, EXPIRY, false);
  private static final SABRDataBundle DATA = new SABRDataBundle(CURVE, B, ATM_VOL, S, DATE, ALPHA, BETA, RHO, VOL_OF_VOL);
  private static final SABRBlackEquivalentVolatilitySurfaceModel MODEL = new SABRBlackEquivalentVolatilitySurfaceModel();
  private static final SABRATMVolatilityCalibrationFunction CALIBRATION = new SABRATMVolatilityCalibrationFunction();
  private static final double EPS = 1e-4;
  private static final DoublesPair PAIR = DoublesPair.of(0., 0.);

  @Test
  public void testBetaApproachesOne() {
    final SABRDataBundle data = DATA.withBeta(1 - 1e-12);
    assertEquals(MODEL.getSurface(OPTION, DATA).getVolatility(PAIR), MODEL.getSurface(OPTION, data).getVolatility(PAIR), 1e-9);
  }

  @Test
  public void testBetaApproachesZero() {
    final SABRDataBundle data = DATA.withBeta(1e-12);
    assertEquals(MODEL.getSurface(OPTION, DATA.withBeta(1e-12)).getVolatility(PAIR), MODEL.getSurface(OPTION, data).getVolatility(PAIR), 1e-9);
  }

  @Test
  public void testWithZeroVolOfVol() {
    final SABRDataBundle data = DATA.withVolOfVol(0);
    assertEquals(MODEL.getSurface(OPTION, data).getVolatility(PAIR), ATM_VOL.getVolatility(PAIR));
  }

  /**
   * Number from Option Pricing Formulas, p270
   */
  @Test
  public void test() {
    assertEquals(MODEL.getSurface(OPTION, DATA).getVolatility(PAIR), 0.3454, EPS);
    SABRDataBundle data1 = DATA.withRho(-0.75).withBeta(0.999);
    SABRDataBundle data2 = DATA.withRho(0).withBeta(0.999);
    SABRDataBundle data3 = DATA.withRho(0.5).withBeta(0.999);
    OptionDefinition option = new EuropeanVanillaOptionDefinition(70, EXPIRY, false);
    data1 = CALIBRATION.calibrate(option, data1);
    data2 = CALIBRATION.calibrate(option, data2);
    data3 = CALIBRATION.calibrate(option, data3);
    assertEquals(MODEL.getSurface(option, data1).getVolatility(PAIR), 0.3668, EPS);
    assertEquals(MODEL.getSurface(option, data2).getVolatility(PAIR), 0.3165, EPS);
    assertEquals(MODEL.getSurface(option, data3).getVolatility(PAIR), 0.2709, EPS);
    option = new EuropeanVanillaOptionDefinition(80, EXPIRY, false);
    data1 = CALIBRATION.calibrate(option, data1);
    data2 = CALIBRATION.calibrate(option, data2);
    data3 = CALIBRATION.calibrate(option, data3);
    assertEquals(MODEL.getSurface(option, data1).getVolatility(PAIR), 0.3418, EPS);
    assertEquals(MODEL.getSurface(option, data2).getVolatility(PAIR), 0.3068, EPS);
    assertEquals(MODEL.getSurface(option, data3).getVolatility(PAIR), 0.2774, EPS);
    option = new EuropeanVanillaOptionDefinition(90, EXPIRY, false);
    data1 = CALIBRATION.calibrate(option, data1);
    data2 = CALIBRATION.calibrate(option, data2);
    data3 = CALIBRATION.calibrate(option, data3);
    assertEquals(MODEL.getSurface(option, data1).getVolatility(PAIR), 0.3197, EPS);
    assertEquals(MODEL.getSurface(option, data2).getVolatility(PAIR), 0.3016, EPS);
    assertEquals(MODEL.getSurface(option, data3).getVolatility(PAIR), 0.2878, EPS);
    option = new EuropeanVanillaOptionDefinition(100, EXPIRY, false);
    data1 = CALIBRATION.calibrate(option, data1);
    data2 = CALIBRATION.calibrate(option, data2);
    data3 = CALIBRATION.calibrate(option, data3);
    assertEquals(MODEL.getSurface(option, data1).getVolatility(PAIR), 0.3000, EPS);
    assertEquals(MODEL.getSurface(option, data2).getVolatility(PAIR), 0.3000, EPS);
    assertEquals(MODEL.getSurface(option, data3).getVolatility(PAIR), 0.3000, EPS);
    option = new EuropeanVanillaOptionDefinition(110, EXPIRY, false);
    data1 = CALIBRATION.calibrate(option, data1);
    data2 = CALIBRATION.calibrate(option, data2);
    data3 = CALIBRATION.calibrate(option, data3);
    assertEquals(MODEL.getSurface(option, data1).getVolatility(PAIR), 0.2826, EPS);
    assertEquals(MODEL.getSurface(option, data2).getVolatility(PAIR), 0.3013, EPS);
    assertEquals(MODEL.getSurface(option, data3).getVolatility(PAIR), 0.3128, EPS);
    option = new EuropeanVanillaOptionDefinition(120, EXPIRY, false);
    data1 = CALIBRATION.calibrate(option, data1);
    data2 = CALIBRATION.calibrate(option, data2);
    data3 = CALIBRATION.calibrate(option, data3);
    assertEquals(MODEL.getSurface(option, data1).getVolatility(PAIR), 0.2674, EPS);
    assertEquals(MODEL.getSurface(option, data2).getVolatility(PAIR), 0.3046, EPS);
    assertEquals(MODEL.getSurface(option, data3).getVolatility(PAIR), 0.3255, EPS);
    option = new EuropeanVanillaOptionDefinition(130, EXPIRY, false);
    data1 = CALIBRATION.calibrate(option, data1);
    data2 = CALIBRATION.calibrate(option, data2);
    data3 = CALIBRATION.calibrate(option, data3);
    assertEquals(MODEL.getSurface(option, data1).getVolatility(PAIR), 0.2546, EPS);
    assertEquals(MODEL.getSurface(option, data2).getVolatility(PAIR), 0.3092, EPS);
    assertEquals(MODEL.getSurface(option, data3).getVolatility(PAIR), 0.3378, EPS);
  }
}
