/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueCurveSensitivityBlackForexCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;

/**
 * 
 */
public class ForexVanillaOptionPresentValueCurveSensitivityFunction extends ForexVanillaOptionFunction {
  private static final PresentValueCurveSensitivityBlackForexCalculator CALCULATOR = PresentValueCurveSensitivityBlackForexCalculator.getInstance();

  public ForexVanillaOptionPresentValueCurveSensitivityFunction(final String putCurveName, final String callCurveName, final String surfaceName) {
    super(putCurveName, callCurveName, surfaceName, ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final ForexDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPutCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getCallCurveName())
        .with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties);
    final PresentValueSensitivity result = CALCULATOR.visit(fxOption, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }
}
