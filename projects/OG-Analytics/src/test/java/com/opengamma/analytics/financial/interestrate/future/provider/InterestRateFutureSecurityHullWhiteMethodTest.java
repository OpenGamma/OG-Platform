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
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ConvexityAdjustmentHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.MarketQuoteCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.MarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ParRateHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.SimpleParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.SimpleParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for the methods related to interest rate securities pricing with Hull-White model convexity adjustment.
 */
@Test(groups = TestGroup.UNIT)
public class InterestRateFutureSecurityHullWhiteMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = INDEX_LIST[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();
  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -EURIBOR3M.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 12);
  private static final InterestRateFutureSecurityDefinition ERU2_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);

  private static final InterestRateFutureSecurity ERU2 = ERU2_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double MEAN_REVERSION = 0.01;
  private static final double[] VOLATILITY = new double[] {0.01, 0.011, 0.012, 0.013, 0.014 };
  private static final double[] VOLATILITY_TIME = new double[] {0.5, 1.0, 2.0, 5.0 };
  private static final HullWhiteOneFactorPiecewiseConstantParameters MODEL_PARAMETERS = new HullWhiteOneFactorPiecewiseConstantParameters(MEAN_REVERSION, VOLATILITY, VOLATILITY_TIME);

  private static final HullWhiteOneFactorProviderDiscount HW_MULTICURVES = new HullWhiteOneFactorProviderDiscount(MULTICURVES, MODEL_PARAMETERS, EUR);
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_IRFUT_HW = InterestRateFutureSecurityHullWhiteMethod.getInstance();

  private static final MarketQuoteHullWhiteCalculator MQHWC = MarketQuoteHullWhiteCalculator.getInstance();
  private static final MarketQuoteCurveSensitivityHullWhiteCalculator MQCSHWC = MarketQuoteCurveSensitivityHullWhiteCalculator.getInstance();
  private static final ConvexityAdjustmentHullWhiteCalculator CAHWC = ConvexityAdjustmentHullWhiteCalculator.getInstance();
  private static final ParRateHullWhiteCalculator PRHWC = ParRateHullWhiteCalculator.getInstance();

  private static final double SHIFT_FD = 1.0E-6;
  private static final SimpleParameterSensitivityParameterCalculator<HullWhiteOneFactorProviderInterface> SPSHWC = new SimpleParameterSensitivityParameterCalculator<>(
      MQCSHWC);
  private static final SimpleParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator SPSHWC_FD = new SimpleParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(MQHWC, SHIFT_FD);

  private static final double TOLERANCE_PRICE = 1.0E-10;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-8;

  @Test
  /**
   * Test the price computed from the curves and HW parameters.
   */
  public void price() {
    final double price = METHOD_IRFUT_HW.price(ERU2, HW_MULTICURVES);
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EURIBOR3M, ERU2.getFixingPeriodStartTime(), ERU2.getFixingPeriodEndTime(), ERU2.getFixingPeriodAccrualFactor());
    final double factor = MODEL.futuresConvexityFactor(MODEL_PARAMETERS, ERU2.getTradingLastTime(), ERU2.getFixingPeriodStartTime(), ERU2.getFixingPeriodEndTime());
    final double expectedPrice = 1.0 - factor * forward + (1 - factor) / ERU2.getFixingPeriodAccrualFactor();
    assertEquals("InterestRateFutureSecurityHullWhiteProviderMethod: price", expectedPrice, price, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the par rate computed from the curves and HW parameters. Par rate = 1-price.
   */
  public void parRate() {
    final double price = METHOD_IRFUT_HW.price(ERU2, HW_MULTICURVES);
    final double parRateExpected = 1.0d - price;
    final double parRateComputed = METHOD_IRFUT_HW.parRate(ERU2, HW_MULTICURVES);
    assertEquals("InterestRateFutureSecurityHullWhiteProviderMethod: parRate", parRateExpected, parRateComputed, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the par rate computed from the method and the calculator.
   */
  public void parRateMethodVsCalculator() {
    final double parRateMethod = METHOD_IRFUT_HW.parRate(ERU2, HW_MULTICURVES);
    final double parRateCalculator = ERU2.accept(PRHWC, HW_MULTICURVES);
    assertEquals("InterestRateFutureSecurityHullWhiteProviderMethod: parRate", parRateMethod, parRateCalculator, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the price as "MarketQuote"
   */
  public void marketQuote() {
    final double priceMethod = METHOD_IRFUT_HW.price(ERU2, HW_MULTICURVES);
    final double marketQuote = ERU2.accept(MQHWC, HW_MULTICURVES);
    assertEquals("InterestRateFutureSecurityHullWhiteProviderMethod: price", priceMethod, marketQuote, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the convexity adjustment
   */
  public void convexityAdjustment() {
    final double price = METHOD_IRFUT_HW.price(ERU2, HW_MULTICURVES);
    final double forward = MULTICURVES.getSimplyCompoundForwardRate(EURIBOR3M, ERU2.getFixingPeriodStartTime(), ERU2.getFixingPeriodEndTime(), ERU2.getFixingPeriodAccrualFactor());
    final double convexityAdjustment = METHOD_IRFUT_HW.convexityAdjustment(ERU2, HW_MULTICURVES);
    assertEquals("InterestRateFutureSecurityHullWhiteProviderMethod: convexity adjustment", price - (1.0d - forward), convexityAdjustment, TOLERANCE_PRICE);
    final double caCalculator = ERU2.accept(CAHWC, HW_MULTICURVES);
    assertEquals("DeliverableSwapFuturesSecurityDefinition: convexity adjustment", caCalculator, convexityAdjustment, TOLERANCE_PRICE);
  }

  @Test
  /**
   * Test the price curve sensitivity versus a finite difference computation.
   */
  public void priceCurveSensitivity() {
    final SimpleParameterSensitivity pcsExact = SPSHWC.calculateSensitivity(ERU2, HW_MULTICURVES, MULTICURVES.getAllNames());
    final SimpleParameterSensitivity pcsFD = SPSHWC_FD.calculateSensitivity(ERU2, HW_MULTICURVES);
    AssertSensitivityObjects.assertEquals("DeliverableSwapFuturesSecurityHullWhiteMethod: priceCurveSensitivity", pcsExact, pcsFD, TOLERANCE_PRICE_DELTA);
  }

}
