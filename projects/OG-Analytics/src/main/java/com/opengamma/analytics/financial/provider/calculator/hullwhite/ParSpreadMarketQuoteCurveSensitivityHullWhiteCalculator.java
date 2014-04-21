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
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator extends InstrumentDerivativeVisitorDelegate<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator INSTANCE = new ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteCurveSensitivityHullWhiteCalculator() {
    super(new HullWhiteProviderAdapter<>(ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance()));
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteMethod METHOD_IRFUT_HW = InterestRateFutureSecurityHullWhiteMethod.getInstance();
  private static final SwapFuturesPriceDeliverableSecurityHullWhiteMethod METHOD_SWAP_FUT = SwapFuturesPriceDeliverableSecurityHullWhiteMethod.getInstance();

  //     -----     Futures     -----

  /**
   * For InterestRateFutures the ParSpread is the spread to be added to the reference price to obtain a present value of zero.
   * @param futures The futures.
   * @param multicurves The multi-curves and Hull-White provider.
   * @return The par spread.
   */
  @Override
  public MulticurveSensitivity visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final HullWhiteOneFactorProviderInterface multicurves) {
    return METHOD_IRFUT_HW.priceCurveSensitivity(futures.getUnderlyingSecurity(), multicurves);
  }

  @Override
  public MulticurveSensitivity visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures, final HullWhiteOneFactorProviderInterface multicurves) {
    return METHOD_SWAP_FUT.priceCurveSensitivity(futures.getUnderlyingSecurity(), multicurves);
  }

}
