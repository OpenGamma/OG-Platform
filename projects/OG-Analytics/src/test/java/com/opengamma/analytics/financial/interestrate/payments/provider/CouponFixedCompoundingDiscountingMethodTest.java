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

import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the methods related to fixed compounded coupons.
 */
@Test(groups = TestGroup.UNIT)
public class CouponFixedCompoundingDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final Period Y1 = Period.ofYears(1);
  private static final double NOTIONAL = 123000000;
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2012, 8, 24);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2022, 8, 24);
  private static final double RATE = .05;
  private final static Currency CURRENCY = Currency.of("EUR");

  private final static CouponFixedCompoundingDefinition COUPON_DEFINITION = CouponFixedCompoundingDefinition.from(CURRENCY, ACCRUAL_START_DATE, ACCRUAL_END_DATE, NOTIONAL, Y1, RATE);

  private static final ZonedDateTime REFERENCE_DATE_BEFORE = DateUtils.getUTCDate(2012, 8, 7);
  private static final CouponFixedCompounding COUPON = COUPON_DEFINITION.toDerivative(REFERENCE_DATE_BEFORE);
  private static final CouponFixedCompoundingDiscountingMethod METHOD_COUPON = CouponFixedCompoundingDiscountingMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final double SHIFT = 1.0E-8;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  @Test
  public void presentValueMarketDiscount() {
    final MultipleCurrencyAmount pvComputed = METHOD_COUPON.presentValue(COUPON, MULTICURVES);
    final int nbSubPeriod = COUPON.getPaymentAccrualFactors().length;
    double notionalAccrued = COUPON.getNotional();
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {

      notionalAccrued *= 1 + COUPON.getPaymentAccrualFactors()[loopsub] * COUPON.getFixedRate();
    }

    final double df = MULTICURVES.getDiscountFactor(COUPON.getCurrency(), COUPON.getPaymentTime());
    final double pvExpected = df * (notionalAccrued - COUPON.getNotional());
    assertEquals("CouponIborDiscountingMarketMethod: present value", pvExpected, pvComputed.getAmount(COUPON.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsAnnuityExact = PSC.calculateSensitivity(COUPON, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsAnnuityFD = PSC_DSC_FD.calculateSensitivity(COUPON, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponFixedCompoundingDiscountingMethod: presentValueCurveSensitivity ", pvpsAnnuityExact, pvpsAnnuityFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_COUPON.presentValueCurveSensitivity(COUPON, MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = COUPON.accept(PVCSDC, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }
}
