/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityBlackSmileForexCalculator;
import com.opengamma.analytics.financial.forex.calculator.PresentValueCurveSensitivityBlackTermStructureForexCalculator;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackForexTermStructureBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the curve sensitivities of FX options using the Black method.
 * @deprecated The parent is deprecated
 */
@Deprecated
public class FXOptionBlackPresentValueCurveSensitivityFunction extends FXOptionBlackMultiValuedFunction {
  private static final PresentValueCurveSensitivityBlackSmileForexCalculator SMILE_CALCULATOR = PresentValueCurveSensitivityBlackSmileForexCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackTermStructureForexCalculator FLAT_CALCULATOR = PresentValueCurveSensitivityBlackTermStructureForexCalculator.getInstance();

  public FXOptionBlackPresentValueCurveSensitivityFunction() {
    super(ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof YieldCurveWithBlackForexTermStructureBundle) {
      final MultipleCurrencyInterestRateCurveSensitivity result = forex.accept(FLAT_CALCULATOR, data);
      ArgumentChecker.isTrue(result.getCurrencies().size() == 1, "Only one currency");
      return Collections.singleton(new ComputedValue(spec, result));
    }
    final MultipleCurrencyInterestRateCurveSensitivity result = forex.accept(SMILE_CALCULATOR, data);
    ArgumentChecker.isTrue(result.getCurrencies().size() == 1, "Only one currency");
    return Collections.singleton(new ComputedValue(spec, result));
  }

}
