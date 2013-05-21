/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the price for an interest rate future with discounting (like a forward).
 * No convexity adjustment is done.
 */
public final class SwapFuturesPriceDeliverableTransactionHullWhiteMethod extends SwapFuturesPriceDeliverableTransactionMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final SwapFuturesPriceDeliverableTransactionHullWhiteMethod INSTANCE = new SwapFuturesPriceDeliverableTransactionHullWhiteMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static SwapFuturesPriceDeliverableTransactionHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private SwapFuturesPriceDeliverableTransactionHullWhiteMethod() {
  }

  private static final SwapFuturesPriceDeliverableSecurityHullWhiteMethod METHOD_SECURITY = SwapFuturesPriceDeliverableSecurityHullWhiteMethod.getInstance();

  /**
   * Computes the present value without convexity adjustment.
   * @param futures The futures.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final SwapFuturesPriceDeliverableTransaction futures, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    return presentValueFromPrice(futures, METHOD_SECURITY.price(futures.getUnderlying(), hwMulticurves));
  }

  /**
   * Computes the present value curve sensitivity by discounting without convexity adjustment.
   * @param futures The futures.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The present value curve sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final SwapFuturesPriceDeliverableTransaction futures, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    return presentValueCurveSensitivity(futures, METHOD_SECURITY.priceCurveSensitivity(futures.getUnderlying(), hwMulticurves));
  }

}
