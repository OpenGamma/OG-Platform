/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Iterator;

import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Triple;

/* package */final class ExistingProductionStep extends FunctionApplicationStep {

  public ExistingProductionStep(final ResolveTask task, final Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> nextFunctions,
      final Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> resolved, final ValueSpecification resolvedOutput) {
    super(task, nextFunctions, resolved, resolvedOutput);
  }

  @Override
  protected void pump(final GraphBuildingContext context) {
    setRunnableTaskState(new FunctionApplicationStep(getTask(), getFunctions(), getResolved(), getResolvedOutput()), context);
  }

  @Override
  public String toString() {
    return "EXISTING_PRODUCTION" + getObjectId();
  }

}
