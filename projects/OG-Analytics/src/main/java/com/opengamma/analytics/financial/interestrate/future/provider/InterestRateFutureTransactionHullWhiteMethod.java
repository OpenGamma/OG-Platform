/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the price for an interest rate future with discounting (like a forward).
 * No convexity adjustment is done.
 */
public final class InterestRateFutureTransactionHullWhiteMethod extends InterestRateFutureTransactionMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final InterestRateFutureTransactionHullWhiteMethod INSTANCE = new InterestRateFutureTransactionHullWhiteMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static InterestRateFutureTransactionHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureTransactionHullWhiteMethod() {
  }

  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_SECURITY = InterestRateFutureSecurityHullWhiteMethod.getInstance();

  /**
   * Computes the present value without convexity adjustment.
   * @param futures The futures.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final InterestRateFutureTransaction futures, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    return presentValueFromPrice(futures, METHOD_SECURITY.price(futures.getUnderlyingFuture(), hwMulticurves));
  }

  /**
   * Computes the present value curve sensitivity by discounting without convexity adjustment.
   * @param futures The futures.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final InterestRateFutureTransaction futures, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    return presentValueCurveSensitivity(futures, METHOD_SECURITY.priceCurveSensitivity(futures.getUnderlyingFuture(), hwMulticurves));
  }

}
