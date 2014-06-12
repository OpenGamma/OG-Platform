/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.equity;

import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.equity.trs.definition.EquityTotalReturnSwap;
import com.opengamma.analytics.financial.equity.trs.method.EquityTotalReturnSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculator of the present value as a multiple currency amount using cash-flow discounting and forward estimation.
 */
public final class PresentValueCurveSensitivityEquityDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<EquityTrsDataBundle, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityEquityDiscountingCalculator INSTANCE = new PresentValueCurveSensitivityEquityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityEquityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityEquityDiscountingCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final EquityTotalReturnSwapDiscountingMethod METHOD_TRS = EquityTotalReturnSwapDiscountingMethod.getInstance();

  //     -----     TRS     -----

  @Override
  public MultipleCurrencyMulticurveSensitivity visitEquityTotalReturnSwap(final EquityTotalReturnSwap trs, final EquityTrsDataBundle multicurve) {
    return METHOD_TRS.presentValueCurveSensitivity(trs, multicurve);
  }

}
