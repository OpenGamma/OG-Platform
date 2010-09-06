/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collection;
import java.util.concurrent.FutureTask;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJobResult;

/* package */class RootGraphFragment extends GraphFragment {

  private static final Runnable NO_OP = new Runnable() {
    @Override
    public void run() {
    }
  };

  private final FutureTask<Object> _future = new FutureTask<Object>(NO_OP, null);
  private final GraphExecutorStatisticsGatherer _statistics;
  private final long _jobStarted = System.nanoTime();

  public RootGraphFragment(final GraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics) {
    super(context);
    _statistics = statistics;
  }

  public RootGraphFragment(final GraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics, final Collection<DependencyNode> nodes) {
    super(context, nodes);
    _statistics = statistics;
  }

  @Override
  public void execute() {
    _future.run();
    _statistics.graphExecuted(getContext().getGraph().getCalcConfName(), getContext().getGraph().getSize(), getContext().getExecutionTime(), System.nanoTime() - _jobStarted);
  }

  /**
   * Only gets called if this was the only node created because the dep graph was
   * too small.
   */
  @Override
  public void resultReceived(final CalculationJobResult result) {
    super.resultReceived(result);
    execute();
  }

  public FutureTask<Object> getFuture() {
    return _future;
  }

}
