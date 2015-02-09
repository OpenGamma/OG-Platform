/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveEUR;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
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

/**
 * Tests the STIR Futures discounting method with standard data.
 * Swap Desk EUR 1 / pv - bpv01 / OIS-IRS3-IRS6
 */
@Test(groups = TestGroup.UNIT)
public class STIRFuturesTransactionDiscountingMethodE2ETest {

  /** Data */
  private static final IborIndex[] INDEX_IBOR_LIST = StandardDataSetsMulticurveEUR.indexIborArrayEUROisE3();
  private static final IborIndex EUREURIBOR3M = INDEX_IBOR_LIST[0];
  private static final Calendar CALENDAR = StandardDataSetsMulticurveEUR.calendarArray()[0];
  private static final Currency EUR = EUREURIBOR3M.getCurrency();
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 2, 18);
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR = 
      StandardDataSetsMulticurveEUR.getCurvesEurOisE3();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK = MULTICURVE_PAIR.getSecond();
  /** Instruments */
  private static final ZonedDateTime LAST_TRADING_DATE = DateUtils.getUTCDate(2014, 12, 15);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERZ4";
  private static final InterestRateFutureSecurityDefinition ERZ4_SEC_DEFINITION =
      new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, EUREURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
  private static final long QUANTITY = -125;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2013, 5, 7);
  private static final double TRADE_PRICE = 0.999;
  private static final InterestRateFutureTransactionDefinition ERZ4_TRA_DEFINITION =
      new InterestRateFutureTransactionDefinition(ERZ4_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);
  private static final double LAST_MARGIN_PRICE = 0.9973; 
  // Closing on (2014, 2, 18); Using the last fixing before or on valuation date.
  private static final InterestRateFutureTransaction ERZ4_TRA = 
      ERZ4_TRA_DEFINITION.toDerivative(VALUATION_DATE, LAST_MARGIN_PRICE);
  /** Calculators */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_STIR =
      InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = 
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC = 
      new MarketQuoteSensitivityBlockCalculator<>(PSC);
  private static final double TOLERANCE_PV = 1.0E-4;
  private static final double TOLERANCE_PV_DELTA = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-8;
  private static final double BP1 = 1.0E-4;

  /** Test the price with a standard set of data against hard-coded values. */
  @Test
  public void price() {
    double priceExpected = 0.9973084085494923;
    double priceComputed = METHOD_STIR.price(ERZ4_TRA.getUnderlyingSecurity(), MULTICURVE);
    assertEquals("STIRFuturesTransactionDiscountingMethodE2ETest: price", 
        priceExpected, priceComputed, TOLERANCE_RATE);
  }

  /** Test the present value with a standard set of data against hard-coded values. */
  @Test
  public void presentValue() {
    MultipleCurrencyAmount pvComputed = ERZ4_TRA.accept(PVDC, MULTICURVE);
    assertTrue("STIRFuturesTransactionDiscountingMethodE2ETest: present value from standard curves", pvComputed.size() == 1);
    MultipleCurrencyAmount pvExpected = MultipleCurrencyAmount.of(Currency.EUR, -262.7672);
    double priceComputed = METHOD_STIR.price(ERZ4_TRA.getUnderlyingSecurity(), MULTICURVE);
    assertEquals("STIRFuturesTransactionDiscountingMethodE2ETest: present value from standard curves", 
        pvExpected.getAmount(EUR), pvComputed.getAmount(EUR), TOLERANCE_PV);
    double pvDetailedInEUR = (priceComputed - LAST_MARGIN_PRICE) * NOTIONAL * FUTURE_FACTOR * QUANTITY;
    assertEquals("STIRFuturesTransactionDiscountingMethodE2ETest: present value from standard curves", 
        pvDetailedInEUR, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  /** Test the par rate with a standard set of data against hard-coded values. */
  @Test
  public void parRate() {
    final double parRate = ERZ4_TRA.accept(PRDC, MULTICURVE);
    final double parRateExpected = 0.00269159145050768;
    assertEquals("STIRFuturesTransactionDiscountingMethodE2ETest: par rate from standard curves", 
        parRateExpected, parRate, TOLERANCE_RATE);
    double parRateMethod = METHOD_STIR.parRate(ERZ4_TRA.getUnderlyingSecurity(), MULTICURVE);
    assertEquals("STIRFuturesTransactionDiscountingMethodE2ETest: par rate from standard curves", 
        parRateMethod, parRate, TOLERANCE_RATE);
  }
  
  /** Test the par spread with a standard set of data against hard-coded values. */
  @Test
  public void parSpread() {
    final double parSpreadComputed = ERZ4_TRA.accept(PSMQDC, MULTICURVE);
    final double parSpreadExpected = 8.4085E-6;
    assertEquals("STIRFuturesTransactionDiscountingMethodE2ETest: par rate from standard curves", 
        parSpreadExpected, parSpreadComputed, TOLERANCE_RATE);
  }

  /** Tests bucketed PV01 with a standard set of data against hard-coded values. */
  @Test
  public void BucketedPV01() {
    final double[] deltaDsc = {0.0003, 0.0003, 0.0000, 0.0000, 1.7334, 3.0714, 4.6402, -18.8887, -0.9835, 
      0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000 };
    final double[] deltaFwd = {-2398.5241, -2479.7772, -2479.1440, 9422.5946, 912.6277, 0.0000, 0.0000, 
      0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(EUR), EUR), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(EUREURIBOR3M), EUR), new DoubleMatrix1D(deltaFwd));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    MultipleCurrencyMulticurveSensitivity pvPointSensi = ERZ4_TRA.accept(PVCSDC, MULTICURVE);
    MultipleCurrencyParameterSensitivity pvParameterSensi = PSC.pointToParameterSensitivity(pvPointSensi, MULTICURVE).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvMarketQuoteSensi = MQSBC.fromParameterSensitivity(pvParameterSensi, BLOCK);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(ERZ4_TRA, MULTICURVE, BLOCK).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("STIRFuturesTransactionDiscountingMethodE2ETest: bucketed delts from standard curves", 
        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
    AssertSensitivityObjects.assertEquals("STIRFuturesTransactionDiscountingMethodE2ETest: bucketed deltas from standard curves", 
        pvMarketQuoteSensi, pvpsComputed, TOLERANCE_PV_DELTA);
  }

}
