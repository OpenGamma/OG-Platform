/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.ConvexityAdjustmentHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.MarketQuoteCurveSensitivityHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.calculator.hullwhite.MarketQuoteHullWhiteCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteDataSets;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.hullwhite.SimpleParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.SimpleParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of deliverable interest rate swap futures as traded on CME.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFuturesPriceDeliverableSecurityHullWhiteMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex USDLIBOR3M = INDEX_LIST[2];
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final Calendar NYC = MulticurveProviderDiscountDataSets.getUSDCalendar();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2012, 12, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, -USD6MLIBOR3M.getSpotLag(), NYC);
  private static final Period TENOR = Period.ofYears(10);
  private static final double NOTIONAL = 100000;
  private static final double RATE = 0.0175;
  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(EFFECTIVE_DATE, TENOR, USD6MLIBOR3M, 1.0, RATE, false);
  private static final SwapFuturesPriceDeliverableSecurityDefinition SWAP_FUTURES_SECURITY_DEFINITION = new SwapFuturesPriceDeliverableSecurityDefinition(LAST_TRADING_DATE, SWAP_DEFINITION, NOTIONAL);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 9, 20);
  private static final SwapFuturesPriceDeliverableSecurity SWAP_FUTURES_SECURITY = SWAP_FUTURES_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteDataSets.createHullWhiteParameters();
  private static final HullWhiteOneFactorProviderDiscount HW_MULTICURVES = new HullWhiteOneFactorProviderDiscount(MULTICURVES, PARAMETERS_HW, USD);

  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  private static final SwapFuturesPriceDeliverableSecurityHullWhiteMethod METHOD_SWAP_FUT_HW = SwapFuturesPriceDeliverableSecurityHullWhiteMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final MarketQuoteHullWhiteCalculator MQHWC = MarketQuoteHullWhiteCalculator.getInstance();
  private static final MarketQuoteCurveSensitivityHullWhiteCalculator MQCSHWC = MarketQuoteCurveSensitivityHullWhiteCalculator.getInstance();
  private static final ConvexityAdjustmentHullWhiteCalculator CAHWC = ConvexityAdjustmentHullWhiteCalculator.getInstance();

  private static final double SHIFT_FD = 1.0E-6;

  private static final SimpleParameterSensitivityParameterCalculator<HullWhiteOneFactorProviderInterface> PS_MQ_C = new SimpleParameterSensitivityParameterCalculator<>(
      MQCSHWC);
  private static final SimpleParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator PS_MQ_FDC = new SimpleParameterSensitivityHullWhiteDiscountInterpolatedFDCalculator(MQHWC, SHIFT_FD);

  private static final double TOLERANCE_PRICE = 1.0E-10;
  private static final double TOLERANCE_PRICE_DELTA = 1.0E-8;

  @Test
  public void price() {
    final AnnuityPaymentFixed cfe = SWAP_FUTURES_SECURITY.getUnderlyingSwap().accept(CFEC, MULTICURVES);
    final int nbCfe = cfe.getNumberOfPayments();
    final double[] adj = new double[nbCfe];
    final double[] df = new double[nbCfe];
    for (int loopcf = 0; loopcf < nbCfe; loopcf++) {
      df[loopcf] = MULTICURVES.getDiscountFactor(USD, cfe.getNthPayment(loopcf).getPaymentTime());
      adj[loopcf] = MODEL.futuresConvexityFactor(PARAMETERS_HW, SWAP_FUTURES_SECURITY.getTradingLastTime(), cfe.getNthPayment(loopcf).getPaymentTime(), SWAP_FUTURES_SECURITY.getDeliveryTime());
      assertTrue("DeliverableSwapFuturesSecurityHullWhiteMethod: price", adj[loopcf] <= 1.0);
    }
    double priceExpected = 1.0;
    for (int loopcf = 0; loopcf < nbCfe; loopcf++) {
      priceExpected += (cfe.getNthPayment(loopcf).getAmount() * df[loopcf] * adj[loopcf]) / df[0];
    }
    final double priceComputed = METHOD_SWAP_FUT_HW.price(SWAP_FUTURES_SECURITY, HW_MULTICURVES);
    assertEquals("DeliverableSwapFuturesSecurityDefinition: price", priceExpected, priceComputed, TOLERANCE_PRICE);
  }

  @Test
  public void convexityAdjustment() {
    final double convexityAdjustment = METHOD_SWAP_FUT_HW.convexityAdjustment(SWAP_FUTURES_SECURITY, HW_MULTICURVES);
    final double price = METHOD_SWAP_FUT_HW.price(SWAP_FUTURES_SECURITY, HW_MULTICURVES);
    final double pvSwap = SWAP_FUTURES_SECURITY.getUnderlyingSwap().accept(PVDC, MULTICURVES).getAmount(USD);
    assertEquals("DeliverableSwapFuturesSecurityDefinition: convexity adjustment", price - (1.0d + pvSwap), convexityAdjustment, TOLERANCE_PRICE);
    final double caCalculator = SWAP_FUTURES_SECURITY.accept(CAHWC, HW_MULTICURVES);
    assertEquals("DeliverableSwapFuturesSecurityDefinition: convexity adjustment", caCalculator, convexityAdjustment, TOLERANCE_PRICE);
  }

  @Test(enabled = false)
  // TODO
  public void priceCurveSensitivity() {
    final SimpleParameterSensitivity pcsExact = PS_MQ_C.calculateSensitivity(SWAP_FUTURES_SECURITY, HW_MULTICURVES, MULTICURVES.getAllNames());
    final SimpleParameterSensitivity pcsFD = PS_MQ_FDC.calculateSensitivity(SWAP_FUTURES_SECURITY, HW_MULTICURVES);
    AssertSensivityObjects.assertEquals("DeliverableSwapFuturesSecurityHullWhiteMethod: priceCurveSensitivity", pcsExact, pcsFD, TOLERANCE_PRICE_DELTA);
  }

}
