/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.market.description.ProviderDiscountDataSets;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.provider.calculator.PresentValueCurveSensitivityDiscountingProviderCalculator;
import com.opengamma.analytics.financial.provider.calculator.PresentValueDiscountingProviderCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing and sensitivities of Ibor coupon with spread in the discounting method.
 */
public class CouponIborSpreadDiscountingProviderMethodTest {

  private static final MulticurveProviderDiscount PROVIDER = ProviderDiscountDataSets.createProvider3();
  private static final IborIndex[] IBOR_INDEXES = ProviderDiscountDataSets.getIndexesIbor();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 31);
  private static final Period START_TENOR = Period.ofMonths(6);
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, START_TENOR, EURIBOR3M);
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, EURIBOR3M);
  private static final double NOTIONAL = -123000000;
  private static final double ACCURAL = 0.25;
  private static final double SPREAD = 0.0010;

  private static final CouponIborSpreadDefinition CPN_IBOR_SPREAD_DEFINITION = CouponIborSpreadDefinition.from(START_DATE, END_DATE, ACCURAL, NOTIONAL, EURIBOR3M, SPREAD);
  private static final CouponIborDefinition CPN_IBOR_DEFINITION = CouponIborDefinition.from(START_DATE, END_DATE, ACCURAL, NOTIONAL, EURIBOR3M);
  private static final CouponFixedDefinition CPN_FIXED_DEFINITION = CouponFixedDefinition.from(CPN_IBOR_DEFINITION, SPREAD);

  public static final String NOT_USED = "Not used";
  public static final String[] NOT_USED_A = {NOT_USED, NOT_USED, NOT_USED};
  private static final CouponIborSpread CPN_IBOR_SPREAD = (CouponIborSpread) CPN_IBOR_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED_A);
  private static final CouponIbor CPN_IBOR = (CouponIbor) CPN_IBOR_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED_A);
  private static final CouponFixed CPN_FIXED = CPN_FIXED_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED_A);

  private static final CouponIborSpreadDiscountingProviderMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingProviderMethod.getInstance();
  private static final CouponIborDiscountingProviderMethod METHOD_CPN_IBOR = CouponIborDiscountingProviderMethod.getInstance();
  private static final CouponFixedDiscountingProviderMethod METHOD_FIXED = CouponFixedDiscountingProviderMethod.getInstance();
  private static final PresentValueDiscountingProviderCalculator PVDC = PresentValueDiscountingProviderCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingProviderCalculator PVCSDC = PresentValueCurveSensitivityDiscountingProviderCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  public void presentValue() {
    MultipleCurrencyAmount pvComputed = METHOD_CPN_IBOR_SPREAD.presentValue(CPN_IBOR_SPREAD, PROVIDER);
    double forward = PROVIDER.getForwardRate(EURIBOR3M, CPN_IBOR_SPREAD.getFixingPeriodStartTime(), CPN_IBOR_SPREAD.getFixingPeriodEndTime(), CPN_IBOR_SPREAD.getFixingAccrualFactor());
    double pv = NOTIONAL * CPN_IBOR_SPREAD.getPaymentYearFraction() * (forward + SPREAD) * PROVIDER.getDiscountFactor(CPN_IBOR_SPREAD.getCurrency(), CPN_IBOR_SPREAD.getPaymentTime());
    CurrencyAmount pvExpected = CurrencyAmount.of(EURIBOR3M.getCurrency(), pv);
    assertEquals("CouponIborSpreadDiscountingMethod: present value", pvExpected.getAmount(), pvComputed.getAmount(EUR), TOLERANCE_PV);
    MultipleCurrencyAmount pvIbor = METHOD_CPN_IBOR.presentValue(CPN_IBOR, PROVIDER);
    MultipleCurrencyAmount pvFixed = METHOD_FIXED.presentValue(CPN_FIXED, PROVIDER);
    assertEquals("CouponIborSpreadDiscountingMethod: present value", pvIbor.plus(pvFixed).getAmount(EUR), pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueNoSpreadPositivNotional() {
    MultipleCurrencyAmount pvComputed = METHOD_CPN_IBOR_SPREAD.presentValueNoSpreadPositiveNotional(CPN_IBOR_SPREAD, PROVIDER);
    double forward = PROVIDER.getForwardRate(EURIBOR3M, CPN_IBOR_SPREAD.getFixingPeriodStartTime(), CPN_IBOR_SPREAD.getFixingPeriodEndTime(), CPN_IBOR_SPREAD.getFixingAccrualFactor());
    double pvExpected = Math.abs(NOTIONAL) * CPN_IBOR_SPREAD.getPaymentYearFraction() * forward * PROVIDER.getDiscountFactor(CPN_IBOR_SPREAD.getCurrency(), CPN_IBOR_SPREAD.getPaymentTime());
    assertEquals("CouponIborSpreadDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    MultipleCurrencyAmount pvMethod = METHOD_CPN_IBOR_SPREAD.presentValue(CPN_IBOR_SPREAD, PROVIDER);
    MultipleCurrencyAmount pvCalculator = PVDC.visit(CPN_IBOR_SPREAD, PROVIDER);
    assertEquals("CouponFixedDiscountingMarketMethod: present value", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivity() {
    MultipleCurrencyCurveSensitivityMarket pvcsComputed = METHOD_CPN_IBOR_SPREAD.presentValueCurveSensitivity(CPN_IBOR_SPREAD, PROVIDER);
    pvcsComputed = pvcsComputed.cleaned();
    MultipleCurrencyCurveSensitivityMarket pvcsIbor = METHOD_CPN_IBOR.presentValueCurveSensitivity(CPN_IBOR, PROVIDER);
    MultipleCurrencyCurveSensitivityMarket pvcsFixed = METHOD_FIXED.presentValueCurveSensitivity(CPN_FIXED, PROVIDER);
    MultipleCurrencyCurveSensitivityMarket pvcsExpected = pvcsIbor.plus(pvcsFixed).cleaned();
    AssertSensivityObjects.assertEquals("CouponIborSpreadDiscountingMethod: present value curve sensitivity", pvcsExpected, pvcsComputed, TOLERANCE_PV);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    MultipleCurrencyCurveSensitivityMarket pvcsMethod = METHOD_CPN_IBOR_SPREAD.presentValueCurveSensitivity(CPN_IBOR_SPREAD, PROVIDER);
    MultipleCurrencyCurveSensitivityMarket pvcsCalculator = PVCSDC.visit(CPN_IBOR_SPREAD, PROVIDER);
    AssertSensivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

}
