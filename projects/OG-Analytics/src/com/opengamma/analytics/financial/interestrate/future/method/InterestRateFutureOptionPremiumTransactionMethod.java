/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;

/**
 * Methods for the pricing of interest rate futures option with premium generic to all models.
 */
public abstract class InterestRateFutureOptionPremiumTransactionMethod implements PricingMethod {

  /**
   * Compute the present value of a future option transaction from a quoted price.
   * @param option The future option.
   * @param curves The yield curves. Should contain the discounting and forward curves associated to the instrument. 
   * @param price The quoted price.
   * @return The present value.
   */
  public double presentValueFromPrice(final InterestRateFutureOptionPremiumTransaction option, final YieldCurveBundle curves, final double price) {
    PresentValueCalculator pvc = PresentValueCalculator.getInstance();
    double premiumPV = pvc.visit(option.getPremium(), curves);
    double optionPV = price * option.getQuantity() * option.getUnderlyingOption().getUnderlyingFuture().getNotional() * option.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor();
    return optionPV + premiumPV;
  }

}
