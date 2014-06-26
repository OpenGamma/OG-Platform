/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFlatCompoundingSpread;
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
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
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
public class CouponIborFlatCompoundingSpreadDiscountingMethodTest {
  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();

  private static final Period TENOR = Period.ofMonths(1);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCounts.ACT_360;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.EUR;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM, "Index");

  private static final int NUM_PRDS = 6;
  private static final int NUM_OBS = 5;
  private static final int NUM_OBS_INI = 2;

  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 7, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 7, 6);
  // The above dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime[][] FIXING_DATES = new ZonedDateTime[NUM_PRDS][NUM_OBS];
  private static final double[][] WEIGHTS = new double[NUM_PRDS][NUM_OBS];
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        FIXING_DATES[j][i] = DateUtils.getUTCDate(2011, j + 1, 3 + 6 * i);
        WEIGHTS[j][i] = 2. * (NUM_OBS - i) / NUM_OBS / (NUM_OBS + 1.);
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
  static {
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        EXP_START_DATES[j][i] = ScheduleCalculator.getAdjustedDate(FIXING_DATES[j][i], INDEX.getSpotLag(), CALENDAR);
        EXP_END_DATES[j][i] = ScheduleCalculator.getAdjustedDate(EXP_START_DATES[j][i], INDEX.getTenor(), INDEX.getBusinessDayConvention(), CALENDAR, INDEX.isEndOfMonth());
      }
    }
  }

  //  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final ZonedDateTime REFERENCE_DATE = FIXING_DATES[0][2].plusDays(1);

  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, PAYMENT_DATE);
  private static final double[][] FIXING_TIMES = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_START_TIMES = new double[NUM_PRDS][NUM_OBS];
  private static final double[][] FIXING_PERIOD_END_TIMES = new double[NUM_PRDS][NUM_OBS];
  private static double[][] FIX_ACC_FACTORS = new double[NUM_PRDS][NUM_OBS];

  static {
    for (int i = 0; i < NUM_PRDS; ++i) {
      FIXING_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE, FIXING_DATES[i]);
      FIXING_PERIOD_START_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXP_START_DATES[i]);
      FIXING_PERIOD_END_TIMES[i] = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXP_END_DATES[i]);
    }
    for (int i = 0; i < NUM_OBS; ++i) {
      for (int j = 0; j < NUM_PRDS; ++j) {
        FIX_ACC_FACTORS[j][i] = INDEX.getDayCount().getDayCountFraction(EXP_START_DATES[j][i], EXP_END_DATES[j][i], CALENDAR);
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
  private static final CouponIborFlatCompoundingSpread DER1 = new CouponIborFlatCompoundingSpread(CUR, PAYMENT_TIME, ACCRUAL_FACTOR, NOTIONAL, ACCRUAL_FACTORS, IBOR_INDEXES[0], FIXING_TIMES, WEIGHTS,
      FIXING_PERIOD_START_TIMES, FIXING_PERIOD_END_TIMES, FIX_ACC_FACTORS, AMOUNT_ACC, RATE_FIXED, SPREAD);
  private static final CouponIborFlatCompoundingSpreadDiscountingMethod METHOD = CouponIborFlatCompoundingSpreadDiscountingMethod.getInstance();
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
    final MultipleCurrencyAmount pvWithCalc = PVDC.visitCouponIborFlatCompoundingSpread(DER1, MULTICURVES);
    assertEquals(pvWithCalc.getAmount(DER1.getCurrency()), pvComputed.getAmount(DER1.getCurrency()), EPS * Math.abs(pvExpected));
  }

  /**
   * 
   */
  @Test
  public void sensitivityFiniteDifferenceTest() {
    final MultipleCurrencyParameterSensitivity senseCalc = PSC.calculateSensitivity(DER1, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity senseFd = PSC_DSC_FD.calculateSensitivity(DER1, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborFlatCompoundingSpreadDiscountingMethod", senseCalc, senseFd, TOLERANCE_PV_DELTA);
  }

  /**
   * 
   */
  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD.presentValueCurveSensitivity(DER1, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = DER1.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborFlatCompoundingSpreadDiscountingMethod", pvcsMethod, pvcsCalculator, EPS);
  }

}
