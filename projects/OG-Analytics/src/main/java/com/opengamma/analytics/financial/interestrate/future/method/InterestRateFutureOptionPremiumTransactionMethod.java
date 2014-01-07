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
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public abstract class InterestRateFutureOptionPremiumTransactionMethod implements PricingMethod {

  /**
   * The method used to price the underlying security.
   */
  private final InterestRateFutureOptionPremiumSecurityMethod _securityMethod;

  /**
   * Constructor.
   * @param securityMethod The method to price the underlying security.
   */
  public InterestRateFutureOptionPremiumTransactionMethod(final InterestRateFutureOptionPremiumSecurityMethod securityMethod) {
    _securityMethod = securityMethod;
  }

  /**
   * Gets the method to price the underlying security.
   * @return The method.
   */
  public InterestRateFutureOptionPremiumSecurityMethod getSecurityMethod() {
    return _securityMethod;
  }

  /**
   * Compute the present value of a future option transaction from a quoted price.
   * @param option The future option.
   * @param curves The yield curves. Should contain the discounting and forward curves associated to the instrument.
   * @param price The option price to be used for the present value.
   * @return The present value.
   */
  public double presentValueFromPrice(final InterestRateFutureOptionPremiumTransaction option, final YieldCurveBundle curves, final double price) {
    final PresentValueCalculator pvc = PresentValueCalculator.getInstance();
    final double premiumPV = option.getPremium().accept(pvc, curves);
    final double optionPV = price * option.getQuantity() * option.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * option.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor();
    return optionPV + premiumPV;
  }

}
