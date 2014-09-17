/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.payment.CouponFixedFxResetDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedFxReset;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests the pricing method for fixed coupon with FX reset notional.
 */
@Test(groups = TestGroup.UNIT)
public class CouponFixedFxResetDiscountingMethodTest {
  
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2010, 1, 6);
  
  /** Details coupon. */
  private static final Currency CUR_REF = Currency.EUR;
  private static final Currency CUR_PAY = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final ZonedDateTime FX_FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime FX_DELIVERY_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final double ACCRUAL_FACTOR = 0.267;
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.04;
  private static final CouponFixedFxResetDefinition CPN_DEFINITION = 
      new CouponFixedFxResetDefinition(CUR_PAY, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, 
          NOTIONAL, RATE, CUR_REF, FX_FIXING_DATE, FX_DELIVERY_DATE);
  private static final CouponFixedFxReset CPN = CPN_DEFINITION.toDerivative(VALUATION_DATE);
  
  private static final MulticurveProviderDiscount MULTICURVES = 
      MulticurveProviderDiscountDataSets.createMulticurveEurUsd();  
  
  /** Methods and calculators. */
  private static final CouponFixedFxResetDiscountingMethod METHOD_CPN_FIXED_FX = 
      CouponFixedFxResetDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = 
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = 
      new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+0;
  
  @Test
  public void presentValue() {
    double fxToday = MULTICURVES.getFxRate(CUR_REF, CUR_PAY);
    double amount = CPN.paymentAmount(fxToday);
    double dfXTp = MULTICURVES.getDiscountFactor(CUR_PAY, CPN.getPaymentTime());
    double dfYT0 = MULTICURVES.getDiscountFactor(CUR_REF, CPN.getFxDeliveryTime());
    double dfXT0 = MULTICURVES.getDiscountFactor(CUR_PAY, CPN.getFxDeliveryTime());
    double pvExpected = amount * dfXTp * dfYT0 / dfXT0;
    MultipleCurrencyAmount pvComputed = METHOD_CPN_FIXED_FX.presentValue(CPN, MULTICURVES);
    assertTrue("CouponFixedFxResetDiscountingMethod: present value", pvComputed.size() == 1);
    assertEquals("CouponFixedFxResetDiscountingMethod: present value", 
        pvExpected, pvComputed.getAmount(CUR_PAY), TOLERANCE_PV);
  }
  
  @Test
  public void currencyExposure() {
    MultipleCurrencyAmount pvComputed = METHOD_CPN_FIXED_FX.presentValue(CPN, MULTICURVES);
    MultipleCurrencyAmount ceComputed = METHOD_CPN_FIXED_FX.currencyExposure(CPN, MULTICURVES);
    assertEquals("CouponFixedFxResetDiscountingMethod: present currencyExposure", 
        MULTICURVES.getFxRates().convert(ceComputed, CUR_PAY).getAmount(),
        MULTICURVES.getFxRates().convert(pvComputed, CUR_PAY).getAmount(), TOLERANCE_PV);
    assertTrue("CouponFixedFxResetDiscountingMethod: present currencyExposure", 
        Math.abs(ceComputed.getAmount(CUR_REF)) > TOLERANCE_PV);
    double amount = NOTIONAL * ACCRUAL_FACTOR * RATE;
    double dfXTp = MULTICURVES.getDiscountFactor(CUR_PAY, CPN.getPaymentTime());
    double dfYT0 = MULTICURVES.getDiscountFactor(CUR_REF, CPN.getFxDeliveryTime());
    double dfXT0 = MULTICURVES.getDiscountFactor(CUR_PAY, CPN.getFxDeliveryTime());
    double ceExpected = amount * dfXTp * dfYT0 / dfXT0;
    assertEquals("CouponFixedFxResetDiscountingMethod: present currencyExposure", 
        ceExpected, ceComputed.getAmount(CUR_REF), TOLERANCE_PV);
  }
  
  @Test
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity senseCalc1 = PSC.calculateSensitivity(CPN, MULTICURVES);
    final MultipleCurrencyParameterSensitivity senseFd1 = PSC_DSC_FD.calculateSensitivity(CPN, MULTICURVES);
    AssertSensitivityObjects.assertEquals("CouponIborAverageFixingDatesDiscountingMethod", 
         senseCalc1, senseFd1, TOLERANCE_PV_DELTA);    
  }
  
}
