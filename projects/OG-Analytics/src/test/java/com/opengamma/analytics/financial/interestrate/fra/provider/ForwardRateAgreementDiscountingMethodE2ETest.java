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
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSD;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingMultipleInstrumentsCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests the ForwardRateAgreement discounting method with standard data.
 * Demo test - worked-out example on how to use OG-Analytics library for compute standard measure to simple instruments. 
 * The data is hard-coded. It is also available in some integration unit test and in snapshots.
 */
@Test(groups = TestGroup.UNIT)
public class ForwardRateAgreementDiscountingMethodE2ETest {

  /** The valuation date */
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 1, 22);
  /** Curves and indexes. */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR = StandardDataSetsMulticurveUSD.getCurvesUSDOisL3();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK = MULTICURVE_PAIR.getSecond();
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_FF_PAIR =
      StandardDataSetsMulticurveUSD.getCurvesUSDOisFFL1L3L6();
  private static final MulticurveProviderDiscount MULTICURVE_FFS = MULTICURVE_FF_PAIR.getFirst();
  private static final IborIndex[] INDEX_IBOR_LIST = StandardDataSetsMulticurveUSD.indexIborArrayUSDOisL3();
  private static final IborIndex USDLIBOR3M = INDEX_IBOR_LIST[0];
  private static final Calendar CALENDAR = StandardDataSetsMulticurveUSD.calendarArray()[0];
  private static final Currency USD = USDLIBOR3M.getCurrency();
  /** Calculators and methods*/
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueDiscountingMultipleInstrumentsCalculator PVMULTIDC = PresentValueDiscountingMultipleInstrumentsCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();
  /** Instrument description: FRA */
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2014, 9, 12);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2014, 12, 12);
  private static final double FRA_RATE = 0.0125;
  private static final double NOTIONAL = -10000000; //-10m
  private static final ForwardRateAgreementDefinition FRA_DEFINITION = ForwardRateAgreementDefinition.from(ACCRUAL_START_DATE, ACCRUAL_END_DATE,
      NOTIONAL, USDLIBOR3M, FRA_RATE, CALENDAR);
  private static final Payment FRA = FRA_DEFINITION.toDerivative(VALUATION_DATE);

  /** Tolerance level for regression tests */
  private static final double TOLERANCE_PV = 1.0E-3;
  private static final double TOLERANCE_PV_DELTA = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-5;
  private static final double BP1 = 1.0E-4;

  @Test
  /** Present value of a FRA before the fixing date. */
  public void presentValue() {
    final MultipleCurrencyAmount pvComputed = FRA.accept(PVDC, MULTICURVE);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, 23182.5437);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value from standard curves",
                 pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);

    final MultipleCurrencyAmount pvComputed2 = FRA.accept(PVDC, MULTICURVE_FFS);
    final MultipleCurrencyAmount pvExpected2 = MultipleCurrencyAmount.of(Currency.USD, 21750.7625);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value Fed Fund swap based curves",
                 pvExpected2.getAmount(USD), pvComputed2.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /** Par rate */
  public void parRate() {
    final double parRate = FRA.accept(PRDC, MULTICURVE);
    final double parRateExpected = 0.003315;
    assertEquals("ForwardRateAgreementDiscountingMethod: par rate from standard curves", parRateExpected, parRate, TOLERANCE_RATE);
    final double parRateMethod = METHOD_FRA.parRate((ForwardRateAgreement) FRA, MULTICURVE);
    assertEquals("ForwardRateAgreementDiscountingMethod: par rate from standard curves", parRateMethod, parRate, TOLERANCE_RATE);
  }

  @Test
  /** Par rate spread */
  public void parRateSpread() {
    final double parRate = FRA.accept(PSMQDC, MULTICURVE);
    ForwardRateAgreement fra = (ForwardRateAgreement) FRA;
    ForwardRateAgreement fra0 = new ForwardRateAgreement(USD, fra.getPaymentTime(), fra.getPaymentYearFraction(),
        fra.getNotional(), USDLIBOR3M, fra.getFixingTime(), fra.getFixingPeriodStartTime(), fra.getFixingPeriodEndTime(),
        fra.getFixingYearFraction(), FRA_RATE + parRate);
    final MultipleCurrencyAmount pvComputed = fra0.accept(PVDC, MULTICURVE);
    assertEquals("ForwardRateAgreementDiscountingMethod: par rate from standard curves", 0.0, pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  /** Bucketed PV01: sensitivity with respect to the market quotes used in the curve calibration. 
   *  The sensitivity is rescaled to a one basis point move. */
  public void BucketedPV01() {
    final double[] deltaDsc = {-0.007, -0.007, 0.000, -0.005, -0.031, -0.552, -1.041, 0.247, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000 };
    final double[] deltaFwd = {119.738, 120.930, -26.462, -460.755, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd));
    MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    MultipleCurrencyMulticurveSensitivity pvPointSensi = FRA.accept(PVCSDC, MULTICURVE);
    MultipleCurrencyParameterSensitivity pvParameterSensi = PSC.pointToParameterSensitivity(pvPointSensi, MULTICURVE);
    MultipleCurrencyParameterSensitivity pvMarketQuoteSensi = MQSBC.fromParameterSensitivity(pvParameterSensi, BLOCK).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqsComputed = MQSBC.fromInstrument(FRA, MULTICURVE, BLOCK).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("ForwardRateAgreementDiscountingMethod: bucketed deltas from standard curves", pvpsExpected, pvmqsComputed, TOLERANCE_PV_DELTA);
    AssertSensitivityObjects.assertEquals("ForwardRateAgreementDiscountingMethod: bucketed deltas from standard curves", pvMarketQuoteSensi, pvmqsComputed, TOLERANCE_PV_DELTA);
  }

  @Test
  /** Fees can be attached to an instrument. */
  public void presentValueAfterFee() {
    Annuity<?> fee = new Annuity<>(new CouponFixed[] {new CouponFixed(Currency.USD, 1. / 365, 1, -23182.647032590383, 1) });
    Pair<InstrumentDerivative[], MulticurveProviderInterface> data = Pairs.of(new InstrumentDerivative[] {fee }, (MulticurveProviderInterface) MULTICURVE);
    final MultipleCurrencyAmount pvComputed = FRA.accept(PVMULTIDC, data);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, 0);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value after fee from standard curves", pvExpected.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
  }

}
