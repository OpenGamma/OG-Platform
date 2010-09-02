/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;

/* package */class GraphFragment implements JobResultReceiver {

  private final int _graphFragmentIdentifier;
  private final GraphFragmentContext _context;
  private final LinkedList<DependencyNode> _nodes = new LinkedList<DependencyNode>();
  private final Set<GraphFragment> _inputs = new HashSet<GraphFragment>();
  private final Set<GraphFragment> _dependencies = new HashSet<GraphFragment>();
  private AtomicInteger _blockCount;
  private GraphFragment _tail;

  private int _startTime;
  private int _startTimeCache;
  private int _cycleCost;

  public GraphFragment(final GraphFragmentContext context) {
    _context = context;
    _graphFragmentIdentifier = context.nextIdentifier();
  }

  public GraphFragment(final GraphFragmentContext context, final DependencyNode node) {
    this(context);
    _nodes.add(node);
    // TODO [ENG-201] this should be some metric relating to the computational overhead of the function
    _cycleCost = 1;
  }

  public GraphFragment(final GraphFragmentContext context, final Collection<DependencyNode> nodes) {
    this(context);
    _nodes.addAll(nodes);
  }

  public GraphFragmentContext getContext() {
    return _context;
  }

  public LinkedList<DependencyNode> getNodes() {
    return _nodes;
  }

  public void initBlockCount() {
    _blockCount = new AtomicInteger(getInputs().size());
  }

  public void setTail(final GraphFragment fragment) {
    _tail = fragment;
  }

  public GraphFragment getTail() {
    return _tail;
  }

  public Set<GraphFragment> getInputs() {
    return _inputs;
  }

  public Set<GraphFragment> getDependencies() {
    return _dependencies;
  }

  public int getJobItems() {
    return _nodes.size();
  }

  public int getJobCycleCost() {
    return _cycleCost;
  }

  public int getJobCost() {
    // TODO [ENG-202] this would include data costs from shared cache I/O
    return _cycleCost;
  }

  public int getStartTime(final int startTimeCache) {
    if (startTimeCache == _startTimeCache) {
      return _startTime;
    }
    _startTimeCache = startTimeCache;
    int latest = 0;
    for (GraphFragment input : getInputs()) {
      final int finish = input.getStartTime(startTimeCache) + input.getJobCost();
      if (finish > latest) {
        latest = finish;
      }
    }
    _startTime = latest;
    return latest;
  }

  public void prependFragment(final GraphFragment fragment) {
    final Iterator<DependencyNode> nodeIterator = fragment.getNodes().descendingIterator();
    while (nodeIterator.hasNext()) {
      getNodes().addFirst(nodeIterator.next());
    }
    _cycleCost += fragment._cycleCost;
  }

  public void appendFragment(final GraphFragment fragment) {
    getNodes().addAll(fragment.getNodes());
    _cycleCost += fragment._cycleCost;
  }

  public void inputCompleted() {
    // TODO [ENG-187] If we're a tail, we don't want to kick off an execution; we've already been dispatched
    final int blockCount = _blockCount.decrementAndGet();
    if (blockCount == 0) {
      execute();
      // Help out the GC - we don't need these any more
      _blockCount = null;
      getInputs().clear();
    }
  }

  public void executeImpl() {
    final CalculationJobSpecification jobSpec = getContext().getExecutor().createJobSpecification(getContext().getGraph());
    final List<CalculationJobItem> items = new ArrayList<CalculationJobItem>();
    final Map<CalculationJobItem, DependencyNode> item2Node = getContext().getItem2Node();
    final Set<ValueSpecification> privateValues = new HashSet<ValueSpecification>();
    final Set<ValueSpecification> sharedValues = new HashSet<ValueSpecification>(getContext().getGraph().getTerminalOutputValues());
    for (DependencyNode node : getNodes()) {
      final Set<ValueSpecification> inputs = node.getInputValues();
      CalculationJobItem jobItem = new CalculationJobItem(node.getFunction().getFunction().getUniqueIdentifier(), node.getFunction().getParameters(), node.getComputationTarget().toSpecification(),
          inputs, node.getOutputRequirements());
      items.add(jobItem);
      item2Node.put(jobItem, node);
      // If node has dependencies which AREN'T in the graph fragment, its outputs for those nodes are "shared" values
      for (ValueSpecification specification : node.getOutputValues()) {
        if (sharedValues.contains(specification)) {
          continue;
        }
        boolean isPrivate = true;
        for (DependencyNode dependent : node.getDependentNodes()) {
          if (!getNodes().contains(dependent)) {
            isPrivate = false;
            break;
          }
        }
        if (isPrivate) {
          privateValues.add(specification);
        } else {
          sharedValues.add(specification);
        }
      }
      // If node has inputs which haven't been seen already, they can't have been generated within this fragment so are "shared"
      for (ValueSpecification specification : inputs) {
        if (sharedValues.contains(specification) || privateValues.contains(specification)) {
          continue;
        }
        sharedValues.add(specification);
      }
    }
    final CacheSelectHint cacheHint;
    if (privateValues.size() < sharedValues.size()) {
      cacheHint = CacheSelectHint.privateValues(privateValues);
    } else {
      cacheHint = CacheSelectHint.sharedValues(sharedValues);
    }
    getContext().getExecutor().addJobToViewProcessorQuery(jobSpec, getContext().getGraph());
    final CalculationJob job = new CalculationJob(jobSpec, items, cacheHint);
    if (getTail() != null) {
      // TODO [ENG-187] Add the tail to the job that's being sent to the dispatcher
    }
    getContext().getExecutor().dispatchJob(job, this);
  }

  public void execute() {
    executeImpl();
  }

  @Override
  public String toString() {
    return _graphFragmentIdentifier + ": " + _nodes.size() + " dep. node(s), earliestStart=" + _startTime + ", executionCost=" + _cycleCost;
  }

  @Override
  public void resultReceived(final CalculationJobResult result) {
    // Mark nodes as good or bad
    final Map<CalculationJobItem, DependencyNode> item2Node = getContext().getItem2Node();
    for (CalculationJobResultItem item : result.getResultItems()) {
      DependencyNode node = item2Node.get(item.getItem());
      if (node == null) {
        continue;
      }
      getContext().getExecutor().markExecuted(node);

      if (item.failed()) {
        getContext().getExecutor().markFailed(node);
      }
    }
    // Release tree fragments up the tree
    for (GraphFragment dependent : getDependencies()) {
      dependent.inputCompleted();
    }
    getContext().addExecutionTime(result.getDuration());
  }

}
