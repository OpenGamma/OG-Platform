/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import com.opengamma.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.util.surface.SurfaceValue;

/**
 * Method for the pricing of interest rate future options with up-front premium. The pricing is done with a Black approach on the future rate (1.0-price).
 * The Black parameters are represented by (expiration-strike-delay) surfaces. The "delay" is the time between option expiration and future last trading date,
 * i.e. 0 for quarterly options and x for x-year mid-curve options. The future prices are computed without convexity adjustments.
 */
public final class InterestRateFutureOptionMarginTransactionBlackSurfaceMethod extends InterestRateFutureOptionMarginTransactionMethod {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureOptionMarginTransactionBlackSurfaceMethod INSTANCE = new InterestRateFutureOptionMarginTransactionBlackSurfaceMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureOptionMarginTransactionBlackSurfaceMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureOptionMarginTransactionBlackSurfaceMethod() {
    super(InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance());
  }

  /**
   * Computes the present value curve sensitivity of a transaction.
   * @param transaction The future option transaction.
   * @param blackData The curve and Black volatility data.
   * @return The present value curve sensitivity.
   */
  public SurfaceValue presentValueBlackSensitivity(final InterestRateFutureOptionMarginTransaction transaction, final YieldCurveWithBlackCubeBundle blackData) {
    SurfaceValue securitySensitivity = ((InterestRateFutureOptionMarginSecurityBlackSurfaceMethod) getSecurityMethod()).priceBlackSensitivity(transaction.getUnderlyingOption(), blackData);
    securitySensitivity = SurfaceValue.multiplyBy(securitySensitivity, transaction.getQuantity() * transaction.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * transaction.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor());
    return securitySensitivity;
  }

}
