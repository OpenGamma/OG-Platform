/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.forward.ForexForwardPresentValueCurveSensitivityFunction;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see ForexForwardPresentValueCurveSensitivityFunction
 */
@Deprecated
public class ForexForwardPresentValueCurveSensitivityFunctionDeprecated extends ForexForwardMultiValuedFunctionDeprecated {
  private static final PresentValueCurveSensitivityForexCalculator CALCULATOR = PresentValueCurveSensitivityForexCalculator.getInstance();

  public ForexForwardPresentValueCurveSensitivityFunctionDeprecated() {
    super(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ValueSpecification spec) {
    final MultipleCurrencyInterestRateCurveSensitivity result = CALCULATOR.visit(fxForward, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }


}
