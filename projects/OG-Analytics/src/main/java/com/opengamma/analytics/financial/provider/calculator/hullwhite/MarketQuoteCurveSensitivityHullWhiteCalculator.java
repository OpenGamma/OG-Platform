/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.DeliverableSwapFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.provider.DeliverableSwapFuturesSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteProviderMethod;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Calculate the market quote of instruments dependent of a Hull-White one factor provider.
 */
public final class MarketQuoteCurveSensitivityHullWhiteCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final MarketQuoteCurveSensitivityHullWhiteCalculator INSTANCE = new MarketQuoteCurveSensitivityHullWhiteCalculator();

  /**
   * Constructor.
   */
  private MarketQuoteCurveSensitivityHullWhiteCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static MarketQuoteCurveSensitivityHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteProviderMethod METHOD_IR_FUT = InterestRateFutureSecurityHullWhiteProviderMethod.getInstance();
  private static final DeliverableSwapFuturesSecurityHullWhiteMethod METHOD_SWAP_FUT = DeliverableSwapFuturesSecurityHullWhiteMethod.getInstance();

  @Override
  public MulticurveSensitivity visit(final InstrumentDerivative derivative, final HullWhiteOneFactorProviderInterface multicurves) {
    return derivative.accept(this, multicurves);
  }

  //     -----     Futures     -----

  @Override
  public MulticurveSensitivity visitInterestRateFuture(final InterestRateFuture futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_IR_FUT.priceCurveSensitivity(futures, hullWhite);
  }

  @Override
  public MulticurveSensitivity visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWAP_FUT.priceCurveSensitivity(futures, hullWhite);
  }

  @Override
  public MulticurveSensitivity visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
