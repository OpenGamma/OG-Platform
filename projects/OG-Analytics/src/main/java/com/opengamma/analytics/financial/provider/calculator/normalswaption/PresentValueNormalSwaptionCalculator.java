/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.normalswaption;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborNormalMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSwaptionProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount for multi-curve with normal swaption volatility.
 */
public final class PresentValueNormalSwaptionCalculator 
  extends InstrumentDerivativeVisitorAdapter<NormalSwaptionProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueNormalSwaptionCalculator INSTANCE = new PresentValueNormalSwaptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueNormalSwaptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueNormalSwaptionCalculator() {
  }

  /** Pricing method for physically-settled swaptions */
  private static final SwaptionPhysicalFixedIborNormalMethod METHOD_SWT_PHYS = 
      SwaptionPhysicalFixedIborNormalMethod.getInstance();

  @Override
  public MultipleCurrencyAmount visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, 
      final NormalSwaptionProviderInterface normal) {
    return METHOD_SWT_PHYS.presentValue(swaption, normal);
  }
  
}
