/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Method for the pricing of interest rate future options with margin process. 
 * The pricing is done with a Black approach on the future rate (1.0-price).
 * The Black parameters are represented by (expiration-strike-delay) surfaces. 
 * The "delay" is the time between option expiration and future last trading date,
 * i.e. 0 for quarterly options and x for x-year mid-curve options. 
 * The future prices are computed without convexity adjustments.
 */
public class InterestRateFutureOptionMarginSecurityBlackRateMethod extends 
    InterestRateFutureOptionMarginSecurityGenericMethod<BlackSTIRFuturesProviderInterface> {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginSecurityBlackRateMethod INSTANCE = 
      new InterestRateFutureOptionMarginSecurityBlackRateMethod();

  /**
   * Constructor.
   */
  public InterestRateFutureOptionMarginSecurityBlackRateMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginSecurityBlackRateMethod getInstance() {
    return INSTANCE;
  }

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * The method used to compute the future price. It is a method without convexity adjustment.
   */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURE = 
      InterestRateFutureSecurityDiscountingMethod.getInstance();

  /**
   * Computes the option security price from future price.
   * @param security The future option security.
   * @param blackData The Black volatility and multi-curves provider.
   * @param priceFuture The price of the underlying future.
   * @return The security price.
   */
  public double priceFromFuturePrice(final InterestRateFutureOptionMarginSecurity security, 
      final BlackSTIRFuturesProviderInterface blackData, final double priceFuture) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(blackData, "Black data");
    final double rateStrike = 1.0 - security.getStrike();
    final EuropeanVanillaOption option = 
        new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double rateFutures = 1 - priceFuture;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFuture);
    final BlackFunctionData dataBlack = new BlackFunctionData(rateFutures, 1.0, volatility);
    final double priceSecurity = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    return priceSecurity;
  }

  /**
   * Computes the option security price. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price.
   */
  @Override
  public double price(final InterestRateFutureOptionMarginSecurity security, 
      final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(blackData, "Black data");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData.getMulticurveProvider());
    return priceFromFuturePrice(security, blackData, priceFuture);
  }

  /**
   * Computes the option security price curve sensitivity. The future price is computed without convexity adjustment.
   * It is supposed that for a given strike the volatility does not change with the curves (sticky strike).
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price curve sensitivity.
   */
  @Override
  public MulticurveSensitivity priceCurveSensitivity(final InterestRateFutureOptionMarginSecurity security, 
      final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(blackData, "Black data");
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData.getMulticurveProvider());
    final double rateStrike = 1.0 - security.getStrike();
    final EuropeanVanillaOption option = 
        new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFuture);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double forwardBar = priceAdjoint[1] * priceBar;
    final double priceFutureBar = -forwardBar;
    MulticurveSensitivity priceFutureDerivative = 
        METHOD_FUTURE.priceCurveSensitivity(security.getUnderlyingFuture(), blackData.getMulticurveProvider());
    return priceFutureDerivative.multipliedBy(priceFutureBar);
  }

  /**
   * Computes the option security price volatility sensitivity. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price Black volatility sensitivity.
   */
  public SurfaceValue priceBlackSensitivity(final InterestRateFutureOptionMarginSecurity security, 
      final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(blackData, "Black data");
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData.getMulticurveProvider());
    final double strike = security.getStrike();
    final double rateStrike = 1.0 - strike;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFuture);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Backward sweep
    final double priceBar = 1.0;
    final double volatilityBar = priceAdjoint[2] * priceBar;
    final DoublesPair expiryStrikeDelay = DoublesPair.of(security.getExpirationTime(), strike);
    final SurfaceValue sensi = SurfaceValue.from(expiryStrikeDelay, volatilityBar);
    return sensi;
  }

  /**
   * Computes the option security price delta, wrt the futures price dV/df. The futures price is computed without convexity adjustment.
   * It is supposed that for a given strike the volatility does not change with the curves.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The delta.
   */
  public double priceDelta(final InterestRateFutureOptionMarginSecurity security, 
      final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(blackData, "Black data");
    // Forward sweep
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    final double rateStrike = 1.0 - security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFutures;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFutures);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    return -priceAdjoint[1];
  }

  /**
   * Computes the option's value gamma, the second derivative of the security price wrt underlying futures rate.
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price.
   */
  public double priceGamma(final InterestRateFutureOptionMarginSecurity security, 
      final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(blackData, "Black data");
    // Forward sweep
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData.getMulticurveProvider());
    final double strike = security.getStrike();
    final double rateStrike = 1.0 - strike;
    EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFutures;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFutures);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] firstDerivs = new double[3];
    final double[][] secondDerivs = new double[3][3];
    BLACK_FUNCTION.getPriceAdjoint2(option, dataBlack, firstDerivs, secondDerivs);
    return secondDerivs[0][0];
  }

  /**
   * Computes the option security vega. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return Black lognormal vega.
   */
  public double priceVega(final InterestRateFutureOptionMarginSecurity security, 
      final BlackSTIRFuturesProviderInterface blackData) {
    // Forward sweep
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    final double strike = security.getStrike();
    final double rateStrike = 1.0 - strike;
    EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFutures;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFutures);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    return priceAdjoint[2];
  }
  
  /**
   * Computes the options theta.
   * @param security the future option.
   * @param blackData the curve and black volatility data.
   * @return the theta.
   */
  public double priceTheta(final InterestRateFutureOptionMarginSecurity security, 
      final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(blackData, "black");
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    final double strike = security.getStrike();
    final double rateStrike = 1.0 - strike;
    final double forward = 1 - priceFutures;
    double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    double volatility = blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFutures);
    double rate = ((MulticurveProviderDiscount) blackData.getMulticurveProvider()).getCurve(security.getCurrency()).
        getInterestRate(security.getExpirationTime());
    return BlackFormulaRepository.theta(forward, rateStrike, delay, volatility, security.isCall(), rate);
  }

  /**
   * Interpolates and returns the option's implied volatility
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return Lognormal Implied Volatility.
   */
  public double impliedVolatility(final InterestRateFutureOptionMarginSecurity security, 
      final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(blackData, "Black data");
    double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    return blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFutures);
  }

  /**
   * Computes the underlying future security price. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price.
   */
  public double underlyingFuturesPrice(final InterestRateFutureOptionMarginSecurity security, 
      final BlackSTIRFuturesProviderInterface blackData) {
    return METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData.getMulticurveProvider());
  }

}
