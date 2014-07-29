/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabrswaption;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborSABRMethod;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;

/**
 * Interpolates, for interest rate instruments using SABR model, and returns the implied volatility required.
 */
public final class ImpliedVolatilitySABRSwaptionCalculator extends InstrumentDerivativeVisitorAdapter<SABRSwaptionProviderInterface, Double> {

  /**
   * The method unique instance.
   */
  private static final ImpliedVolatilitySABRSwaptionCalculator INSTANCE = new ImpliedVolatilitySABRSwaptionCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ImpliedVolatilitySABRSwaptionCalculator getInstance() {
    return INSTANCE;
  }
  
  /** Private Constructor */
  private ImpliedVolatilitySABRSwaptionCalculator() {
  }
  
  // The Pricing Methods
  /** The implied volatility calculator for physically-settled swaptions */
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWAPTION_PHYSICAL = SwaptionPhysicalFixedIborSABRMethod.getInstance();
  /** The implied volatility calculator for cash-settled swaptions */
  private static final SwaptionCashFixedIborSABRMethod METHOD_SWAPTION_CASH = SwaptionCashFixedIborSABRMethod.getInstance();  
  
  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface curves) {
    return METHOD_SWAPTION_PHYSICAL.impliedVolatility(swaption, curves);
  }


  
  @Override
  public Double visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final SABRSwaptionProviderInterface curves) {
    return METHOD_SWAPTION_CASH.impliedVolatility(swaption, curves);
  }
}
