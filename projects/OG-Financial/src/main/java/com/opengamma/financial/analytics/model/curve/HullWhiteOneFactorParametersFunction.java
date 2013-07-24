/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.HULL_WHITE_ONE_FACTOR_PARAMETERS;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_HULL_WHITE_PARAMETERS;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Function that supplies Hull-White one factor parameters from a snapshot.
 */
public class HullWhiteOneFactorParametersFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = new HullWhiteOneFactorPiecewiseConstantParameters(0.02, new double[] {0.01}, new double[0]);
    final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy().get();
    final ValueSpecification spec = new ValueSpecification(HULL_WHITE_ONE_FACTOR_PARAMETERS, ComputationTargetSpecification.NULL, properties);
    return Collections.singleton(new ComputedValue(spec, parameters));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.NULL;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(PROPERTY_HULL_WHITE_PARAMETERS)
        .withAny(CURRENCY)
        .get();
    return Collections.singleton(new ValueSpecification(HULL_WHITE_ONE_FACTOR_PARAMETERS, ComputationTargetSpecification.NULL, properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> names = constraints.getValues(PROPERTY_HULL_WHITE_PARAMETERS);
    if (names == null || names.size() != 1) {
      return null;
    }
    final Set<String> currencies = constraints.getValues(CURRENCY);
    if (currencies == null || currencies.size() != 1) {
      return null;
    }
    return Collections.emptySet(); //TODO - just putting in dummy values for now
  }

}
