/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
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
 * Base class of the graph fragments. A graph fragment is a subset of an executable dependency graph that corresponds to a single computation job. Fragments are linked to create a graph of fragments.
 * At the extreme, there could be a fragment for each node in the original graph and the graph of fragments will be the same shape as the graph of nodes.
 */
/* package */class GraphFragment<F extends GraphFragment<F>> {

  private final int _graphFragmentIdentifier;
  private final List<DependencyNode> _nodes;
  private final Set<F> _inputFragments = new HashSet<F>();
  private final Set<F> _outputFragments = new HashSet<F>();
  private CacheSelectHint _cacheSelectHint;
  private AtomicInteger _blockCount;
  private long[] _requiredJobs;
  private int _requiredJobIndex;
  private Collection<F> _tail;

  public GraphFragment(final GraphFragmentContext context) {
    _graphFragmentIdentifier = context.nextIdentifier();
    _nodes = new LinkedList<DependencyNode>();
  }

  public GraphFragment(final GraphFragmentContext context, final DependencyNode node) {
    this(context);
    _nodes.add(node);
  }

  public GraphFragment(final GraphFragmentContext context, final Collection<DependencyNode> nodes) {
    _graphFragmentIdentifier = context.nextIdentifier();
    _nodes = new ArrayList<DependencyNode>(nodes);
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

  public void inputCompleted(final GraphFragmentContext context) {
    // If _blockCount is null, we are a tail job that has already been dispatched
    if (_blockCount != null) {
      final int blockCount = _blockCount.decrementAndGet();
      if (blockCount == 0) {
        execute(context);
        _blockCount = null;
      }
    }
  }

  public CalculationJob createCalculationJob(final GraphFragmentContext context) {
    final CalculationJobSpecification jobSpec = context.getExecutor().createJobSpecification(context.getGraph());
    final List<DependencyNode> nodes = getNodes();
    final List<CalculationJobItem> items = new ArrayList<CalculationJobItem>(nodes.size());
    for (DependencyNode node : nodes) {
      final Set<ValueSpecification> inputs = node.getInputValues();
      CalculationJobItem jobItem = new CalculationJobItem(node.getFunction().getFunction().getFunctionDefinition().getUniqueId(), node.getFunction().getParameters(),
          node.getComputationTarget(), inputs, node.getOutputValues());
      items.add(jobItem);
    }
    context.getExecutor().addJobToViewProcessorQuery(jobSpec, context.getGraph());
    final CalculationJob job = new CalculationJob(jobSpec, context.getFunctionInitId(), context.getResolverVersionCorrection(), _requiredJobs, items, getCacheSelectHint());
    if (getTail() != null) {
      for (GraphFragment<F> tail : getTail()) {
        tail._blockCount = null;
        final int size = tail.getInputFragments().size();
        if (tail._requiredJobs == null) {
          tail._requiredJobs = new long[size];
          tail._requiredJobs[0] = jobSpec.getJobId();
          tail._requiredJobIndex = 1;
        } else {
          tail._requiredJobs[tail._requiredJobIndex++] = jobSpec.getJobId();
        }
        if (tail._requiredJobIndex == size) {
          final CalculationJob tailJob = tail.createCalculationJob(context);
          job.addTail(tailJob);
        }
      }
    }
    context.registerCallback(jobSpec, this);
    return job;
  }

  private static String toString(final long[] a) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < a.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(a[i]);
    }
    return sb.toString();
  }

  private void printJob(final PrintStream out, final String indent, final CalculationJob job) {
    out.println(indent + getIdentifier() + " - " + job.getSpecification());
    final long[] required = job.getRequiredJobIds();
    if (required != null) {
      out.println(indent + "\trequires " + toString(required));
    }
    for (CalculationJobItem item : job.getJobItems()) {
      out.println(indent + "\t" + item.getFunctionUniqueIdentifier() + " on " + item.getComputationTargetSpecification());
    }
    Collection<CalculationJob> tailJobs = job.getTail();
    if (tailJobs != null) {
      for (CalculationJob tailJob : tailJobs) {
        printJob(out, indent + "  ", tailJob);
      }
    }
  }

  public void execute(final GraphFragmentContext context) {
    final CalculationJob job = createCalculationJob(context);
    /*try {
      synchronized (System.out) {
        final PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream("/tmp/graphFragment.txt", true)));
        printJob(out, "", job);
        out.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }*/
    context.dispatchJob(job);
  }

  @Override
  public String toString() {
    return getIdentifier() + ": " + getJobItems() + " dep. node(s)";
  }

  public void resultReceived(final GraphFragmentContext context, final CalculationJobResult result) {
    // Release tree fragments up the tree
    context.addExecutionTime(result.getDuration());
    for (GraphFragment<F> dependent : getOutputFragments()) {
      dependent.inputCompleted(context);
    }
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
      _future = new RootGraphFragmentFuture(context, this, statistics);
    }

    @Override
    public void execute(final GraphFragmentContext context) {
      _future.executed();
    }

    public Future<DependencyGraph> getFuture() {
      return _future;
    }

  }

}
