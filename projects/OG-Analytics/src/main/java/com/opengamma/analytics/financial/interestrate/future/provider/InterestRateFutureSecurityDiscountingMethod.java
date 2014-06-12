/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Methods for the pricing of Federal Funds futures by discounting (using average of forward rates; not convexity adjustment).
 */
public final class InterestRateFutureSecurityDiscountingMethod extends FuturesSecurityMulticurveMethod {

  /**
   * Creates the method unique instance.
   */
  private static final InterestRateFutureSecurityDiscountingMethod INSTANCE = new InterestRateFutureSecurityDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static InterestRateFutureSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureSecurityDiscountingMethod() {
  }

  /**
   * Computes the future rate (1-price) from the curves using an estimation of the future rate without convexity adjustment.
   * @param futures The futures.
   * @param multicurves The multi-curve provider.
   * @return The rate.
   */
  public double parRate(final InterestRateFutureSecurity futures, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(futures, "Futures");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    return multicurves.getSimplyCompoundForwardRate(futures.getIborIndex(), futures.getFixingPeriodStartTime(), futures.getFixingPeriodEndTime(),
        futures.getFixingPeriodAccrualFactor());
  }

}
