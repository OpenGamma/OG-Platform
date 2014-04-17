/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.provider.calculator.discounting.GammaPV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the gamma PV01 of an equity total return swap.
 */
public final class EqyTrsGammaPV01Calculator extends InstrumentDerivativeVisitorAdapter<EquityTrsDataBundle, Double> {
  /** The singleton instance */
  private static final EqyTrsGammaPV01Calculator INSTANCE = new EqyTrsGammaPV01Calculator();
  /** The gamma PV01 calculator */
  private static final GammaPV01CurveParametersCalculator<MulticurveProviderInterface> CALCULATOR =
      new GammaPV01CurveParametersCalculator<>(PresentValueCurveSensitivityDiscountingCalculator.getInstance());

  /**
   * Gets the instance.
   * @return The instance
   */
  public static EqyTrsGammaPV01Calculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private EqyTrsGammaPV01Calculator() {
  }

  @Override
  public Double visitEquityTotalReturnSwap(final EquityTotalReturnSwap equityTrs, final EquityTrsDataBundle data) {
    ArgumentChecker.notNull(equityTrs, "equityTrs");
    ArgumentChecker.notNull(data, "data");
    return equityTrs.getFundingLeg().accept(CALCULATOR, data.getCurves());
  }
}
