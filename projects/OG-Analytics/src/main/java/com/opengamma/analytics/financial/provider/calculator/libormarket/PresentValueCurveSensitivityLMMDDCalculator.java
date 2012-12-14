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
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueCurveSensitivityLMMDDCalculator extends
    InstrumentDerivativeVisitorAdapter<LiborMarketModelDisplacedDiffusionProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityLMMDDCalculator INSTANCE = new PresentValueCurveSensitivityLMMDDCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityLMMDDCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityLMMDDCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_SWT_PHYS = SwaptionPhysicalFixedIborLMMDDMethod.getInstance();

  // -----     Swaption     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final LiborMarketModelDisplacedDiffusionProviderInterface lmm) {
    return METHOD_SWT_PHYS.presentValueCurveSensitivity(swaption, lmm);
  }

}
