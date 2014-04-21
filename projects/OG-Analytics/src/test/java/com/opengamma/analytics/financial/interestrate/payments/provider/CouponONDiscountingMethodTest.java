/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
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
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing methods for OIS coupon in the discounting method with data in MarketBundle.
 */
@Test(groups = TestGroup.UNIT)
public class CouponONDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwapFixedON GENERATOR_SWAP_EONIA = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final IndexON EONIA = MulticurveProviderDiscountDataSets.getIndexesON()[0];
  private static final Currency EUR = EONIA.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final Period TENOR = Period.ofMonths(3);
  private static final double NOTIONAL = 100000000; // 100m
  private static final CouponONDefinition CPN_OIS_DEFINITION = CouponONDefinition.from(EONIA, EFFECTIVE_DATE, TENOR, NOTIONAL, 2, GENERATOR_SWAP_EONIA.getBusinessDayConvention(),
      GENERATOR_SWAP_EONIA.isEndOfMonth(), CALENDAR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final CouponON CPN_OIS = CPN_OIS_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final CouponONDiscountingMethod METHOD_CPN_OIS = CouponONDiscountingMethod.getInstance();
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
    final MultipleCurrencyAmount pvComputed = METHOD_CPN_OIS.presentValue(CPN_OIS, MULTICURVES);
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EONIA, CPN_OIS.getFixingPeriodStartTime(), CPN_OIS.getFixingPeriodEndTime(), CPN_OIS.getFixingPeriodAccrualFactor());
    final double pvExpected = NOTIONAL * CPN_OIS.getFixingPeriodAccrualFactor() * forward * MULTICURVES.getDiscountFactor(CPN_OIS.getCurrency(), CPN_OIS.getPaymentTime());
    assertEquals("CouponOISDiscountingMarketMethod: present value", pvExpected, pvComputed.getAmount(EONIA.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void presentValueStarted() {
    final double fixing = 0.0015;
    final ZonedDateTimeDoubleTimeSeries TS_ON = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 5, 20), DateUtils.getUTCDate(2011, 5, 23) }, new double[] {
        0.0010, fixing });
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, 1, TARGET);
    final CouponON cpnOISStarted = (CouponON) CPN_OIS_DEFINITION.toDerivative(referenceDate, TS_ON);
    final double notionalAccrued = NOTIONAL * (1 + fixing * EONIA.getDayCount().getDayCountFraction(EFFECTIVE_DATE, referenceDate));
    assertEquals("CouponOISDiscountingMarketMethod: present value", notionalAccrued, cpnOISStarted.getNotionalAccrued(), TOLERANCE_PV);
    final MultipleCurrencyAmount pvComputed = METHOD_CPN_OIS.presentValue(cpnOISStarted, MULTICURVES);
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EONIA, cpnOISStarted.getFixingPeriodStartTime(), cpnOISStarted.getFixingPeriodEndTime(), cpnOISStarted.getFixingPeriodAccrualFactor());
    final double pvExpected = (cpnOISStarted.getNotionalAccrued() * (1 + cpnOISStarted.getFixingPeriodAccrualFactor() * forward) - NOTIONAL)
        * MULTICURVES.getDiscountFactor(cpnOISStarted.getCurrency(), cpnOISStarted.getPaymentTime());
    assertEquals("CouponOISDiscountingMarketMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_CPN_OIS.presentValue(CPN_OIS, MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = CPN_OIS.accept(PVDC, MULTICURVES);
    assertEquals("CouponFixedDiscountingMarketMethod: present value", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PSC.calculateSensitivity(CPN_OIS, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PSC_DSC_FD.calculateSensitivity(CPN_OIS, MULTICURVES);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_CPN_OIS.presentValueCurveSensitivity(CPN_OIS, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CPN_OIS.accept(PVCSDC, MULTICURVES);
    AssertSensivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_DELTA);
  }

}
