/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import com.opengamma.util.ArgumentChecker;

/**
 * Given a set of European-type option data (price, spot, time to expiry) and futures (or discount factor),
 * derive implied volatility via {@link BlackFormulaRepository}, then compute Greeks by using the derived volatility
 *
 * The inverse is also implemented, i.e., volatility -> price,  where the Greeks are computed at the same time.
 */
public class BlackImpliedVolatilityWithGreeks {

  /**
   * Compute implied volatility, spot option delta, spot option gamma, spot option vega WITH forward price of underlying
   * @param spotOptionPrice Option price of spot
   * @param forward Forward price of underlying
   * @param spot Spot price of underlying
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param isCall True for call, false for put
   * @return {implied volatility, delta, gamma, vega}
   */
  public double[] getImpliedVolatilityAndGreeksForward(final double spotOptionPrice, final double forward, final double spot, final double strike, final double timeToExpiry,
      final boolean isCall) {
    ArgumentChecker.isTrue(spotOptionPrice > 0.0, "non-positive/NaN spot option price; have {}", spotOptionPrice);
    ArgumentChecker.isTrue(forward > 0.0, "non-positive/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(spot > 0.0, "non-positive/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike > 0.0, "non-positive/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "non-positive/NaN time to expiry; have {}", timeToExpiry);

    final double df = spot / forward;
    final double rescaledPrice = spotOptionPrice / df;

    final double impliedVol = BlackFormulaRepository.impliedVolatility(rescaledPrice, forward, strike, timeToExpiry, isCall);
    final double delta = BlackFormulaRepository.delta(forward, strike, timeToExpiry, impliedVol, isCall);
    final double gamma = BlackFormulaRepository.gamma(forward, strike, timeToExpiry, impliedVol) / df;
    final double vega = df * BlackFormulaRepository.vega(forward, strike, timeToExpiry, impliedVol);

    return new double[] {impliedVol, delta, gamma, vega };
  }

  /**
   * Compute implied volatility, spot option delta, spot option gamma, spot option vega WITH forward option price
   * @param spotOptionPrice Spot option price
   * @param forwardOptionPrice Forward option price
   * @param spot Spot price of underlying
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param isCall True for call, false for put
   * @return {implied volatility, delta, gamma, vega}
   */
  public double[] getImpliedVolatilityAndGreeksForwardOption(final double spotOptionPrice, final double forwardOptionPrice, final double spot, final double strike, final double timeToExpiry,
      final boolean isCall) {
    ArgumentChecker.isTrue(spotOptionPrice > 0.0, "non-positive/NaN spot option price; have {}", spotOptionPrice);
    ArgumentChecker.isTrue(forwardOptionPrice > 0.0, "non-positive/NaN forward option price; have {}", forwardOptionPrice);
    ArgumentChecker.isTrue(spot > 0.0, "non-positive/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike > 0.0, "non-positive/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "non-positive/NaN time to expiry; have {}", timeToExpiry);

    final double df = spotOptionPrice / forwardOptionPrice;
    final double forward = spot / df;

    final double impliedVol = BlackFormulaRepository.impliedVolatility(forwardOptionPrice, forward, strike, timeToExpiry, isCall);
    final double delta = BlackFormulaRepository.delta(forward, strike, timeToExpiry, impliedVol, isCall);
    final double gamma = BlackFormulaRepository.gamma(forward, strike, timeToExpiry, impliedVol) / df;
    final double vega = df * BlackFormulaRepository.vega(forward, strike, timeToExpiry, impliedVol);

    return new double[] {impliedVol, delta, gamma, vega };
  }

  /**
   * Compute implied volatility, spot option delta, spot option gamma, spot option vega WITH discount factor
   * @param spotOptionPrice Spot option price
   * @param discountFactor Discount factor
   * @param spot Spot price of underlying
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param isCall True for call, false for put
   * @return {implied volatility, delta, gamma, vega}
   */
  public double[] getImpliedVolatilityAndGreeksDiscountFactor(final double spotOptionPrice, final double discountFactor, final double spot, final double strike, final double timeToExpiry,
      final boolean isCall) {
    ArgumentChecker.isTrue(spotOptionPrice > 0.0, "non-positive/NaN spot option price; have {}", spotOptionPrice);
    ArgumentChecker.isTrue(discountFactor > 0.0, "non-positive/NaN discount factor; have {}", discountFactor);
    ArgumentChecker.isTrue(spot > 0.0, "non-positive/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike > 0.0, "non-positive/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "non-positive/NaN time to expiry; have {}", timeToExpiry);

    final double rescaledPrice = spotOptionPrice / discountFactor;
    final double forward = spot / discountFactor;

    final double impliedVol = BlackFormulaRepository.impliedVolatility(rescaledPrice, forward, strike, timeToExpiry, isCall);
    final double delta = BlackFormulaRepository.delta(forward, strike, timeToExpiry, impliedVol, isCall);
    final double gamma = BlackFormulaRepository.gamma(forward, strike, timeToExpiry, impliedVol) / discountFactor;
    final double vega = discountFactor * BlackFormulaRepository.vega(forward, strike, timeToExpiry, impliedVol);

    return new double[] {impliedVol, delta, gamma, vega };
  }

  /**
   * Compute spot option price, spot option delta, spot option gamma, spot option vega
   * @param forward Forward price of underlying
   * @param spot Spot price of underlying
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param vol Volatility
   * @param isCall True for call, false for put
   * @return {price, delta, gamma, vega}
   */
  public double[] getPriceAndGreeksForward(final double forward, final double spot, final double strike, final double timeToExpiry, final double vol, final boolean isCall) {
    ArgumentChecker.isTrue(forward > 0.0, "non-positive/NaN forward; have {}", forward);
    ArgumentChecker.isTrue(spot > 0.0, "non-positive/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike > 0.0, "non-positive/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "non-positive/NaN time to expiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(vol > 0.0, "non-positive/NaN volatility; have {}", vol);

    final double df = spot / forward;

    final double price = df * BlackFormulaRepository.price(forward, strike, timeToExpiry, vol, isCall);
    final double delta = BlackFormulaRepository.delta(forward, strike, timeToExpiry, vol, isCall);
    final double gamma = BlackFormulaRepository.gamma(forward, strike, timeToExpiry, vol) / df;
    final double vega = df * BlackFormulaRepository.vega(forward, strike, timeToExpiry, vol);

    return new double[] {price, delta, gamma, vega };
  }

  /**
   * Compute spot option price, spot option delta, spot option gamma, spot option vega
   * @param discountFactor Discount factor
   * @param spot Spot price of underlying
   * @param strike Strike price
   * @param timeToExpiry Time to expiry
   * @param vol Volatility
   * @param isCall True for call, false for put
   * @return {price, delta, gamma, vega}
   */
  public double[] getPriceAndGreeksDiscountFactor(final double discountFactor, final double spot, final double strike, final double timeToExpiry, final double vol, final boolean isCall) {
    ArgumentChecker.isTrue(discountFactor > 0.0, "non-positive/NaN discount factor; have {}", discountFactor);
    ArgumentChecker.isTrue(spot > 0.0, "non-positive/NaN spot; have {}", spot);
    ArgumentChecker.isTrue(strike > 0.0, "non-positive/NaN strike; have {}", strike);
    ArgumentChecker.isTrue(timeToExpiry > 0.0, "non-positive/NaN time to expiry; have {}", timeToExpiry);
    ArgumentChecker.isTrue(vol > 0.0, "non-positive/NaN volatility; have {}", vol);

    final double forward = spot / discountFactor;

    final double price = discountFactor * BlackFormulaRepository.price(forward, strike, timeToExpiry, vol, isCall);
    final double delta = BlackFormulaRepository.delta(forward, strike, timeToExpiry, vol, isCall);
    final double gamma = BlackFormulaRepository.gamma(forward, strike, timeToExpiry, vol) / discountFactor;
    final double vega = discountFactor * BlackFormulaRepository.vega(forward, strike, timeToExpiry, vol);

    return new double[] {price, delta, gamma, vega };
  }
}
