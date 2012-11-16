/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabr;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.provider.description.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueCurveSensitivitySABRSwaptionCalculator extends AbstractInstrumentDerivativeVisitor<SABRSwaptionProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivitySABRSwaptionCalculator INSTANCE = new PresentValueCurveSensitivitySABRSwaptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivitySABRSwaptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivitySABRSwaptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWT_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();

  @Override
  public MultipleCurrencyMulticurveSensitivity visit(final InstrumentDerivative derivative, final SABRSwaptionProviderInterface sabr) {
    return derivative.accept(this, sabr);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabr) {
    return METHOD_SWT_SABR.presentValueCurveSensitivity(swaption, sabr);
  }

}
