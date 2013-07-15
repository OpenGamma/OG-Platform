/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.libormarket;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborLMMDDMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueLMMDDCalculator extends InstrumentDerivativeVisitorAdapter<LiborMarketModelDisplacedDiffusionProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueLMMDDCalculator INSTANCE = new PresentValueLMMDDCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueLMMDDCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueLMMDDCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_SWT_PHYS = SwaptionPhysicalFixedIborLMMDDMethod.getInstance();

  // -----     Swaption     ------

  @Override
  public MultipleCurrencyAmount visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final LiborMarketModelDisplacedDiffusionProviderInterface lmm) {
    return METHOD_SWT_PHYS.presentValue(swaption, lmm);
  }

}
