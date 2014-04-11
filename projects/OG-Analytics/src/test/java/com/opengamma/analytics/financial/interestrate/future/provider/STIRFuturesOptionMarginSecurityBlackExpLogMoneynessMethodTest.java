/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map.Entry;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackSTIRFuturesCubeSensitivity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.StandardDataSetsBlack;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesExpLogMoneynessProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Triple;

public class STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethodTest {

  /** Option on STIR futures */
  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
  private static final IborIndex EURIBOR3M = MASTER_IBOR_INDEX.getIndex("EURIBOR3M");
  private static final ZonedDateTime LAST_TRADE_DATE = DateUtils.getUTCDate(2014, 12, 15);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERZ4";
  private static final Calendar TARGET = new CalendarTarget("TARGET");
  private static final InterestRateFutureSecurityDefinition ERZ4_DEFINITION =
      new InterestRateFutureSecurityDefinition(LAST_TRADE_DATE, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, TARGET);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2014, 11, 17);
  private static final double STRIKE_099 = 0.99;
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurityDefinition CALL_ERZ4_099_DEFINITION =
      new InterestRateFutureOptionMarginSecurityDefinition(ERZ4_DEFINITION, EXPIRY_DATE, STRIKE_099, IS_CALL);
  private static final InterestRateFutureOptionMarginSecurityDefinition PUT_ERZ4_099_DEFINITION =
      new InterestRateFutureOptionMarginSecurityDefinition(ERZ4_DEFINITION, EXPIRY_DATE, STRIKE_099, !IS_CALL);
  /** Black surface expiry/log-moneyness */
  final private static InterpolatedDoublesSurface BLACK_SURFACE_LOGMONEY = StandardDataSetsBlack.blackSurfaceExpiryLogMoneyness();
  /** EUR curves */
  final private static MulticurveProviderDiscount MULTICURVE = MulticurveProviderDiscountDataSets.createMulticurveEUR();
  final private static BlackSTIRFuturesExpLogMoneynessProvider MULTICURVE_BLACK = new BlackSTIRFuturesExpLogMoneynessProvider(MULTICURVE, BLACK_SURFACE_LOGMONEY, EURIBOR3M);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 4, 10);
  private static final InterestRateFutureOptionMarginSecurity CALL_ERZ4_099 = CALL_ERZ4_099_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final InterestRateFutureOptionMarginSecurity PUT_ERZ4_099 = PUT_ERZ4_099_DEFINITION.toDerivative(REFERENCE_DATE);
  /** Methods and calculators */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURE = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityBlackSTIRFuturesMethod METHOD_OPT =
      InterestRateFutureOptionMarginSecurityBlackSTIRFuturesMethod.getInstance();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /** Tolerances */
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_DELTA = 1.0E-8;

  @Test
  public void impliedVolatility() {
    final double priceFutures = METHOD_FUTURE.price(CALL_ERZ4_099.getUnderlyingFuture(), MULTICURVE);
    final double rateFutures = 1.0d - priceFutures;
    final double rateStrike = 1.0d - STRIKE_099;
    final double logmoney = Math.log(rateStrike / rateFutures);
    final double expiry = CALL_ERZ4_099.getExpirationTime();
    final double ivExpected = BLACK_SURFACE_LOGMONEY.getZValue(expiry, logmoney);
    final double ivComputed = METHOD_OPT.impliedVolatility(CALL_ERZ4_099, MULTICURVE_BLACK);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: impliedVolatility", ivExpected, ivComputed, TOLERANCE_RATE);
  }

  @Test
  public void futurePrice() {
    final double priceExpected = METHOD_FUTURE.price(CALL_ERZ4_099.getUnderlyingFuture(), MULTICURVE);
    final double priceComputed = METHOD_OPT.underlyingFuturesPrice(CALL_ERZ4_099, MULTICURVE_BLACK);
    final double priceComputed2 = METHOD_OPT.underlyingFuturesPrice(CALL_ERZ4_099, MULTICURVE);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: underlying futures price", priceExpected, priceComputed, TOLERANCE_RATE);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: underlying futures price", priceExpected, priceComputed2, TOLERANCE_RATE);
  }

  @Test
  public void priceFromFuturesPrice() {
    final double priceFutures = 0.9875;
    final double rateFutures = 1.0d - priceFutures;
    final double rateStrike = 1.0d - STRIKE_099;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, CALL_ERZ4_099.getExpirationTime(), !CALL_ERZ4_099.isCall());
    final double logmoney = Math.log(rateStrike / rateFutures);
    final double expiry = CALL_ERZ4_099.getExpirationTime();
    final double volatility = BLACK_SURFACE_LOGMONEY.getZValue(expiry, logmoney);
    final BlackFunctionData dataBlack = new BlackFunctionData(rateFutures, 1.0, volatility);
    final double priceExpected = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    final double priceComputed = METHOD_OPT.price(CALL_ERZ4_099, MULTICURVE_BLACK, priceFutures);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: underlying futures price", priceExpected, priceComputed, TOLERANCE_RATE);
  }

  @Test
  public void priceFromCurves() {
    final double priceFutures = METHOD_FUTURE.price(CALL_ERZ4_099.getUnderlyingFuture(), MULTICURVE);
    final double priceExpected = METHOD_OPT.price(CALL_ERZ4_099, MULTICURVE_BLACK, priceFutures);
    final double priceComputed = METHOD_OPT.price(CALL_ERZ4_099, MULTICURVE_BLACK);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: price", priceExpected, priceComputed, TOLERANCE_RATE);
  }

  @Test
  public void putCallParity() {
    final double priceFutures = METHOD_FUTURE.price(CALL_ERZ4_099.getUnderlyingFuture(), MULTICURVE);
    final double priceCallComputed = METHOD_OPT.price(CALL_ERZ4_099, MULTICURVE_BLACK);
    final double pricePutComputed = METHOD_OPT.price(PUT_ERZ4_099, MULTICURVE_BLACK);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: put call parity price", priceCallComputed - pricePutComputed,
        priceFutures - STRIKE_099, TOLERANCE_RATE);
  }

  @Test
  public void priceBlackSensitivity() {
    final double priceFutures = METHOD_FUTURE.price(CALL_ERZ4_099.getUnderlyingFuture(), MULTICURVE);
    final double rateFutures = 1.0d - priceFutures;
    final double rateStrike = 1.0d - STRIKE_099;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, CALL_ERZ4_099.getExpirationTime(), !CALL_ERZ4_099.isCall());
    final double logmoney = Math.log(rateStrike / rateFutures);
    final double expiry = CALL_ERZ4_099.getExpirationTime();
    final double volatility = BLACK_SURFACE_LOGMONEY.getZValue(expiry, logmoney);
    final BlackFunctionData dataBlack = new BlackFunctionData(rateFutures, 1.0, volatility);
    final double[] priceAD = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final double vega = priceAD[2];
    final PresentValueBlackSTIRFuturesCubeSensitivity vegaComputed = METHOD_OPT.priceBlackSensitivity(CALL_ERZ4_099, MULTICURVE_BLACK);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: Black parameters sensitivity", vega, vegaComputed.getSensitivity().toSingleValue(), TOLERANCE_DELTA);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: Black parameters sensitivity", 1, vegaComputed.getSensitivity().getMap().size());
    final Entry<Triple<Double, Double, Double>, Double> point = vegaComputed.getSensitivity().getMap().entrySet().iterator().next();
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: Black parameters sensitivity", CALL_ERZ4_099.getExpirationTime(), point.getKey().getFirst(), TOLERANCE_RATE);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: Black parameters sensitivity",
        CALL_ERZ4_099.getUnderlyingFuture().getTradingLastTime() - CALL_ERZ4_099.getExpirationTime(), point.getKey().getSecond(), TOLERANCE_RATE);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: Black parameters sensitivity", CALL_ERZ4_099.getStrike(), point.getKey().getThird(), TOLERANCE_RATE);
  }

  @Test
  public void theoreticalDelta() {
    final double priceFutures = METHOD_FUTURE.price(CALL_ERZ4_099.getUnderlyingFuture(), MULTICURVE);
    final double rateFutures = 1.0d - priceFutures;
    final double rateStrike = 1.0d - STRIKE_099;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, CALL_ERZ4_099.getExpirationTime(), !CALL_ERZ4_099.isCall());
    final double logmoney = Math.log(rateStrike / rateFutures);
    final double expiry = CALL_ERZ4_099.getExpirationTime();
    final double volatility = BLACK_SURFACE_LOGMONEY.getZValue(expiry, logmoney);
    final BlackFunctionData dataBlack = new BlackFunctionData(rateFutures, 1.0, volatility);
    final double[] priceAD = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final double deltaCallExpected = -priceAD[1];
    final double deltaCallComputed = METHOD_OPT.deltaUnderlyingPrice(CALL_ERZ4_099, MULTICURVE_BLACK);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: delta", deltaCallExpected, deltaCallComputed, TOLERANCE_DELTA);
    assertTrue("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: delta", (0.0d < deltaCallComputed) && (deltaCallComputed < 1.0d));
    final double deltaPutComputed = METHOD_OPT.deltaUnderlyingPrice(PUT_ERZ4_099, MULTICURVE_BLACK);
    assertEquals("STIRFuturesOptionMarginSecurityBlackExpLogMoneynessMethod: delta", deltaCallExpected - 1.0d, deltaPutComputed, TOLERANCE_DELTA);
  }

}
