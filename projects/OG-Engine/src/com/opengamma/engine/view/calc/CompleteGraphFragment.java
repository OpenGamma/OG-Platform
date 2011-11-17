/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collection;
import java.util.concurrent.Future;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJobResult;

/**
 * Special case of a graph fragment for small graphs where all nodes end up in a single fragment.
 */
/* package */class CompleteGraphFragment extends GraphFragment<GraphFragmentContext, CompleteGraphFragment> {

  private final RootGraphFragmentFuture _future;

  public CompleteGraphFragment(final GraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics, final Collection<DependencyNode> nodes) {
    super(context, nodes);
    _future = new RootGraphFragmentFuture(this, statistics);
  }

  /**
   * Only gets called if this was the only node created because the dep graph was too small.
   */
  @Override
  public void resultReceived(final CalculationJobResult result) {
    super.resultReceived(result);
    _future.executed();
  }

  public Future<DependencyGraph> getFuture() {
    return _future;
  }

}
