/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Methods for the pricing of Federal Funds futures by discounting (using average of forward rates; not convexity adjustment).
 */
public final class FederalFundsFutureSecurityDiscountingMethod {

  /**
   * Creates the method unique instance.
   */
  private static final FederalFundsFutureSecurityDiscountingMethod INSTANCE = new FederalFundsFutureSecurityDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static FederalFundsFutureSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FederalFundsFutureSecurityDiscountingMethod() {
  }

  //  /**
  //   * Computes the present value of the future security as the value of one future with a price of 0.
  //   * @param futures The futures.
  //   * @param multicurves The multi-curve provider.
  //   * @return The present value.
  //   */
  //  public MultipleCurrencyAmount presentValue(final FederalFundsFutureSecurity futures, final MulticurveProviderInterface multicurves) {
  //    ArgumentChecker.notNull(futures, "Futures");
  //    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
  //    double price = price(futures, multicurves);
  //    double pv = price * futures.getPaymentAccrualFactor() * futures.getNotional();
  //    return MultipleCurrencyAmount.of(futures.getCurrency(), pv);
  //  }

  /**
   * Computes the Federal Funds future price using average of forward rates (not convexity adjustment).
   * @param futures The futures.
   * @param multicurves The multi-curve provider.
   * @return The price.
   */
  public double price(final FederalFundsFutureSecurity futures, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(futures, "Futures");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final IndexON index = futures.getIndex();
    final int nbFixing = futures.getFixingPeriodAccrualFactor().length;
    final double[] rates = new double[nbFixing];
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      rates[loopfix] = multicurves.getForwardRate(index, futures.getFixingPeriodTime()[loopfix], futures.getFixingPeriodTime()[loopfix + 1], futures.getFixingPeriodAccrualFactor()[loopfix]);
    }
    double interest = futures.getAccruedInterest();
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      interest += rates[loopfix] * futures.getFixingPeriodAccrualFactor()[loopfix];
    }
    return 1.0 - interest / futures.getFixingTotalAccrualFactor();
  }

  /**
   * Computes the interest rate sensitivity of future price.
   * @param futures The futures.
   * @param multicurves The multi-curve provider.
   * @return The curve sensitivity.
   */
  public MulticurveSensitivity priceCurveSensitivity(final FederalFundsFutureSecurity futures, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(futures, "Futures");
    ArgumentChecker.notNull(multicurves, "Multi-curves provider");
    final IndexON index = futures.getIndex();
    final int nbFixing = futures.getFixingPeriodAccrualFactor().length;
    final double[] rates = new double[nbFixing];
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      rates[loopfix] = multicurves.getForwardRate(index, futures.getFixingPeriodTime()[loopfix], futures.getFixingPeriodTime()[loopfix + 1], futures.getFixingPeriodAccrualFactor()[loopfix]);
    }
    // Backward sweep
    final double priceBar = 1.0;
    final double interestBar = -1.0 / futures.getFixingTotalAccrualFactor() * priceBar;
    final double[] ratesBar = new double[nbFixing];
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      ratesBar[loopfix] = futures.getFixingPeriodAccrualFactor()[loopfix] * interestBar;
    }
    final Map<String, List<ForwardSensitivity>> resultMap = new HashMap<>();
    final List<ForwardSensitivity> listON = new ArrayList<>();
    for (int loopfix = 0; loopfix < nbFixing; loopfix++) {
      listON.add(new ForwardSensitivity(futures.getFixingPeriodTime()[loopfix], futures.getFixingPeriodTime()[loopfix + 1], futures.getFixingPeriodAccrualFactor()[loopfix], ratesBar[loopfix]));
    }
    resultMap.put(multicurves.getName(index), listON);
    return MulticurveSensitivity.ofForward(resultMap);
  }

}
