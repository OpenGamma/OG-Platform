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

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProvider;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the price for an interest rate future with convexity adjustment from a Hull-White one factor model.
 * <p> Reference: Henrard M., Eurodollar Futures and Options: Convexity Adjustment in HJM One-Factor Model. March 2005. 
 * Available at <a href="http://ssrn.com/abstract=682343">http://ssrn.com/abstract=682343</a>
 */
public final class InterestRateFutureSecurityHullWhiteMethod extends InterestRateFutureMethod {

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

  /**
   * Computes the price of a future from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The price.
   */
  public double price(final InterestRateFuture future, final HullWhiteOneFactorProvider hwMulticurves) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(hwMulticurves, "Multi-curves with Hull-White");
    double forward = hwMulticurves.getMulticurveProvider().getForwardRate(future.getIborIndex(), future.getFixingPeriodStartTime(), future.getFixingPeriodEndTime(),
        future.getFixingPeriodAccrualFactor());
    double futureConvexityFactor = MODEL.futureConvexityFactor(hwMulticurves.getHullWhiteParameters(), future.getLastTradingTime(), future.getFixingPeriodStartTime(), future.getFixingPeriodEndTime());
    double price = 1.0 - futureConvexityFactor * forward + (1 - futureConvexityFactor) / future.getFixingPeriodAccrualFactor();
    return price;
  }

  public MultipleCurrencyAmount presentValue(final InterestRateFuture future, final HullWhiteOneFactorProvider hwMulticurves) {
    final double pv = presentValueFromPrice(future, price(future, hwMulticurves));
    return MultipleCurrencyAmount.of(future.getCurrency(), pv);
  }

  /**
   * Compute the price sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The price rate sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final InterestRateFuture future, final HullWhiteOneFactorProvider hwMulticurves) {
    ArgumentChecker.notNull(future, "Future");
    ArgumentChecker.notNull(hwMulticurves, "Multi-curves with Hull-White");
    double futureConvexityFactor = MODEL.futureConvexityFactor(hwMulticurves.getHullWhiteParameters(), future.getLastTradingTime(), future.getFixingPeriodStartTime(), future.getFixingPeriodEndTime());
    // Backward sweep
    double priceBar = 1.0;
    double forwardBar = -futureConvexityFactor * priceBar;
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<String, List<ForwardSensitivity>>();
    final List<ForwardSensitivity> listForward = new ArrayList<ForwardSensitivity>();
    listForward.add(new ForwardSensitivity(future.getFixingPeriodStartTime(), future.getFixingPeriodEndTime(), future.getFixingPeriodAccrualFactor(), forwardBar));
    mapFwd.put(hwMulticurves.getMulticurveProvider().getName(future.getIborIndex()), listForward);
    return MulticurveSensitivity.ofForward(mapFwd);
  }

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param hwMulticurves The multi-curves provider with Hull-White one factor parameters.
   * @return The present value rate sensitivity.
   * TODO: REVIEW: Should this method be in InterestRateFutureProviderMethod?
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(final InterestRateFuture future, final HullWhiteOneFactorProvider hwMulticurves) {
    MulticurveSensitivity priceSensi = priceCurveSensitivity(future, hwMulticurves);
    MultipleCurrencyMulticurveSensitivity result = MultipleCurrencyMulticurveSensitivity.of(future.getCurrency(),
        priceSensi.multipliedBy(future.getPaymentAccrualFactor() * future.getNotional() * future.getQuantity()));
    return result;
  }

}
