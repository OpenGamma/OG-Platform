/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map.Entry;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.BondFuturesDataSets;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackBondFuturesCubeSensitivity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.StandardDataSetsBlack;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesFlatProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Triple;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesOptionMarginSecurityBlackFlatMethodTest {

  /** Bond future option: Bobl */
  private static final BondFuturesSecurityDefinition BOBLM4_DEFINITION = BondFuturesDataSets.boblM4Definition();
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 3, 31);
  private static final double STRIKE_125 = 1.25;
  private static final ZonedDateTime EXPIRY_DATE_OPT = DateUtils.getUTCDate(2014, 6, 5);
  private static final ZonedDateTime LAST_TRADING_DATE_OPT = DateUtils.getUTCDate(2014, 6, 4);
  private static final boolean IS_CALL = true;
  private static final BondFuturesOptionMarginSecurityDefinition CALL_BOBL_125_DEFINITION = new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION,
      LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_125, IS_CALL);
  private static final BondFuturesOptionMarginSecurity CALL_BOBL_125 = CALL_BOBL_125_DEFINITION.toDerivative(REFERENCE_DATE);
  /** Black surface expiry/delay */
  final private static InterpolatedDoublesSurface BLACK_SURFACE = StandardDataSetsBlack.blackSurfaceExpiryDelay();
  /** Curves for a specific issuer name */
  private static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVES = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  /** The legal entity */
  private static final LegalEntity[] LEGAL_ENTITIES = IssuerProviderDiscountDataSets.getIssuers();
  private static final LegalEntity LEGAL_ENTITY_GERMANY = LEGAL_ENTITIES[2];
  /** The Black bond futures provider **/
  private static final BlackBondFuturesFlatProviderDiscount BLACK_FLAT_BNDFUT = new BlackBondFuturesFlatProviderDiscount(ISSUER_SPECIFIC_MULTICURVES,
      BLACK_SURFACE, LEGAL_ENTITY_GERMANY);
  /** Methods and calculators */
  private static final BondFuturesOptionMarginSecurityBlackBondFuturesMethod METHOD_OPT = BondFuturesOptionMarginSecurityBlackBondFuturesMethod.getInstance();
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUTURE = BondFuturesSecurityDiscountingMethod.getInstance();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /** Tolerances */
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_DELTA = 1.0E-8;

  public void impliedVolatility() {
    final double delay = CALL_BOBL_125.getUnderlyingFuture().getNoticeLastTime() - CALL_BOBL_125.getExpirationTime();
    final double expiry = CALL_BOBL_125.getExpirationTime();
    final double ivExpected = BLACK_SURFACE.getZValue(expiry, delay);
    final double ivComputed = METHOD_OPT.impliedVolatility(CALL_BOBL_125, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: impliedVolatility", ivExpected, ivComputed, TOLERANCE_RATE);
  }

  public void futurePrice() {
    final double priceExpected = METHOD_FUTURE.price(CALL_BOBL_125.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final double priceComputed = METHOD_OPT.underlyingFuturePrice(CALL_BOBL_125, ISSUER_SPECIFIC_MULTICURVES);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: underlying futures price", priceExpected, priceComputed, TOLERANCE_RATE);
  }

  public void priceFromFuturesPrice() {
    final double price = 1.26;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE_125, CALL_BOBL_125.getExpirationTime(), CALL_BOBL_125.isCall());
    final double volatility = METHOD_OPT.impliedVolatility(CALL_BOBL_125, BLACK_FLAT_BNDFUT);
    final BlackFunctionData dataBlack = new BlackFunctionData(price, 1.0, volatility);
    final double priceExpected = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    final double priceComputed = METHOD_OPT.price(CALL_BOBL_125, BLACK_FLAT_BNDFUT, price);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: underlying futures price", priceExpected, priceComputed, TOLERANCE_RATE);
  }

  public void priceFromCurves() {
    final double priceFutures = METHOD_FUTURE.price(CALL_BOBL_125.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final double priceExpected = METHOD_OPT.price(CALL_BOBL_125, BLACK_FLAT_BNDFUT, priceFutures);
    final double priceComputed = METHOD_OPT.price(CALL_BOBL_125, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: underlying futures price", priceExpected, priceComputed, TOLERANCE_RATE);
  }

  public void priceBlackSensitivity() {
    final double priceFutures = METHOD_FUTURE.price(CALL_BOBL_125.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE_125, CALL_BOBL_125.getExpirationTime(), CALL_BOBL_125.isCall());
    final double expiry = CALL_BOBL_125.getExpirationTime();
    final double delay = CALL_BOBL_125.getUnderlyingFuture().getNoticeLastTime() - CALL_BOBL_125.getExpirationTime();
    final double volatility = BLACK_SURFACE.getZValue(expiry, delay);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatility);
    final double[] priceAD = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final double vega = priceAD[2];
    final PresentValueBlackBondFuturesCubeSensitivity vegaComputed = METHOD_OPT.priceBlackSensitivity(CALL_BOBL_125, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: Black parameters sensitivity", vega, vegaComputed.getSensitivity().toSingleValue(), TOLERANCE_DELTA);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: Black parameters sensitivity", 1, vegaComputed.getSensitivity().getMap().size());
    final Entry<Triple<Double, Double, Double>, Double> point = vegaComputed.getSensitivity().getMap().entrySet().iterator().next();
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: Black parameters sensitivity", CALL_BOBL_125.getExpirationTime(), point.getKey().getFirst(), TOLERANCE_RATE);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: Black parameters sensitivity",
        CALL_BOBL_125.getUnderlyingFuture().getTradingLastTime() - CALL_BOBL_125.getExpirationTime(), point.getKey().getSecond(), TOLERANCE_RATE);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: Black parameters sensitivity", CALL_BOBL_125.getStrike(), point.getKey().getThird(), TOLERANCE_RATE);
  }

}
