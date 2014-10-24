/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDatesCompounding;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
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
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the CouponIborAverageCompoundingDiscountingMethod.
 */
public class CouponIborAverageFixingDatesCompoundingDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final IborIndex INDEX1 = IBOR_INDEXES[0]; // EURIBOR3M
  private static final IborIndex INDEX2 = IBOR_INDEXES[3]; // LIBOR6M
  private static final Currency CUR1 = Currency.EUR;
  private static final Currency CUR2 = Currency.USD;
  private static final DayCount DAY_COUNT_INDEX = INDEX1.getDayCount();

  private static final int NUM_PRDS = 6;
  private static final int NUM_OBS = 5;
  private static final int NUM_OBS_INI = 3;

  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 7, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 7, 6);
  // The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime[][] FIXING_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static final double[][] WEIGHTS1 = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] WEIGHTS2 = new double[NUM_PRDS][NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        FIXING_DATES[j][i] = DateUtils.getUTCDate(2011, j + 1, 3 + 6 * i);
        WEIGHTS1[j][i] = 2. * (NUM_OBS - i) / NUM_OBS / (NUM_OBS + 1.);
        WEIGHTS2[j][i] = 2. * (NUM_OBS - i) / NUM_OBS / (NUM_OBS + 1.);
      }
    }
  }

  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double[] ACCRUAL_FACTORS = new double[NUM_PRDS];
  static {
    Arrays.fill(ACCRUAL_FACTORS, ACCRUAL_FACTOR / NUM_PRDS);
  }
  private static final double NOTIONAL = 100000000; // 100m

  private static final ZonedDateTime REFERENCE_DATE1 = FIXING_DATES[0][1].plusDays(1);
  private static final ZonedDateTime REFERENCE_DATE2 = DateUtils.getUTCDate(2010, 12, 27);

  private static ZonedDateTime[][] EXP_START_DATES1 = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static ZonedDateTime[][] EXP_END_DATES1 = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static ZonedDateTime[][] EXP_START_DATES2 = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static ZonedDateTime[][] EXP_END_DATES2 = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        EXP_START_DATES1[j][i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[j][i], INDEX1.getSpotLag(), CALENDAR);
        EXP_END_DATES1[j][i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES1[j][i], INDEX1.getTenor(),
            INDEX1.getBusinessDayConvention(), CALENDAR, INDEX1.isEndOfMonth());
        EXP_START_DATES2[j][i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[j][i], INDEX2.getSpotLag(), CALENDAR);
        EXP_END_DATES2[j][i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES2[j][i], INDEX2.getTenor(),
            INDEX2.getBusinessDayConvention(), CALENDAR, INDEX2.isEndOfMonth());
      }
    }
  }

  private static final double PAYMENT_TIME1 = TimeCalculator.getTimeBetween(REFERENCE_DATE1, PAYMENT_DATE);
  private static final double PAYMENT_TIME2 = TimeCalculator.getTimeBetween(REFERENCE_DATE2, PAYMENT_DATE);
  private static final double[][] FIXING_TIMES1 = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_START_TIMES1 = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_END_TIMES1 = new double[NUM_PRDS][NUM_OBS];
  private static double[][] FIX_ACC_FACTORS1 = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_TIMES2 = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_START_TIMES2 = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_END_TIMES2 = new double[NUM_PRDS][NUM_OBS];
  private static double[][] FIX_ACC_FACTORS2 = new double[NUM_PRDS][NUM_OBS];
  static {
    for (int i = 0; i < NUM_PRDS; ++i) {
      FIXING_TIMES1[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE1, FIXING_DATES[i]);
      FIXING_PERIOD_START_TIMES1[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE1, EXP_START_DATES1[i]);
      FIXING_PERIOD_END_TIMES1[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE1, EXP_END_DATES2[i]);
      FIXING_TIMES2[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE2, FIXING_DATES[i]);
      FIXING_PERIOD_START_TIMES2[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE2, EXP_START_DATES2[i]);
      FIXING_PERIOD_END_TIMES2[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE2, EXP_END_DATES2[i]);
    }
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        FIX_ACC_FACTORS1[j][i] = INDEX1.getDayCount().getDayCountFraction(EXP_START_DATES1[j][i], EXP_END_DATES1[j][i], CALENDAR);
        FIX_ACC_FACTORS2[j][i] = INDEX2.getDayCount().getDayCountFraction(EXP_START_DATES2[j][i], EXP_END_DATES2[j][i], CALENDAR);
      }
    }
  }

  private static final double[] FIXING_TIMES_INI = new double[NUM_OBS_INI];
  private static final double[] FIXING_PERIOD_START_TIMES_INI = new double[NUM_OBS_INI];
  private static final double[] FIXING_PERIOD_END_TIMES_INI = new double[NUM_OBS_INI];
  private static double[] FIX_ACC_FACTORS_INI = new double[NUM_OBS_INI];
  private static final double[] WEIGHTS_INI = new double[NUM_OBS_INI];
  static {
    for (int i = 0; i < NUM_OBS_INI; ++i) {
      FIXING_TIMES_INI[i] = FIXING_TIMES1[0][NUM_OBS - NUM_OBS_INI + i];
      FIXING_PERIOD_START_TIMES_INI[i] = FIXING_PERIOD_START_TIMES1[0][NUM_OBS - NUM_OBS_INI + i];
      FIXING_PERIOD_END_TIMES_INI[i] = FIXING_PERIOD_END_TIMES1[0][NUM_OBS - NUM_OBS_INI + i];
      FIX_ACC_FACTORS_INI[i] = FIX_ACC_FACTORS1[0][NUM_OBS - NUM_OBS_INI + i];
      WEIGHTS_INI[i] = WEIGHTS1[0][NUM_OBS - NUM_OBS_INI + i];
    }
    FIXING_TIMES1[0] = FIXING_TIMES_INI;
    FIXING_PERIOD_START_TIMES1[0] = FIXING_PERIOD_START_TIMES_INI;
    FIXING_PERIOD_END_TIMES1[0] = FIXING_PERIOD_END_TIMES_INI;
    FIX_ACC_FACTORS1[0] = FIX_ACC_FACTORS_INI;
    WEIGHTS1[0] = WEIGHTS_INI;
  }

  private static final double AMOUNT_ACC = 0.005;
  private static final double RATE_FIXED = 1.02;

  //Examples 1 & 2: same numbers of fixing 
  private static final CouponIborAverageFixingDatesCompounding DER1 = new CouponIborAverageFixingDatesCompounding(CUR1,
      PAYMENT_TIME1, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX1, FIXING_TIMES1, WEIGHTS1,
      FIXING_PERIOD_START_TIMES1, FIXING_PERIOD_END_TIMES1, FIX_ACC_FACTORS1, AMOUNT_ACC, RATE_FIXED);
  private static final CouponIborAverageFixingDatesCompounding DER2 = new CouponIborAverageFixingDatesCompounding(CUR2,
      PAYMENT_TIME2, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, INDEX2, FIXING_TIMES2, WEIGHTS2,
      FIXING_PERIOD_START_TIMES2, FIXING_PERIOD_END_TIMES2, FIX_ACC_FACTORS2, 0.0, 1.0);

  // Example 3: different number of fixing in each subperiod
  private static final int NB_SUBPERIODS = 3;
  private static final ZonedDateTime[] ACCRUAL_START_DATE_SUB_3 = new ZonedDateTime[NB_SUBPERIODS + 1];
  private static final ZonedDateTime[][] FIXING_DATES_3 = new ZonedDateTime[NB_SUBPERIODS][];
  private static final double[][] WEIGHTS_3 = new double[NB_SUBPERIODS][];
  private static final double[] ACCRUAL_FACTORS_3 = new double[NB_SUBPERIODS];
  static {
    for (int loopsub = 0; loopsub <= NB_SUBPERIODS; loopsub++) {
      ACCRUAL_START_DATE_SUB_3[loopsub] = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, Period.ofMonths(loopsub), INDEX1, CALENDAR);
    }
    for (int loopsub = 0; loopsub < NB_SUBPERIODS; loopsub++) {
      List<ZonedDateTime> listDates = new ArrayList<>();
      ZonedDateTime startFixPeriod = ACCRUAL_START_DATE_SUB_3[loopsub];
      listDates.add(ScheduleCalculator.getAdjustedDate(startFixPeriod, -INDEX1.getSpotLag(), CALENDAR));
      startFixPeriod = ScheduleCalculator.getAdjustedDate(startFixPeriod, 1, CALENDAR);
      while (startFixPeriod.isBefore(ACCRUAL_START_DATE_SUB_3[loopsub + 1])) {
        listDates.add(ScheduleCalculator.getAdjustedDate(startFixPeriod, -INDEX1.getSpotLag(), CALENDAR));
        startFixPeriod = ScheduleCalculator.getAdjustedDate(startFixPeriod, 1, CALENDAR);
      }
      FIXING_DATES_3[loopsub] = listDates.toArray(new ZonedDateTime[0]);
      WEIGHTS_3[loopsub] = new double[FIXING_DATES_3[loopsub].length];
      ACCRUAL_FACTORS_3[loopsub] = DAY_COUNT_INDEX.getDayCountFraction(ACCRUAL_START_DATE_SUB_3[loopsub], ACCRUAL_START_DATE_SUB_3[loopsub + 1]);
      for (int loopf = 0; loopf < FIXING_DATES_3[loopsub].length; loopf++) {
        WEIGHTS_3[loopsub][loopf] = 1.0d / FIXING_DATES_3[loopsub].length;
      }
    }
  }
  private static final CouponIborAverageFixingDatesCompoundingDefinition DFN3 = new CouponIborAverageFixingDatesCompoundingDefinition(CUR1, ACCRUAL_START_DATE_SUB_3[3],
      ACCRUAL_START_DATE_SUB_3[0], ACCRUAL_START_DATE_SUB_3[3], ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS_3, INDEX1, FIXING_DATES_3, WEIGHTS_3, CALENDAR);
  private static final CouponIborAverageFixingDatesCompounding DER3 = DFN3.toDerivative(FIXING_DATES_3[0][0].minusDays(1));

  private static final CouponIborAverageFixingDatesCompoundingDiscountingMethod METHOD = CouponIborAverageFixingDatesCompoundingDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final double EPS = 1.0e-10;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E2;

  /**
   * pv for partially fixed coupon
   */
  @Test
  public void presentValueFixedTest() {
    final MultipleCurrencyAmount pvComputed = METHOD.presentValue(DER1, MULTICURVES);
    double fwd = AMOUNT_ACC;
    for (int j = 0; j < NUM_OBS_INI; ++j) {
      fwd += WEIGHTS1[0][j] * MULTICURVES.getSimplyCompoundForwardRate(INDEX1, FIXING_PERIOD_START_TIMES1[0][j], FIXING_PERIOD_END_TIMES1[0][j], FIX_ACC_FACTORS1[0][j]);
    }
    double invFactor = RATE_FIXED;
    invFactor *= (1.0 + fwd * DER1.getPaymentAccrualFactors()[0]);
    for (int i = 1; i < NUM_PRDS; ++i) {
      double forward = 0.0;
      for (int j = 0; j < NUM_OBS; ++j) {
        forward += WEIGHTS1[i][j] * MULTICURVES.getSimplyCompoundForwardRate(INDEX1, FIXING_PERIOD_START_TIMES1[i][j], FIXING_PERIOD_END_TIMES1[i][j], FIX_ACC_FACTORS1[i][j]);
      }
      invFactor *= (1.0 + forward * DER1.getPaymentAccrualFactors()[i]);
    }
    final double pvExpected = DER1.getNotional() * MULTICURVES.getDiscountFactor(DER1.getCurrency(), DER1.getPaymentTime()) * (invFactor - 1.0);
    assertEquals("CouponIborAverageCompoundingDiscountingMethod: present value", pvExpected, pvComputed.getAmount(DER1.getCurrency()), TOLERANCE_PV);
    final MultipleCurrencyAmount pvWithCalc = PVDC.visitCouponIborAverageCompounding(DER1, MULTICURVES);
    assertEquals("CouponIborAverageCompoundingDiscountingMethod: present value", pvWithCalc.getAmount(DER1.getCurrency()),
        pvComputed.getAmount(DER1.getCurrency()), TOLERANCE_PV);
  }

  /**
   * pv for no fixed coupon
   */
  @Test
  public void presentValueNotFixedTest() {
    final MultipleCurrencyAmount pvComputed = METHOD.presentValue(DER2, MULTICURVES);
    double invFactor = 1.0;
    for (int i = 0; i < NUM_PRDS; ++i) {
      double forward = 0.0;
      for (int j = 0; j < NUM_OBS; ++j) {
        forward += WEIGHTS2[i][j] * MULTICURVES.getSimplyCompoundForwardRate(INDEX2, FIXING_PERIOD_START_TIMES2[i][j], FIXING_PERIOD_END_TIMES2[i][j], FIX_ACC_FACTORS2[i][j]);
      }
      invFactor *= (1.0 + forward * DER2.getPaymentAccrualFactors()[i]);
    }
    final double pvExpected = DER2.getNotional() * MULTICURVES.getDiscountFactor(DER2.getCurrency(), DER2.getPaymentTime()) * (invFactor - 1.0);
    assertEquals(pvExpected, pvComputed.getAmount(DER2.getCurrency()), TOLERANCE_PV);
    final MultipleCurrencyAmount pvWithCalc = PVDC.visitCouponIborAverageCompounding(DER2, MULTICURVES);
    assertEquals(pvWithCalc.getAmount(DER2.getCurrency()), pvComputed.getAmount(DER2.getCurrency()), EPS * Math.abs(pvExpected));
  }

  /**
   * pv for no fixed coupon with different numbers of fixing dates
   */
  @Test
  public void presentValueNotFixedNoSquareTest() {
    final MultipleCurrencyAmount pvComputed = METHOD.presentValue(DER3, MULTICURVES);
    double invFactor = DER3.getInvestmentFactor();
    double fwd = DER3.getAmountAccrued();
    for (int j = 0; j < DER3.getFixingPeriodAccrualFactor()[0].length; ++j) {
      fwd += WEIGHTS_3[0][j] * MULTICURVES.getSimplyCompoundForwardRate(INDEX1, DER3.getFixingPeriodStartTime()[0][j],
          DER3.getFixingPeriodEndTime()[0][j], DER3.getFixingPeriodAccrualFactor()[0][j]);
    }
    invFactor *= (1.0 + fwd * DER3.getPaymentAccrualFactors()[0]);
    for (int i = 1; i < DER3.getFixingPeriodAccrualFactor().length; ++i) {
      double forward = 0.0;
      for (int j = 0; j < DER3.getFixingPeriodAccrualFactor()[i].length; ++j) {
        forward += WEIGHTS_3[i][j] *
            MULTICURVES.getSimplyCompoundForwardRate(INDEX1, DER3.getFixingPeriodStartTime()[i][j], DER3.getFixingPeriodEndTime()[i][j], DER3.getFixingPeriodAccrualFactor()[i][j]);
      }
      invFactor *= (1.0 + forward * DER3.getPaymentAccrualFactors()[i]);
    }
    final double pvExpected = DER3.getNotional() * MULTICURVES.getDiscountFactor(DER3.getCurrency(), DER3.getPaymentTime()) * (invFactor - 1.0);
    assertEquals(pvExpected, pvComputed.getAmount(DER3.getCurrency()), EPS * Math.abs(pvExpected));
    final MultipleCurrencyAmount pvWithCalc = PVDC.visitCouponIborAverageCompounding(DER3, MULTICURVES);
    assertEquals(pvWithCalc.getAmount(DER3.getCurrency()), pvComputed.getAmount(DER3.getCurrency()), EPS * Math.abs(pvExpected));
  }

  @Test
  public void presentValueCurveSensitivityFiniteDifference() {
    final MultipleCurrencyParameterSensitivity senseCalc1 = PSC.calculateSensitivity(DER1, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity senseFd1 = PSC_DSC_FD.calculateSensitivity(DER1, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageCompoundingDiscountingMethod", senseCalc1, senseFd1, TOLERANCE_PV_DELTA);
    final MultipleCurrencyParameterSensitivity senseCalc2 = PSC.calculateSensitivity(DER2, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity senseFd2 = PSC_DSC_FD.calculateSensitivity(DER2, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageCompoundingDiscountingMethod", senseCalc2, senseFd2, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod1 = METHOD.presentValueCurveSensitivity(DER1, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator1 = DER1.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageCompoundingDiscountingMethod", pvcsMethod1, pvcsCalculator1, EPS);
    final MultipleCurrencyMulticurveSensitivity pvcsMethod2 = METHOD.presentValueCurveSensitivity(DER2, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator2 = DER2.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageCompoundingDiscountingMethod", pvcsMethod2, pvcsCalculator2, EPS);
  }

}
