/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.fra.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.datasets.DataSetsUSD20140122OnOisLibor3MIrs;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.SimpleParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the ForwardRateAgreement discounting method.
 */
@Test(groups = TestGroup.UNIT)
public class ForwardRateAgreementDiscountingMethodTest {

  private static final MulticurveProviderDiscount PROVIDER = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex USDLIBOR3M = INDEX_LIST[2];
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getUSDCalendar();
  private static final Currency CUR = USDLIBOR3M.getCurrency();
  // Dates : The dates are not standard but selected for insure correct testing.
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2011, 1, 3);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 6);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 4);
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 1, 7);
  private static final DayCount DAY_COUNT_PAYMENT = DayCounts.ACT_365;
  private static final double ACCRUAL_FACTOR_PAYMENT = DAY_COUNT_PAYMENT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double FRA_RATE = 0.05;
  private static final double NOTIONAL = 1000000; //1m
  // Coupon with specific payment and accrual dates.
  private static final ForwardRateAgreementDefinition FRA_DEFINITION = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL,
      FIXING_DATE, USDLIBOR3M, FRA_RATE, CALENDAR);
  // To derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 10, 9);

  private static final ForwardRateAgreement FRA = (ForwardRateAgreement) FRA_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final ForwardRateAgreementDiscountingProviderMethod FRA_METHOD = ForwardRateAgreementDiscountingProviderMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();
  private static final SimpleParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSPSC = new SimpleParameterSensitivityParameterCalculator<>(PSMQCSDC);
  private static final SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSMQCS_FDC = new SimpleParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PSMQDC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move.
  private static final double TOLERANCE_SPREAD_DELTA = 1.0E-8;

  @Test
  public void parRate() {
    final double forward = FRA_METHOD.parRate(FRA, PROVIDER);
    final double forwardExpected = PROVIDER.getForwardRate(USDLIBOR3M, FRA.getFixingPeriodStartTime(), FRA.getFixingPeriodEndTime(), FRA.getFixingYearFraction());
    assertEquals("FRA discounting: par rate", forwardExpected, forward, TOLERANCE_RATE);
  }

  @Test
  public void presentValue() {
    final double forward = FRA_METHOD.parRate(FRA, PROVIDER);
    final double dfSettle = PROVIDER.getDiscountFactor(CUR, FRA.getPaymentTime());
    final double expectedPv = FRA.getNotional() * dfSettle * FRA.getPaymentYearFraction() * (forward - FRA_RATE) / (1 + FRA.getPaymentYearFraction() * forward);
    final MultipleCurrencyAmount pv = FRA_METHOD.presentValue(FRA, PROVIDER);
    assertEquals("FRA discounting: present value", expectedPv, pv.getAmount(CUR), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = FRA_METHOD.presentValue(FRA, PROVIDER);
    final MultipleCurrencyAmount pvCalculator = FRA.accept(PVDC, PROVIDER);
    assertEquals("FRA discounting: present value calculator vs method", pvCalculator.getAmount(CUR), pvMethod.getAmount(CUR), 1.0E-2);
  }

  @Test
  public void presentValueBuySellParity() {
    final ForwardRateAgreementDefinition fraDefinitionSell = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, -NOTIONAL,
        FIXING_DATE, USDLIBOR3M, FRA_RATE, CALENDAR);
    final ForwardRateAgreement fraSell = (ForwardRateAgreement) fraDefinitionSell.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pvBuy = FRA_METHOD.presentValue(FRA, PROVIDER);
    final MultipleCurrencyAmount pvSell = FRA_METHOD.presentValue(fraSell, PROVIDER);
    assertEquals("FRA discounting: present value - buy/sell parity", pvSell.getAmount(CUR), -pvBuy.getAmount(CUR), 1.0E-2);
  }

  @Test
  /**
   * Tests present value curve sensitivity when the valuation date is on trade date.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PSC.calculateSensitivity(FRA, PROVIDER, PROVIDER.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PSC_DSC_FD.calculateSensitivity(FRA, PROVIDER);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueMarketSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = FRA_METHOD.presentValueCurveSensitivity(FRA, PROVIDER);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = FRA.accept(PVCSDC, PROVIDER);
    AssertSensivityObjects.assertEquals("CouponFixedDiscountingMarketMethod: presentValueMarketSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  public void parSpread() {
    final double parSpread = FRA_METHOD.parSpread(FRA, PROVIDER);
    final ForwardRateAgreementDefinition fra0Definition = new ForwardRateAgreementDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR_PAYMENT, NOTIONAL, FIXING_DATE,
        USDLIBOR3M, FRA_RATE + parSpread, CALENDAR);
    final ForwardRateAgreement fra0 = (ForwardRateAgreement) fra0Definition.toDerivative(REFERENCE_DATE);
    final MultipleCurrencyAmount pv0 = fra0.accept(PVDC, PROVIDER);
    assertEquals("FRA discounting: par spread", pv0.getAmount(CUR), 0, TOLERANCE_PV);
  }

  @Test
  public void parSpreadMethodVsCalculator() {
    final double parSpreadMethod = FRA_METHOD.parSpread(FRA, PROVIDER);
    final double parSpreadCalculator = FRA.accept(PSMQDC, PROVIDER);
    assertEquals("FRA discounting: par spread", parSpreadMethod, parSpreadCalculator, TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the par spread curve sensitivity versus a finite difference computation.
   */
  public void parSpreadCurveSensitivity() {
    final SimpleParameterSensitivity psComputed = PSPSC.calculateSensitivity(FRA, PROVIDER, PROVIDER.getAllNames());
    final SimpleParameterSensitivity psFD = PSMQCS_FDC.calculateSensitivity(FRA, PROVIDER);
    AssertSensivityObjects.assertEquals("CashDiscountingProviderMethod: presentValueCurveSensitivity ", psFD, psComputed, TOLERANCE_SPREAD_DELTA);
  }

  @Test
  /**
   * Tests the par spread curve sensitivity through the method and through the calculator.
   */
  public void parSpreadCurveSensitivityMethodVsCalculator() {
    final MulticurveSensitivity pvcsMethod = FRA_METHOD.parSpreadCurveSensitivity(FRA, PROVIDER);
    final MulticurveSensitivity pvcsCalculator = FRA.accept(PSMQCSDC, PROVIDER);
    assertEquals("Forex swap present value curve sensitivity: Method vs Calculator", pvcsMethod, pvcsCalculator);
  }

  // Test with standard data - harcoded numbers
  private static final ZonedDateTime STD_REFERENCE_DATE = DateUtils.getUTCDate(2014, 1, 22);
  // Instrument description
  private static final ZonedDateTime STD_ACCRUAL_START_DATE = DateUtils.getUTCDate(2014, 9, 12);
  private static final ZonedDateTime STD_ACCRUAL_END_DATE = DateUtils.getUTCDate(2014, 12, 12);
  private static final double STD_FRA_RATE = 0.0125;
  private static final double STD_NOTIONAL = -10000000; //-10m
  private static final ForwardRateAgreementDefinition STD_FRA_STD_DEFINITION = ForwardRateAgreementDefinition.from(STD_ACCRUAL_START_DATE, STD_ACCRUAL_END_DATE,
      STD_NOTIONAL, USDLIBOR3M, STD_FRA_RATE, CALENDAR);
  private static final Payment STD_FRA = STD_FRA_STD_DEFINITION.toDerivative(STD_REFERENCE_DATE);
  // Data
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR_STD = DataSetsUSD20140122OnOisLibor3MIrs.getCurvesUSD();
  private static final MulticurveProviderDiscount MULTICURVE_STD = MULTICURVE_PAIR_STD.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_STD = MULTICURVE_PAIR_STD.getSecond();

  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final double STD_TOLERANCE_PV = 1.0E-3;
  private static final double STD_TOLERANCE_PV_DELTA = 1.0E-2;
  private static final double BP1 = 1.0E-4;

  @Test
  /**
   * Test different results with a standard set of data against hardcoded values. Can be used for platform testing or regression testing.
   */
  public void resultsStandardDataSet() {
    // Present Value
    final MultipleCurrencyAmount pvComputed = STD_FRA.accept(PVDC, MULTICURVE_STD);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, 23182.5437);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value from standard curves", pvExpected.getAmount(CUR), pvComputed.getAmount(CUR), STD_TOLERANCE_PV);
    // Delta
    final double[] deltaDsc = {-0.007, -0.007, 0.000, -0.005, -0.031, -0.552, -1.041, 0.247, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000 };
    final double[] deltaFwd = {119.738, 120.930, -26.462, -460.755, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_STD.getName(CUR), CUR), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE_STD.getName(USDLIBOR3M), CUR), new DoubleMatrix1D(deltaFwd));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    //    final ParameterSe
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(STD_FRA, MULTICURVE_STD, BLOCK_STD).multipliedBy(BP1);
    AssertSensivityObjects.assertEquals("ForwardRateAgreementDiscountingMethod: bucketed delts from standard curves", pvpsExpected, pvpsComputed, STD_TOLERANCE_PV_DELTA);
  }

}
