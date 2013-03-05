/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Special case of function implementation that is never executed by the graph executor but is used to source market data. It will not be considered directly during graph construction; the singleton
 * instance is associated with DependencyNode objects to act as a marker on the node.
 */
public final class MarketDataSourcingFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Singleton instance.
   */
  public static final MarketDataSourcingFunction INSTANCE = new MarketDataSourcingFunction();

  /**
   * Function unique ID
   */
  public static final String UNIQUE_ID = "MarketDataSourcingFunction";

  private MarketDataSourcingFunction() {
    setUniqueId(UNIQUE_ID);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    return null;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.emptySet();
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.ANYTHING;
  }

}
