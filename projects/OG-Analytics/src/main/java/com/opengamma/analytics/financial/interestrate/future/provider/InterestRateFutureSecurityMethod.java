/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Methods for the pricing of interest rate futures generic to all models.
 */
public abstract class InterestRateFutureSecurityMethod {

  /**
   * Compute the price of STIR Futures in a given model.
   * @param futures The STIR futures.
   * @param multicurve The multi-curve and parameters provider.
   * @return The price.
   */
  abstract double price(final InterestRateFutureSecurity futures, final ParameterProviderInterface multicurve);

  /**
   * Compute the price sensitivity to rates of a interest rate future by discounting.
   * @param futures The STIR futures.
   * @param multicurve The multi-curves provider. 
   * @return The price rate sensitivity.
   */
  public abstract MulticurveSensitivity priceCurveSensitivity(final InterestRateFutureSecurity futures, final ParameterProviderInterface multicurve);

  /**
   * Returns the convexity adjustment, i.e. the difference between the price and the forward rate of the underlying Ibor.
   * @param futures The STIR futures.
   * @param multicurve The multi-curve and parameters provider.
   * @return The adjustment.
   */
  public double convexityAdjustment(final InterestRateFutureSecurity futures, final ParameterProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "swap futures");
    ArgumentChecker.notNull(multicurve, "parameter provider");
    double rate = multicurve.getMulticurveProvider().getForwardRate(futures.getIborIndex(), futures.getFixingPeriodStartTime(), futures.getFixingPeriodEndTime(),
        futures.getFixingPeriodAccrualFactor());
    double price = price(futures, multicurve);
    return price - (1.0d - rate);
  }

}
