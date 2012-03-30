/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class ForexForwardPresentValueCurveSensitivityFunction extends ForexForwardFunction {
  private static final PresentValueCurveSensitivityForexCalculator CALCULATOR = PresentValueCurveSensitivityForexCalculator.getInstance();

  public ForexForwardPresentValueCurveSensitivityFunction() {
    super(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ValueSpecification spec) {
    final MultipleCurrencyInterestRateCurveSensitivity result = CALCULATOR.visit(fxForward, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }


}
