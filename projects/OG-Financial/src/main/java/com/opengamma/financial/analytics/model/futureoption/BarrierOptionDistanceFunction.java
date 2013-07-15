/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction.NonCompiledInvoker;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Function to compute barrier distance for equity options
 *
 * Defined as absolute difference (optionally expressed as a percentage) between barrier level and market price
 */
public abstract class BarrierOptionDistanceFunction extends NonCompiledInvoker {
  /** absolute difference */
  public static final String BARRIER_ABS = "abs";
  /** percentage difference from barrier level */
  public static final String BARRIER_PERCENTAGE = "percentage";

  /**
   *
   */
  public BarrierOptionDistanceFunction() {
    super();
  }

  protected abstract double getBarrierLevel(final FinancialSecurity security);

  protected abstract ValueRequirement getMarketDataRequirement(final FinancialSecurity security);

  protected abstract Double getSpot(FunctionInputs inputs);

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.BARRIER_DISTANCE_OUTPUT_FORMAT)
        .get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.BARRIER_DISTANCE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();

    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();

    final Set<String> outputFormat = constraints.getValues(ValuePropertyNames.BARRIER_DISTANCE_OUTPUT_FORMAT);
    if (outputFormat == null || outputFormat.size() != 1) {
      return null;
    }

    final ValueRequirement marketRequirement = getMarketDataRequirement(security);
    return Collections.singleton(marketRequirement);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();

    // Do we care whether the option has expired?
    final Double spot = getSpot(inputs);

    final String outputFormat = desiredValue.getConstraint(ValuePropertyNames.BARRIER_DISTANCE_OUTPUT_FORMAT);

    Double distance;
    final double barrier = getBarrierLevel(security);
    switch (outputFormat) {
      case BARRIER_ABS:
        distance = Double.valueOf(Math.abs(barrier - spot.doubleValue()));
        break;
      case BARRIER_PERCENTAGE:
        distance = Double.valueOf(100 * Math.abs(barrier - spot.doubleValue()) / barrier);
        break;
      default:
        throw new OpenGammaRuntimeException("Unknown barrier output display type " + outputFormat);
    }

    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.BARRIER_DISTANCE, target.toSpecification(), desiredValue.getConstraints().copy().get());
    return Collections.singleton(new ComputedValue(resultSpec, distance));
  }

}
