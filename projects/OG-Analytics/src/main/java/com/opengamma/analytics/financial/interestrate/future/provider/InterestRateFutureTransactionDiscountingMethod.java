/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the price for an interest rate future with discounting (like a forward).
 * No convexity adjustment is done.
 */
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

  private static final InterestRateFutureSecurityDiscountingMethod METHOD_SECURITY = InterestRateFutureSecurityDiscountingMethod.getInstance();

  /**
   * Computes the present value without convexity adjustment.
   * @param futures The futures.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final InterestRateFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    return presentValueFromPrice(futures, METHOD_SECURITY.price(futures.getUnderlyingFuture(), multicurves));
  }

  /**
   * Computes the present value curve sensitivity by discounting without convexity adjustment.
   * @param futures The futures.
   * @param multicurves The multi-curve provider.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final InterestRateFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    return presentValueCurveSensitivity(futures, METHOD_SECURITY.priceCurveSensitivity(futures.getUnderlyingFuture(), multicurves));
  }

}
