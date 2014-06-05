/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.trs;

import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.provider.calculator.equity.PresentValueEquityDiscountingCalculator;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the currency exposure by discounting with issuer specific curves.
 */
public final class EqyTrsCurrencyExposureCalculator extends InstrumentDerivativeVisitorDelegate<EquityTrsDataBundle, MultipleCurrencyAmount> {

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
    super(PresentValueEquityDiscountingCalculator.getInstance());
  }

}
