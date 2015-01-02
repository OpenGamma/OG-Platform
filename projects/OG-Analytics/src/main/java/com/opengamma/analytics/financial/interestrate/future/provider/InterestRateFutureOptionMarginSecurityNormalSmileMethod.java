/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method for the pricing of interest rate future options with daily margining. The pricing is done with a Normal approach on the future price.
 * The normal parameters are represented by (expiration-strike-delay) surfaces. The "delay" is the time between option expiration and future last trading date,
 * i.e. 0 for quarterly options and x for x-year mid-curve options. The future prices are computed without convexity adjustments.
 */
public final class InterestRateFutureOptionMarginSecurityNormalSmileMethod extends
    InterestRateFutureOptionMarginSecurityGenericMethod<NormalSTIRFuturesProviderInterface> {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginSecurityNormalSmileMethod INSTANCE = new InterestRateFutureOptionMarginSecurityNormalSmileMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginSecurityNormalSmileMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginSecurityNormalSmileMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Black function used in the pricing.
   */
  private static final NormalPriceFunction NORMAL_FUNCTION = new NormalPriceFunction();

  /**
   * The method used to compute the future price. It is a method without convexity adjustment.
   */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURE = InterestRateFutureSecurityDiscountingMethod.getInstance();

  /**
   * Computes the option security price from future price.
   * @param security The future option security.
   * @param normalData The normal volatility and multi-curves provider.
   * @param priceFuture The price of the underlying future.
   * @return The security price.
   */
  public double priceFromFuturePrice(final InterestRateFutureOptionMarginSecurity security,
      final NormalSTIRFuturesProviderInterface normalData, final double priceFuture) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "Normal data");
    final EuropeanVanillaOption option = new EuropeanVanillaOption(security.getStrike(), security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = normalData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFuture);
    final NormalFunctionData normalPoint = new NormalFunctionData(priceFuture, 1.0, volatility);
    final double priceSecurity = NORMAL_FUNCTION.getPriceFunction(option).evaluate(normalPoint);
    return priceSecurity;
  }

  /**
   * Computes the option security price. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param normalData The normal volatility and multi-curves provider.
   * @return The security price.
   */
  @Override
  public double price(final InterestRateFutureOptionMarginSecurity security,
      final NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "Normal data");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), normalData.getMulticurveProvider());
    return priceFromFuturePrice(security, normalData, priceFuture);
  }

  /**
   * Computes the option security price curve sensitivity. The future price is computed without convexity adjustment.
   * It is supposed that for a given strike the volatility does not change with the curves (sticky strike).
   * @param security The future option security.
   * @param normalData The normal volatility and multi-curves provider.
   * @return The security price curve sensitivity.
   */
  @Override
  public MulticurveSensitivity priceCurveSensitivity(final InterestRateFutureOptionMarginSecurity security,
      final NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "Normal data");
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), normalData.getMulticurveProvider());
    final EuropeanVanillaOption option = new EuropeanVanillaOption(security.getStrike(), security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = normalData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFuture);
    final NormalFunctionData normalPoint = new NormalFunctionData(priceFuture, 1.0, volatility);
    // Backward sweep
    final double[] priceAdjoint = new double[3];
    NORMAL_FUNCTION.getPriceAdjoint(option, normalPoint, priceAdjoint);
    final double priceBar = 1.0;
    final double priceFutureBar = priceAdjoint[0] * priceBar;
    final MulticurveSensitivity priceFutureDerivative = METHOD_FUTURE.priceCurveSensitivity(security.getUnderlyingFuture(), normalData.getMulticurveProvider());
    return priceFutureDerivative.multipliedBy(priceFutureBar);
  }

  /**
   * Computes the option security price volatility sensitivity. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param normalData The normal volatility and multi-curves provider.
   * @return The security price Black volatility sensitivity.
   */
  public SurfaceValue priceNormalSensitivity(final InterestRateFutureOptionMarginSecurity security,
      final NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "Normal data");
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), normalData.getMulticurveProvider());
    final double strike = security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, security.getExpirationTime(), security.isCall());
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = normalData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFuture);
    final NormalFunctionData normalPoint = new NormalFunctionData(priceFuture, 1.0, volatility);
    // Backward sweep
    final double[] priceAdjoint = new double[3];
    NORMAL_FUNCTION.getPriceAdjoint(option, normalPoint, priceAdjoint);
    final double priceBar = 1.0;
    final double volatilityBar = priceAdjoint[1] * priceBar;
    final DoublesPair expiryStrikeDelay = DoublesPair.of(security.getExpirationTime(), strike);
    final SurfaceValue sensi = SurfaceValue.from(expiryStrikeDelay, volatilityBar);
    return sensi;
  }

  /**
   * Interpolates and returns the option's implied volatility 
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param normalData The normal volatility and multi-curves provider.
   * @return Lognormal Implied Volatility.
   */
  public double impliedVolatility(final InterestRateFutureOptionMarginSecurity security,
      final NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "Normal data");
    double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), normalData);
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    return normalData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFutures);
  }

  /**
   * Computes the underlying future security price. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param multicurves The multi-curves provider.
   * @return The security price.
   */
  public double underlyingFuturesPrice(final InterestRateFutureOptionMarginSecurity security, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(security, "Option security");
    return METHOD_FUTURE.price(security.getUnderlyingFuture(), multicurves);
  }

  /**
   * Computes the option security price delta, wrt the futures price dV/df. The futures price is computed without convexity adjustment.
   * It is supposed that for a given strike the volatility does not change with the curves.
   * @param security The future option security.
   * @param normalData The curve and normal volatility data.
   * @return The delta.
   */
  public double priceDelta(InterestRateFutureOptionMarginSecurity security,
      NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "Normal data");
    double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), normalData.getMulticurveProvider());
    return priceDeltaFromFuturePrice(security, normalData, priceFuture);
  }

  /**
   * Computes the option security price delta, wrt the futures price dV/df. The futures price is computed without convexity adjustment.
   * It is supposed that for a given strike the volatility does not change with the curves.
   * @param security The future option security.
   * @param normalData The curve and normal volatility data.
   * @param priceFuture The price of the underlying future.
   * @return The delta.
   */
  public double priceDeltaFromFuturePrice(final InterestRateFutureOptionMarginSecurity security,
      final NormalSTIRFuturesProviderInterface normalData, final double priceFuture) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "Normal data");
    final EuropeanVanillaOption option = new EuropeanVanillaOption(security.getStrike(), security.getExpirationTime(),
        security.isCall());
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = normalData
        .getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFuture);
    final NormalFunctionData normalPoint = new NormalFunctionData(priceFuture, 1.0, volatility);
    return NORMAL_FUNCTION.getDelta(option, normalPoint);
  }

  /**
   * Computes the option's value gamma, the second derivative of the security price wrt underlying futures rate.
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param normalData The curve and normal volatility data.
   * @return The gamma.
   */
  public double priceGamma(InterestRateFutureOptionMarginSecurity security,
      NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "Normal data");
    double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), normalData.getMulticurveProvider());
    return priceGammaFromFuturePrice(security, normalData, priceFuture);
  }

  /**
   * Computes the option's value gamma, the second derivative of the security price wrt underlying futures rate.
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param normalData The curve and normal volatility data.
   * @param priceFuture The price of the underlying future.
   * @return The gamma.
   */
  public double priceGammaFromFuturePrice(final InterestRateFutureOptionMarginSecurity security,
      final NormalSTIRFuturesProviderInterface normalData, final double priceFuture) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "Normal data");
    final EuropeanVanillaOption option = new EuropeanVanillaOption(security.getStrike(), security.getExpirationTime(),
        security.isCall());
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = normalData
        .getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFuture);
    final NormalFunctionData normalPoint = new NormalFunctionData(priceFuture, 1.0, volatility);
    return NORMAL_FUNCTION.getGamma(option, normalPoint);
  }

  /**
   * Computes the option security vega. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param normalData The curve and normal volatility data.
   * @return The vega.
   */
  public double priceVega(InterestRateFutureOptionMarginSecurity security,
      NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "Normal data");
    double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), normalData.getMulticurveProvider());
    return priceVegaFromFuturePrice(security, normalData, priceFuture);
  }

  /**
   * Computes the option security vega. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param normalData The curve and normal volatility data.
   * @param priceFuture The price of the underlying future.
   * @return The vega.
   */
  public double priceVegaFromFuturePrice(final InterestRateFutureOptionMarginSecurity security,
      final NormalSTIRFuturesProviderInterface normalData, final double priceFuture) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "Normal data");
    final EuropeanVanillaOption option = new EuropeanVanillaOption(security.getStrike(), security.getExpirationTime(),
        security.isCall());
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = normalData
        .getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFuture);
    final NormalFunctionData normalPoint = new NormalFunctionData(priceFuture, 1.0, volatility);
    return NORMAL_FUNCTION.getVega(option, normalPoint);
  }

}
