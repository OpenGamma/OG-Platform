/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityHullWhiteProviderMethod;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderInterface;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class ParSpreadMarketQuoteHullWhiteCalculator extends AbstractInstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> {

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
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureSecurityHullWhiteProviderMethod METHOD_IRFUT_HW = InterestRateFutureSecurityHullWhiteProviderMethod.getInstance();
  /**
   * Composite calculator.
   */
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();

  @Override
  public Double visit(final InstrumentDerivative derivative, final HullWhiteOneFactorProviderInterface multicurves) {
    try {
      return derivative.accept(this, multicurves);
    } catch (Exception e) {
      return derivative.accept(PSMQDC, multicurves.getMulticurveProvider());
    }
  }

  //     -----     Futures     -----

  /**
   * For InterestRateFutures the ParSpread is the spread to be added to the reference price to obtain a present value of zero.
   * @param future The futures.
   * @param multicurves The multi-curves and Hull-White provider.
   * @return The par spread.
   */
  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final HullWhiteOneFactorProviderInterface multicurves) {
    return METHOD_IRFUT_HW.price(future, multicurves) - future.getReferencePrice();
  }

}
