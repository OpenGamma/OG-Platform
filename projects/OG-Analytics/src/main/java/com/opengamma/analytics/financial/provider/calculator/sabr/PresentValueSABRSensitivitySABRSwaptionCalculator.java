/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabr;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.provider.description.SABRSwaptionProviderInterface;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueSABRSensitivitySABRSwaptionCalculator extends AbstractInstrumentDerivativeVisitor<SABRSwaptionProviderInterface, PresentValueSABRSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSABRSensitivitySABRSwaptionCalculator INSTANCE = new PresentValueSABRSensitivitySABRSwaptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueSABRSensitivitySABRSwaptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueSABRSensitivitySABRSwaptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWT_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();

  @Override
  public PresentValueSABRSensitivityDataBundle visit(final InstrumentDerivative derivative, final SABRSwaptionProviderInterface sabr) {
    return derivative.accept(this, sabr);
  }

  @Override
  public PresentValueSABRSensitivityDataBundle visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabr) {
    return METHOD_SWT_SABR.presentValueSABRSensitivity(swaption, sabr);
  }

}
