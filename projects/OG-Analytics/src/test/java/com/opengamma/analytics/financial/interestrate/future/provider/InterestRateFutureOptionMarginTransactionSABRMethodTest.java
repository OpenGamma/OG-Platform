/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.sabrstirfutures.PresentValueCurveSensitivitySABRSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrstirfutures.PresentValueSABRSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrstirfutures.PresentValueSABRSensitivitySABRSTIRFuturesCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSTIRFuturesProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.sabrstirfutures.ParameterSensitivitySABRSTIRFuturesDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the method for interest rate future option with SABR volatility parameter surfaces.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureOptionMarginTransactionSABRMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETERS = SABRDataSets.createSABR1();
  private static final SABRSTIRFuturesProviderDiscount SABR_MULTICURVES = new SABRSTIRFuturesProviderDiscount(MULTICURVES, SABR_PARAMETERS, EURIBOR3M);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -EURIBOR3M.getSpotLag(), TARGET);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "EDU2";
  private static final double STRIKE = 0.9850;
  private static final InterestRateFutureSecurityDefinition EDU2_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, TARGET);
  private static final InterestRateFutureSecurity EDU2 = EDU2_DEFINITION.toDerivative(REFERENCE_DATE);
  // Option
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 9, 16);
  private static final double EXPIRATION_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRATION_DATE);
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurity OPTION_EDU2 = new InterestRateFutureOptionMarginSecurity(EDU2, EXPIRATION_TIME, STRIKE, IS_CALL);
  // Transaction
  private static final int QUANTITY = -123;
  private static final double TRADE_PRICE = 0.0050;
  private static final InterestRateFutureOptionMarginTransaction TRANSACTION = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);

  private static final InterestRateFutureOptionMarginTransactionSABRMethod METHOD_SABR_TRA = InterestRateFutureOptionMarginTransactionSABRMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecuritySABRMethod METHOD_SABR_SEC = InterestRateFutureOptionMarginSecuritySABRMethod.getInstance();
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUT = InterestRateFutureSecurityDiscountingMethod.getInstance();

  private static final PresentValueSABRSTIRFuturesCalculator PVSFC = PresentValueSABRSTIRFuturesCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRSTIRFuturesCalculator PVCSSFC = PresentValueCurveSensitivitySABRSTIRFuturesCalculator.getInstance();
  private static final PresentValueSABRSensitivitySABRSTIRFuturesCalculator PVSSSFC = PresentValueSABRSensitivitySABRSTIRFuturesCalculator.getInstance();

  private static final double SHIFT = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<SABRSTIRFuturesProviderInterface> PSSFC = new ParameterSensitivityParameterCalculator<>(PVCSSFC);
  private static final ParameterSensitivitySABRSTIRFuturesDiscountInterpolatedFDCalculator PSSFC_FD = new ParameterSensitivitySABRSTIRFuturesDiscountInterpolatedFDCalculator(PVSFC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test
  /**
   * Test the present value from the quoted option price.
   */
  public void presentValueFromOptionPrice() {
    final double priceQuoted = 0.01;
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);
    final MultipleCurrencyAmount pv = METHOD_SABR_TRA.presentValueFromPrice(transactionNoPremium, priceQuoted);
    final double pvExpected = (priceQuoted - TRADE_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    assertEquals("Future option: present value from quoted price", pvExpected, pv.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value from the future price.
   */
  public void presentValueFromFuturePrice() {
    final double priceFuture = 0.9905;
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);
    final MultipleCurrencyAmount pv = METHOD_SABR_TRA.presentValueFromFuturePrice(transactionNoPremium, SABR_MULTICURVES, priceFuture);
    final double priceSecurity = METHOD_SABR_SEC.priceFromFuturePrice(OPTION_EDU2, SABR_MULTICURVES, priceFuture);
    final double pvExpected = (priceSecurity - TRADE_PRICE) * QUANTITY * NOTIONAL * FUTURE_FACTOR;
    assertEquals("Future option: present value from future price", pvExpected, pv.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value from the future price.
   */
  public void presentValue() {
    final double priceFuture = METHOD_FUT.price(EDU2, MULTICURVES);
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, TRADE_PRICE);
    final double pvNoPremium = METHOD_SABR_TRA.presentValue(transactionNoPremium, SABR_MULTICURVES).getAmount(EUR);
    final double pvNoPremiumExpected = METHOD_SABR_TRA.presentValueFromFuturePrice(transactionNoPremium, SABR_MULTICURVES, priceFuture).getAmount(EUR);
    assertEquals("Future option: present value", pvNoPremiumExpected, pvNoPremium, TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value from the method and from the calculator.
   */
  public void presentValueMethodVsCalculator() {
    final InterestRateFutureOptionMarginTransaction transactionNoPremium = new InterestRateFutureOptionMarginTransaction(OPTION_EDU2, QUANTITY, 0.0);
    final MultipleCurrencyAmount pvNoPremiumMethod = METHOD_SABR_TRA.presentValue(transactionNoPremium, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvNoPremiumCalculator = transactionNoPremium.accept(PVSFC, SABR_MULTICURVES);
    assertEquals("Future option: present value: Method vs Calculator", pvNoPremiumMethod.getAmount(EUR), pvNoPremiumCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the present value curves sensitivity computed from the curves
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PSSFC.calculateSensitivity(TRANSACTION, SABR_MULTICURVES, SABR_MULTICURVES.getMulticurveProvider().getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PSSFC_FD.calculateSensitivity(TRANSACTION, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("InterestRateFutureOptionMarginTransactionSABRMethod: presentValueCurveSensitivity", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Tests that the method return the same result as the calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = METHOD_SABR_TRA.presentValueCurveSensitivity(TRANSACTION, SABR_MULTICURVES);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = TRANSACTION.accept(PVCSSFC, SABR_MULTICURVES);
    AssertSensivityObjects.assertEquals("InterestRateFutureOptionMarginTransactionSABRMethod: presentValueCurveSensitivity", pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueSABRSensitivity() {
    final PresentValueSABRSensitivityDataBundle pvcs = METHOD_SABR_TRA.presentValueSABRSensitivity(TRANSACTION, SABR_MULTICURVES);
    // SABR sensitivity vs finite difference
    final double pv = METHOD_SABR_TRA.presentValue(TRANSACTION, SABR_MULTICURVES).getAmount(EUR);
    final double shift = 0.000001;
    final double delay = EDU2.getTradingLastTime() - OPTION_EDU2.getExpirationTime();
    final DoublesPair expectedExpiryDelay = DoublesPair.of(OPTION_EDU2.getExpirationTime(), delay);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterAlphaBumped = SABRDataSets.createSABR1AlphaBumped(shift);
    final SABRSTIRFuturesProviderDiscount sabrBundleAlphaBumped = new SABRSTIRFuturesProviderDiscount(MULTICURVES, sabrParameterAlphaBumped, EURIBOR3M);
    final double pvAlphaBumped = METHOD_SABR_TRA.presentValue(TRANSACTION, sabrBundleAlphaBumped).getAmount(EUR);
    final double expectedAlphaSensi = (pvAlphaBumped - pv) / shift;
    assertEquals("Number of alpha sensitivity", pvcs.getAlpha().getMap().keySet().size(), 1);
    assertEquals("Alpha sensitivity expiry/tenor", pvcs.getAlpha().getMap().keySet().contains(expectedExpiryDelay), true);
    assertEquals("Alpha sensitivity value", pvcs.getAlpha().getMap().get(expectedExpiryDelay), expectedAlphaSensi, 1.0E+1);
    // Rho sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterRhoBumped = SABRDataSets.createSABR1RhoBumped(shift);
    final SABRSTIRFuturesProviderDiscount sabrBundleRhoBumped = new SABRSTIRFuturesProviderDiscount(MULTICURVES, sabrParameterRhoBumped, EURIBOR3M);
    final double pvRhoBumped = METHOD_SABR_TRA.presentValue(TRANSACTION, sabrBundleRhoBumped).getAmount(EUR);
    final double expectedRhoSensi = (pvRhoBumped - pv) / shift;
    assertEquals("Number of rho sensitivity", pvcs.getRho().getMap().keySet().size(), 1);
    assertEquals("Rho sensitivity expiry/tenor", pvcs.getRho().getMap().keySet().contains(expectedExpiryDelay), true);
    assertEquals("Rho sensitivity value", pvcs.getRho().getMap().get(expectedExpiryDelay), expectedRhoSensi, 1.0E+0);
    // Alpha sensitivity vs finite difference computation
    final SABRInterestRateParameters sabrParameterNuBumped = SABRDataSets.createSABR1NuBumped(shift);
    final SABRSTIRFuturesProviderDiscount sabrBundleNuBumped = new SABRSTIRFuturesProviderDiscount(MULTICURVES, sabrParameterNuBumped, EURIBOR3M);
    final double pvNuBumped = METHOD_SABR_TRA.presentValue(TRANSACTION, sabrBundleNuBumped).getAmount(EUR);
    final double expectedNuSensi = (pvNuBumped - pv) / shift;
    assertEquals("Number of nu sensitivity", pvcs.getNu().getMap().keySet().size(), 1);
    assertEquals("Nu sensitivity expiry/tenor", pvcs.getNu().getMap().keySet().contains(expectedExpiryDelay), true);
    assertEquals("Nu sensitivity value", pvcs.getNu().getMap().get(expectedExpiryDelay), expectedNuSensi, 1.0E+0);
  }

  @Test
  /**
   * Tests that the method return the same result as the calculator.
   */
  public void presentValueSABRSensitivityMethodVsCalculator() {
    final PresentValueSABRSensitivityDataBundle sensiCalculator = TRANSACTION.accept(PVSSSFC, SABR_MULTICURVES);
    final PresentValueSABRSensitivityDataBundle sensiMethod = METHOD_SABR_TRA.presentValueSABRSensitivity(TRANSACTION, SABR_MULTICURVES);
    assertEquals("Future option curve sensitivity: method comparison with present value calculator", sensiCalculator, sensiMethod);
    final InterestRateFutureOptionMarginSecuritySABRMethod methodSecurity = InterestRateFutureOptionMarginSecuritySABRMethod.getInstance();
    PresentValueSABRSensitivityDataBundle sensiSecurity = methodSecurity.priceSABRSensitivity(OPTION_EDU2, SABR_MULTICURVES);
    sensiSecurity = sensiSecurity.multiplyBy(QUANTITY * NOTIONAL * FUTURE_FACTOR);
    assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getAlpha(), sensiSecurity.getAlpha());
    assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getRho(), sensiSecurity.getRho());
    assertEquals("Future discounting curve sensitivity: security price vs transaction sensitivity", sensiMethod.getNu(), sensiSecurity.getNu());
  }

}
