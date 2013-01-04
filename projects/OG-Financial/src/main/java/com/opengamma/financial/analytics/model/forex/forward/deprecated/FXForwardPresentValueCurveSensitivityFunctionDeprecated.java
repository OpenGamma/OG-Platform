/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.calculator.PresentValueCurveSensitivityMCSCalculator;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.forward.FXForwardPresentValueCurveSensitivityFunction;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see FXForwardPresentValueCurveSensitivityFunction
 */
@Deprecated
public class FXForwardPresentValueCurveSensitivityFunctionDeprecated extends FXForwardMultiValuedFunctionDeprecated {
  private static final PresentValueCurveSensitivityMCSCalculator CALCULATOR = PresentValueCurveSensitivityMCSCalculator.getInstance();

  public FXForwardPresentValueCurveSensitivityFunctionDeprecated() {
    super(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ValueSpecification spec) {
    final MultipleCurrencyInterestRateCurveSensitivity result = fxForward.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }

}
