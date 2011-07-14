/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.CurrencyLabelledMatrix1D;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueForexCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexForwardPresentValueFunction extends ForexForwardFunction {
  private static final PresentValueForexCalculator CALCULATOR = PresentValueForexCalculator.getInstance();

  public ForexForwardPresentValueFunction(final String payCurveName, final String receiveCurveName, final String valueRequirementName) {
    super(payCurveName, receiveCurveName, ValueRequirementNames.FX_PRESENT_VALUE);
  }

  @Override
  protected Object getResult(final ForexDerivative fxForward, final YieldCurveBundle data) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxForward);
    final int n = result.size();
    final Currency[] keys = new Currency[n];
    final double[] values = new double[n];
    int i = 0;
    for (final CurrencyAmount ca : result) {
      keys[i] = ca.getCurrency();
      values[i++] = ca.getAmount();
    }
    return new CurrencyLabelledMatrix1D(keys, values);
  }

}
