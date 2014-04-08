/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Methods for the pricing of interest rate futures generic to all models.
 */
public abstract class InterestRateFutureTransactionMethod {

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param futures The future.
   * @param price The quoted price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromPrice(final InterestRateFutureTransaction futures, final double price) {
    double pv = (price - futures.getReferencePrice()) * futures.getUnderlyingFuture().getPaymentAccrualFactor() * futures.getUnderlyingFuture().getNotional() * futures.getQuantity();
    return MultipleCurrencyAmount.of(futures.getCurrency(), pv);
  }

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param futures The future.
   * @param priceSensitivity The sensitivity of the futures price.
   * @return The present value rate sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final InterestRateFutureTransaction futures, final MulticurveSensitivity priceSensitivity) {
    Validate.notNull(futures, "Future");
    MulticurveSensitivity result = priceSensitivity.multipliedBy(futures.getUnderlyingFuture().getPaymentAccrualFactor() * futures.getUnderlyingFuture().getNotional() * futures.getQuantity());
    return MultipleCurrencyMulticurveSensitivity.of(futures.getCurrency(), result);
  }

}
