/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFuture;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;

/**
 * Calculate the market quote of instruments dependent of a Hull-White one factor provider.
 */
public final class MarketQuoteDiscountingCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<MulticurveProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final MarketQuoteDiscountingCalculator INSTANCE = new MarketQuoteDiscountingCalculator();

  /**
   * Constructor.
   */
  private MarketQuoteDiscountingCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static MarketQuoteDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureDiscountingMethod METHOD_IR_FUT = InterestRateFutureDiscountingMethod.getInstance();

  @Override
  public Double visit(final InstrumentDerivative derivative, final MulticurveProviderInterface multicurves) {
    return derivative.accept(this, multicurves);
  }

  //     -----     Futures     -----

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture futures, final MulticurveProviderInterface multicurves) {
    return METHOD_IR_FUT.price(futures, multicurves);
  }

  @Override
  public Double visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
