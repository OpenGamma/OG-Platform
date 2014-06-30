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
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverageFixingDatesCompoundingFlatSpread;
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
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class CouponIborAverageFixingDatesCompoundingFlatSpreadDiscountingMethodTest {
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();

  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = IBOR_INDEXES[0].getDayCount();
  private static final Currency CUR1 = Currency.EUR;
  private static final Currency CUR2 = Currency.USD;

  private static final int NUM_PRDS = 6;
  private static final int NUM_OBS = 5;
  private static final int NUM_OBS_INI = 2;

  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 7, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 7, 6);
  // The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime[][] FIXING_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static final double[][] WEIGHTS = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] WEIGHTS_ORG = new double[NUM_PRDS][NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        FIXING_DATES[j][i] = DateUtils.getUTCDate(2011, j + 1, 3 + 6 * i);
        WEIGHTS[j][i] = 2. * (NUM_OBS - i) / NUM_OBS / (NUM_OBS + 1.);
        WEIGHTS_ORG[j][i] = WEIGHTS[j][i];
      }
    }
  }

  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double[] ACCRUAL_FACTORS = new double[NUM_PRDS];
  static {
    Arrays.fill(ACCRUAL_FACTORS, ACCRUAL_FACTOR / NUM_PRDS);
  }
  private static final double NOTIONAL = 1000000;
  private static final double SPREAD = 0.02;

  private static ZonedDateTime[][] EXP_START_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static ZonedDateTime[][] EXP_END_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static ZonedDateTime[][] EXP_START_DATES_ORG = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static ZonedDateTime[][] EXP_END_DATES_ORG = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        EXP_START_DATES[j][i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[j][i], IBOR_INDEXES[0].getSpotLag(), CALENDAR);
        EXP_END_DATES[j][i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES[j][i], IBOR_INDEXES[0].getTenor(), IBOR_INDEXES[0].getBusinessDayConvention(), CALENDAR,
            IBOR_INDEXES[0].isEndOfMonth());
        EXP_START_DATES_ORG[j][i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[j][i], IBOR_INDEXES[2].getSpotLag(), CALENDAR);
        EXP_END_DATES_ORG[j][i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES[j][i], IBOR_INDEXES[2].getTenor(), IBOR_INDEXES[2].getBusinessDayConvention(), CALENDAR,
            IBOR_INDEXES[2].isEndOfMonth());
      }
    }
  }

  private static final ZonedDateTime REFERENCE_DATE1 = FIXING_DATES[0][2].plusDays(1);
  private static final ZonedDateTime REFERENCE_DATE2 = DateUtils.getUTCDate(2010, 12, 27);

  private static final double PAYMENT_TIME1 = TimeCalculator.getTimeBetween(REFERENCE_DATE1, PAYMENT_DATE);
  private static final double PAYMENT_TIME2 = TimeCalculator.getTimeBetween(REFERENCE_DATE2, PAYMENT_DATE);
  private static final double[][] FIXING_TIMES = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_START_TIMES = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_END_TIMES = new double[NUM_PRDS][NUM_OBS];
  private static double[][] FIX_ACC_FACTORS = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_TIMES_ORG = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_START_TIMES_ORG = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_END_TIMES_ORG = new double[NUM_PRDS][NUM_OBS];
  private static double[][] FIX_ACC_FACTORS_ORG = new double[NUM_PRDS][NUM_OBS];

  static {
    for (int i = 0; i < NUM_PRDS; ++i) {
      FIXING_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE1, FIXING_DATES[i]);
      FIXING_PERIOD_START_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE1, EXP_START_DATES[i]);
      FIXING_PERIOD_END_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE1, EXP_END_DATES[i]);
      FIXING_TIMES_ORG[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE2, FIXING_DATES[i]);
      FIXING_PERIOD_START_TIMES_ORG[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE2, EXP_START_DATES_ORG[i]);
      FIXING_PERIOD_END_TIMES_ORG[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE2, EXP_END_DATES_ORG[i]);
    }
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        FIX_ACC_FACTORS[j][i] = IBOR_INDEXES[0].getDayCount().getDayCountFraction(EXP_START_DATES[j][i], EXP_END_DATES[j][i], CALENDAR);
        FIX_ACC_FACTORS_ORG[j][i] = IBOR_INDEXES[2].getDayCount().getDayCountFraction(EXP_START_DATES_ORG[j][i], EXP_END_DATES_ORG[j][i], CALENDAR);
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
      FIXING_TIMES_INI[i] = FIXING_TIMES[0][NUM_OBS - NUM_OBS_INI + i];
      FIXING_PERIOD_START_TIMES_INI[i] = FIXING_PERIOD_START_TIMES[0][NUM_OBS - NUM_OBS_INI + i];
      FIXING_PERIOD_END_TIMES_INI[i] = FIXING_PERIOD_END_TIMES[0][NUM_OBS - NUM_OBS_INI + i];
      FIX_ACC_FACTORS_INI[i] = FIX_ACC_FACTORS[0][NUM_OBS - NUM_OBS_INI + i];
      WEIGHTS_INI[i] = WEIGHTS[0][NUM_OBS - NUM_OBS_INI + i];
    }
    FIXING_TIMES[0] = FIXING_TIMES_INI;
    FIXING_PERIOD_START_TIMES[0] = FIXING_PERIOD_START_TIMES_INI;
    FIXING_PERIOD_END_TIMES[0] = FIXING_PERIOD_END_TIMES_INI;
    FIX_ACC_FACTORS[0] = FIX_ACC_FACTORS_INI;
    WEIGHTS[0] = WEIGHTS_INI;
  }

  private static final double AMOUNT_ACC = 0.02;
  private static final double RATE_FIXED = 0.01;
  // Examples 1 & 2: same number of fixing for all periods
  private static final CouponIborAverageFixingDatesCompoundingFlatSpread DER1 = new CouponIborAverageFixingDatesCompoundingFlatSpread(CUR1, PAYMENT_TIME1, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, IBOR_INDEXES[0],
      FIXING_TIMES, WEIGHTS, FIXING_PERIOD_START_TIMES, FIXING_PERIOD_END_TIMES, FIX_ACC_FACTORS, AMOUNT_ACC, RATE_FIXED, SPREAD);
  private static final CouponIborAverageFixingDatesCompoundingFlatSpread DER2 = new CouponIborAverageFixingDatesCompoundingFlatSpread(CUR2, PAYMENT_TIME2, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, IBOR_INDEXES[2],
      FIXING_TIMES_ORG, WEIGHTS_ORG, FIXING_PERIOD_START_TIMES_ORG, FIXING_PERIOD_END_TIMES_ORG, FIX_ACC_FACTORS_ORG, 0., 0., SPREAD);

  // Example 3: different number of fixing in each subperiod
  private static final int NB_SUBPERIODS = 3;
  private static final ZonedDateTime[] ACCRUAL_START_DATE_SUB_3 = new ZonedDateTime[NB_SUBPERIODS + 1];
  private static final ZonedDateTime[][] FIXING_DATES_3 = new ZonedDateTime[NB_SUBPERIODS][];
  private static final double[][] WEIGHTS_3 = new double[NB_SUBPERIODS][];
  private static final double[] ACCRUAL_FACTORS_3 = new double[NB_SUBPERIODS];
  static {
    for (int loopsub = 0; loopsub <= NB_SUBPERIODS; loopsub++) {
      ACCRUAL_START_DATE_SUB_3[loopsub] = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, Period.ofMonths(loopsub), IBOR_INDEXES[0], CALENDAR);
    }
    for (int loopsub = 0; loopsub < NB_SUBPERIODS; loopsub++) {
      List<ZonedDateTime> listDates = new ArrayList<>();
      ZonedDateTime startFixPeriod = ACCRUAL_START_DATE_SUB_3[loopsub];
      listDates.add(ScheduleCalculator.getAdjustedDate(startFixPeriod, -IBOR_INDEXES[0].getSpotLag(), CALENDAR));
      startFixPeriod = ScheduleCalculator.getAdjustedDate(startFixPeriod, 1, CALENDAR);
      while (startFixPeriod.isBefore(ACCRUAL_START_DATE_SUB_3[loopsub + 1])) {
        listDates.add(ScheduleCalculator.getAdjustedDate(startFixPeriod, -IBOR_INDEXES[0].getSpotLag(), CALENDAR));
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
  private static final CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition DFN3 = new CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition(CUR1, ACCRUAL_START_DATE_SUB_3[3],
      ACCRUAL_START_DATE_SUB_3[0], ACCRUAL_START_DATE_SUB_3[3], ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS_3, IBOR_INDEXES[0], FIXING_DATES_3, WEIGHTS_3, CALENDAR, SPREAD);
  private static final CouponIborAverageFixingDatesCompoundingFlatSpread DER3 = DFN3.toDerivative(FIXING_DATES_3[0][0].minusDays(1));

  private static final CouponIborAverageFlatCompoundingSpreadDiscountingMethod METHOD = CouponIborAverageFlatCompoundingSpreadDiscountingMethod.getInstance();
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
  public void presentValueFixedTest() {
    final MultipleCurrencyAmount pvComputed = METHOD.presentValue(DER1, MULTICURVES);
    double acc = AMOUNT_ACC;
    final double[] fwds = new double[NUM_PRDS];
    Arrays.fill(fwds, 0.0);
    final double[] cpas = new double[NUM_PRDS];
    Arrays.fill(cpas, 0.0);
    fwds[0] += DER1.getRateFixed();
    for (int j = 0; j < NUM_OBS_INI; ++j) {
      fwds[0] += WEIGHTS[0][j] * MULTICURVES.getSimplyCompoundForwardRate(IBOR_INDEXES[0], FIXING_PERIOD_START_TIMES[0][j], FIXING_PERIOD_END_TIMES[0][j], FIX_ACC_FACTORS[0][j]);
    }
    for (int i = 1; i < NUM_PRDS; ++i) {
      for (int j = 0; j < NUM_OBS; ++j) {
        fwds[i] += WEIGHTS[i][j] * MULTICURVES.getSimplyCompoundForwardRate(IBOR_INDEXES[0], FIXING_PERIOD_START_TIMES[i][j], FIXING_PERIOD_END_TIMES[i][j], FIX_ACC_FACTORS[i][j]);
      }
    }
    cpas[0] = (fwds[0] + DER1.getSpread()) * DER1.getPaymentAccrualFactors()[0] + acc * fwds[0] * DER1.getPaymentAccrualFactors()[0];
    for (int i = 1; i < NUM_PRDS; ++i) {
      cpas[i] = (fwds[i] + DER1.getSpread()) * DER1.getPaymentAccrualFactors()[i];
      double sum = AMOUNT_ACC * DER1.getPaymentAccrualFactors()[i] * fwds[i];
      for (int k = 0; k < i; ++k) {
        sum += (cpas[k] * DER1.getPaymentAccrualFactors()[i] * fwds[i]);
      }
      cpas[i] += sum;
    }

    for (int i = 0; i < NUM_PRDS; ++i) {
      acc += cpas[i];
    }

    final double pvExpected = DER1.getNotional() * MULTICURVES.getDiscountFactor(DER1.getCurrency(), DER1.getPaymentTime()) * acc;
    assertEquals(pvExpected, pvComputed.getAmount(DER1.getCurrency()), EPS * Math.abs(pvExpected));
    final MultipleCurrencyAmount pvWithCalc = PVDC.visitCouponIborAverageFlatCompoundingSpread(DER1, MULTICURVES);
    assertEquals(pvWithCalc.getAmount(DER1.getCurrency()), pvComputed.getAmount(DER1.getCurrency()), EPS * Math.abs(pvExpected));
  }

  /**
   * 
   */
  @Test
  public void presentValueNotFixedTest() {
    final MultipleCurrencyAmount pvComputed = METHOD.presentValue(DER2, MULTICURVES);
    double acc = 0.0;
    final double[] fwds = new double[NUM_PRDS];
    Arrays.fill(fwds, 0.0);
    final double[] cpas = new double[NUM_PRDS];
    Arrays.fill(cpas, 0.0);
    for (int i = 0; i < NUM_PRDS; ++i) {
      for (int j = 0; j < NUM_OBS; ++j) {
        fwds[i] += WEIGHTS_ORG[i][j] * MULTICURVES.getSimplyCompoundForwardRate(IBOR_INDEXES[2], FIXING_PERIOD_START_TIMES_ORG[i][j], FIXING_PERIOD_END_TIMES_ORG[i][j], FIX_ACC_FACTORS_ORG[i][j]);
      }
    }
    for (int i = 0; i < NUM_PRDS; ++i) {
      cpas[i] = (fwds[i] + DER2.getSpread()) * DER2.getPaymentAccrualFactors()[i];
      double sum = 0.0;
      for (int k = 0; k < i; ++k) {
        sum += (cpas[k] * DER2.getPaymentAccrualFactors()[i] * fwds[i]);
      }
      cpas[i] += sum;
    }

    for (int i = 0; i < NUM_PRDS; ++i) {
      acc += cpas[i];
    }

    final double pvExpected = DER2.getNotional() * MULTICURVES.getDiscountFactor(DER2.getCurrency(), DER2.getPaymentTime()) * acc;
    assertEquals(pvExpected, pvComputed.getAmount(DER2.getCurrency()), EPS * Math.abs(pvExpected));
    final MultipleCurrencyAmount pvWithCalc = PVDC.visitCouponIborAverageFlatCompoundingSpread(DER2, MULTICURVES);
    assertEquals(pvWithCalc.getAmount(DER2.getCurrency()), pvComputed.getAmount(DER2.getCurrency()), EPS * Math.abs(pvExpected));
  }

  /**
   * pv for no fixed coupon with different numbers of fixing dates
   */
  @Test
  public void presentValueNotFixedNoSquareTest() {
    final MultipleCurrencyAmount pvComputed = METHOD.presentValue(DER3, MULTICURVES);
    double acc = 0.0;
    final double[] fwds = new double[DER3.getFixingPeriodAccrualFactor().length];
    Arrays.fill(fwds, 0.0);
    final double[] cpas = new double[DER3.getFixingPeriodAccrualFactor().length];
    Arrays.fill(cpas, 0.0);
    for (int i = 0; i < DER3.getFixingPeriodAccrualFactor().length; ++i) {
      for (int j = 0; j < DER3.getFixingPeriodAccrualFactor()[i].length; ++j) {
        fwds[i] += WEIGHTS_3[i][j] *
            MULTICURVES.getSimplyCompoundForwardRate(IBOR_INDEXES[0], DER3.getFixingPeriodStartTime()[i][j], DER3.getFixingPeriodEndTime()[i][j], DER3.getFixingPeriodAccrualFactor()[i][j]);
      }
    }
    for (int i = 0; i < DER3.getFixingPeriodAccrualFactor().length; ++i) {
      cpas[i] = (fwds[i] + DER3.getSpread()) * DER3.getPaymentAccrualFactors()[i];
      double sum = 0.0;
      for (int k = 0; k < i; ++k) {
        sum += (cpas[k] * DER3.getPaymentAccrualFactors()[i] * fwds[i]);
      }
      cpas[i] += sum;
    }

    for (int i = 0; i < DER3.getFixingPeriodAccrualFactor().length; ++i) {
      acc += cpas[i];
    }

    final double pvExpected = DER3.getNotional() * MULTICURVES.getDiscountFactor(DER3.getCurrency(), DER3.getPaymentTime()) * acc;
    assertEquals(pvExpected, pvComputed.getAmount(DER3.getCurrency()), EPS * Math.abs(pvExpected));
    final MultipleCurrencyAmount pvWithCalc = PVDC.visitCouponIborAverageFlatCompoundingSpread(DER3, MULTICURVES);
    assertEquals(pvWithCalc.getAmount(DER3.getCurrency()), pvComputed.getAmount(DER3.getCurrency()), EPS * Math.abs(pvExpected));
  }

  /**
   * 
   */
  @Test
  public void sensitivityFiniteDifferenceTest() {
    final MultipleCurrencyParameterSensitivity senseCalc1 = PSC.calculateSensitivity(DER1, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity senseFd1 = PSC_DSC_FD.calculateSensitivity(DER1, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageFlatCompoundingSpreadDiscountingMethod", senseCalc1, senseFd1, TOLERANCE_PV_DELTA);

    final MultipleCurrencyParameterSensitivity senseCalc2 = PSC.calculateSensitivity(DER2, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity senseFd2 = PSC_DSC_FD.calculateSensitivity(DER2, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageFlatCompoundingSpreadDiscountingMethod", senseCalc2, senseFd2, TOLERANCE_PV_DELTA);
  }

  /**
   * 
   */
  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod1 = METHOD.presentValueCurveSensitivity(DER1, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator1 = DER1.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageFlatCompoundingSpreadDiscountingMethod", pvcsMethod1, pvcsCalculator1, EPS);

    final MultipleCurrencyMulticurveSensitivity pvcsMethod2 = METHOD.presentValueCurveSensitivity(DER2, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator2 = DER2.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageFlatCompoundingSpreadDiscountingMethod", pvcsMethod2, pvcsCalculator2, EPS);
  }

}
