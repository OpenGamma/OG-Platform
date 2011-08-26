/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value and its sensitivities for an interest rate future with Hull-White model convexity adjustment.
 */
public class InterestRateFutureTransactionHullWhiteMethod extends InterestRateFutureTransactionMethod {

  /**
   * The method used to compute the future price.
   */
  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_SECURITY = new InterestRateFutureSecurityHullWhiteMethod();

  /**
   * Present value computation using the hull-White model for the convexity adjustment.
   * @param future The future.
   * @param curves The Hull-White parameters and the curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final InterestRateFutureTransaction future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    double futurePrice = METHOD_SECURITY.price(future.getUnderlyingFuture(), curves);
    double pv = presentValueFromPrice(future, futurePrice);
    return CurrencyAmount.of(future.getUnderlyingFuture().getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof InterestRateFutureTransaction, "Interest rate future transaction");
    Validate.isTrue(curves instanceof HullWhiteOneFactorPiecewiseConstantDataBundle, "Bundle should contain Hull-White data");
    return presentValue((InterestRateFutureTransaction) instrument, (HullWhiteOneFactorPiecewiseConstantDataBundle) curves);
  }

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The Hull-White parameters and the curves.
   * @return The present value rate sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final InterestRateFutureTransaction future, final HullWhiteOneFactorPiecewiseConstantDataBundle curves) {
    Validate.notNull(future, "Future");
    final InterestRateFutureSecurity underlyingFuture = future.getUnderlyingFuture();
    PresentValueSensitivity priceSensi = METHOD_SECURITY.priceCurveSensitivity(underlyingFuture, curves);
    PresentValueSensitivity result = priceSensi.multiply(future.getUnderlyingFuture().getPaymentAccrualFactor() * future.getUnderlyingFuture().getNotional() * future.getQuantity());
    return result;
  }

}
