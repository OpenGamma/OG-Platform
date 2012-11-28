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
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Method to compute the price for an interest rate future with discounting (like a forward).
 * No convexity adjustment is done.
 */
public final class InterestRateFutureDiscountingMethod extends InterestRateFutureMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final InterestRateFutureDiscountingMethod INSTANCE = new InterestRateFutureDiscountingMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static InterestRateFutureDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private InterestRateFutureDiscountingMethod() {
  }

  /**
   * Computes the price of a future from the curves using an estimation of the future rate without convexity adjustment.
   * @param futures The futures.
   * @param multicurves The multi-curve provider.
   * @return The price.
   */
  public double price(final InterestRateFuture futures, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(futures, "Futures");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final double forward = multicurves.getForwardRate(futures.getIborIndex(), futures.getFixingPeriodStartTime(), futures.getFixingPeriodEndTime(), futures.getFixingPeriodAccrualFactor());
    final double price = 1.0 - forward;
    return price;
  }

  /**
   * Computes the present value without convexity adjustment.
   * @param futures The futures.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(final InterestRateFuture futures, final MulticurveProviderInterface multicurves) {
    final double pv = presentValueFromPrice(futures, price(futures, multicurves));
    return MultipleCurrencyAmount.of(futures.getCurrency(), pv);
  }

  /**
   * Computes the future rate (1-price) from the curves using an estimation of the future rate without convexity adjustment.
   * @param futures The futures.
   * @param multicurves The multi-curve provider.
   * @return The rate.
   */
  public double parRate(final InterestRateFuture futures, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(futures, "Futures");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    return multicurves.getForwardRate(futures.getIborIndex(), futures.getFixingPeriodStartTime(), futures.getFixingPeriodEndTime(), futures.getFixingPeriodAccrualFactor());
  }

  /**
   * Compute the price sensitivity to rates of an interest rate future by discounting.
   * @param futures The futures.
   * @param multicurves The multi-curve provider.
   * @return The price rate sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final InterestRateFuture futures, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(futures, "Futures");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    // Partials - XBar => d(price)/dX
    final double priceBar = 1.0;
    final double forwardBar = -priceBar;
    final Map<String, List<ForwardSensitivity>> mapFwd = new HashMap<String, List<ForwardSensitivity>>();
    final List<ForwardSensitivity> listForward = new ArrayList<ForwardSensitivity>();
    listForward.add(new ForwardSensitivity(futures.getFixingPeriodStartTime(), futures.getFixingPeriodEndTime(), futures.getFixingPeriodAccrualFactor(), forwardBar));
    mapFwd.put(multicurves.getName(futures.getIborIndex()), listForward);
    return MulticurveSensitivity.ofForward(mapFwd);
  }

}
