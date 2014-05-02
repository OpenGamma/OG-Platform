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

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueMarketQuoteSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing and sensitivities of Ibor coupon with spread in the discounting method.
 */
@Test(groups = TestGroup.UNIT)
public class CouponIborSpreadDiscountingMethodTest {

  private static final MulticurveProviderDiscount PROVIDER = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 8, 31);
  private static final Period START_TENOR = Period.ofMonths(6);
  private static final ZonedDateTime START_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, START_TENOR, EURIBOR3M, CALENDAR);
  private static final ZonedDateTime END_DATE = ScheduleCalculator.getAdjustedDate(START_DATE, EURIBOR3M, CALENDAR);
  private static final double NOTIONAL = -123000000;
  private static final double ACCURAL = 0.25;
  private static final double SPREAD = 0.0010;

  private static final CouponIborSpreadDefinition CPN_IBOR_SPREAD_DEFINITION = CouponIborSpreadDefinition.from(START_DATE, END_DATE, ACCURAL, NOTIONAL, EURIBOR3M, SPREAD, CALENDAR);
  private static final CouponIborDefinition CPN_IBOR_DEFINITION = CouponIborDefinition.from(START_DATE, END_DATE, ACCURAL, NOTIONAL, EURIBOR3M, CALENDAR);
  private static final CouponFixedDefinition CPN_FIXED_DEFINITION = CouponFixedDefinition.from(CPN_IBOR_DEFINITION, SPREAD);

  private static final CouponIborSpread CPN_IBOR_SPREAD = (CouponIborSpread) CPN_IBOR_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponIbor CPN_IBOR = (CouponIbor) CPN_IBOR_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final CouponFixed CPN_FIXED = CPN_FIXED_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingMethod.getInstance();
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final CouponFixedDiscountingMethod METHOD_FIXED = CouponFixedDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityDiscountingCalculator PVMQSDC = PresentValueMarketQuoteSensitivityDiscountingCalculator.getInstance();

  private static final double SHIFT = 1.0E-6;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.

  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pvComputed = METHOD_CPN_IBOR_SPREAD.presentValue(CPN_IBOR_SPREAD, PROVIDER);
    final double forward = PROVIDER.getSimplyCompoundForwardRate(EURIBOR3M, CPN_IBOR_SPREAD.getFixingPeriodStartTime(), CPN_IBOR_SPREAD.getFixingPeriodEndTime(),
        CPN_IBOR_SPREAD.getFixingAccrualFactor());
    final double pv = NOTIONAL * CPN_IBOR_SPREAD.getPaymentYearFraction() * (forward + SPREAD) * PROVIDER.getDiscountFactor(CPN_IBOR_SPREAD.getCurrency(), CPN_IBOR_SPREAD.getPaymentTime());
    final CurrencyAmount pvExpected = CurrencyAmount.of(EURIBOR3M.getCurrency(), pv);
    assertEquals("CouponIborSpreadDiscountingMethod: present value", pvExpected.getAmount(), pvComputed.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pvIbor = METHOD_CPN_IBOR.presentValue(CPN_IBOR, PROVIDER);
    final MultipleCurrencyAmount pvFixed = METHOD_FIXED.presentValue(CPN_FIXED, PROVIDER);
    assertEquals("CouponIborSpreadDiscountingMethod: present value", pvIbor.plus(pvFixed).getAmount(EUR), pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueNoSpreadPositivNotional() {
    final MultipleCurrencyAmount pvComputed = METHOD_CPN_IBOR_SPREAD.presentValueNoSpreadPositiveNotional(CPN_IBOR_SPREAD, PROVIDER);
    final double forward = PROVIDER.getSimplyCompoundForwardRate(EURIBOR3M, CPN_IBOR_SPREAD.getFixingPeriodStartTime(), CPN_IBOR_SPREAD.getFixingPeriodEndTime(),
        CPN_IBOR_SPREAD.getFixingAccrualFactor());
    final double pvExpected = -NOTIONAL * CPN_IBOR_SPREAD.getPaymentYearFraction() * forward * PROVIDER.getDiscountFactor(CPN_IBOR_SPREAD.getCurrency(), CPN_IBOR_SPREAD.getPaymentTime());
    assertEquals("CouponIborSpreadDiscountingMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_CPN_IBOR_SPREAD.presentValue(CPN_IBOR_SPREAD, PROVIDER);
    final MultipleCurrencyAmount pvCalculator = CPN_IBOR_SPREAD.accept(PVDC, PROVIDER);
    assertEquals("CouponFixedDiscountingMarketMethod: present value", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivity() {
    MultipleCurrencyMulticurveSensitivity pvcsComputed = METHOD_CPN_IBOR_SPREAD.presentValueCurveSensitivity(CPN_IBOR_SPREAD, PROVIDER);
    pvcsComputed = pvcsComputed.cleaned();
    final MultipleCurrencyMulticurveSensitivity pvcsIbor = METHOD_CPN_IBOR.presentValueCurveSensitivity(CPN_IBOR, PROVIDER);
    final MultipleCurrencyMulticurveSensitivity pvcsFixed = METHOD_FIXED.presentValueCurveSensitivity(CPN_FIXED, PROVIDER);
    final MultipleCurrencyMulticurveSensitivity pvcsExpected = pvcsIbor.plus(pvcsFixed).cleaned();
    AssertSensitivityObjects.assertEquals("CouponIborSpreadDiscountingMethod: present value curve sensitivity", pvcsExpected, pvcsComputed, TOLERANCE_PV);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_CPN_IBOR_SPREAD.presentValueCurveSensitivity(CPN_IBOR_SPREAD, PROVIDER);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = CPN_IBOR_SPREAD.accept(PVCSDC, PROVIDER);
    AssertSensitivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests the sensitivity of the present value to the change of the market quote (i.e. the spread).
   */
  public void presentValueMarketQuoteSensitivity() {
    final CouponIborSpreadDefinition cpnSpreadPDefinition = CouponIborSpreadDefinition.from(START_DATE, END_DATE, ACCURAL, NOTIONAL, EURIBOR3M, SPREAD + SHIFT, CALENDAR);
    final CouponIborSpreadDefinition cpnSpreadMDefinition = CouponIborSpreadDefinition.from(START_DATE, END_DATE, ACCURAL, NOTIONAL, EURIBOR3M, SPREAD - SHIFT, CALENDAR);
    final CouponIborSpread cpnSpreadP = (CouponIborSpread) cpnSpreadPDefinition.toDerivative(REFERENCE_DATE);
    final CouponIborSpread cpnSpreadM = (CouponIborSpread) cpnSpreadMDefinition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvP = cpnSpreadP.accept(PVDC, PROVIDER);
    final MultipleCurrencyAmount pvM = cpnSpreadM.accept(PVDC, PROVIDER);
    final double mqsExpected = (pvP.getAmount(EUR) - pvM.getAmount(EUR)) / (2 * SHIFT);
    final double mqsComputed = CPN_IBOR_SPREAD.accept(PVMQSDC, PROVIDER);
    assertEquals("CouponIborCompoundingFlatSpreadDiscountingMethod: Present value market quote sensitivity", mqsExpected, mqsComputed, TOLERANCE_PV_DELTA);
  }

}
