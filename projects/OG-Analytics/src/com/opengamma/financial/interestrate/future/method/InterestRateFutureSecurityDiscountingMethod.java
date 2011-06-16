/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.InterestRateFutureSecurity;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Method to compute the price for an interest rate future with discounting (like a forward). 
 * No convexity adjustment is done. 
 */
public final class InterestRateFutureSecurityDiscountingMethod {
  private static final InterestRateFutureSecurityDiscountingMethod INSTANCE = new InterestRateFutureSecurityDiscountingMethod();

  public static InterestRateFutureSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  private InterestRateFutureSecurityDiscountingMethod() {
  }

  /**
   * Computes the price of a future from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The price.
   */
  public double price(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime()) - 1)
        / future.getFixingPeriodAccrualFactor();
    final double price = 1.0 - forward;
    return price;
  }

  /**
   * Computes the future rate (1-price) from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The rate.
   */
  public double parRate(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    final double forward = (forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime()) - 1)
        / future.getFixingPeriodAccrualFactor();
    return forward;

  }

}
