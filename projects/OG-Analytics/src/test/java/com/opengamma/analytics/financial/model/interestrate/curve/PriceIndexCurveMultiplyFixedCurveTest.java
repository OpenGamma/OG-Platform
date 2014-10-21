/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the class PriceIndexCurveMultiplyFixedCurve.
 */
public class PriceIndexCurveMultiplyFixedCurveTest {
  
  private static ZonedDateTime CURVE_DATE = DateUtils.getUTCDate(2014, 10, 9);
  private static final Interpolator1D INTERPOLATOR_LINEAR = 
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, 
          Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static double[] INDEX_VALUE = new double[] {108.23, 108.64, 111.0, 115.0};
  private static double[] TIME_VALUE = new double[] {-3.0 / 12.0, -2.0 / 12.0, 9.0 / 12.0, 2.0 + 9.0 / 12.0};
  private static final String UNDERLYING_NAME = "Under";
  private static final InterpolatedDoublesCurve UNDERLYING = 
      InterpolatedDoublesCurve.from(TIME_VALUE, INDEX_VALUE, INTERPOLATOR_LINEAR, UNDERLYING_NAME);
  private static final PriceIndexCurveSimple UNDERLYING_CURVE = new PriceIndexCurveSimple(UNDERLYING);
  

  public static final double[] SEASONAL_FACTORS = 
    {1.005, 1.001, 1.01, .999, .998, .9997, 1.004, 1.006, .994, .993, .9991 };
  private static final ZonedDateTime DATE_OFFSET = CURVE_DATE.minusMonths(3).with(TemporalAdjusters.lastDayOfMonth());
  private static final ZonedDateTime[] SEASONALITY_DATES = ScheduleCalculator.getUnadjustedDateSchedule(DATE_OFFSET, 
      DATE_OFFSET.plusYears(30), Period.ofMonths(1), true, false);
  private static final double[] SEASONAL_STEPS = new double[SEASONALITY_DATES.length];
  static {
    for (int loopins = 0; loopins < SEASONALITY_DATES.length; loopins++) {
      SEASONAL_STEPS[loopins] = TimeCalculator.getTimeBetween(CURVE_DATE, SEASONALITY_DATES[loopins]);
    }
  }
  private static final SeasonalCurve SEASONAL_CURVE = new SeasonalCurve(SEASONAL_STEPS, SEASONAL_FACTORS, false);
  private static final String PI_CURVE_NAME = "Price-Curve";
  private static final PriceIndexCurveMultiplyFixedCurve PI_CURVE = 
      new PriceIndexCurveMultiplyFixedCurve(PI_CURVE_NAME, UNDERLYING_CURVE, SEASONAL_CURVE);
  
  private static final double TOLERANCE_INDEX = 1.0E-10;
  
  @Test
  public void getter() {
    assertEquals("PriceIndexCurveMultiplyFixedCurve: getter", PI_CURVE_NAME, PI_CURVE.getName());
  }
  
  @Test
  public void getPriceIndex() {
    int nbTime = 25;
    for (int i = 0; i <= nbTime; i++) {
      assertEquals("PriceIndexCurveMultiplyFixedCurve: price index",
          UNDERLYING_CURVE.getPriceIndex(SEASONAL_STEPS[i]) * SEASONAL_CURVE.getYValue(SEASONAL_STEPS[i]),
          PI_CURVE.getPriceIndex(SEASONAL_STEPS[i]), TOLERANCE_INDEX);
    }
  }
  
  @Test
  public void getPriceIndexParameterSensitivity() {
    int nbTime = 25;
    for (int looptime = 0; looptime <= nbTime; looptime++) {
      double[] sensiUnderlying = UNDERLYING_CURVE.getPriceIndexParameterSensitivity(SEASONAL_STEPS[looptime]);
      double[] sensiComputed = PI_CURVE.getPriceIndexParameterSensitivity(SEASONAL_STEPS[looptime]);
      double[] sensiExpected = new double[sensiUnderlying.length];
      for(int loopparam = 0 ; loopparam < sensiUnderlying.length; loopparam++) {
        sensiExpected[loopparam] = sensiUnderlying[loopparam] * SEASONAL_CURVE.getYValue(SEASONAL_STEPS[looptime]);
        assertEquals("PriceIndexCurveMultiplyFixedCurve: price index parameter sensitivity",
            sensiExpected[loopparam], sensiComputed[loopparam], TOLERANCE_INDEX);
      }
    }
  }
  
}
