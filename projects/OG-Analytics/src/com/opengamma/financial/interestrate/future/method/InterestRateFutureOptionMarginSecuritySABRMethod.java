/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import com.opengamma.financial.interestrate.future.definition.InterestRateFutureOptionMarginSecurity;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * Method for the pricing of interest rate future options with margin process. The pricing is done with a SABR approach on the future rate (1.0-price).
 * The SABR parameters are represented by (expiration-delay) surfaces. The "delay" is the time between option expiration and future last trading date, 
 * i.e. 0 for quarterly options and x for x-year mid-curve options. The future prices are computed without convexity adjustments.
 */
public class InterestRateFutureOptionMarginSecuritySABRMethod {

  /**
   * The Black function used in the pricing.
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  /**
   * The method used to compute the future price. It is a method without convexity adjustment.
   */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_FUTURE = new InterestRateFutureSecurityDiscountingMethod();

  /**
   * Computes the option security price from future price.
   * @param security The future option security.
   * @param sabrData The SABR data bundle. 
   * @param priceFuture The price of the underlying future.
   * @return The security price.
   */
  public double optionPriceFromFuturePrice(final InterestRateFutureOptionMarginSecurity security, final SABRInterestRateDataBundle sabrData, final double priceFuture) {
    double rateStrike = 1.0 - security.getStrike();
    EuropeanVanillaOption option = new EuropeanVanillaOption(rateStrike, security.getExpirationTime(), !security.isCall());
    double forward = 1 - priceFuture;
    double delay = security.getUnderlyingFuture().getLastTradingTime() - security.getExpirationTime();
    double volatility = sabrData.getSABRParameter().getVolatility(new double[] {security.getExpirationTime(), delay, rateStrike, forward});
    BlackFunctionData dataBlack = new BlackFunctionData(forward, 1.0, volatility);
    double priceSecurity = BLACK_FUNCTION.getPriceFunction(option).evaluate(dataBlack);
    return priceSecurity;
  }

  /**
   * Computes the option security price. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param sabrData The SABR data bundle. 
   * @return The security price.
   */
  public double optionPrice(final InterestRateFutureOptionMarginSecurity security, final SABRInterestRateDataBundle sabrData) {
    double priceFuture = METHOD_FUTURE.price(security.getUnderlyingFuture(), sabrData);
    return optionPriceFromFuturePrice(security, sabrData, priceFuture);
  }

}
