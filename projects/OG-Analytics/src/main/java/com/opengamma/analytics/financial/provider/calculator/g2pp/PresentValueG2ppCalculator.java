/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.g2pp;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborG2ppApproximationMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueG2ppCalculator extends InstrumentDerivativeVisitorAdapter<G2ppProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueG2ppCalculator INSTANCE = new PresentValueG2ppCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueG2ppCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueG2ppCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final SwaptionPhysicalFixedIborG2ppApproximationMethod METHOD_SWT_PHYS = SwaptionPhysicalFixedIborG2ppApproximationMethod.getInstance();

  // -----     Swaption     ------

  @Override
  public MultipleCurrencyAmount visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final G2ppProviderInterface g2) {
    return METHOD_SWT_PHYS.presentValue(swaption, g2);
  }

}
