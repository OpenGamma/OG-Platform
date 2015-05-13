/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;


import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.BondFuturesDataSets;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.StandardDataSetsBlack;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesExpStrikeProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing methods for bond future options with up-front premium payment.
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesOptionPremiumSecurityBlackExpStrikeMethodTest {

  /** Bond future option: JGB */
  private static final BondFuturesSecurityDefinition JBM5_DEFINITION = BondFuturesDataSets.JBM5_DEFINITION;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2015, 5, 12);
  private static final double STRIKE_147 = 1.47; // To be close to ATM for the data set used.
  private static final ZonedDateTime EXPIRY_DATE_OPT = DateUtils.getUTCDate(2015, 5, 31);
  private static final boolean IS_CALL = true;
  private static final BondFuturesOptionPremiumSecurityDefinition CALL_JB_147_DEFINITION = 
      new BondFuturesOptionPremiumSecurityDefinition(JBM5_DEFINITION, EXPIRY_DATE_OPT, STRIKE_147, IS_CALL);
  private static final BondFuturesOptionPremiumSecurityDefinition PUT_JB_147_DEFINITION = 
      new BondFuturesOptionPremiumSecurityDefinition(JBM5_DEFINITION, EXPIRY_DATE_OPT, STRIKE_147, !IS_CALL);
  private static final BondFuturesOptionPremiumSecurity CALL_JB_147 = CALL_JB_147_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesOptionPremiumSecurity PUT_JB_147 = PUT_JB_147_DEFINITION.toDerivative(REFERENCE_DATE);
  /** Black surface expiry/log-moneyness */
  final private static InterpolatedDoublesSurface BLACK_SURFACE_EXP_STRIKE = StandardDataSetsBlack.BLACK_SURFACE_BND_EXP_STRIKE;
  /** Curves for a specific issuer name */
  private static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVES = 
      IssuerProviderDiscountDataSets.ISSUER_SPECIFIC_MULTICURVE_JP;
  /** The legal entity */
  private static final LegalEntity LEGAL_ENTITY_JAPAN = IssuerProviderDiscountDataSets.JP_GOVT;
  /** The Black bond futures provider **/ //TODO: Change to strike
  private static final BlackBondFuturesExpStrikeProvider BLACK_EXP_STRIKE_BNDFUT =
      new BlackBondFuturesExpStrikeProvider(ISSUER_SPECIFIC_MULTICURVES, BLACK_SURFACE_EXP_STRIKE, LEGAL_ENTITY_JAPAN);
  /** Methods and calculators */
  private static final BondFuturesOptionPremiumSecurityBlackBondFuturesMethod METHOD_OPT = 
      BondFuturesOptionPremiumSecurityBlackBondFuturesMethod.getInstance();
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUTURE = 
      BondFuturesSecurityDiscountingMethod.getInstance();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /** Tolerances */
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_DELTA = 1.0E-8;

  public void impliedVolatility() {
    final double strike = STRIKE_147;
    final double expiry = CALL_JB_147.getExpirationTime();
    final double ivExpected = BLACK_SURFACE_EXP_STRIKE.getZValue(expiry, strike);
    final double ivComputed = METHOD_OPT.impliedVolatility(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals("BondFuturesOptionPremiumSecurityBlackSurfaceMethod: impliedVolatility", ivExpected, ivComputed, TOLERANCE_RATE);
  }

  public void futurePrice() {
    final double priceExpected = METHOD_FUTURE.price(CALL_JB_147.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final double priceComputed = METHOD_OPT.underlyingFuturePrice(CALL_JB_147, ISSUER_SPECIFIC_MULTICURVES);
    assertEquals("BondFuturesOptionPremiumSecurityBlackBondFuturesMethod: underlying futures price", priceExpected, priceComputed, TOLERANCE_RATE);
  }

  public void priceFromFuturesPrice() {
    final double price = 1.465;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE_147, CALL_JB_147.getExpirationTime(), CALL_JB_147.isCall());
    final double logmoney = Math.log(STRIKE_147 / price);
    final double expiry = CALL_JB_147.getExpirationTime();
    final double volatility = BLACK_SURFACE_EXP_STRIKE.getZValue(expiry, logmoney);
    double df = ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider().getDiscountFactor(JBM5_DEFINITION.getCurrency(), expiry);
    final BlackFunctionData dataBlack = new BlackFunctionData(price, df, volatility);
    final double priceExpected = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    final double priceComputed = METHOD_OPT.priceFromUnderlyingPrice(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT, price);
    assertEquals("BondFuturesOptionPremiumSecurityBlackBondFuturesMethod: underlying futures price", 
        priceExpected, priceComputed, TOLERANCE_RATE);
  }

  public void priceFromCurves() {
    final double priceFutures = METHOD_FUTURE.price(CALL_JB_147.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final double priceExpected = METHOD_OPT.priceFromUnderlyingPrice(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT, priceFutures);
    final double priceComputed = METHOD_OPT.price(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals("BondFuturesOptionPremiumSecurityBlackBondFuturesMethod: underlying futures price", priceExpected, priceComputed, TOLERANCE_RATE);
  }

  public void putCallParity() {
    final double priceFutures = METHOD_FUTURE.price(CALL_JB_147.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final double priceCallComputed = METHOD_OPT.price(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    final double pricePutComputed = METHOD_OPT.price(PUT_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    double df = ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider()
        .getDiscountFactor(JBM5_DEFINITION.getCurrency(), CALL_JB_147.getExpirationTime());
    assertEquals("BondFuturesOptionPremiumSecurityBlackBondFuturesMethod: put call parity price", 
        priceCallComputed - pricePutComputed, (priceFutures - STRIKE_147) * df, TOLERANCE_RATE);
  }

  public void vega() {
    final double priceFutures = METHOD_FUTURE.price(CALL_JB_147.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE_147, CALL_JB_147.getExpirationTime(), CALL_JB_147.isCall());
    final double expiry = CALL_JB_147.getExpirationTime();
    final double volatility = BLACK_SURFACE_EXP_STRIKE.getZValue(expiry, STRIKE_147);
    double df = ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider()
        .getDiscountFactor(JBM5_DEFINITION.getCurrency(), CALL_JB_147.getExpirationTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, df, volatility);
    final double[] priceAD = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final double vegaExpected = priceAD[2];
    final double vegaComputed = METHOD_OPT.vega(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals("BondFuturesOptionPremiumSecurityBlackBondFuturesMethod: Black parameters sensitivity", 
        vegaExpected, vegaComputed, TOLERANCE_RATE);
  }

  public void delta() {
    final double priceFutures = METHOD_FUTURE.price(CALL_JB_147.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE_147, CALL_JB_147.getExpirationTime(),
        CALL_JB_147.isCall());
    final double expiry = CALL_JB_147.getExpirationTime();
    final double volatility = BLACK_SURFACE_EXP_STRIKE.getZValue(expiry, STRIKE_147);
    double df = ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider()
        .getDiscountFactor(JBM5_DEFINITION.getCurrency(), CALL_JB_147.getExpirationTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, df, volatility);
    final double[] priceAD = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final double deltaCallExpected = priceAD[1];
    final double deltaCallComputed = METHOD_OPT.delta(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals("BondFuturesOptionPremiumSecurityBlackBondFuturesMethod: delta",
        deltaCallExpected, deltaCallComputed, TOLERANCE_DELTA);
    final double deltaPutComputed = METHOD_OPT.delta(PUT_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals("BondFuturesOptionPremiumSecurityBlackBondFuturesMethod: delta",
        deltaCallExpected - deltaPutComputed, df, TOLERANCE_DELTA);
  }

  public void gamma() {
    final double priceFutures = METHOD_FUTURE.price(CALL_JB_147.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final EuropeanVanillaOption option =
        new EuropeanVanillaOption(STRIKE_147, CALL_JB_147.getExpirationTime(), CALL_JB_147.isCall());
    final double expiry = CALL_JB_147.getExpirationTime();
    final double volatility = BLACK_SURFACE_EXP_STRIKE.getZValue(expiry, STRIKE_147);
    double df = ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider()
        .getDiscountFactor(JBM5_DEFINITION.getCurrency(), CALL_JB_147.getExpirationTime());
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, df, volatility);
    final double[] firstDerivs = new double[3];
    final double[][] secondDerivs = new double[3][3];
    BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, firstDerivs, secondDerivs);
    final double gammaCallExpected = secondDerivs[0][0];
    final double gammaCallComputed = METHOD_OPT.gamma(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals("BondFuturesOptionPremiumSecurityBlackBondFuturesMethod: gamma",
        gammaCallExpected, gammaCallComputed, TOLERANCE_DELTA);
  }

  public void theta() {
    double priceFutures = METHOD_FUTURE.price(CALL_JB_147.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    double expiry = CALL_JB_147.getExpirationTime();
    double volatility = BLACK_SURFACE_EXP_STRIKE.getZValue(expiry, STRIKE_147);
    double rate = -Math.log(ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider()
        .getDiscountFactor(CALL_JB_147.getCurrency(), CALL_JB_147.getExpirationTime())) / CALL_JB_147.getExpirationTime();
    double thetaCallExpected = BlackFormulaRepository.theta(priceFutures, STRIKE_147, CALL_JB_147.getExpirationTime(), 
        volatility, CALL_JB_147.isCall(), rate);
    double thetaCallComputed = METHOD_OPT.theta(CALL_JB_147, BLACK_EXP_STRIKE_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: theta", 
        thetaCallExpected, thetaCallComputed, TOLERANCE_DELTA);
  }

}
