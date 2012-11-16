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
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueSABRSwaptionCalculator extends AbstractInstrumentDerivativeVisitor<SABRSwaptionProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSABRSwaptionCalculator INSTANCE = new PresentValueSABRSwaptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueSABRSwaptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueSABRSwaptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWT_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative, final SABRSwaptionProviderInterface sabr) {
    return derivative.accept(this, sabr);
  }

  @Override
  public MultipleCurrencyAmount visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final SABRSwaptionProviderInterface sabr) {
    return METHOD_SWT_SABR.presentValue(swaption, sabr);
  }

}
