/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackcap;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborBlackSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSmileCapProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculator of the present value as a multiple currency amount for Black smile cap/floor provider.
 */
public final class PresentValueCurveSensitivityBlackSmileCapCalculator extends InstrumentDerivativeVisitorAdapter<BlackSmileCapProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityBlackSmileCapCalculator INSTANCE = new PresentValueCurveSensitivityBlackSmileCapCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityBlackSmileCapCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityBlackSmileCapCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final CapFloorIborBlackSmileMethod METHOD_CAP = CapFloorIborBlackSmileMethod.getInstance();

  // -----     Payments     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitCapFloorIbor(final CapFloorIbor cap, final BlackSmileCapProviderInterface black) {
    return METHOD_CAP.presentValueCurveSensitivity(cap, black);
  }

}
