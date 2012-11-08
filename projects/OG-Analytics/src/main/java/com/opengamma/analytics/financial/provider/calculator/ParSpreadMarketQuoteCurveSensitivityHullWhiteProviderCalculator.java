/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteProviderMethod;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.MulticurveSensitivity;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class ParSpreadMarketQuoteCurveSensitivityHullWhiteProviderCalculator extends AbstractInstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteCurveSensitivityHullWhiteProviderCalculator INSTANCE = new ParSpreadMarketQuoteCurveSensitivityHullWhiteProviderCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteCurveSensitivityHullWhiteProviderCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteCurveSensitivityHullWhiteProviderCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteProviderMethod METHOD_IRFUT_HW = InterestRateFutureSecurityHullWhiteProviderMethod.getInstance();
  /**
   * Composite calculator.
   */
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingProviderCalculator PVDC = ParSpreadMarketQuoteCurveSensitivityDiscountingProviderCalculator.getInstance();

  @Override
  public MulticurveSensitivity visit(final InstrumentDerivative derivative, final HullWhiteOneFactorProviderInterface multicurves) {
    try {
      return derivative.accept(this, multicurves);
    } catch (Exception e) {
      return derivative.accept(PVDC, multicurves.getMulticurveProvider());
    }
  }

  //     -----     Futures     -----

  /**
   * For InterestRateFutures the ParSpread is the spread to be added to the reference price to obtain a present value of zero.
   * @param futures The futures.
   * @param multicurves The multi-curves and Hull-White provider.
   * @return The par spread.
   */
  @Override
  public MulticurveSensitivity visitInterestRateFuture(final InterestRateFuture futures, final HullWhiteOneFactorProviderInterface multicurves) {
    return METHOD_IRFUT_HW.priceCurveSensitivity(futures, multicurves).getSensitivity(futures.getCurrency());
  }

}
