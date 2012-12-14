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
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;

/**
 * Calculate the market quote of instruments dependent of a Hull-White one factor provider.
 */
public final class MarketQuoteHullWhiteCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<HullWhiteOneFactorProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final MarketQuoteHullWhiteCalculator INSTANCE = new MarketQuoteHullWhiteCalculator();

  /**
   * Constructor.
   */
  private MarketQuoteHullWhiteCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static MarketQuoteHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final DeliverableSwapFuturesSecurityHullWhiteMethod METHOD_SWAP_FUT = DeliverableSwapFuturesSecurityHullWhiteMethod.getInstance();
  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_IR_FUT = InterestRateFutureSecurityHullWhiteMethod.getInstance();

  @Override
  public Double visit(final InstrumentDerivative derivative, final HullWhiteOneFactorProviderInterface multicurves) {
    return derivative.accept(this, multicurves);
  }

  //     -----     Futures     -----

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_IR_FUT.price(futures, hullWhite);
  }

  @Override
  public Double visitDeliverableSwapFuturesSecurity(final DeliverableSwapFuturesSecurity futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWAP_FUT.price(futures, hullWhite);
  }

  @Override
  public Double visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
