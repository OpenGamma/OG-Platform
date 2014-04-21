/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborBlackMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;


/**
 * Interpolates, for interest rate instruments using Black model, and returns the implied volatility required.
 */
public final class SwaptionBlackImpliedVolatilityCalculator extends InstrumentDerivativeVisitorAdapter<BlackSwaptionFlatProviderInterface, Double> {

  /**
   * The method unique instance.
   */
  private static final SwaptionBlackImpliedVolatilityCalculator INSTANCE = new SwaptionBlackImpliedVolatilityCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static SwaptionBlackImpliedVolatilityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private SwaptionBlackImpliedVolatilityCalculator() {
  }

  /** The implied volatility calculator for physically-settled swaptions */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWAPTION_PHYSICAL = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  /** The implied volatility calculator for cash-settled swaptions */
  private static final SwaptionCashFixedIborBlackMethod METHOD_SWAPTION_CASH = SwaptionCashFixedIborBlackMethod.getInstance();

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final BlackSwaptionFlatProviderInterface curves) {
    return METHOD_SWAPTION_PHYSICAL.impliedVolatility(swaption, curves);
  }

  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final BlackSwaptionFlatProviderInterface curves) {
    return METHOD_SWAPTION_CASH.impliedVolatility(swaption, curves);
  }

}
