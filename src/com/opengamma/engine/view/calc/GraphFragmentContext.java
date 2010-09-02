/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.view.calcnode.CalculationJobItem;

/* package */class GraphFragmentContext {

  private final AtomicInteger _graphFragmentIdentifiers = new AtomicInteger();
  private final AtomicLong _executionTime = new AtomicLong();
  private final MultipleNodeExecutor _executor;
  private final DependencyGraph _graph;
  private final Map<CalculationJobItem, DependencyNode> _item2node;

  public GraphFragmentContext(final MultipleNodeExecutor executor, final DependencyGraph graph) {
    _executor = executor;
    _graph = graph;
    _item2node = new HashMap<CalculationJobItem, DependencyNode> ((graph.getSize () * 4) / 3);
  }

  public MultipleNodeExecutor getExecutor() {
    return _executor;
  }

  public DependencyGraph getGraph() {
    return _graph;
  }

  public int nextIdentifier() {
    return _graphFragmentIdentifiers.incrementAndGet();
  }

  public void addExecutionTime(final long duration) {
    _executionTime.addAndGet(duration);
  }

  public long getExecutionTime() {
    return _executionTime.get();
  }
  
  public Map<CalculationJobItem, DependencyNode> getItem2Node () {
    return _item2node;
  }

}
