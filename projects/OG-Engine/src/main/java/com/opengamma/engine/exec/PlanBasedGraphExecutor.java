/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import java.util.Map;
import java.util.Set;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.exec.plan.GraphExecutionPlan;
import com.opengamma.engine.exec.plan.GraphExecutionPlanner;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link DependencyGraphExecutor} that is based on a {@link GraphExecutionPlanner} service.
 */
public class PlanBasedGraphExecutor implements DependencyGraphExecutor {

  private final GraphExecutionPlanner _planner;
  private final SingleComputationCycle _cycle;

  public PlanBasedGraphExecutor(final GraphExecutionPlanner planner, final SingleComputationCycle cycle) {
    ArgumentChecker.notNull(planner, "planner");
    ArgumentChecker.notNull(cycle, "cycle");
    _planner = planner;
    _cycle = cycle;
  }

  protected GraphExecutionPlanner getPlanner() {
    return _planner;
  }

  protected SingleComputationCycle getCycle() {
    return _cycle;
  }

  // DependencyGraphExecutor

  @Override
  public DependencyGraphExecutionFuture execute(final DependencyGraph graph, final Set<ValueSpecification> sharedValues, final Map<ValueSpecification, FunctionParameters> parameters) {
    final GraphExecutionPlan plan = getPlanner().createPlan(graph, getCycle().getViewProcessContext().getExecutionLogModeSource(), getCycle().getFunctionInitId(), sharedValues, parameters);
    final PlanExecutor executor = new PlanExecutor(getCycle(), plan);
    executor.start();
    return executor;
  }

}
