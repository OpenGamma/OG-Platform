/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.datasets.StandardDataSetsEURUSDForex;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.provider.calculator.discounting.CurrencyExposureDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Tests related to the method for Forex transaction by discounting on each payment.
 * Tests using fixed data set.
 */
@Test(groups = TestGroup.UNIT)
public class ForexDiscountingMethodE2ETest {
  
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_OIS_BLOCK = 
      StandardDataSetsEURUSDForex.getCurvesEUROisUSDOis(); // EUR and USD built with OIS
//  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_EURFX_BLOCK = 
//      DataSetsEURUSD20140310Forex.getCurvesEUROisUSDOis(); // USD built with OIS, EUR with FX
//  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_USDFX_BLOCK = 
//      DataSetsEURUSD20140310Forex.getCurvesEUROisUSDOis(); // EUR built with OIS, USD with FX
  private static final MulticurveProviderDiscount MULTICURVE_OIS = MULTICURVE_OIS_BLOCK.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_OIS = MULTICURVE_OIS_BLOCK.getSecond();
//  private static final MulticurveProviderDiscount MULTICURVE_EURFX = MULTICURVE_EURFX_BLOCK.getFirst();
//  private static final MulticurveProviderDiscount MULTICURVE_USDFX = MULTICURVE_USDFX_BLOCK.getFirst();
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2015, 2, 27);
  private static final double NOMINAL_1 = 10000000;
  private static final double TRADE_FX_RATE = 1.40;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(EUR, USD, PAYMENT_DATE, NOMINAL_1, TRADE_FX_RATE);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 3, 10);

  private static final Forex FX = FX_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final CurrencyExposureDiscountingCalculator CEDC = CurrencyExposureDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();

  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);
//  private static final TodayPaymentCalculator TPC = TodayPaymentCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-3; // one cent out of 100m
  private static final double TOLERANCE_PV_DELTA = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-5;
//  private static final double TOLERANCE_RATE_DELTA = 1.0E-8;
  private static final double BP1 = 1.0E-4;

  /**
   * Tests the present value computation.
   */
  @Test
  public void presentValueEUROISUSDOIS() {
    final MultipleCurrencyAmount pvComputed = FX.accept(PVDC, MULTICURVE_OIS);
    final double pvEURExpected = 9982954.0610;
    final double pvUSDExpected = -13984806.7328;
    assertEquals("ForexDiscountingMethod: presentValue - standard data set", pvComputed.getAmount(EUR), pvEURExpected, TOLERANCE_PV);
    assertEquals("ForexDiscountingMethod: presentValue - standard data set", pvComputed.getAmount(USD), pvUSDExpected, TOLERANCE_PV);
  }

//  /**
//   * Tests the present value computation.
//   */
//  @Test
//  public void presentValueUSDOISEURFX() {
//    final MultipleCurrencyAmount pvComputed = FX.accept(PVDC, MULTICURVES_EURFX);
//    final double pvEURExpected = 9989068.065195373;
//    final double pvUSDExpected = -13984806.732845595;
//    assertEquals("ForexDiscountingMethod: presentValue - standard data set", pvComputed.getAmount(EUR), pvEURExpected, TOLERANCE_PV);
//    assertEquals("ForexDiscountingMethod: presentValue - standard data set", pvComputed.getAmount(USD), pvUSDExpected, TOLERANCE_PV);
//  }

  /**
   * Tests the currency exposure computation.
   */
  @Test
  public void currencyExposureEUROISUSDOIS() {
    final MultipleCurrencyAmount ceComputed = FX.accept(CEDC, MULTICURVE_OIS);
    final double ceEURExpected = 9982954.0610;
    final double ceUSDExpected = -13984806.7328;
    assertEquals("ForexDiscountingMethod: presentValue - standard data set", ceComputed.getAmount(EUR), ceEURExpected, TOLERANCE_PV);
    assertEquals("ForexDiscountingMethod: presentValue - standard data set", ceComputed.getAmount(USD), ceUSDExpected, TOLERANCE_PV);
  }

  /**
   * Tests the forward Forex rate computation.
   */
  @Test
  public void forwardRate() {
    final Double prComputed = FX.accept(PRDC, MULTICURVE_OIS);
    final double prExpected = 1.386890;
    assertEquals("ForexDiscountingMethod: presentValue - standard data set", prComputed, prExpected, TOLERANCE_RATE);
  }

//  /**
//   * Tests the parSpread for forex transactions.
//   */
//  @Test
//  public void parSpread() {
//  }
//
//  /**
//   * Tests the TodayPaymentCalculator for forex transactions.
//   */
//  @Test
//  public void forexTodayPaymentBeforePayment() {
//  }

  /**
//   * Tests the TodayPaymentCalculator for forex transactions.
//   */
//  @Test
//  public void forexTodayPaymentOnPayment() {
//  }


  @Test
  /**
   * Test different results with a standard set of data against hardcoded values. Can be used for platform testing or regression testing.
   */
  public void BucketedPV01() {
    final double[] deltaDscEUR = {-2.7967, -2.7967, -0.000198, 0.0055, -0.1666, 5.1052, -150.3922, -828.9601, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    final double[] deltaDscUSD = {3.9161, 3.9161, 0.0046, -0.0625, 0.4837, -9.8799, 215.6977, 1159.6196, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(EUR), EUR), new DoubleMatrix1D(deltaDscEUR));
    sensitivity.put(ObjectsPair.of(MULTICURVE_OIS.getName(USD), USD), new DoubleMatrix1D(deltaDscUSD));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    //    final ParameterSe
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(FX, MULTICURVE_OIS, BLOCK_OIS).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("ForwardRateAgreementDiscountingMethod: bucketed delts from standard curves", pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }
  
//  @Test
//  public void parSpreadCurveSensitivity() {
//  }

}
