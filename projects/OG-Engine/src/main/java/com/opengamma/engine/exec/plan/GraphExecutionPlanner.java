/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import java.util.Map;
import java.util.Set;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;

/**
 * Produces a {@link GraphExecutionPlan} for a {@link DependencyGraph}.
 */
public interface GraphExecutionPlanner {

  GraphExecutionPlan createPlan(DependencyGraph graph, ExecutionLogModeSource logModeSource, long functionInitialisationId, Set<ValueSpecification> sharedValues,
      Map<ValueSpecification, FunctionParameters> parameters);

}
