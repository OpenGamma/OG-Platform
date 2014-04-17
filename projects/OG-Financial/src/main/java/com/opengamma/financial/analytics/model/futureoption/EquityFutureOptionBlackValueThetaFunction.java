/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.Collections;
import java.util.Set;

/**
 * Calculates the value theta of an equity index future option using the Black theta.
 */
public class EquityFutureOptionBlackValueThetaFunction extends EquityFutureOptionBlackFunction {
  /** Value theta calculator */
  private static final ValueThetaCalculator CALCULATOR = ValueThetaCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_THETA}
   */
  public EquityFutureOptionBlackValueThetaFunction() {
    super(ValueRequirementNames.VALUE_THETA);
  }

  @Override
  protected Set<ComputedValue> computeValues(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ComputationTargetSpecification targetSpec, final ValueProperties resultProperties) {
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    final Object thetaObject = inputs.getValue(ValueRequirementNames.THETA);
    if (thetaObject == null) {
      throw new OpenGammaRuntimeException("Could not get theta");
    }
    final double theta = (Double) thetaObject;
    final double valueTheta = CALCULATOR.valueGreek(derivative, market, theta);
    return Collections.singleton(new ComputedValue(resultSpec, valueTheta));
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
    requirements.add(new ValueRequirement(ValueRequirementNames.THETA, target.toSpecification(), properties));
    return requirements;
  }

}
