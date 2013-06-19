/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import com.opengamma.engine.exec.plan.GraphExecutionPlanner;
import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link DependencyGraphExecutorFactory} based on a {@link GraphExecutionPlanner} service.
 */
public class PlanBasedGraphExecutorFactory implements DependencyGraphExecutorFactory {

  private GraphExecutionPlanner _planner;

  public PlanBasedGraphExecutorFactory(final GraphExecutionPlanner planner) {
    setPlanner(planner);
  }

  protected void setPlanner(final GraphExecutionPlanner planner) {
    ArgumentChecker.notNull(planner, "planner");
    _planner = planner;
  }

  protected GraphExecutionPlanner getPlanner() {
    return _planner;
  }

  // DependencyGraphExecutorFactory

  @Override
  public DependencyGraphExecutor createExecutor(SingleComputationCycle cycle) {
    return new PlanBasedGraphExecutor(getPlanner(), cycle);
  }

}
