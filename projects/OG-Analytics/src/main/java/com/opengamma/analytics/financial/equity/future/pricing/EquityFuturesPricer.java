/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.pricing;

import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;

/**
 * Common interface for all {@link EquityFuturePricingMethod}'s.
 */
public interface EquityFuturesPricer {

  /**
   * @return singleton instance of the pricing method
   */
  //EquityFuturesPricer getInstance();

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return Present value of the derivative
   */
  double presentValue(final EquityFuture future,
                             final SimpleFutureDataBundle dataBundle);

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a unit value change in the underlying's spot value
   */
  double spotDelta(final EquityFuture future,
                             final SimpleFutureDataBundle dataBundle);

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a unit value change in the discount rate
   */
  double ratesDelta(final EquityFuture future,
                             final SimpleFutureDataBundle dataBundle);

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The change in the present value given a basis point change in the discount rate
   */
  double pv01(EquityFuture future, SimpleFutureDataBundle dataBundle);

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The spot price of the equity or index
   */
  double spotPrice(EquityFuture future, SimpleFutureDataBundle dataBundle);

  /**
   * @param future EquityFuture derivative
   * @param dataBundle Contains funding curve, spot value and continuous dividend yield 
   * @return The forward price of the equity or index
   */
  double forwardPrice(EquityFuture future, SimpleFutureDataBundle dataBundle);

}
