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

import com.opengamma.analytics.financial.instrument.future.BondFuturesDataSets;
import com.opengamma.analytics.financial.instrument.future.BondFuturesOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackBondFuturesCubeSensitivity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.provider.description.IssuerProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.StandardDataSetsBlack;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesExpLogMoneynessProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Triple;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesOptionMarginSecurityBlackExpLogMoneynessMethodTest {

  /** Bond future option: Bobl */
  private static final BondFuturesSecurityDefinition BOBLM4_DEFINITION = BondFuturesDataSets.boblM4Definition();
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 3, 31);
  private static final double STRIKE_116 = 1.16; // To be close to ATM for the data set used.
  private static final ZonedDateTime EXPIRY_DATE_OPT = DateUtils.getUTCDate(2014, 6, 5);
  private static final ZonedDateTime LAST_TRADING_DATE_OPT = DateUtils.getUTCDate(2014, 6, 4);
  private static final boolean IS_CALL = true;
  private static final BondFuturesOptionMarginSecurityDefinition CALL_BOBL_116_DEFINITION = new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION,
      LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_116, IS_CALL);
  private static final BondFuturesOptionMarginSecurityDefinition PUT_BOBL_116_DEFINITION = new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION,
      LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_116, !IS_CALL);
  private static final BondFuturesOptionMarginSecurity CALL_BOBL_116 = CALL_BOBL_116_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final BondFuturesOptionMarginSecurity PUT_BOBL_116 = PUT_BOBL_116_DEFINITION.toDerivative(REFERENCE_DATE);
  /** Black surface expiry/log-moneyness */
  final private static InterpolatedDoublesSurface BLACK_SURFACE = StandardDataSetsBlack.blackSurfaceExpiryLogMoneyness();
  /** Curves for a specific issuer name */
  private static final IssuerProviderDiscount ISSUER_SPECIFIC_MULTICURVES = IssuerProviderDiscountDataSets.getIssuerSpecificProvider();
  /** The legal entity */
  private static final LegalEntity[] LEGAL_ENTITIES = IssuerProviderDiscountDataSets.getIssuers();
  private static final LegalEntity LEGAL_ENTITY_GERMANY = LEGAL_ENTITIES[2];
  /** The Black bond futures provider **/
  private static final BlackBondFuturesExpLogMoneynessProviderDiscount BLACK_FLAT_BNDFUT =
      new BlackBondFuturesExpLogMoneynessProviderDiscount(ISSUER_SPECIFIC_MULTICURVES, BLACK_SURFACE, LEGAL_ENTITY_GERMANY);
  /** Methods and calculators */
  private static final BondFuturesOptionMarginSecurityBlackBondFuturesMethod METHOD_OPT = BondFuturesOptionMarginSecurityBlackBondFuturesMethod.getDefaultInstance();
  private static final BondFuturesSecurityDiscountingMethod METHOD_FUTURE = BondFuturesSecurityDiscountingMethod.getInstance();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final BondFutureOptionMarginSecurityBlackSmileMethod METHOD_SMILE = BondFutureOptionMarginSecurityBlackSmileMethod
      .getInstance();

  /** Tolerances */
  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_DELTA = 1.0E-8;

  public void impliedVolatility() {
    final double priceFutures = METHOD_FUTURE.price(CALL_BOBL_116.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final double logmoney = Math.log(STRIKE_116 / priceFutures);
    final double expiry = CALL_BOBL_116.getExpirationTime();
    final double ivExpected = BLACK_SURFACE.getZValue(expiry, logmoney);
    final double ivComputed = METHOD_OPT.impliedVolatility(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: impliedVolatility", ivExpected, ivComputed, TOLERANCE_RATE);
  }

  public void futurePrice() {
    final double priceExpected = METHOD_FUTURE.price(CALL_BOBL_116.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final double priceComputed = METHOD_OPT.underlyingFuturePrice(CALL_BOBL_116, ISSUER_SPECIFIC_MULTICURVES);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: underlying futures price", priceExpected, priceComputed, TOLERANCE_RATE);
    double priceSmile = METHOD_SMILE.underlyingFuturePrice(CALL_BOBL_116, ISSUER_SPECIFIC_MULTICURVES);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: underlying futures price", priceSmile, priceComputed,
        TOLERANCE_RATE);
  }

  public void priceFromFuturesPrice() {
    final double price = 1.26;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE_116, CALL_BOBL_116.getExpirationTime(), CALL_BOBL_116.isCall());
    final double logmoney = Math.log(STRIKE_116 / price);
    final double expiry = CALL_BOBL_116.getExpirationTime();
    final double volatility = BLACK_SURFACE.getZValue(expiry, logmoney);
    final BlackFunctionData dataBlack = new BlackFunctionData(price, 1.0, volatility);
    final double priceExpected = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    final double priceComputed = METHOD_OPT.price(CALL_BOBL_116, BLACK_FLAT_BNDFUT, price);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: underlying futures price", priceExpected, priceComputed, TOLERANCE_RATE);
    double priceSmile = METHOD_SMILE.priceFromUnderlyingPrice(CALL_BOBL_116, BLACK_FLAT_BNDFUT, price);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: underlying futures price", priceSmile,
        priceComputed, TOLERANCE_RATE);
  }

  public void priceFromCurves() {
    final double priceFutures = METHOD_FUTURE.price(CALL_BOBL_116.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final double priceExpected = METHOD_OPT.price(CALL_BOBL_116, BLACK_FLAT_BNDFUT, priceFutures);
    final double priceComputed = METHOD_OPT.price(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: underlying futures price", priceExpected, priceComputed, TOLERANCE_RATE);
    double priceSmile = METHOD_SMILE.price(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: underlying futures price", priceSmile,
        priceComputed, TOLERANCE_RATE);
  }

  public void putCallParity() {
    final double priceFutures = METHOD_FUTURE.price(CALL_BOBL_116.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final double priceCallComputed = METHOD_OPT.price(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    final double pricePutComputed = METHOD_OPT.price(PUT_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: put call parity price", priceCallComputed - pricePutComputed, priceFutures - STRIKE_116, TOLERANCE_RATE);
  }

  public void priceBlackSensitivity() {
    final double priceFutures = METHOD_FUTURE.price(CALL_BOBL_116.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE_116, CALL_BOBL_116.getExpirationTime(), CALL_BOBL_116.isCall());
    final double logmoney = Math.log(STRIKE_116 / priceFutures);
    final double expiry = CALL_BOBL_116.getExpirationTime();
    final double volatility = BLACK_SURFACE.getZValue(expiry, logmoney);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatility);
    final double[] priceAD = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final double vega = priceAD[2];
    final PresentValueBlackBondFuturesCubeSensitivity vegaComputed = METHOD_OPT.priceBlackSensitivity(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: Black parameters sensitivity", vega, vegaComputed.getSensitivity().toSingleValue(), TOLERANCE_DELTA);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: Black parameters sensitivity", 1, vegaComputed.getSensitivity().getMap().size());
    final Entry<Triple<Double, Double, Double>, Double> point = vegaComputed.getSensitivity().getMap().entrySet().iterator().next();
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: Black parameters sensitivity", CALL_BOBL_116.getExpirationTime(), point.getKey().getFirst(), TOLERANCE_RATE);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: Black parameters sensitivity",
        CALL_BOBL_116.getUnderlyingFuture().getTradingLastTime() - CALL_BOBL_116.getExpirationTime(), point.getKey().getSecond(), TOLERANCE_RATE);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: Black parameters sensitivity", CALL_BOBL_116.getStrike(), point.getKey().getThird(), TOLERANCE_RATE);
  }

  public void theoreticalDelta() {
    final double priceFutures = METHOD_FUTURE.price(CALL_BOBL_116.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE_116, CALL_BOBL_116.getExpirationTime(), CALL_BOBL_116.isCall());
    final double logmoney = Math.log(STRIKE_116 / priceFutures);
    final double expiry = CALL_BOBL_116.getExpirationTime();
    final double volatility = BLACK_SURFACE.getZValue(expiry, logmoney);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatility);
    final double[] priceAD = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final double deltaCallExpected = priceAD[1];
    final double deltaCallComputed = METHOD_OPT.deltaUnderlyingPrice(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: delta", deltaCallExpected, deltaCallComputed, TOLERANCE_DELTA);
    assertTrue("BondFuturesOptionMarginSecurityBlackFlatMethod: delta", (0.0d < deltaCallComputed) && (deltaCallComputed < 1.0d));
    final double deltaPutComputed = METHOD_OPT.deltaUnderlyingPrice(PUT_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: delta", deltaCallExpected - 1.0d, deltaPutComputed, TOLERANCE_DELTA);
    double deltaCallSmile = METHOD_SMILE.delta(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: delta", deltaCallSmile, deltaCallComputed,
        TOLERANCE_DELTA);
    double deltaPutSmile = METHOD_SMILE.delta(PUT_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: delta", deltaPutSmile, deltaPutComputed,
        TOLERANCE_DELTA);
  }

  public void theoreticalGamma() {
    final double priceFutures = METHOD_FUTURE.price(CALL_BOBL_116.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE_116, CALL_BOBL_116.getExpirationTime(), CALL_BOBL_116.isCall());
    final double logmoney = Math.log(STRIKE_116 / priceFutures);
    final double expiry = CALL_BOBL_116.getExpirationTime();
    final double volatility = BLACK_SURFACE.getZValue(expiry, logmoney);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatility);
    final double[] firstDerivs = new double[3];
    final double[][] secondDerivs = new double[3][3];
    BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, firstDerivs, secondDerivs);
    final double gammaCallExpected = secondDerivs[0][0];
    final double gammaCallComputed = METHOD_OPT.gammaUnderlyingPrice(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: gamma", gammaCallExpected, gammaCallComputed, TOLERANCE_DELTA);
    assertTrue("BondFuturesOptionMarginSecurityBlackFlatMethod: gamma", 0.0d < gammaCallComputed);
    double gammaCallSmile = METHOD_SMILE.gamma(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: gamma", gammaCallSmile, gammaCallComputed,
        TOLERANCE_DELTA);
  }

  public void theoreticalVega() {
    final double priceFutures = METHOD_FUTURE.price(CALL_BOBL_116.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(STRIKE_116, CALL_BOBL_116.getExpirationTime(), CALL_BOBL_116.isCall());
    final double logmoney = Math.log(STRIKE_116 / priceFutures);
    final double expiry = CALL_BOBL_116.getExpirationTime();
    final double volatility = BLACK_SURFACE.getZValue(expiry, logmoney);
    final BlackFunctionData dataBlack = new BlackFunctionData(priceFutures, 1.0, volatility);
    final double[] priceAD = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    final double vegaCallExpected = priceAD[2];
    final double vegaCallComputed = METHOD_OPT.vegaUnderlyingPrice(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: vega", vegaCallExpected, vegaCallComputed, TOLERANCE_DELTA);
    assertTrue("BondFuturesOptionMarginSecurityBlackFlatMethod: vega", (0.0d < vegaCallComputed) && (vegaCallComputed < 1.0d));
    double vegaCallSmile = METHOD_SMILE.vega(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: vega", vegaCallSmile, vegaCallComputed,
        TOLERANCE_DELTA);
  }


  public void theoreticalTheta() {
    final double priceFutures = METHOD_FUTURE.price(CALL_BOBL_116.getUnderlyingFuture(), ISSUER_SPECIFIC_MULTICURVES);
    final double logmoney = Math.log(STRIKE_116 / priceFutures);
    final double expiry = CALL_BOBL_116.getExpirationTime();
    final double volatility = BLACK_SURFACE.getZValue(expiry, logmoney);
    final double rate = -Math.log(ISSUER_SPECIFIC_MULTICURVES.getMulticurveProvider().getDiscountFactor(CALL_BOBL_116.getCurrency(), CALL_BOBL_116.getExpirationTime())) / CALL_BOBL_116.getExpirationTime();
    final double thetaCallExpected = BlackFormulaRepository.theta(priceFutures, STRIKE_116, CALL_BOBL_116.getExpirationTime(), volatility, CALL_BOBL_116.isCall(), rate);
    final double thetaCallComputed = METHOD_OPT.theta(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: theta", thetaCallExpected, thetaCallComputed, TOLERANCE_DELTA);
    double thetaSmile = METHOD_SMILE.theta(CALL_BOBL_116, BLACK_FLAT_BNDFUT);
    assertEquals("BondFuturesOptionMarginSecurityBlackFlatMethod: theta", thetaSmile, thetaCallComputed,
        TOLERANCE_DELTA);
  }

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D TIME_SQUARE_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.TIME_SQUARE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_TIMESQUARE_LINEAR_2D = new GridInterpolator2D(TIME_SQUARE_FLAT, LINEAR_FLAT);
  private static final GridInterpolator2D INTERPOLATOR_LINEAR_LINEAR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);
  private static final GridInterpolator2D INTERPOLATOR_LINEAR_TIMESQUARE_2D = new GridInterpolator2D(LINEAR_FLAT, TIME_SQUARE_FLAT);
  private static final double[] EXP = new double[] {5.0d / 365.0d, 0.20, 0.45 };
  private static final double[] LOGMONEY = new double[] {-0.010, -0.005, 0.000, 0.005, 0.010 };
  private static final double[] VALUE_EXP_MON = new double[] {0.50, 0.50, 0.45, 0.49, 0.49, 0.44, 0.47, 0.47, 0.42, 0.48, 0.48, 0.43, 0.51, 0.51, 0.46 };
  private static final double[] VALUE_MON_EXP = new double[VALUE_EXP_MON.length];
  private static final double[] VALUE_EXP_MON_VAR = new double[VALUE_EXP_MON.length];
  static {
    for (int loopmon = 0; loopmon < LOGMONEY.length; loopmon++) {
      for (int loopexp = 0; loopexp < EXP.length; loopexp++) {
        VALUE_MON_EXP[loopmon + loopexp * LOGMONEY.length] = VALUE_EXP_MON[loopexp + loopmon * EXP.length];
        VALUE_EXP_MON_VAR[loopexp + loopmon * EXP.length] = Math.pow(VALUE_EXP_MON[loopexp + loopmon * EXP.length], 2) * EXP[loopexp];
      }
    }
  }

  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_LOGMONEY = InterpolatedDoublesSurface.fromGrid(EXP, LOGMONEY, VALUE_EXP_MON,
      INTERPOLATOR_TIMESQUARE_LINEAR_2D); // Replace LINEAR by "SQUARE_LINEAR" (TBC)
  private static final InterpolatedDoublesSurface BLACK_SURFACE_LOGMONEY_EXP = InterpolatedDoublesSurface.fromGrid(LOGMONEY, EXP, VALUE_MON_EXP,
      INTERPOLATOR_LINEAR_TIMESQUARE_2D);
  private static final InterpolatedDoublesSurface BLACK_SURFACE_EXP_LOGMONEY_VAR = InterpolatedDoublesSurface.fromGrid(EXP, LOGMONEY, VALUE_EXP_MON_VAR,
      INTERPOLATOR_LINEAR_LINEAR_2D);

  private static final double TOLERANCE_VOL = 2.0E-4;

  public void interpolation() {
    final int nbExpTest = 4;
    final int nbMonTest = 5;
    final double startExp = 5.0d / 365.0d;
    final double stepExp = 0.10;
    final double startMon = -0.011;
    final double stepMon = 0.005;
    for (int loopmon = 0; loopmon < nbMonTest; loopmon++) {
      for (int loopexp = 0; loopexp < nbExpTest; loopexp++) {
        double exp = startExp + loopexp * stepExp;
        double mon = startMon + loopmon * stepMon;
        double intExpMon = BLACK_SURFACE_EXP_LOGMONEY.getZValue(exp, mon);
        double intMonExp = BLACK_SURFACE_LOGMONEY_EXP.getZValue(mon, exp);
        double intVar = BLACK_SURFACE_EXP_LOGMONEY_VAR.getZValue(exp, mon);
        assertEquals("Time square interpolation: change of order", intExpMon, intMonExp, TOLERANCE_VOL);
        assertEquals("Time square interpolation: change of order " + loopmon + " - " + loopexp, intExpMon, Math.sqrt(intVar / exp), TOLERANCE_VOL);
      }
    }
  }

}
