/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.Collections;
import java.util.Set;

/**
 * Calculates the value vega of an equity index future option using the Black vega.
 */
public class EquityFutureOptionBlackValueVegaFunction extends EquityFutureOptionBlackFunction {
  /** Value vega calculator */
  private static final ValueVegaCalculator CALCULATOR = ValueVegaCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_VEGA}
   */
  public EquityFutureOptionBlackValueVegaFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final Object vegaObject = inputs.getValue(ValueRequirementNames.VEGA);
    if (vegaObject == null) {
      throw new OpenGammaRuntimeException("Could not get vega");
    }
    final double vega = (Double) vegaObject;
    final double valueVega = CALCULATOR.valueGreek(derivative, market, vega);
    return Collections.singleton(new ComputedValue(resultSpec, valueVega));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final ValueProperties properties = desiredValue.getConstraints().copy()
        .withoutAny(ValuePropertyNames.CURRENCY)
        .get();
    requirements.add(new ValueRequirement(ValueRequirementNames.VEGA, target.toSpecification(), properties));
    return requirements;
  }

}
