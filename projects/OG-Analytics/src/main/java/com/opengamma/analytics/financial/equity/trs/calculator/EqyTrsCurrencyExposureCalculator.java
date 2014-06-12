/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.trs.calculator;

import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.equity.trs.definition.EquityTotalReturnSwap;
import com.opengamma.analytics.financial.equity.trs.method.EquityTotalReturnSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the currency exposure by discounting with issuer specific curves.
 */
public final class EqyTrsCurrencyExposureCalculator extends InstrumentDerivativeVisitorAdapter<EquityTrsDataBundle, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final EqyTrsCurrencyExposureCalculator INSTANCE = new EqyTrsCurrencyExposureCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static EqyTrsCurrencyExposureCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private EqyTrsCurrencyExposureCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final EquityTotalReturnSwapDiscountingMethod METHOD_TRS = EquityTotalReturnSwapDiscountingMethod.getInstance();

  //     -----     TRS     -----

  @Override
  public MultipleCurrencyAmount visitEquityTotalReturnSwap(final EquityTotalReturnSwap trs, final EquityTrsDataBundle multicurve) {
    return METHOD_TRS.currencyExposure(trs, multicurve);
  }
}
