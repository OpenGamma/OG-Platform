/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureDiscountingMethod;

/**
 * Returns the underlying future price of the security, given a yield curve bundle
 */
public class DiscountingUnderlyingFuturePriceCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /** The method unique instance.*/
  private static final DiscountingUnderlyingFuturePriceCalculator INSTANCE = new DiscountingUnderlyingFuturePriceCalculator();

  /** @return the unique instance of the class. */
  public static DiscountingUnderlyingFuturePriceCalculator getInstance() {
    return INSTANCE;
  }

  /** Constructor. */
  DiscountingUnderlyingFuturePriceCalculator() {
  }

  /** The method used to compute the future price. It is a method without convexity adjustment.  */
  private static final InterestRateFutureDiscountingMethod METHOD_FUTURE = InterestRateFutureDiscountingMethod.getInstance();

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final YieldCurveBundle curves) {
    return METHOD_FUTURE.price(option.getUnderlyingFuture(), curves);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction option, final YieldCurveBundle curves) {
    return METHOD_FUTURE.price(option.getUnderlyingOption().getUnderlyingFuture(), curves);
  }

}
