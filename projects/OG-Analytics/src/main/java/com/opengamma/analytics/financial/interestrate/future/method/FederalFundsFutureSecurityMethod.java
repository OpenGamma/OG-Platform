/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;

/**
 * Methods for the pricing of Federal Funds futures generic to all models.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public abstract class FederalFundsFutureSecurityMethod implements PricingMethod {

  /**
   * Computes the price (quoted number) for Federal Funds futures securities from curves.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated.
   * @return The price.
   */
  public abstract double price(final FederalFundsFutureSecurity future, final YieldCurveBundle curves);

  /**
   * Compute the price sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated.
   * @return The price rate sensitivity.
   */
  public abstract InterestRateCurveSensitivity priceCurveSensitivity(final FederalFundsFutureSecurity future, final YieldCurveBundle curves);

}
