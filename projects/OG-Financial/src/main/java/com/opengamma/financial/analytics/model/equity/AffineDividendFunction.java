/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class AffineDividendFunction extends AbstractFunction.NonCompiledInvoker {
  private static final double[] TAU = new double[] {0.25, 0.5, 0.75, 1, 2, 3, 4};
  private static final double[] ALPHA = new double[] {0.23, 0.24, 0.25, 0.26, 0, 0, 0};
  private static final double[] BETA = new double[] {0, 0, 0, 0, 0.15, 0.2, 0.3};

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final AffineDividends dividends = new AffineDividends(TAU, ALPHA, BETA);
    final ValueProperties properties = createValueProperties().get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.AFFINE_DIVIDENDS, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, dividends));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.PRIMITIVE) {
      return false;
    }
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.AFFINE_DIVIDENDS, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }


}
