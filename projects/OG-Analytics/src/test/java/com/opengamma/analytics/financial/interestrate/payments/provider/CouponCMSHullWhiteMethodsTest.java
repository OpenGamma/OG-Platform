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

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.payment.CouponCMSDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponCMS;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of CMS coupons with Hull-White (extended Vasicek) model and different numerical methods.
 */
@Test(groups = TestGroup.UNIT)
public class CouponCMSHullWhiteMethodsTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final HullWhiteOneFactorPiecewiseConstantParameters HW_PARAMETERS = HullWhiteDataSets.createHullWhiteParameters();
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final Calendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();
  private static final HullWhiteOneFactorProviderDiscount HW_MULTICURVES = new HullWhiteOneFactorProviderDiscount(MULTICURVES, HW_PARAMETERS, EUR);

  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor GENERATOR_EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", TARGET);
  private static final Period TENOR_SWAP = Period.ofYears(10);
  private static final IndexSwap SWAP_EUR10Y = new IndexSwap(GENERATOR_EUR1YEURIBOR6M, TENOR_SWAP);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 17);

  // Coupon CMS: 6m fixing in advance (payment in arrears); ACT/360
  private static final Period TENOR_COUPON = Period.ofMonths(6);
  private static final Period TENOR_FIXING = Period.ofMonths(60);
  private static final DayCount ACT360 = DayCounts.ACT_360;
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, TENOR_FIXING, GENERATOR_EUR1YEURIBOR6M.getBusinessDayConvention(), TARGET,
      GENERATOR_EUR1YEURIBOR6M.isEndOfMonth());
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(FIXING_DATE, GENERATOR_EUR1YEURIBOR6M.getSpotLag(), TARGET);
  private static final ZonedDateTime PAYMENT_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, TENOR_COUPON, GENERATOR_EUR1YEURIBOR6M.getBusinessDayConvention(), TARGET,
      GENERATOR_EUR1YEURIBOR6M.isEndOfMonth());
  private static final double NOTIONAL = 100000000; //100m
  private static final double ACCRUAL_FACTOR = ACT360.getDayCountFraction(START_DATE, PAYMENT_DATE);
  private static final CouponCMSDefinition CPN_CMS_DEFINITION = CouponCMSDefinition.from(PAYMENT_DATE, START_DATE, PAYMENT_DATE, ACCRUAL_FACTOR, NOTIONAL, SWAP_EUR10Y, TARGET);

  private static final CouponCMS CPN_CMS = (CouponCMS) CPN_CMS_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final CouponCMSHullWhiteNumericalIntegrationMethod METHOD_NI = CouponCMSHullWhiteNumericalIntegrationMethod.getInstance();
  private static final CouponCMSHullWhiteApproximationMethod METHOD_APP = CouponCMSHullWhiteApproximationMethod.getInstance();
  private static final CouponCMSDiscountingMethod METHOD_DSC = CouponCMSDiscountingMethod.getInstance();

  private static final PresentValueHullWhiteCalculator PVHWC = PresentValueHullWhiteCalculator.getInstance();
  private static final PresentValueCurveSensitivityHullWhiteCalculator PVCSHWC = PresentValueCurveSensitivityHullWhiteCalculator.getInstance();
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<HullWhiteOneFactorProviderInterface> PS_HW_C = new ParameterSensitivityParameterCalculator<>(PVCSHWC);
  private static final ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator PS_HW_FDC = new ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(PVHWC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_APP = 5.0E+0;
  private static final double TOLERANCE_PV_DELTA = 1.0E+0; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  public void presentValueNumericalIntegration() {
    final MultipleCurrencyAmount pvNumericalIntegration = METHOD_NI.presentValue(CPN_CMS, HW_MULTICURVES);
    final double pvPrevious = 851848.400; // From previous run
    assertEquals("Coupon CMS - Hull-White - present value - numerical integration", pvPrevious, pvNumericalIntegration.getAmount(EUR), TOLERANCE_PV);
    // Comparison with non-adjusted figures: to have the right order of magnitude
    final MultipleCurrencyAmount pvDiscounting = METHOD_DSC.presentValue(CPN_CMS, MULTICURVES);
    assertEquals("Coupon CMS - Hull-White - present value - numerical integration", 1.0, pvDiscounting.getAmount(EUR) / pvNumericalIntegration.getAmount(EUR), 0.20);
  }

  @Test
  public void presentValueApproximation() {
    final MultipleCurrencyAmount pvNumericalIntegration = METHOD_NI.presentValue(CPN_CMS, HW_MULTICURVES);
    final MultipleCurrencyAmount pvApproximation = METHOD_APP.presentValue(CPN_CMS, HW_MULTICURVES);
    assertEquals("Coupon CMS - Hull-White - present value - approximation", pvApproximation.getAmount(EUR), pvNumericalIntegration.getAmount(EUR), TOLERANCE_PV_APP);
  }

  @Test
  /**
   * Tests the method against the present value calculator.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_APP.presentValue(CPN_CMS, HW_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = CPN_CMS.accept(PVHWC, HW_MULTICURVES);
    assertEquals("SwaptionPhysicalFixedIborSABRMethod: present value : method and calculator", pvMethod, pvCalculator);
  }

  @Test
  public void presentValueCurveSensitivityApproximation() {
    final MultipleCurrencyParameterSensitivity pvpsExact = PS_HW_C.calculateSensitivity(CPN_CMS, HW_MULTICURVES, HW_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsFD = PS_HW_FDC.calculateSensitivity(CPN_CMS, HW_MULTICURVES);
    AssertSensivityObjects.assertEquals("SwaptionPhysicalFixedIborSABRMethod: presentValueCurveSensitivity ", pvpsExact, pvpsFD, TOLERANCE_PV_DELTA);
  }

}
