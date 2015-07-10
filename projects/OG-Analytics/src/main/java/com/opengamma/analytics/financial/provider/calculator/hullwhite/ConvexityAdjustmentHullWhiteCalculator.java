/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.SwapFuturesPriceDeliverableSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;

/**
 * Calculate the convexity adjustment of instruments in the Hull-White one factor model.
 */
public class ConvexityAdjustmentHullWhiteCalculator extends InstrumentDerivativeVisitorAdapter<HullWhiteOneFactorProviderInterface, Double> {

  /**
   * An instance of the calculator.
   */
  private static final ConvexityAdjustmentHullWhiteCalculator INSTANCE = new ConvexityAdjustmentHullWhiteCalculator();

  /**
   * Constructor.
   */
  protected ConvexityAdjustmentHullWhiteCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ConvexityAdjustmentHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final SwapFuturesPriceDeliverableSecurityHullWhiteMethod METHOD_SWAPFUT = SwapFuturesPriceDeliverableSecurityHullWhiteMethod.getInstance();
  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_STIRFUT = InterestRateFutureSecurityHullWhiteMethod.getInstance();

  //     -----     Futures     -----

  @Override
  public Double visitInterestRateFutureSecurity(final InterestRateFutureSecurity futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_STIRFUT.convexityAdjustment(futures, hullWhite);
  }

  @Override
  public Double visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWAPFUT.convexityAdjustment(futures, hullWhite);
  }

}
