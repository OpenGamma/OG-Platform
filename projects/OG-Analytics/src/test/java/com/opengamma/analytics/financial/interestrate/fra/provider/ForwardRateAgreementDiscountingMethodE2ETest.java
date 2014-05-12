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
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingMultipleInstrumentsCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
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
 * Tests the ForwardRateAgreement discounting method.
 */
@Test(groups = TestGroup.UNIT)
public class ForwardRateAgreementDiscountingMethodE2ETest {

  private static final IborIndex[] INDEX_IBOR_LIST = StandardDataSetsMulticurveUSD.indexIborArrayUSDOisL3();
  private static final IborIndex USDLIBOR3M = INDEX_IBOR_LIST[0];
  private static final Calendar CALENDAR = StandardDataSetsMulticurveUSD.calendarArray()[0];
  private static final Currency CUR = USDLIBOR3M.getCurrency();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueDiscountingMultipleInstrumentsCalculator PVMULTIDC = PresentValueDiscountingMultipleInstrumentsCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);

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
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR_STD = StandardDataSetsMulticurveUSD.getCurvesUSDOisL3();
  private static final MulticurveProviderDiscount MULTICURVE_STD = MULTICURVE_PAIR_STD.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_STD = MULTICURVE_PAIR_STD.getSecond();

  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final double STD_TOLERANCE_PV = 1.0E-3;
  private static final double STD_TOLERANCE_PV_DELTA = 1.0E-2;
  private static final double STD_TOLERANCE_RATE = 1.0E-5;
  private static final double BP1 = 1.0E-4;

  @Test
  /**
   * Test different results with a standard set of data against hardcoded values. Can be used for platform testing or regression testing.
   */
  public void presentValue() {
    // Present Value
    final MultipleCurrencyAmount pvComputed = STD_FRA.accept(PVDC, MULTICURVE_STD);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, 23182.5437);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value from standard curves", pvExpected.getAmount(CUR), pvComputed.getAmount(CUR), STD_TOLERANCE_PV);
  }

  @Test
  /**
  * Test different results with a standard set of data against hardcoded values. Can be used for platform testing or regression testing
  */
  public void presentValueAfterFee() {
    // fee offsets PV
    Annuity<?> fee = new Annuity<>(new CouponFixed[] {new CouponFixed(Currency.USD, 1. / 365, 1, -23182.647032590383, 1)});
    // Present Value
    Pair<InstrumentDerivative[], MulticurveProviderInterface> data = Pairs.of(new InstrumentDerivative[] {fee}, (MulticurveProviderInterface) MULTICURVE_STD);
    final MultipleCurrencyAmount pvComputed = STD_FRA.accept(PVMULTIDC, data);
    final MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.USD, 0);
    assertEquals("ForwardRateAgreementDiscountingMethod: present value after fee from standard curves", pvExpected.getAmount(CUR), pvComputed.getAmount(CUR), STD_TOLERANCE_PV);
  }

  @Test
  /**
   * Test different results with a standard set of data against hard coded values. Can be used for platform testing or regression testing.
   */
  public void BucketedPV01() {
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

  @Test
  /**
   * Test different results with a standard set of data against hardcoded values. Can be used for platform testing or regression testing.
   */
  public void parRate() {
    final double parRate = STD_FRA.accept(PRDC, MULTICURVE_STD);
    final double parRateExpected = 0.003315;
    assertEquals("ForwardRateAgreementDiscountingMethod: par rate from standard curves", parRateExpected, parRate, STD_TOLERANCE_RATE);
  }

}
