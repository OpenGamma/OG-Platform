/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginSecurityBlackRateMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method for the pricing of interest rate future options with margin process. The pricing is done with a Black approach on the future rate (1.0-price).
 * The Black parameters are represented by (expiration-strike-delay) surfaces. The "delay" is the time between option expiration and future last trading date,
 * i.e. 0 for quarterly options and x for x-year mid-curve options. The future prices are computed without convexity adjustments.
 * @deprecated Use {@link InterestRateFutureOptionMarginSecurityBlackRateMethod}
 */
@Deprecated
public final class InterestRateFutureOptionMarginSecurityBlackSurfaceMethod extends InterestRateFutureOptionMarginSecurityMethod {
  // TODO: Change to a surface when available.

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod INSTANCE = new InterestRateFutureOptionMarginSecurityBlackSurfaceMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginSecurityBlackSurfaceMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginSecurityBlackSurfaceMethod() {
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * The method used to compute the future price. It is a method without convexity adjustment.
   */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURE = InterestRateFutureSecurityDiscountingMethod.getInstance();

  /**
   * Computes the option security price from future price.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @param priceFuture The price of the underlying future.
   * @return The security price.
   */
  public double optionPriceFromFuturePrice(final InterestRateFutureOptionMarginSecurity security, final YieldCurveWithBlackCubeBundle blackData, final double priceFuture) {
    final double rateStrike = 1.0 - security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;
    //    final double delay = security.getUnderlyingFuture().getLastTradingTime() - security.getExpirationTime();
    final double volatility = blackData.getVolatility(security.getExpirationTime(), security.getStrike()); // , delay
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double priceSecurity = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    return priceSecurity;
  }

  @Override
  public double optionPriceFromFuturePrice(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves, final double priceFuture) {
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return optionPriceFromFuturePrice(security, (YieldCurveWithBlackCubeBundle) curves, priceFuture);
  }

  /**
   * Computes the option security price. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price.
   */
  public double optionPrice(final InterestRateFutureOptionMarginSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    return optionPriceFromFuturePrice(security, blackData, priceFuture);
  }

  @Override
  public double optionPrice(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return optionPrice(security, (YieldCurveWithBlackCubeBundle) curves);
  }

  /**
   * Computes the option security price curve sensitivity, ie the sensitivity to the rate, not the futures price. The future price is computed without convexity adjustment.
   * It is supposed that for a given strike the volatility does not change with the curves.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price curve sensitivity.
   */
  public InterestRateCurveSensitivity priceCurveSensitivity(final InterestRateFutureOptionMarginSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    final double priceFutureBar = optionPriceDelta(security, blackData);
    final InterestRateCurveSensitivity priceFutureDerivative = METHOD_FUTURE.priceCurveSensitivity(security.getUnderlyingFuture(), blackData);
    return priceFutureDerivative.multipliedBy(priceFutureBar);
  }

  @Override
  public InterestRateCurveSensitivity priceCurveSensitivity(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return priceCurveSensitivity(security, (YieldCurveWithBlackCubeBundle) curves);
  }

  /**
   * Computes the option security price delta, wrt the futures price dV/df. The futures price is computed without convexity adjustment.
   * It is supposed that for a given strike the volatility does not change with the curves.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The delta.
   */
  public double optionPriceDelta(final InterestRateFutureOptionMarginSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
 // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    final double rateStrike = 1.0 - security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;
    //    final double delay = security.getUnderlyingFuture().getLastTradingTime() - security.getExpirationTime();
    final double volatility = blackData.getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    return -priceAdjoint[1];
  }

  /**
   * Computes the option security Vega. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return Black lognormal Vega.
   */
  public double optionPriceVega(final InterestRateFutureOptionMarginSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    final double strike = security.getStrike();
    final double rateStrike = 1.0 - strike;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;
    final double volatility = blackData.getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    return priceAdjoint[2];
  }

  /**
   * Computes the option security theta. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return Black lognormal theta.
   */
  public double optionPriceTheta(final InterestRateFutureOptionMarginSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    final double strike = security.getStrike();
    final double rateStrike = 1.0 - strike;
    final double forward = 1 - priceFuture;
    final double volatility = blackData.getVolatility(security.getExpirationTime(), security.getStrike());
    return BlackFormulaRepository.driftlessTheta(forward, rateStrike, security.getExpirationTime(), volatility);
  }

  /**
   * Computes the option security price volatility sensitivity. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price Black volatility sensitivity.
   */
  public SurfaceValue priceBlackSensitivity(final InterestRateFutureOptionMarginSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    final double volatilityBar = optionPriceVega(security, blackData);
    final DoublesPair expiryStrikeDelay = DoublesPair.of(security.getExpirationTime(), security.getStrike());
    final SurfaceValue sensi = SurfaceValue.from(expiryStrikeDelay, volatilityBar);
    return sensi;
  }

  /**
   * Computes the option's value gamma, the second derivative of the security price wrt underlying futures rate.
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price.
   */
  public double optionPriceGamma(final InterestRateFutureOptionMarginSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(blackData, "YieldCurveWithBlackCubeBundle was unexpectedly null");
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    final double strike = security.getStrike();
    final double rateStrike = 1.0 - strike;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;

    final double volatility = blackData.getVolatility(security.getExpirationTime(), security.getStrike());
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);

    // TODO This is overkill. We only need one value, but it provides extra calculations while doing testing
    final double[] firstDerivs = new double[3];
    final double[][] secondDerivs = new double[3][3];
    BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, firstDerivs, secondDerivs);
    return secondDerivs[0][0];
  }

  /**
   * Computes the option's value gamma, the second derivative of the security price wrt underlying futures rate.
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param curves The curve and Black volatility data.
   * @return The security price.
   */
  public double optionPriceGamma(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return optionPriceGamma(security, (YieldCurveWithBlackCubeBundle) curves);
  }

  /**
   * Interpolates and returns the option's implied volatility
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param curves The curve and Black volatility data.
   * @return Lognormal Implied Volatility
   */
  public double impliedVolatility(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black Cube");
    return impliedVolatility(security, (YieldCurveWithBlackCubeBundle) curves);
  }

  /**
   * Interpolates and returns the option's implied volatility
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return Lognormal Implied Volatility.
   */
  public double impliedVolatility(final InterestRateFutureOptionMarginSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackData, "blackData");
    return blackData.getVolatility(security.getExpirationTime(), security.getStrike());
  }

  public double underlyingFuturePrice(final InterestRateFutureOptionMarginSecurity security, final YieldCurveBundle curves) {
    ArgumentChecker.isTrue(curves instanceof YieldCurveWithBlackCubeBundle, "Yield curve bundle should contain Black cube");
    return underlyingFuturePrice(security, (YieldCurveWithBlackCubeBundle) curves);
  }

  /**
   * Computes the underlying future security price. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price.
   */
  public double underlyingFuturePrice(final InterestRateFutureOptionMarginSecurity security, final YieldCurveWithBlackCubeBundle blackData) {
    return METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
  }

}
