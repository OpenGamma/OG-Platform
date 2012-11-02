/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.interestrate.market.description.MultipleCurrencyCurveSensitivityMarket;
import com.opengamma.analytics.financial.interestrate.market.description.ProviderDiscountDataSets;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.provider.calculator.PresentValueCurveSensitivityDiscountingProviderCalculator;
import com.opengamma.analytics.financial.provider.calculator.PresentValueDiscountingProviderCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing methods for Ibor coupon in the discounting method with data in MarketBundle.
 */
public class CouponIborDiscountingProviderMethodTest {

  private static final IborIndex[] IBOR_INDEXES = ProviderDiscountDataSets.getIndexesIbor();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();

  private static final DayCount DAY_COUNT_COUPON = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 8, 22);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final CouponIborDefinition CPN_IBOR_DEFINITION = CouponIborDefinition.from(ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, EURIBOR3M);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);
  private static final String[] NOT_USED = new String[] {"Not used 1", "not used 2"};
  private static final CouponIbor CPN_IBOR = (CouponIbor) CPN_IBOR_DEFINITION.toDerivative(REFERENCE_DATE, NOT_USED);

  private static final CouponIborDiscountingProviderMethod METHOD_CPN_IBOR = CouponIborDiscountingProviderMethod.getInstance();
  private static final PresentValueDiscountingProviderCalculator PVDC = PresentValueDiscountingProviderCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingProviderCalculator PVCSDC = PresentValueCurveSensitivityDiscountingProviderCalculator.getInstance();

  private static final MulticurveProviderDiscount PROVIDER = ProviderDiscountDataSets.createProvider3();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test
  public void presentValueMarketDiscount() {
    MultipleCurrencyAmount pvComputed = METHOD_CPN_IBOR.presentValue(CPN_IBOR, PROVIDER);
    double forward = PROVIDER.getForwardRate(EURIBOR3M, CPN_IBOR.getFixingPeriodStartTime(), CPN_IBOR.getFixingPeriodEndTime(), CPN_IBOR.getFixingAccrualFactor());
    double df = PROVIDER.getDiscountFactor(EURIBOR3M.getCurrency(), CPN_IBOR.getPaymentTime());
    double pvExpected = NOTIONAL * ACCRUAL_FACTOR * forward * df;
    assertEquals("CouponIborDiscountingMarketMethod: present value", pvExpected, pvComputed.getAmount(EURIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    MultipleCurrencyAmount pvMethod = METHOD_CPN_IBOR.presentValue(CPN_IBOR, PROVIDER);
    MultipleCurrencyAmount pvCalculator = PVDC.visit(CPN_IBOR, PROVIDER);
    assertEquals("CouponFixedDiscountingMarketMethod: present value", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  // Testing note: the presentValueMarketSensitivity is tested in ParameterSensitivityProviderCalculatorTest

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    MultipleCurrencyCurveSensitivityMarket pvcsMethod = METHOD_CPN_IBOR.presentValueCurveSensitivity(CPN_IBOR, PROVIDER);
    MultipleCurrencyCurveSensitivityMarket pvcsCalculator = PVCSDC.visit(CPN_IBOR, PROVIDER);
    AssertSensivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

}
