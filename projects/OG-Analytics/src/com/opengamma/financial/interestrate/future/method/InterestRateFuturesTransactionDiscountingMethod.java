/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.InterestRateFutureTransaction;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Method to compute the present value and its sensitivities for an interest rate future with discounting (like a forward). 
 * No convexity adjustment is done. 
 */
public class InterestRateFuturesTransactionDiscountingMethod {

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param future The future.
   * @param price The quoted price.
   * @return The present value.
   */
  public double presentValueFromPrice(final InterestRateFutureTransaction future, final double price) {
    double pv = (price - future.getReferencePrice()) * future.getUnderlyingFuture().getPaymentAccrualFactor() * future.getUnderlyingFuture().getNotional() * future.getQuantity();
    return pv;
  }

  /**
   * Computes the present value of future from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The present value.
   */
  public double presentValueFromCurve(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "FRA");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getUnderlyingFuture().getForwardCurveName());
    double forward = (forwardCurve.getDiscountFactor(future.getUnderlyingFuture().getFixingPeriodStartTime()) / 
        forwardCurve.getDiscountFactor(future.getUnderlyingFuture().getFixingPeriodEndTime()) - 1)
        / future.getUnderlyingFuture().getFixingPeriodAccrualFactor();
    double pv = (1 - forward - future.getReferencePrice()) * future.getUnderlyingFuture().getPaymentAccrualFactor() * future.getUnderlyingFuture().getNotional() * future.getQuantity();
    return pv;
  }

}
