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
 * Calculates the present value of the funding leg of an equity total return swap.
 */
public final class EqyTrsFundingLegPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<EquityTrsDataBundle, MultipleCurrencyAmount> {
  /** The singleton instance */
  private static final EqyTrsFundingLegPresentValueCalculator INSTANCE = new EqyTrsFundingLegPresentValueCalculator();

  /**
   * Gets the instance.
   * @return The instance
   */
  public static EqyTrsFundingLegPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private EqyTrsFundingLegPresentValueCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final EquityTotalReturnSwapDiscountingMethod METHOD_TRS = EquityTotalReturnSwapDiscountingMethod.getInstance();

  //     -----     TRS     -----

  @Override
  public MultipleCurrencyAmount visitEquityTotalReturnSwap(final EquityTotalReturnSwap trs, final EquityTrsDataBundle multicurve) {
    return METHOD_TRS.presentValueFundingLeg(trs, multicurve);
  }

}
