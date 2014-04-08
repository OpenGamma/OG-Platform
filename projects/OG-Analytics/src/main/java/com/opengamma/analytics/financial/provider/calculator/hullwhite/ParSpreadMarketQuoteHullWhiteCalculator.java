/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.SwapFuturesPriceDeliverableSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class ParSpreadMarketQuoteHullWhiteCalculator extends InstrumentDerivativeVisitorDelegate<HullWhiteOneFactorProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteHullWhiteCalculator INSTANCE = new ParSpreadMarketQuoteHullWhiteCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteHullWhiteCalculator() {
    super(new HullWhiteProviderAdapter<>(ParSpreadMarketQuoteDiscountingCalculator.getInstance()));
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_STIR_FUT = InterestRateFutureSecurityHullWhiteMethod.getInstance();
  private static final SwapFuturesPriceDeliverableSecurityHullWhiteMethod METHOD_SWAP_FUT = SwapFuturesPriceDeliverableSecurityHullWhiteMethod.getInstance();

  //     -----     Futures     -----

  /**
   * For InterestRateFutures the ParSpread is the spread to be added to the reference price to obtain a present value of zero.
   * @param future The futures.
   * @param multicurves The multi-curves and Hull-White provider.
   * @return The par spread.
   */
  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final HullWhiteOneFactorProviderInterface multicurves) {
    return METHOD_STIR_FUT.price(future.getUnderlyingFuture(), multicurves) - future.getReferencePrice();
  }

  @Override
  public Double visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures, final HullWhiteOneFactorProviderInterface multicurves) {
    return METHOD_SWAP_FUT.price(futures.getUnderlyingFuture(), multicurves) - futures.getReferencePrice();
  }

}
