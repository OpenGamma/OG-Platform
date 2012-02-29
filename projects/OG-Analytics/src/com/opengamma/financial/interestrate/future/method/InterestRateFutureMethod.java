/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.financial.interestrate.method.PricingMethod;

/**
 * Methods for the pricing of interest rate futures generic to all models.
 */
public abstract class InterestRateFutureMethod implements PricingMethod {

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param future The future.
   * @param price The quoted price.
   * @return The present value.
   */
  public double presentValueFromPrice(final InterestRateFuture future, final double price) {
    double pv = (price - future.getReferencePrice()) * future.getPaymentAccrualFactor() * future.getNotional() * future.getQuantity();
    return pv;
  }

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The present value rate sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFuture future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    InterestRateCurveSensitivity priceSensi = priceCurveSensitivity(future, curves);
    InterestRateCurveSensitivity result = priceSensi.multiply(future.getPaymentAccrualFactor() * future.getNotional() * future.getQuantity());
    return result;
  }

  /**
   * Compute the price sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The price rate sensitivity.
   */
  public abstract InterestRateCurveSensitivity priceCurveSensitivity(final InterestRateFuture future, final YieldCurveBundle curves);

}
