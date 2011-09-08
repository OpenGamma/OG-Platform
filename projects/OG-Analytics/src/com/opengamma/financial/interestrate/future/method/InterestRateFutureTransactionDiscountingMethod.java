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
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value and its sensitivities for an interest rate future with discounting (like a forward). 
 * No convexity adjustment is done. 
 */
public final class InterestRateFutureTransactionDiscountingMethod extends InterestRateFutureTransactionMethod {

  /**
   * The rate future security associated method.
   */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_SECURITY = InterestRateFutureSecurityDiscountingMethod.getInstance();

  private static final InterestRateFutureTransactionDiscountingMethod INSTANCE = new InterestRateFutureTransactionDiscountingMethod();

  public static InterestRateFutureTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  private InterestRateFutureTransactionDiscountingMethod() {
  }

  /**
   * Computes the present value of future from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the discounting and forward curves associated to the instrument.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    final InterestRateFutureSecurity underlyingFuture = future.getUnderlyingFuture();
    final double futurePrice = METHOD_SECURITY.price(underlyingFuture, curves);
    final double pv = presentValueFromPrice(future, futurePrice);
    return CurrencyAmount.of(underlyingFuture.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof InterestRateFutureTransaction, "Interest rate future transaction");
    return presentValue((InterestRateFutureTransaction) instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The present value rate sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    final InterestRateFutureSecurity underlyingFuture = future.getUnderlyingFuture();
    PresentValueSensitivity priceSensi = METHOD_SECURITY.priceCurveSensitivity(underlyingFuture, curves);
    PresentValueSensitivity result = priceSensi.multiply(future.getUnderlyingFuture().getPaymentAccrualFactor() * future.getUnderlyingFuture().getNotional() * future.getQuantity());
    return result;
  }

}
