/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackPresentValueCurveSensitivityFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated See the version that does not refer to funding or forward curves
 * @see FXOptionBlackPresentValueCurveSensitivityFunction
 */
@Deprecated
public class FXOptionBlackPresentValueCurveSensitivityFunctionDeprecated extends FXOptionBlackSingleValuedFunctionDeprecated {
  private static final PresentValueCurveSensitivityBlackSmileForexCalculator CALCULATOR = PresentValueCurveSensitivityBlackSmileForexCalculator.getInstance();

  public FXOptionBlackPresentValueCurveSensitivityFunctionDeprecated() {
    super(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final MultipleCurrencyInterestRateCurveSensitivity result = fxOption.accept(CALCULATOR, data);
    ArgumentChecker.isTrue(result.getCurrencies().size() == 1, "Only one currency");
    return Collections.singleton(new ComputedValue(spec, result));
  }

}
