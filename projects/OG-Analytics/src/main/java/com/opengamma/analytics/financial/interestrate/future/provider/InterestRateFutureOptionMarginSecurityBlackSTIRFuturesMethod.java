/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Method for the pricing of STIR future options (with futures-like margin). The pricing is done with a Black approach on the future rate (1-price).
 * The Black parameters are obtain from implied volatility providers at (expiration-delay-strike-rate) points. 
 * The delay is the time difference between the underlying futures last trading date and the option expiration.
 * The price of the underlying STIR futures is computed by "discounting" (no convexity adjustment).
 */
public final class InterestRateFutureOptionMarginSecurityBlackSTIRFuturesMethod extends FuturesSecurityBlackSTIRFuturesMethod {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginSecurityBlackSTIRFuturesMethod INSTANCE =
      new InterestRateFutureOptionMarginSecurityBlackSTIRFuturesMethod();

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginSecurityBlackSTIRFuturesMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginSecurityBlackSTIRFuturesMethod getInstance() {
    return INSTANCE;
  }

  /** The method used to compute the future price. It is a method without convexity adjustment. */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURE = InterestRateFutureSecurityDiscountingMethod.getInstance();
  /** The Black function used in the pricing. */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * Computes the option security price from future price.
   * @param security The future option security.
   * @param blackData The Black volatility and multi-curves provider.
   * @param priceFuture The price of the underlying future.
   * @return The security price.
   */
  public double price(final InterestRateFutureOptionMarginSecurity security, final BlackSTIRFuturesProviderInterface blackData, final double priceFuture) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(blackData, "Black data");
    final double rateStrike = 1.0 - security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double rateFutures = 1 - priceFuture;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    final double volatility = blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFuture);
    final BlackFunctionData dataBlack = new BlackFunctionData(rateFutures, 1.0, volatility);
    final double priceSecurity = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    return priceSecurity;
  }

  /**
   * Interpolates and returns the option's implied volatility
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return Lognormal Implied Volatility.
   */
  public double impliedVolatility(final InterestRateFutureOptionMarginSecurity security, final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(blackData, "Black data");
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    return blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFutures);
  }

  /**
   * Computes the underlying future security price. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price.
   */
  public double underlyingFuturesPrice(final InterestRateFutureOptionMarginSecurity security, final ParameterProviderInterface blackData) {
    return METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData.getMulticurveProvider());
  }

  /**
   * Computes the option security theoretical delta wrt the underlying futures price. The futures price is computed without convexity adjustment.
   * It is supposed that for a given strike the volatility does not change with the curves.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The delta.
   */
  public double deltaUnderlyingPrice(final InterestRateFutureOptionMarginSecurity security, final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(blackData, "Black data");
    // Forward sweep
    final double priceFutures = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    final double rateStrike = 1.0 - security.getStrike();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double rateFutures = 1 - priceFutures;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    final double volatility = blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), priceFutures);
    final BlackFunctionData dataBlack = new BlackFunctionData(rateFutures, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    // Implementation note: the black delta is wrt the rateFutures; the function returns the delta with respect to the priceFutures
    return -priceAdjoint[1];
  }



  /**
   * Computes the option's value gamma, the second derivative of the security price wrt underlying futures rate.
   * The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param blackData The curve and Black volatility data.
   * @return The security price.
   */
  public double gammaUnderlyingPrice(final InterestRateFutureOptionMarginSecurity security, final BlackSTIRFuturesProviderInterface blackData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(blackData, "Black data");
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData.getMulticurveProvider());
    final double strike = security.getStrike();
    final double rateStrike = 1.0 - strike;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    final double volatility = blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), rateStrike);
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
  public double vegaUnderlyingPrice(final InterestRateFutureOptionMarginSecurity security, final BlackSTIRFuturesProviderInterface blackData) {
    // Forward sweep
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), blackData);
    final double strike = security.getStrike();
    final double rateStrike = 1.0 - strike;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    final double forward = 1 - priceFuture;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    final double volatility = blackData.getVolatility(security.getExpirationTime(), delay, security.getStrike(), rateStrike);
    final BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    final double[] priceAdjoint = BLACK_FUNCTION.getPriceAdjoint(option, dataBlack);
    return priceAdjoint[2];
  }
  
  /**
   * Computes the options theta.
   * @param security the future option.
   * @param black the curve and black volatility data.
   * @return the theta.
   */
  public double thetaUnderlyingPrice(final InterestRateFutureOptionMarginSecurity security, final BlackSTIRFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "black");
    final double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), black);
    final double strike = security.getStrike();
    final double rateStrike = 1.0 - strike;
    final double forward = 1 - priceFuture;
    final double delay = security.getUnderlyingFuture().getTradingLastTime() - security.getExpirationTime();
    final double volatility = black.getVolatility(security.getExpirationTime(), delay, security.getStrike(), rateStrike);
    final double rate = -Math.log(black.getMulticurveProvider().getDiscountFactor(security.getCurrency(), security.getExpirationTime())) / security.getExpirationTime();
    return BlackFormulaRepository.theta(forward, rateStrike, security.getExpirationTime(), volatility, !security.isCall(), rate);
  }
}
