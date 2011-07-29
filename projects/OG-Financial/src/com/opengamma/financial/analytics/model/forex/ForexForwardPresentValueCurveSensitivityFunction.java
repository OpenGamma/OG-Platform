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
import com.opengamma.financial.forex.calculator.PresentValueCurveSensitivityForexCalculator;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.interestrate.YieldCurveBundle;

/**
 * 
 */
public class ForexForwardPresentValueCurveSensitivityFunction extends ForexForwardFunction {
  private static final PresentValueCurveSensitivityForexCalculator CALCULATOR = PresentValueCurveSensitivityForexCalculator.getInstance();

  public ForexForwardPresentValueCurveSensitivityFunction(final String payCurveName, final String receiveCurveName) {
    super(payCurveName, receiveCurveName, ValueRequirementNames.FX_CURVE_SENSITIVITIES);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPayCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getReceiveCurveName()).get();
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, CALCULATOR.visit(fxForward, data)));
  }
}
