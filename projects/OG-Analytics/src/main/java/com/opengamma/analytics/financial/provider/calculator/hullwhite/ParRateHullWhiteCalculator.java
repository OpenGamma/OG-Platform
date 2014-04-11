/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class ParRateHullWhiteCalculator extends InstrumentDerivativeVisitorDelegate<HullWhiteOneFactorProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParRateHullWhiteCalculator INSTANCE = new ParRateHullWhiteCalculator();

  /**
   * Constructor.
   */
  private ParRateHullWhiteCalculator() {
    super(new HullWhiteProviderAdapter<>(ParRateDiscountingCalculator.getInstance()));
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParRateHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_STIRFUT = InterestRateFutureSecurityHullWhiteMethod.getInstance();

  //     -----     Futures     -----

  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_STIRFUT.parRate(futures.getUnderlyingSecurity(), hullWhite);
  }

  @Override
  public Double visitInterestRateFutureSecurity(final InterestRateFutureSecurity futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_STIRFUT.parRate(futures, hullWhite);
  }

}
