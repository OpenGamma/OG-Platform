/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Method to compute the price for an interest rate future with convexity adjustment from a Hull-White one factor model.
 * <p> Reference: Henrard M., Eurodollar Futures and Options: Convexity Adjustment in HJM One-Factor Model. March 2005.
 * Available at <a href="http://ssrn.com/abstract=682343">http://ssrn.com/abstract=682343</a>
 */
public final class InterestRateFutureSecurityHullWhiteMethod extends InterestRateFutureSecurityMethod {

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
   * The Hull-White model.
   */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  @Override
  public double price(final InterestRateFutureSecurity futures, final ParameterProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "Multi-curve and parameter provider");
    ArgumentChecker.isTrue(multicurve instanceof HullWhiteOneFactorProviderInterface, "Multi-curve and HW provider");
    return price(futures, (HullWhiteOneFactorProviderInterface) multicurve);
  }

  /**
   * Computes the price of a future from the curves using an estimation of the future rate without convexity adjustment.
   * @param futures The STIR future.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The price.
   */
  public double price(final InterestRateFutureSecurity futures, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(hwMulticurves, "Multi-curve with Hull-White");
    final double forward = hwMulticurves.getMulticurveProvider().getSimplyCompoundForwardRate(futures.getIborIndex(), futures.getFixingPeriodStartTime(), futures.getFixingPeriodEndTime(),
        futures.getFixingPeriodAccrualFactor());
    final double futureConvexityFactor = MODEL.futuresConvexityFactor(hwMulticurves.getHullWhiteParameters(), futures.getTradingLastTime(),
        futures.getFixingPeriodStartTime(), futures.getFixingPeriodEndTime());
    final double price = 1.0 - futureConvexityFactor * forward + (1 - futureConvexityFactor) / futures.getFixingPeriodAccrualFactor();
    return price;
  }

  /**
   * Computes the future rate (1-price) from the curves using an estimation of the future rate with Hull-White one factor convexity adjustment.
   * @param futures The futures.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The rate.
   */
  public double parRate(final InterestRateFutureSecurity futures, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    return 1.0d - price(futures, hwMulticurves);
  }

  @Override
  public MulticurveSensitivity priceCurveSensitivity(final InterestRateFutureSecurity futures, final ParameterProviderInterface multicurve) {
    ArgumentChecker.notNull(multicurve, "Multi-curve and parameter provider");
    ArgumentChecker.isTrue(multicurve instanceof HullWhiteOneFactorProviderInterface, "Multi-curve and HW provider");
    return priceCurveSensitivity(futures, (HullWhiteOneFactorProviderInterface) multicurve);
  }

  /**
   * Compute the price sensitivity to rates of a interest rate future by discounting.
   * @param futures The future.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The price rate sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final InterestRateFutureSecurity futures, final HullWhiteOneFactorProviderInterface hwMulticurves) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(hwMulticurves, "Multi-curves with Hull-White");
    final double futureConvexityFactor = MODEL.futuresConvexityFactor(hwMulticurves.getHullWhiteParameters(), futures.getTradingLastTime(), futures.getFixingPeriodStartTime(),
        futures.getFixingPeriodEndTime());
    // Backward sweep
    final double priceBar = 1.0;
    final double forwardBar = -futureConvexityFactor * priceBar;
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<>();
    final List<ForwardSensitivity> listForward = new ArrayList<>();
    listForward.add(new SimplyCompoundedForwardSensitivity(futures.getFixingPeriodStartTime(), futures.getFixingPeriodEndTime(), futures.getFixingPeriodAccrualFactor(), forwardBar));
    mapFwd.put(hwMulticurves.getMulticurveProvider().getName(futures.getIborIndex()), listForward);
    return MulticurveSensitivity.ofForward(mapFwd);
  }

}
