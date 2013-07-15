/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Methods for the pricing of interest rate futures generic to all models.
 */
public abstract class InterestRateFutureSecurityMethod {

  /**
   * Compute the price sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param multicurve The multi-curves provider. 
   * @return The price rate sensitivity.
   */
  public abstract MulticurveSensitivity priceCurveSensitivity(final InterestRateFutureSecurity future, final MulticurveProviderInterface multicurve);

}
