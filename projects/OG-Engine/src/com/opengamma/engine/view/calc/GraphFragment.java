/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;

/**
 * Base class of the graph fragments. A graph fragment is a subset of an executable dependency graph
 * that corresponds to a single computation job. Fragments are links to create a graph of fragments.
 * At the extreme, there could be a fragment for each node in the original graph and the graph of
 * fragments will be the same shape as the graph of nodes.
 */
/* package */class GraphFragment<C extends GraphFragmentContext, F extends GraphFragment<C, F>> {

  private final int _graphFragmentIdentifier;
  private final C _context;
  private final List<DependencyNode> _nodes;
  private final Set<F> _inputFragments = new HashSet<F>();
  private final Set<F> _outputFragments = new HashSet<F>();
  private CacheSelectHint _cacheSelectHint;
  private AtomicInteger _blockCount;
  private Collection<Long> _requiredJobs;
  private Collection<F> _tail;

  public GraphFragment(final C context) {
    _context = context;
    _graphFragmentIdentifier = context.nextIdentifier();
    _nodes = new LinkedList<DependencyNode>();
  }

  public GraphFragment(final C context, final DependencyNode node) {
    this(context);
    _nodes.add(node);
  }

  public GraphFragment(final C context, final Collection<DependencyNode> nodes) {
    _context = context;
    _graphFragmentIdentifier = context.nextIdentifier();
    _nodes = new ArrayList<DependencyNode>(nodes);
  }

  public C getContext() {
    return _context;
  }

  public List<DependencyNode> getNodes() {
    return _nodes;
  }

  public void initBlockCount() {
    _blockCount = new AtomicInteger(getInputFragments().size());
  }

  public void setTail(final Collection<F> fragment) {
    _tail = fragment;
  }

  public Collection<F> getTail() {
    return _tail;
  }

  public Set<F> getInputFragments() {
    return _inputFragments;
  }

  public Set<F> getOutputFragments() {
    return _outputFragments;
  }

  public void setCacheSelectHint(final CacheSelectHint cacheHint) {
    _cacheSelectHint = cacheHint;
  }

  public CacheSelectHint getCacheSelectHint() {
    return _cacheSelectHint;
  }

  public int getJobItems() {
    return _nodes.size();
  }

  public void inputCompleted() {
    // If _blockCount is null, we are a tail job that has already been dispatched
    if (_blockCount != null) {
      final int blockCount = _blockCount.decrementAndGet();
      if (blockCount == 0) {
        execute();
        _blockCount = null;
      }
    }
  }

  public CalculationJob createCalculationJob() {
    final CalculationJobSpecification jobSpec = getContext().getExecutor().createJobSpecification(getContext().getGraph());
    final List<CalculationJobItem> items = new ArrayList<CalculationJobItem>();
    for (DependencyNode node : getNodes()) {
      final Set<ValueSpecification> inputs = node.getInputValues();
      CalculationJobItem jobItem = new CalculationJobItem(node.getFunction().getFunction().getFunctionDefinition().getUniqueId(), node.getFunction().getParameters(), node
          .getComputationTarget().toSpecification(), inputs, node.getOutputRequirements());
      items.add(jobItem);
      getContext().registerJobItem(jobItem, node);
    }
    getContext().getExecutor().addJobToViewProcessorQuery(jobSpec, getContext().getGraph());
    final CalculationJob job = new CalculationJob(jobSpec, getFunctionInitializationTimestamp(), _requiredJobs, items, getCacheSelectHint());
    if (getTail() != null) {
      for (GraphFragment<C, F> tail : getTail()) {
        tail._blockCount = null;
        final int size = tail.getInputFragments().size();
        if (tail._requiredJobs == null) {
          if (size == 1) {
            tail._requiredJobs = Collections.singleton(jobSpec.getJobId());
          } else {
            tail._requiredJobs = new ArrayList<Long>(size);
            tail._requiredJobs.add(jobSpec.getJobId());
          }
        } else {
          tail._requiredJobs.add(jobSpec.getJobId());
        }
        if (tail._requiredJobs.size() == size) {
          final CalculationJob tailJob = tail.createCalculationJob();
          job.addTail(tailJob);
        }
      }
    }
    getContext().registerCallback(jobSpec, this);
    return job;
  }

  public void execute() {
    getContext().dispatchJob(createCalculationJob());
  }

  @Override
  public String toString() {
    return getIdentifier() + ": " + getJobItems() + " dep. node(s)";
  }

  public void resultReceived(final CalculationJobResult result) {
    // Release tree fragments up the tree
    getContext().addExecutionTime(result.getDuration());
    for (GraphFragment<C, F> dependent : getOutputFragments()) {
      dependent.inputCompleted();
    }
  }

  public long getFunctionInitializationTimestamp() {
    return getContext().getFunctionInitId();
  }

  /**
   * Returns an identifier that is unique within the fragments associated with a given context.
   * 
   * @return the identifier
   */
  public int getIdentifier() {
    return _graphFragmentIdentifier;
  }

  @SuppressWarnings("unchecked")
  public static class Root extends GraphFragment {

    private final RootGraphFragmentFuture _future;

    public Root(final GraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics) {
      super(context);
      _future = new RootGraphFragmentFuture(this, statistics);
    }

    @Override
    public void execute() {
      _future.executed();
    }

    public Future<DependencyGraph> getFuture() {
      return _future;
    }

  }

}
