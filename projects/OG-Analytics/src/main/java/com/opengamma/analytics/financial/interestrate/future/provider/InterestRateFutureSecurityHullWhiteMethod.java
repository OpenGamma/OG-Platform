/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Method to compute the price for an interest rate future with convexity adjustment from a Hull-White one factor model.
 * <p> Reference: Henrard M., Eurodollar Futures and Options: Convexity Adjustment in HJM One-Factor Model. March 2005.
 * Available at <a href="http://ssrn.com/abstract=682343">http://ssrn.com/abstract=682343</a>
 */
public final class InterestRateFutureSecurityHullWhiteMethod extends FuturesSecurityHullWhiteMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final InterestRateFutureSecurityHullWhiteMethod INSTANCE = new InterestRateFutureSecurityHullWhiteMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static InterestRateFutureSecurityHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureSecurityHullWhiteMethod() {
  }

  /**
   * Computes the future rate (1-price) from the curves using an estimation of the future rate with Hull-White one factor convexity adjustment.
   * @param futures The futures.
   * @param multicurve The multi-curves provider with Hull-White one factor parameters.
   * @return The rate.
   */
  public double parRate(final InterestRateFutureSecurity futures, final HullWhiteOneFactorProviderInterface multicurve) {
    return 1.0d - price(futures, multicurve);
  }

  /**
   * Returns the convexity adjustment, i.e. the difference between the price and the forward rate of the underlying Ibor.
   * @param futures The STIR futures.
   * @param multicurve The multi-curve and parameters provider.
   * @return The adjustment.
   */
  public double convexityAdjustment(final InterestRateFutureSecurity futures, final HullWhiteOneFactorProviderInterface multicurve) {
    ArgumentChecker.notNull(futures, "swap futures");
    ArgumentChecker.notNull(multicurve, "parameter provider");
    double rate = multicurve.getMulticurveProvider().getSimplyCompoundForwardRate(futures.getIborIndex(), futures.getFixingPeriodStartTime(), futures.getFixingPeriodEndTime(),
        futures.getFixingPeriodAccrualFactor());
    double price = price(futures, multicurve);
    return price - (1.0d - rate);
  }

}
