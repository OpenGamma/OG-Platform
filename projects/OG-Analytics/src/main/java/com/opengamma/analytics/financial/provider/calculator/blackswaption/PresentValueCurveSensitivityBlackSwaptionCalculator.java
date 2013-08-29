/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackswaption;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionCashFixedIborBlackMethod;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborBlackMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueCurveSensitivityBlackSwaptionCalculator extends InstrumentDerivativeVisitorAdapter<BlackSwaptionFlatProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityBlackSwaptionCalculator INSTANCE = new PresentValueCurveSensitivityBlackSwaptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityBlackSwaptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityBlackSwaptionCalculator() {
  }

  /**  Pricing method for physically-settled swaptions */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWT_PHYS = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  /**  Pricing method for cash-settled swaptions */
  private static final SwaptionCashFixedIborBlackMethod METHOD_SWT_CASH = SwaptionCashFixedIborBlackMethod.getInstance();

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final BlackSwaptionFlatProviderInterface black) {
    return METHOD_SWT_PHYS.presentValueCurveSensitivity(swaption, black);
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwaptionCashFixedIbor(final SwaptionCashFixedIbor swaption, final BlackSwaptionFlatProviderInterface black) {
    return METHOD_SWT_CASH.presentValueCurveSensitivity(swaption, black);
  }
}
