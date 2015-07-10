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
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculator of the present value curve sensitivity for multi-curve with normal swaption volatility.
 */
public final class PresentValueCurveSensitivityNormalSwaptionCalculator 
  extends InstrumentDerivativeVisitorAdapter<NormalSwaptionProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityNormalSwaptionCalculator INSTANCE = new PresentValueCurveSensitivityNormalSwaptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityNormalSwaptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityNormalSwaptionCalculator() {
  }

  /** Pricing method for physically-settled swaptions */
  private static final SwaptionPhysicalFixedIborNormalMethod METHOD_SWT_PHYS = 
      SwaptionPhysicalFixedIborNormalMethod.getInstance();

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, 
      final NormalSwaptionProviderInterface normal) {
    return METHOD_SWT_PHYS.presentValueCurveSensitivity(swaption, normal);
  }
  
}
