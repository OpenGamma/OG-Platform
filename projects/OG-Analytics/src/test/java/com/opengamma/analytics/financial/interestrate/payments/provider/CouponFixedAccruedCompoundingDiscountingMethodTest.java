/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
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
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the methods related to fixed accrued compounding coupons.
 */
@Test(groups = TestGroup.UNIT)
public class CouponFixedAccruedCompoundingDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final IborIndex USDLIBOR1M = MASTER_IBOR.getIndex("USDLIBOR1M");

  private static final Period TENOR_3M = Period.ofMonths(3);
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final double NOTIONAL = 123454321;
  private static final DayCount DAY_COUNT = DayCounts.BUSINESS_252;

  private static final ZonedDateTime ACCRUAL_END_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, TENOR_3M, NYC);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 17);

  private static final double PAYMENT_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, ACCRUAL_END_DATE);
  private static final double PAYMENT_ACCRUAL_FACTOR = DAY_COUNT.getDayCountFraction(REFERENCE_DATE, ACCRUAL_END_DATE, NYC);
  private static final double FIXED_RATE = .02;

  private static final CouponFixedAccruedCompounding CPN_REC = new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXED_RATE);
  private static final CouponFixedAccruedCompounding CPN_REC_WITH_ACCRUAL_DATES = new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
      FIXED_RATE, START_DATE, ACCRUAL_END_DATE);
  private static final CouponFixedAccruedCompounding CPN_PAY = new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL, FIXED_RATE);
  private static final CouponFixedAccruedCompounding CPN_PAY_WITH_ACCRUAL_DATES = new CouponFixedAccruedCompounding(USDLIBOR1M.getCurrency(), PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, -NOTIONAL,
      FIXED_RATE, START_DATE, ACCRUAL_END_DATE);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  private static final CouponFixedAccruedCompoundingDiscountingMethod METHOD = CouponFixedAccruedCompoundingDiscountingMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final double SHIFT = 1.0E-8;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  @Test
  /**
   * Tests the present value of fixed accrued compounding coupons.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pvRecComputed = METHOD.presentValue(CPN_REC, MULTICURVES);
    final double pvExpected = CPN_REC.getAmount() * MULTICURVES.getDiscountFactor(CPN_REC.getCurrency(), CPN_REC.getPaymentTime());
    assertEquals("CouponFixedAccruedCompounding: Present value by discounting", pvExpected, pvRecComputed.getAmount(CPN_REC.getCurrency()), TOLERANCE_PV);
    final double pvPayExpected = CPN_PAY.getAmount() * MULTICURVES.getDiscountFactor(CPN_PAY.getCurrency(), CPN_PAY.getPaymentTime());
    assertEquals("CouponFixedAccruedCompounding: Present value by discounting", pvPayExpected, -pvRecComputed.getAmount(CPN_REC.getCurrency()), TOLERANCE_PV);
    final CurrencyAmount pvPosPayComputed = METHOD.presentValuePositiveNotional(CPN_REC, MULTICURVES);
    assertEquals("CouponFixedAccruedCompounding: Present value by discounting", pvRecComputed.getAmount(CPN_REC.getCurrency()), pvPosPayComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value curve sensitivity to parallel curve movements of fixed accrued compounding coupons.
   */
  public void presentValueParallelCurveSensitivity() {
    final MultipleCurrencyMulticurveSensitivity pvpcsComputed = METHOD.presentValueCurveSensitivity(CPN_PAY, MULTICURVES);
    final double pvpcsExpectedDouble = -CPN_PAY.getPaymentTime() * CPN_PAY.getAmount() * MULTICURVES.getDiscountFactor(CPN_PAY.getCurrency(), CPN_PAY.getPaymentTime());
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final DoublesPair s = DoublesPair.of(CPN_PAY.getPaymentTime(), pvpcsExpectedDouble);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(s);
    mapDsc.put(MULTICURVES.getName(CPN_PAY.getCurrency()), list);
    MultipleCurrencyMulticurveSensitivity pvpcsExpected = new MultipleCurrencyMulticurveSensitivity();
    pvpcsExpected = pvpcsExpected.plus(CPN_PAY.getCurrency(), MulticurveSensitivity.ofYieldDiscounting(mapDsc));
    AssertSensitivityObjects.assertEquals("CouponFixedAccruedCompounding: Present value parallel curve sensitivity by discounting", pvpcsExpected, pvpcsComputed, 1.0E-2);
  }

  @Test
  /**
   * Tests the present value curve sensitivity to parallel curve movements of fixed accrued compounding  coupons.
   */
  public void presentValueParallelCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvpcsMethod = METHOD.presentValueCurveSensitivity(CPN_PAY, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvpcsCalculator = CPN_PAY.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponFixed: Present value parallel curve sensitivity by discounting", pvpcsMethod, pvpcsCalculator, 1.0E-5);
  }

  @Test
  /**
   * Tests the present value curve sensitivity against finite difference of fixed accrued compounding coupons.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsAnnuityExact = PSC.calculateSensitivity(CPN_PAY, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsAnnuityFD = PSC_DSC_FD.calculateSensitivity(CPN_PAY, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponFixedCompoundingDiscountingMethod: presentValueCurveSensitivity ", pvpsAnnuityExact, pvpsAnnuityFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the present value of fixed accrued compounding coupons.
   */
  public void presentValueWithAccrualDates() {
    final MultipleCurrencyAmount pvRecComputed = METHOD.presentValue(CPN_REC_WITH_ACCRUAL_DATES, MULTICURVES);
    final double pvExpected = CPN_REC_WITH_ACCRUAL_DATES.getAmount() * MULTICURVES.getDiscountFactor(CPN_REC_WITH_ACCRUAL_DATES.getCurrency(), CPN_REC_WITH_ACCRUAL_DATES.getPaymentTime());
    assertEquals("CouponFixedAccruedCompounding: Present value by discounting", pvExpected, pvRecComputed.getAmount(CPN_REC_WITH_ACCRUAL_DATES.getCurrency()), TOLERANCE_PV);
    final double pvPayExpected = CPN_PAY_WITH_ACCRUAL_DATES.getAmount() * MULTICURVES.getDiscountFactor(CPN_PAY_WITH_ACCRUAL_DATES.getCurrency(), CPN_PAY_WITH_ACCRUAL_DATES.getPaymentTime());
    assertEquals("CouponFixedAccruedCompounding: Present value by discounting", pvPayExpected, -pvRecComputed.getAmount(CPN_REC_WITH_ACCRUAL_DATES.getCurrency()), TOLERANCE_PV);
    final CurrencyAmount pvPosPayComputed = METHOD.presentValuePositiveNotional(CPN_REC_WITH_ACCRUAL_DATES, MULTICURVES);
    assertEquals("CouponFixedAccruedCompounding: Present value by discounting", pvRecComputed.getAmount(CPN_REC_WITH_ACCRUAL_DATES.getCurrency()), pvPosPayComputed.getAmount(), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value curve sensitivity to parallel curve movements of fixed accrued compounding coupons.
   */
  public void presentValueParallelCurveSensitivityWithAccrualDates() {
    final MultipleCurrencyMulticurveSensitivity pvpcsComputed = METHOD.presentValueCurveSensitivity(CPN_PAY_WITH_ACCRUAL_DATES, MULTICURVES);
    final double pvpcsExpectedDouble = -CPN_PAY_WITH_ACCRUAL_DATES.getPaymentTime() * CPN_PAY_WITH_ACCRUAL_DATES.getAmount() *
        MULTICURVES.getDiscountFactor(CPN_PAY_WITH_ACCRUAL_DATES.getCurrency(), CPN_PAY_WITH_ACCRUAL_DATES.getPaymentTime());
    final Map<String, List<DoublesPair>> mapDsc = new HashMap<>();
    final DoublesPair s = DoublesPair.of(CPN_PAY_WITH_ACCRUAL_DATES.getPaymentTime(), pvpcsExpectedDouble);
    final List<DoublesPair> list = new ArrayList<>();
    list.add(s);
    mapDsc.put(MULTICURVES.getName(CPN_PAY_WITH_ACCRUAL_DATES.getCurrency()), list);
    MultipleCurrencyMulticurveSensitivity pvpcsExpected = new MultipleCurrencyMulticurveSensitivity();
    pvpcsExpected = pvpcsExpected.plus(CPN_PAY_WITH_ACCRUAL_DATES.getCurrency(), MulticurveSensitivity.ofYieldDiscounting(mapDsc));
    AssertSensitivityObjects.assertEquals("CouponFixedAccruedCompounding: Present value parallel curve sensitivity by discounting", pvpcsExpected, pvpcsComputed, 1.0E-2);
  }

  @Test
  /**
   * Tests the present value curve sensitivity to parallel curve movements of fixed accrued compounding  coupons.
   */
  public void presentValueParallelCurveSensitivityMethodVsCalculatorWithAccrualDates() {
    final MultipleCurrencyMulticurveSensitivity pvpcsMethod = METHOD.presentValueCurveSensitivity(CPN_PAY_WITH_ACCRUAL_DATES, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvpcsCalculator = CPN_PAY_WITH_ACCRUAL_DATES.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponFixed: Present value parallel curve sensitivity by discounting", pvpcsMethod, pvpcsCalculator, 1.0E-5);
  }

  @Test
  /**
   * Tests the present value curve sensitivity against finite difference of fixed accrued compounding coupons.
   */
  public void presentValueCurveSensitivityWithAccrualDates() {
    final MultipleCurrencyParameterSensitivity pvpsAnnuityExact = PSC.calculateSensitivity(CPN_PAY_WITH_ACCRUAL_DATES, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsAnnuityFD = PSC_DSC_FD.calculateSensitivity(CPN_PAY_WITH_ACCRUAL_DATES, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponFixedCompoundingDiscountingMethod: presentValueCurveSensitivity ", pvpsAnnuityExact, pvpsAnnuityFD, TOLERANCE_PV_DELTA);
  }

}
