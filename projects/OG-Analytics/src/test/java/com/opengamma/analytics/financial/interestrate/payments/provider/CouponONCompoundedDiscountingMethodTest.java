/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
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
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing methods for ON Compounded coupon in the discounting method.
 */
@Test(groups = TestGroup.UNIT)
public class CouponONCompoundedDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedON GENERATOR_SWAP_EONIA = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON INDEX_ON = MulticurveProviderDiscountDataSets.getIndexesON()[0];
  private static final Currency EUR = INDEX_ON.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final Period TENOR = Period.ofMonths(3);
  private static final double NOTIONAL = 100000000; // 100m
  private static final CouponONCompoundedDefinition CPN_ON_COMPOUNDED_DEFINITION = CouponONCompoundedDefinition.from(INDEX_ON, EFFECTIVE_DATE, TENOR, NOTIONAL, 2,
      GENERATOR_SWAP_EONIA.getBusinessDayConvention(),
      GENERATOR_SWAP_EONIA.isEndOfMonth(), CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final CouponONCompounded CPN_ON_COMPOUNDED = CPN_ON_COMPOUNDED_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final CouponONCompoundedDiscountingMethod METHOD_CPN_ON = CouponONCompoundedDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_DELTA = 1.0E+2;

  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pvComputed = METHOD_CPN_ON.presentValue(CPN_ON_COMPOUNDED, MULTICURVES);
    double ratio = 1.0;
    for (int i = 0; i < CPN_ON_COMPOUNDED.getFixingPeriodAccrualFactors().length; i++) {
      ratio *= Math.pow(
          1 + MULTICURVES.getAnnuallyCompoundForwardRate(CPN_ON_COMPOUNDED.getIndex(), CPN_ON_COMPOUNDED.getFixingPeriodStartTimes()[i], CPN_ON_COMPOUNDED.getFixingPeriodEndTimes()[i],
              CPN_ON_COMPOUNDED.getFixingPeriodAccrualFactors()[i]),
          CPN_ON_COMPOUNDED.getFixingPeriodAccrualFactors()[i]);
    }
    final double df = MULTICURVES.getDiscountFactor(CPN_ON_COMPOUNDED.getCurrency(), CPN_ON_COMPOUNDED.getPaymentTime());
    final double pvExpected = df * CPN_ON_COMPOUNDED.getNotionalAccrued() * ratio;
    assertEquals("CouponONCompoundedDiscountingMethod: present value", pvExpected, pvComputed.getAmount(INDEX_ON.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void presentValueStarted() {
    final double fixing = 0.0015;
    final ZonedDateTimeDoubleTimeSeries TS_ON = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 5, 20), DateUtils.getUTCDate(2011, 5, 23) }, new double[] {
      0.0010, fixing });
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, 1, TARGET);
    final CouponONCompounded cpnONCompoundedStarted = (CouponONCompounded) CPN_ON_COMPOUNDED_DEFINITION.toDerivative(referenceDate, TS_ON);
    final double notionalAccrued = NOTIONAL * Math.pow(1 + fixing, CPN_ON_COMPOUNDED_DEFINITION.getFixingPeriodAccrualFactors()[0]);
    assertEquals("CouponONCompoundedDiscountingMethod: present value", notionalAccrued, cpnONCompoundedStarted.getNotionalAccrued(), TOLERANCE_PV);
    final MultipleCurrencyAmount pvComputed = METHOD_CPN_ON.presentValue(cpnONCompoundedStarted, MULTICURVES);
    double ratio = 1.0;
    for (int i = 0; i < cpnONCompoundedStarted.getFixingPeriodAccrualFactors().length; i++) {
      ratio *= Math.pow(
          1 + MULTICURVES.getAnnuallyCompoundForwardRate(cpnONCompoundedStarted.getIndex(), cpnONCompoundedStarted.getFixingPeriodStartTimes()[i], cpnONCompoundedStarted.getFixingPeriodEndTimes()[i],
              cpnONCompoundedStarted.getFixingPeriodAccrualFactors()[i]), cpnONCompoundedStarted.getFixingPeriodAccrualFactors()[i]);
    }
    final double df = MULTICURVES.getDiscountFactor(cpnONCompoundedStarted.getCurrency(), cpnONCompoundedStarted.getPaymentTime());
    final double pvExpected = cpnONCompoundedStarted.getNotionalAccrued() * ratio * df;
    assertEquals("CouponONCompoundedDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_CPN_ON.presentValue(CPN_ON_COMPOUNDED, MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = CPN_ON_COMPOUNDED.accept(PVDC, MULTICURVES);
    assertEquals("CouponONCompoundedDiscountingMethod: present value", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PSC.calculateSensitivity(CPN_ON_COMPOUNDED, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PSC_DSC_FD.calculateSensitivity(CPN_ON_COMPOUNDED, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponONCompoundedDiscountingMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_CPN_ON.presentValueCurveSensitivity(CPN_ON_COMPOUNDED, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CPN_ON_COMPOUNDED.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_DELTA);
  }

}
