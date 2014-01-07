/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve;

import static com.opengamma.engine.value.ValueRequirementNames.HULL_WHITE_ONE_FACTOR_PARAMETERS;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_HULL_WHITE_CURRENCY;
import static com.opengamma.financial.analytics.model.curve.CurveCalculationPropertyNamesAndValues.PROPERTY_HULL_WHITE_PARAMETERS;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.util.money.Currency;

/**
 * Function that supplies hard-coded Hull-White one factor parameters. Used for testing only.
 */
public class HardCodedHullWhiteOneFactorParametersFunction extends AbstractFunction.NonCompiledInvoker {
  /** The name of this configuration */
  private static final String CONFIG_NAME = "Test";
  /** Hard-coded Hull-White one factor parameters */
  private static final HullWhiteOneFactorPiecewiseConstantParameters CONSTANT_PARAMETERS =
      new HullWhiteOneFactorPiecewiseConstantParameters(0.02, new double[] {0.01}, new double[0]);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy().get();
    final ValueSpecification spec = new ValueSpecification(HULL_WHITE_ONE_FACTOR_PARAMETERS, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, CONSTANT_PARAMETERS));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.CURRENCY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = ((Currency) target.getValue()).getCode();
    final ValueProperties properties = createValueProperties()
        .with(PROPERTY_HULL_WHITE_PARAMETERS, CONFIG_NAME)
        .with(PROPERTY_HULL_WHITE_CURRENCY, currency)
        .get();
    return Collections.singleton(new ValueSpecification(HULL_WHITE_ONE_FACTOR_PARAMETERS, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

}
