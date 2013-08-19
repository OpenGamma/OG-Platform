/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;

/**
 * Methods for the pricing of interest rate futures generic to all models.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public abstract class InterestRateFutureSecurityMethod implements PricingMethod {

  /**
   * Compute the price sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated.
   * @return The price rate sensitivity.
   */
  public abstract InterestRateCurveSensitivity priceCurveSensitivity(final InterestRateFutureSecurity future, final YieldCurveBundle curves);

  public abstract InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFutureSecurity future, final YieldCurveBundle curves);

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param priceSensi Price sensitivity.
   * @return The present value rate sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFutureSecurity future, final InterestRateCurveSensitivity priceSensi) {
    Validate.notNull(future, "Future");
    final InterestRateCurveSensitivity result = priceSensi.multipliedBy(future.getPaymentAccrualFactor() * future.getNotional());
    return result;
  }

}
