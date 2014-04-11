/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.SwapFuturesPriceDeliverableSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.MarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;

/**
 * Calculate the market quote of instruments dependent of a Hull-White one factor provider.
 */
public class MarketQuoteHullWhiteCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<HullWhiteOneFactorProviderInterface, Double> {

  /**
   * An instance of the calculator.
   */
  private static final MarketQuoteHullWhiteCalculator INSTANCE = new MarketQuoteHullWhiteCalculator();

  /**
   * Constructor.
   */
  protected MarketQuoteHullWhiteCalculator() {
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
  private static final SwapFuturesPriceDeliverableSecurityHullWhiteMethod METHOD_SWAP_FUT = SwapFuturesPriceDeliverableSecurityHullWhiteMethod.getInstance();
  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_STIRFUT = InterestRateFutureSecurityHullWhiteMethod.getInstance();
  private static final InterestRateFutureOptionMarginSecurityHullWhiteMethod METHOD_OPT_STIRFUT_MARG = InterestRateFutureOptionMarginSecurityHullWhiteMethod.getInstance();

  @Override
  public Double visit(final InstrumentDerivative derivative, final HullWhiteOneFactorProviderInterface multicurves) {
    return derivative.accept(MarketQuoteDiscountingCalculator.getInstance(), multicurves.getMulticurveProvider());
  }

  //     -----     Futures     -----

  @Override
  public Double visitInterestRateFutureSecurity(final InterestRateFutureSecurity futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_STIRFUT.price(futures, hullWhite);
  }

  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_STIRFUT.price(futures.getUnderlyingSecurity(), hullWhite);
  }

  @Override
  public Double visitSwapFuturesPriceDeliverableSecurity(final SwapFuturesPriceDeliverableSecurity futures, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_SWAP_FUT.price(futures, hullWhite);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity option, final HullWhiteOneFactorProviderInterface hullWhite) {
    return METHOD_OPT_STIRFUT_MARG.price(option, hullWhite);
  }

  @Override
  public Double visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
