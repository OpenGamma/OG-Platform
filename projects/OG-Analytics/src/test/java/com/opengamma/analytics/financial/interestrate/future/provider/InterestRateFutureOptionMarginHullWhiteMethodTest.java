/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.MarketQuoteCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.MarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.PresentValueHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.SimpleParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.SimpleParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureOptionMarginHullWhiteMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = INDEX_LIST[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar TARGET = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final HullWhiteOneFactorPiecewiseConstantParameters HW_PARAMETERS = HullWhiteDataSets.createHullWhiteParameters();
  private static final HullWhiteOneFactorProviderDiscount HW_MULTICURVES = new HullWhiteOneFactorProviderDiscount(MULTICURVES, HW_PARAMETERS, EUR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 12, 18);
  // Dates and names
  private static final ZonedDateTime FUT_SPOT_MAR13 = DateUtils.getUTCDate(2013, 3, 20);
  private static final ZonedDateTime FUT_LAST_MAR13 = ScheduleCalculator.getAdjustedDate(FUT_SPOT_MAR13, -EURIBOR3M.getSpotLag(), TARGET);
  private static final ZonedDateTime OPT_EXP_MAR13 = FUT_LAST_MAR13;
  private static final ZonedDateTime FUT_SPOT_JUN14 = DateUtils.getUTCDate(2014, 6, 18);
  private static final ZonedDateTime FUT_LAST_JUN14 = ScheduleCalculator.getAdjustedDate(FUT_SPOT_JUN14, -EURIBOR3M.getSpotLag(), TARGET);
  private static final ZonedDateTime OPT_MID_EXP_JUN13 = DateUtils.getUTCDate(2013, 6, 14);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME_MAR13 = "ERH3";
  private static final String NAME_JUN14 = "ERM4";
  // Futures
  private static final InterestRateFutureSecurityDefinition ERH3_DEFINITION = new InterestRateFutureSecurityDefinition(FUT_LAST_MAR13, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME_MAR13, TARGET);
  private static final InterestRateFutureSecurityDefinition ERM4_DEFINITION = new InterestRateFutureSecurityDefinition(FUT_LAST_JUN14, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME_JUN14, TARGET);
  private static final InterestRateFutureSecurity ERH3 = ERH3_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final InterestRateFutureSecurity ERM4 = ERM4_DEFINITION.toDerivative(REFERENCE_DATE);
  // Options on futures - securities
  private static final double STRIKE_1 = 0.9900;
  private static final double STRIKE_2 = 0.9875;

  private static final InterestRateFutureOptionMarginSecurityDefinition OPT_ERH3_CALL_9900_DEFINITION = new InterestRateFutureOptionMarginSecurityDefinition(ERH3_DEFINITION, OPT_EXP_MAR13, STRIKE_1,
      true);
  private static final InterestRateFutureOptionMarginSecurityDefinition OPT_ERH3_PUT_9900_DEFINITION = new InterestRateFutureOptionMarginSecurityDefinition(ERH3_DEFINITION, OPT_EXP_MAR13, STRIKE_1,
      false);
  private static final InterestRateFutureOptionMarginSecurityDefinition OPT_ERM4_MID_CALL_9875_DEFINITION = new InterestRateFutureOptionMarginSecurityDefinition(ERM4_DEFINITION, OPT_MID_EXP_JUN13,
      STRIKE_2, true);
  private static final InterestRateFutureOptionMarginSecurityDefinition OPT_ERM4_MID_PUT_9875_DEFINITION = new InterestRateFutureOptionMarginSecurityDefinition(ERM4_DEFINITION, OPT_MID_EXP_JUN13,
      STRIKE_2, false);
  private static final InterestRateFutureOptionMarginSecurity OPT_ERH3_CALL_9900 = OPT_ERH3_CALL_9900_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final InterestRateFutureOptionMarginSecurity OPT_ERH3_PUT_9900 = OPT_ERH3_PUT_9900_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final InterestRateFutureOptionMarginSecurity OPT_ERM4_MID_CALL_9875 = OPT_ERM4_MID_CALL_9875_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final InterestRateFutureOptionMarginSecurity OPT_ERM4_MID_PUT_9875 = OPT_ERM4_MID_PUT_9875_DEFINITION.toDerivative(REFERENCE_DATE);
  //Options on futures - transactions
  private static final int QUANTITY_1 = -1234;
  private static final int QUANTITY_2 = 4321;
  private static final double TRADE_PRICE_1 = 0.0050;
  private static final double TRADE_PRICE_2 = 0.0100;
  private static final double LAST_MARGIN_1 = 0.0055;
  //  private static final double LAST_MARGIN_2 = 0.0105;
  private static final ZonedDateTime TRADE_DATE_1 = DateUtils.getUTCDate(2012, 12, 17, 13, 00);
  private static final ZonedDateTime TRADE_DATE_2 = DateUtils.getUTCDate(2012, 12, 18, 9, 30);
  private static final InterestRateFutureOptionMarginTransactionDefinition OPT_ERH3_CALL_9900_TRA_1_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(OPT_ERH3_CALL_9900_DEFINITION,
      QUANTITY_1, TRADE_DATE_1, TRADE_PRICE_1);
  private static final InterestRateFutureOptionMarginTransactionDefinition OPT_ERH3_CALL_9900_TRA_2_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(OPT_ERH3_CALL_9900_DEFINITION,
      -QUANTITY_1, TRADE_DATE_1, TRADE_PRICE_1);
  private static final InterestRateFutureOptionMarginTransactionDefinition OPT_ERH3_PUT_9900_TRA_1_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(OPT_ERH3_PUT_9900_DEFINITION,
      QUANTITY_1, TRADE_DATE_1, TRADE_PRICE_1);
  private static final InterestRateFutureOptionMarginTransaction OPT_ERH3_CALL_9900_TRA_1 = OPT_ERH3_CALL_9900_TRA_1_DEFINITION.toDerivative(REFERENCE_DATE, LAST_MARGIN_1);
  private static final InterestRateFutureOptionMarginTransaction OPT_ERH3_CALL_9900_TRA_2 = OPT_ERH3_CALL_9900_TRA_2_DEFINITION.toDerivative(REFERENCE_DATE, LAST_MARGIN_1);
  private static final InterestRateFutureOptionMarginTransaction OPT_ERH3_PUT_9900_TRA_1 = OPT_ERH3_PUT_9900_TRA_1_DEFINITION.toDerivative(REFERENCE_DATE, LAST_MARGIN_1);
  private static final InterestRateFutureOptionMarginTransactionDefinition OPT_ERM4_MID_CALL_9875_TRA_1_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(
      OPT_ERM4_MID_CALL_9875_DEFINITION, QUANTITY_2, TRADE_DATE_2, TRADE_PRICE_2);
  private static final InterestRateFutureOptionMarginTransactionDefinition OPT_ERM4_MID_CALL_9875_TRA_2_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(
      OPT_ERM4_MID_CALL_9875_DEFINITION, -QUANTITY_2, TRADE_DATE_2, TRADE_PRICE_2);
  private static final InterestRateFutureOptionMarginTransactionDefinition OPT_ERM4_MID_PUT_9875_TRA_1_DEFINITION = new InterestRateFutureOptionMarginTransactionDefinition(
      OPT_ERM4_MID_PUT_9875_DEFINITION, QUANTITY_2, TRADE_DATE_2, TRADE_PRICE_2);
  private static final InterestRateFutureOptionMarginTransaction OPT_ERM4_MID_CALL_9875_TRA_1 = OPT_ERM4_MID_CALL_9875_TRA_1_DEFINITION.toDerivative(REFERENCE_DATE, LAST_MARGIN_1);
  private static final InterestRateFutureOptionMarginTransaction OPT_ERM4_MID_CALL_9875_TRA_2 = OPT_ERM4_MID_CALL_9875_TRA_2_DEFINITION.toDerivative(REFERENCE_DATE, LAST_MARGIN_1);
  private static final InterestRateFutureOptionMarginTransaction OPT_ERM4_MID_PUT_9875_TRA_1 = OPT_ERM4_MID_PUT_9875_TRA_1_DEFINITION.toDerivative(REFERENCE_DATE, LAST_MARGIN_1);

  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL_HW = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_FUT = InterestRateFutureSecurityHullWhiteMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityHullWhiteMethod METHOD_OPT_SEC = InterestRateFutureOptionMarginSecurityHullWhiteMethod.getInstance();
  private static final InterestRateFutureOptionMarginTransactionHullWhiteMethod METHOD_OPT_TRA = InterestRateFutureOptionMarginTransactionHullWhiteMethod.getInstance();

  private static final MarketQuoteHullWhiteCalculator MQHWC = MarketQuoteHullWhiteCalculator.getInstance();
  private static final MarketQuoteCurveSensitivityHullWhiteCalculator MQCSHWC = MarketQuoteCurveSensitivityHullWhiteCalculator.getInstance();
  private static final PresentValueHullWhiteCalculator PVHWC = PresentValueHullWhiteCalculator.getInstance();
  private static final PresentValueCurveSensitivityHullWhiteCalculator PVCSHWC = PresentValueCurveSensitivityHullWhiteCalculator.getInstance();
  private static final double SHIFT_FD = 1.0E-6;
  private static final SimpleParameterSensitivityParameterCalculator<HullWhiteOneFactorProviderInterface> SPSHWC = new SimpleParameterSensitivityParameterCalculator<>(
      MQCSHWC);
  private static final SimpleParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator SPSHWC_FD = new SimpleParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(MQHWC, SHIFT_FD);
  private static final ParameterSensitivityParameterCalculator<HullWhiteOneFactorProviderInterface> PSHWC = new ParameterSensitivityParameterCalculator<>(PVCSHWC);
  private static final ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator PSHWC_FD = new ParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(PVHWC, SHIFT_FD);

  private static final double TOLERANCE_PRICE = 1.0E-10;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-8;
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;

  @Test
  /**
   * Tests the price versus an explicit formula.
   */
  public void price() {
    final double t0 = ERH3.getTradingLastTime();
    final double delta = ERH3.getFixingPeriodAccrualFactor();
    final double t1 = ERH3.getFixingPeriodStartTime();
    final double t2 = ERH3.getFixingPeriodEndTime();
    final double expiry = OPT_ERH3_CALL_9900.getExpirationTime();
    final double alphaOpt = MODEL_HW.alpha(HW_PARAMETERS, 0.0, expiry, t1, t2);
    final double gammaFut = MODEL_HW.futuresConvexityFactor(HW_PARAMETERS, t0, t1, t2);
    final double ktilde = 1 - STRIKE_1;
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EURIBOR3M, t1, t2, delta);
    final double exerciseBoundary = -1.0 / alphaOpt * (Math.log((1.0 + delta * ktilde) / (1 + delta * forward) / gammaFut) + alphaOpt * alphaOpt / 2.0);
    final double nKC = NORMAL.getCDF(-exerciseBoundary);
    final double nAKC = NORMAL.getCDF(-alphaOpt - exerciseBoundary);
    final double priceCallExpected = (1 - STRIKE_1 + 1.0 / delta) * nKC - 1.0 / delta * (1 + delta * forward) * gammaFut * nAKC;
    final double priceCallComputed = METHOD_OPT_SEC.price(OPT_ERH3_CALL_9900, HW_MULTICURVES);
    assertEquals("InterestRateFutureOptionMarginSecurityHullWhiteMethod: price", priceCallExpected, priceCallComputed, TOLERANCE_PRICE);
    final double nKP = NORMAL.getCDF(exerciseBoundary);
    final double nAKP = NORMAL.getCDF(alphaOpt + exerciseBoundary);
    final double pricePutExpected = 1.0 / delta * (1 + delta * forward) * gammaFut * nAKP - (1 - STRIKE_1 + 1.0 / delta) * nKP;
    final double pricePutComputed = METHOD_OPT_SEC.price(OPT_ERH3_PUT_9900, HW_MULTICURVES);
    assertEquals("InterestRateFutureOptionMarginSecurityHullWhiteMethod: price", pricePutExpected, pricePutComputed, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the price for very deep out-of-the-money options.
   */
  public void priceDeepOTM() {
    final InterestRateFutureOptionMarginSecurityDefinition callDefinition = new InterestRateFutureOptionMarginSecurityDefinition(ERH3_DEFINITION, OPT_EXP_MAR13, 1.25, true);
    final InterestRateFutureOptionMarginSecurity call = callDefinition.toDerivative(REFERENCE_DATE);
    final double priceCallDeep = METHOD_OPT_SEC.price(call, HW_MULTICURVES);
    assertEquals("InterestRateFutureOptionMarginSecurityHullWhiteMethod: price", 0.0, priceCallDeep, TOLERANCE_PRICE);
    final InterestRateFutureOptionMarginSecurityDefinition putDefinition = new InterestRateFutureOptionMarginSecurityDefinition(ERH3_DEFINITION, OPT_EXP_MAR13, 0.75, false);
    final InterestRateFutureOptionMarginSecurity put = putDefinition.toDerivative(REFERENCE_DATE);
    final double pricePutDeep = METHOD_OPT_SEC.price(put, HW_MULTICURVES);
    assertEquals("InterestRateFutureOptionMarginSecurityHullWhiteMethod: price", 0.0, pricePutDeep, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the price positivity for a range of strikes.
   */
  public void pricePositive() {
    final double minStrike = 0.9700;
    final double maxStrike = 1.0100;
    final double nbStrikes = 20;
    for (int loopstrike = 0; loopstrike <= nbStrikes; loopstrike++) {
      final double strike = minStrike + loopstrike * (maxStrike - minStrike) / nbStrikes;
      final InterestRateFutureOptionMarginSecurityDefinition callDefinition = new InterestRateFutureOptionMarginSecurityDefinition(ERH3_DEFINITION, OPT_EXP_MAR13, strike, true);
      final InterestRateFutureOptionMarginSecurity call = callDefinition.toDerivative(REFERENCE_DATE);
      final double priceCall = METHOD_OPT_SEC.price(call, HW_MULTICURVES);
      assertTrue("InterestRateFutureOptionMarginSecurityHullWhiteMethod: price", priceCall > 0);
      final InterestRateFutureOptionMarginSecurityDefinition putDefinition = new InterestRateFutureOptionMarginSecurityDefinition(ERH3_DEFINITION, OPT_EXP_MAR13, strike, false);
      final InterestRateFutureOptionMarginSecurity put = putDefinition.toDerivative(REFERENCE_DATE);
      final double pricePut = METHOD_OPT_SEC.price(put, HW_MULTICURVES);
      assertTrue("InterestRateFutureOptionMarginSecurityHullWhiteMethod: price", pricePut > 0);
    }
  }

  @Test
  /**
   * Tests the Call/Put parity for options on futures at the price level.
   */
  public void priceCallPutParityStandard() {
    final double priceFutures = METHOD_FUT.price(ERH3, HW_MULTICURVES);
    final double priceCall = METHOD_OPT_SEC.price(OPT_ERH3_CALL_9900, HW_MULTICURVES);
    final double pricePut = METHOD_OPT_SEC.price(OPT_ERH3_PUT_9900, HW_MULTICURVES);
    assertEquals("InterestRateFutureOptionMarginSecurityHullWhiteMethod: price", priceCall - pricePut, priceFutures - STRIKE_1, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Tests the Call/Put parity for mid-curve options on futures at the price level.
   */
  public void priceCallPutParityMid() {
    final double priceFutures = METHOD_FUT.price(ERM4, HW_MULTICURVES);
    final double priceCall = METHOD_OPT_SEC.price(OPT_ERM4_MID_CALL_9875, HW_MULTICURVES);
    final double pricePut = METHOD_OPT_SEC.price(OPT_ERM4_MID_PUT_9875, HW_MULTICURVES);
    assertEquals("InterestRateFutureOptionMarginSecurityHullWhiteMethod: price", priceCall - pricePut, priceFutures - STRIKE_2, TOLERANCE_PRICE);
  }

  @Test
  public void priceMethodVsCalculator() {
    final double priceMethod = METHOD_OPT_SEC.price(OPT_ERM4_MID_CALL_9875, HW_MULTICURVES);
    final double priceCalculator = OPT_ERM4_MID_CALL_9875.accept(MQHWC, HW_MULTICURVES);
    assertEquals("InterestRateFutureSecurityHullWhiteProviderMethod: present value - calculator vs method", priceCalculator, priceMethod, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the price curve sensitivity versus a finite difference computation.
   */
  public void priceCurveSensitivity() {
    final SimpleParameterSensitivity pcsExact = SPSHWC.calculateSensitivity(OPT_ERM4_MID_CALL_9875, HW_MULTICURVES, MULTICURVES.getAllNames());
    final SimpleParameterSensitivity pcsFD = SPSHWC_FD.calculateSensitivity(OPT_ERM4_MID_CALL_9875, HW_MULTICURVES);
    AssertSensivityObjects.assertEquals("DeliverableSwapFuturesSecurityHullWhiteMethod: priceCurveSensitivity", pcsExact, pcsFD, TOLERANCE_PRICE_DELTA);
  }

  @Test
  public void priceCurveSensitivityMethodVsCalculator() {
    final MulticurveSensitivity pcsMethod = METHOD_OPT_SEC.priceCurveSensitivity(OPT_ERM4_MID_CALL_9875, HW_MULTICURVES);
    final MulticurveSensitivity pcsCalculator = OPT_ERM4_MID_CALL_9875.accept(MQCSHWC, HW_MULTICURVES);
    AssertSensivityObjects.assertEquals("InterestRateFutureSecurityHullWhiteProviderMethod: present value - calculator vs method", pcsCalculator, pcsMethod, TOLERANCE_PRICE_DELTA);
  }

  @Test
  /**
   * Tests the present value versus an explicit formula.
   */
  public void presentValueStandard() {
    final MultipleCurrencyAmount pvComputed = METHOD_OPT_TRA.presentValue(OPT_ERH3_CALL_9900_TRA_1, HW_MULTICURVES);
    final double price = METHOD_OPT_SEC.price(OPT_ERH3_CALL_9900, HW_MULTICURVES);
    final double pvExpected = (price - LAST_MARGIN_1) * QUANTITY_1 * NOTIONAL * FUTURE_FACTOR;
    assertEquals("InterestRateFutureOptionMarginTransactionHullWhiteMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value versus an explicit formula.
   */
  public void presentValueLongShort() {
    final MultipleCurrencyAmount pvLongStd = METHOD_OPT_TRA.presentValue(OPT_ERH3_CALL_9900_TRA_1, HW_MULTICURVES);
    final MultipleCurrencyAmount pvShortStd = METHOD_OPT_TRA.presentValue(OPT_ERH3_CALL_9900_TRA_2, HW_MULTICURVES);
    assertEquals("InterestRateFutureOptionMarginTransactionHullWhiteMethod: present value", pvLongStd.getAmount(EUR), -pvShortStd.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pvLongMid = METHOD_OPT_TRA.presentValue(OPT_ERM4_MID_CALL_9875_TRA_1, HW_MULTICURVES);
    final MultipleCurrencyAmount pvShortMid = METHOD_OPT_TRA.presentValue(OPT_ERM4_MID_CALL_9875_TRA_2, HW_MULTICURVES);
    assertEquals("InterestRateFutureOptionMarginTransactionHullWhiteMethod: present value", pvLongMid.getAmount(EUR), -pvShortMid.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value versus an explicit formula.
   */
  public void presentValueMidCurve() {
    final MultipleCurrencyAmount pvComputed = METHOD_OPT_TRA.presentValue(OPT_ERM4_MID_CALL_9875_TRA_1, HW_MULTICURVES);
    final double price = METHOD_OPT_SEC.price(OPT_ERM4_MID_CALL_9875, HW_MULTICURVES);
    final double pvExpected = (price - TRADE_PRICE_2) * QUANTITY_2 * NOTIONAL * FUTURE_FACTOR;
    assertEquals("InterestRateFutureOptionMarginTransactionHullWhiteMethod: present value", pvExpected, pvComputed.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value: Put/Call parity.
   */
  public void presentValuePutCallParity() {
    final MultipleCurrencyAmount pvLongCallStd = METHOD_OPT_TRA.presentValue(OPT_ERH3_CALL_9900_TRA_1, HW_MULTICURVES);
    final MultipleCurrencyAmount pvShortPutStd = METHOD_OPT_TRA.presentValue(OPT_ERH3_PUT_9900_TRA_1, HW_MULTICURVES);
    final double priceFuturesH3 = METHOD_FUT.price(ERH3, HW_MULTICURVES);
    assertEquals("InterestRateFutureOptionMarginSecurityHullWhiteMethod: price", pvLongCallStd.getAmount(EUR) - pvShortPutStd.getAmount(EUR), (priceFuturesH3 - STRIKE_1) * QUANTITY_1 * NOTIONAL
        * FUTURE_FACTOR, TOLERANCE_PV);
    final MultipleCurrencyAmount pvLongCallMid = METHOD_OPT_TRA.presentValue(OPT_ERM4_MID_CALL_9875_TRA_1, HW_MULTICURVES);
    final MultipleCurrencyAmount pvShortPutMid = METHOD_OPT_TRA.presentValue(OPT_ERM4_MID_PUT_9875_TRA_1, HW_MULTICURVES);
    final double priceFuturesM4 = METHOD_FUT.price(ERM4, HW_MULTICURVES);
    assertEquals("InterestRateFutureOptionMarginSecurityHullWhiteMethod: price", pvLongCallMid.getAmount(EUR) - pvShortPutMid.getAmount(EUR), (priceFuturesM4 - STRIKE_2) * QUANTITY_2 * NOTIONAL
        * FUTURE_FACTOR, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value versus an explicit formula.
   */
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = METHOD_OPT_TRA.presentValue(OPT_ERH3_CALL_9900_TRA_1, HW_MULTICURVES);
    final MultipleCurrencyAmount pvCalculator = OPT_ERH3_CALL_9900_TRA_1.accept(PVHWC, HW_MULTICURVES);
    assertEquals("InterestRateFutureOptionMarginTransactionHullWhiteMethod: present value", pvMethod.getAmount(EUR), pvCalculator.getAmount(EUR), TOLERANCE_PV);
  }

  @Test
  /**
   * Test the price curve sensitivity versus a finite difference computation.
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pcsExact = PSHWC.calculateSensitivity(OPT_ERM4_MID_CALL_9875_TRA_1, HW_MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pcsFD = PSHWC_FD.calculateSensitivity(OPT_ERM4_MID_CALL_9875_TRA_1, HW_MULTICURVES);
    AssertSensivityObjects.assertEquals("InterestRateFutureOptionMarginTransactionHullWhiteMethod: presentValueCurveSensitivity", pcsExact, pcsFD, TOLERANCE_PV_DELTA);
  }

}
