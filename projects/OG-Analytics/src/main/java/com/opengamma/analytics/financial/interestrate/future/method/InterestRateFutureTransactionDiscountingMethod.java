/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the price for an interest rate future with discounting (like a forward).
 * No convexity adjustment is done.
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureTransactionDiscountingMethod}
 */
@Deprecated
public final class InterestRateFutureTransactionDiscountingMethod extends InterestRateFutureTransactionMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final InterestRateFutureTransactionDiscountingMethod INSTANCE = new InterestRateFutureTransactionDiscountingMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static InterestRateFutureTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureTransactionDiscountingMethod() {
  }

  /**
   * The method to compute result for the underlying security.
   */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_SECURITY = InterestRateFutureSecurityDiscountingMethod.getInstance();

  public CurrencyAmount presentValue(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    final double pv = presentValueFromPrice(future, METHOD_SECURITY.price(future.getUnderlyingFuture(), curves));
    return CurrencyAmount.of(future.getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof InterestRateFutureTransaction, "Interest rate future");
    return presentValue((InterestRateFutureTransaction) instrument, curves);
  }

  /**
   * Computes the future rate (1-price) from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated.
   * @return The rate.
   */
  public double parRate(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getUnderlyingFuture().getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(future.getUnderlyingFuture().getFixingPeriodStartTime()) /
        forwardCurve.getDiscountFactor(future.getUnderlyingFuture().getFixingPeriodEndTime()) - 1)
        / future.getUnderlyingFuture().getFixingPeriodAccrualFactor();
    return forward;
  }

  @Override
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    return presentValueCurveSensitivity(future, METHOD_SECURITY.priceCurveSensitivity(future.getUnderlyingFuture(), curves));
  }

}
