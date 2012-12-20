/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
public abstract class FXForwardMultiValuedFunction extends FXForwardFunction {

  public FXForwardMultiValuedFunction(final String valueRequirementName) {
    super(valueRequirementName);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    String payCurveName = null;
    String payCurveCalculationConfig = null;
    String receiveCurveName = null;
    String receiveCurveCalculationConfig = null;
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueRequirement requirement = entry.getValue();
      if (requirement.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        final ValueProperties constraints = requirement.getConstraints();
        if (constraints.getProperties().contains(ValuePropertyNames.PAY_CURVE)) {
          payCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          payCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
        } else if (constraints.getProperties().contains(ValuePropertyNames.RECEIVE_CURVE)) {
          receiveCurveName = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE));
          receiveCurveCalculationConfig = Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
        }
      }
    }
    assert payCurveName != null;
    assert receiveCurveName != null;
    final ValueProperties properties = getResultProperties(target, payCurveName, receiveCurveName, payCurveCalculationConfig, receiveCurveCalculationConfig).get();
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(PAY_CURVE_CALC_CONFIG)
        .withAny(RECEIVE_CURVE_CALC_CONFIG);
  }

  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String payCurveName, final String receiveCurveName,
      final String payCurveCalculationConfig, final String receiveCurveCalculationConfig) {
    return createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(PAY_CURVE_CALC_CONFIG, payCurveCalculationConfig)
        .with(RECEIVE_CURVE_CALC_CONFIG, receiveCurveCalculationConfig);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveCalculationConfig = desiredValue.getConstraint(PAY_CURVE_CALC_CONFIG);
    final String receiveCurveCalculationConfig = desiredValue.getConstraint(RECEIVE_CURVE_CALC_CONFIG);
    return createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(PAY_CURVE_CALC_CONFIG, payCurveCalculationConfig)
        .with(RECEIVE_CURVE_CALC_CONFIG, receiveCurveCalculationConfig);
  }
}
