/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDates;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborAverageFixingDatesDiscountingMethodTest {
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = IBOR_INDEXES[0];

  private static final int NUM_OBS = 6;

  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 7, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 7, 6);
  private static final ZonedDateTime[] FIXING_DATES = new ZonedDateTime[NUM_OBS];
  private static final double[] WEIGHTS = new double[NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      FIXING_DATES[i] = DateUtils.getUTCDate(2011, i + 1, 3);
      WEIGHTS[i] = 2. * (NUM_OBS - i) / NUM_OBS / (NUM_OBS + 1.);
    }
  }

  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000;

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);

  private static ZonedDateTime[] EXP_START_DATES = new ZonedDateTime[NUM_OBS];
  private static ZonedDateTime[] EXP_END_DATES = new ZonedDateTime[NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      EXP_START_DATES[i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[i], INDEX.getSpotLag(), CALENDAR);
      EXP_END_DATES[i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES[i], INDEX.getTenor(), INDEX.getBusinessDayConvention(), CALENDAR, INDEX.isEndOfMonth());
    }
  }

  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);
  private static final double[] FIXING_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATES);
  private static final double[] FIXING_PERIOD_START_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXP_START_DATES);
  private static final double[] FIXING_PERIOD_END_TIMES = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXP_END_DATES);
  private static final double[] FIX_ACC_FACTORS = new double[NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      FIX_ACC_FACTORS[i] = INDEX.getDayCount().getDayCountFraction(EXP_START_DATES[i], EXP_END_DATES[i]);
    }
  }

  private static final double AMOUNT_ACC = 0.05;
  private static final CouponIborAverageFixingDates DER1 = new CouponIborAverageFixingDates(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, INDEX,
      FIXING_TIMES, WEIGHTS, FIXING_PERIOD_START_TIMES, FIXING_PERIOD_END_TIMES, FIX_ACC_FACTORS, AMOUNT_ACC);
  private static final CouponIborAverageFixingDatesDiscountingMethod METHOD = CouponIborAverageFixingDatesDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final double EPS = 1.0e-10;
  private static final double TOLERANCE_PV_DELTA = 1.0;

  /**
   * 
   */
  @Test
  public void presentValueTest() {
    final MultipleCurrencyAmount pvComputed = METHOD.presentValue(DER1, MULTICURVES);
    double forward = AMOUNT_ACC;
    for (int i = 0; i < NUM_OBS; ++i) {
      forward += WEIGHTS[i] * MULTICURVES.getSimplyCompoundForwardRate(INDEX, FIXING_PERIOD_START_TIMES[i], FIXING_PERIOD_END_TIMES[i], FIX_ACC_FACTORS[i]);
    }
    final double pvExpected = DER1.getNotional() * MULTICURVES.getDiscountFactor(DER1.getCurrency(), DER1.getPaymentTime()) * DER1.getPaymentYearFraction() * forward;
    assertEquals(pvExpected, pvComputed.getAmount(DER1.getCurrency()), EPS * pvExpected);
    final MultipleCurrencyAmount pvWithCalc = PVDC.visitCouponIborAverageSinglePeriod(DER1, MULTICURVES);
    assertEquals(pvWithCalc.getAmount(DER1.getCurrency()), pvComputed.getAmount(DER1.getCurrency()), EPS * pvExpected);
  }

  /**
   * 
   */
  @Test
  public void sensitivityFiniteDifferenceTest() {
    final MultipleCurrencyParameterSensitivity senseCalc = PSC.calculateSensitivity(DER1, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity senseFd = PSC_DSC_FD.calculateSensitivity(DER1, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageFixingDatesDiscountingMethod", senseCalc, senseFd, TOLERANCE_PV_DELTA);
  }

  /**
   * 
   */
  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD.presentValueCurveSensitivity(DER1, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = DER1.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageFixingDatesDiscountingMethod", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }
}
