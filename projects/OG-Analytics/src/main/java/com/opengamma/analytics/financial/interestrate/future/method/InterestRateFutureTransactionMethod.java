/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;

/**
 * Methods for the pricing of interest rate futures generic to all models.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public abstract class InterestRateFutureTransactionMethod implements PricingMethod {

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param future The future.
   * @param price The quoted price.
   * @return The present value.
   */
  public double presentValueFromPrice(final InterestRateFutureTransaction future, final double price) {
    final double pv = (price - future.getReferencePrice()) * future.getUnderlyingSecurity().getPaymentAccrualFactor() * future.getUnderlyingSecurity().getNotional() * future.getQuantity();
    return pv;
  }

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param priceSensi Price sensitivity.
   * @return The present value rate sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFutureTransaction future, final InterestRateCurveSensitivity priceSensi) {
    Validate.notNull(future, "Future");
    final InterestRateCurveSensitivity result = priceSensi.multipliedBy(future.getUnderlyingSecurity().getPaymentAccrualFactor() * future.getUnderlyingSecurity().getNotional() * future.getQuantity());
    return result;
  }

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated.
   * @return The present value sensitivity.
   */
  public abstract InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFutureTransaction future, final YieldCurveBundle curves);

}
