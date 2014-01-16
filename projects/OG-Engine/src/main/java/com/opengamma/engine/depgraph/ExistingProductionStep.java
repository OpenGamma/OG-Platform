/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;

import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Triple;

/* package */final class ExistingProductionStep extends FunctionApplicationStep {

  /**
   * Creates a new instance.
   * <p>
   * The {@code resolvedOutput} parameter must be normalized.
   * 
   * @param task the resolve task this step is part of, not null
   * @param base the superclass data, not null
   * @param resolved the resolved function information, not null
   * @param resolvedOutput the provisional resolved value specification, not null
   */
  public ExistingProductionStep(final ResolveTask task, final FunctionIterationStep.IterationBaseStep base,
      final Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> resolved, final ValueSpecification resolvedOutput) {
    super(task, base, resolved, resolvedOutput);
  }

  @Override
  protected void pump(final GraphBuildingContext context) {
    setRunnableTaskState(new FunctionApplicationStep(getTask(), getIterationBase(), getResolved(), getResolvedOutput()), context);
  }

  @Override
  public String toString() {
    return "EXISTING_PRODUCTION" + getObjectId();
  }

}
