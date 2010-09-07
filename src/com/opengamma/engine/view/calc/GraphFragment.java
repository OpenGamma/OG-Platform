/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;

/* package */class GraphFragment {

  private final int _graphFragmentIdentifier;
  private final GraphFragmentContext _context;
  private final LinkedList<DependencyNode> _nodes = new LinkedList<DependencyNode>();
  private final Set<GraphFragment> _inputs = new HashSet<GraphFragment>();
  private final Set<GraphFragment> _dependencies = new HashSet<GraphFragment>();
  private AtomicInteger _blockCount;
  private Collection<Long> _requiredJobs;
  private Collection<GraphFragment> _tail;

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

  public void addTail(final GraphFragment fragment) {
    if (_tail == null) {
      _tail = new LinkedList<GraphFragment>();
    }
    _tail.add(fragment);
  }

  public Collection<GraphFragment> getTail() {
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

  /**
   * Set an attribute used for graph coloring type algorithms.
   */
  public void setColour(final int colour) {
    _startTimeCache = colour;
  }

  /**
   * Returns an attribute used for graph coloring type algorithms.
   */
  public int getColour() {
    return _startTimeCache;
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
    // If _blockCount is null, we are a tail job that has already been dispatched
    if (_blockCount != null) {
      final int blockCount = _blockCount.decrementAndGet();
      if (blockCount == 0) {
        execute();
        _blockCount = null;
      }
    }
  }

  public CalculationJob createCalculationJob(final Integer executionId) {
    final CalculationJobSpecification jobSpec = getContext().getExecutor().createJobSpecification(getContext().getGraph());
    final Map<DependencyNode, Integer> node2executionId = getContext().getNode2ExecutionId();
    final List<CalculationJobItem> items = new ArrayList<CalculationJobItem>();
    final Map<CalculationJobItem, DependencyNode> item2Node = getContext().getItem2Node();
    final Map<ValueSpecification, Boolean> sharedValues = getContext().getSharedCacheValues();
    final Set<ValueSpecification> localPrivateValues = new HashSet<ValueSpecification>();
    final Set<ValueSpecification> localSharedValues = new HashSet<ValueSpecification>();
    for (DependencyNode node : getNodes()) {
      final Set<ValueSpecification> inputs = node.getInputValues();
      CalculationJobItem jobItem = new CalculationJobItem(node.getFunction().getFunction().getUniqueIdentifier(), node.getFunction().getParameters(), node.getComputationTarget().toSpecification(),
          inputs, node.getOutputRequirements());
      items.add(jobItem);
      item2Node.put(jobItem, node);
      // If node has dependencies which aren't in the execution fragment, its outputs for those nodes are "shared" values
      final Collection<DependencyNode> dependencies = node.getDependentNodes();
      for (ValueSpecification specification : node.getOutputValues()) {
        // Output values are unique in the graph, so this code executes ONCE for each ValueSpecification
        if (sharedValues.containsKey(specification)) {
          // A terminal output
          localSharedValues.add(specification);
          continue;
        }
        boolean isPrivate = true;
        for (DependencyNode dependent : dependencies) {
          if (!dependent.hasInputValue(specification)) {
            // The dependent node doesn't need this value
            continue;
          }
          if (node2executionId.get(dependent) != executionId) {
            // Node is not part of our execution
            isPrivate = false;
            break;
          }
        }
        if (isPrivate) {
          localPrivateValues.add(specification);
          sharedValues.put(specification, Boolean.FALSE);
        } else {
          localSharedValues.add(specification);
          sharedValues.put(specification, Boolean.TRUE);
        }
      }
      // Any input graph fragments will have been processed (but maybe not executed), so inputs coming from
      // them will be in the shared value map. Anything not in the map is an input that will be in the
      // shared cache before the graph starts executing.
      for (ValueSpecification specification : inputs) {
        if (!localSharedValues.contains(specification) && !localPrivateValues.contains(specification)) {
          final Boolean shared = sharedValues.get(specification);
          if (shared == Boolean.FALSE) {
            localPrivateValues.add(specification);
          } else {
            localSharedValues.add(specification);
          }
        }
      }
    }
    // System.err.println(localPrivateValues.size() + " private value(s), " + localSharedValues.size() + " shared value(s)");
    final CacheSelectHint cacheHint;
    if (localPrivateValues.size() < localSharedValues.size()) {
      cacheHint = CacheSelectHint.privateValues(localPrivateValues);
    } else {
      cacheHint = CacheSelectHint.sharedValues(localSharedValues);
    }
    getContext().getExecutor().addJobToViewProcessorQuery(jobSpec, getContext().getGraph());
    final CalculationJob job = new CalculationJob(jobSpec, _requiredJobs, items, cacheHint);
    if (getTail() != null) {
      for (GraphFragment tail : getTail()) {
        tail._blockCount = null;
        final int size = tail.getInputs().size();
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
          final CalculationJob tailJob = tail.createCalculationJob(executionId);
          job.addTail(tailJob);
        }
      }
    }
    getContext().registerCallback(jobSpec, this);
    return job;
  }

  private void markNodes(final Integer executionId) {
    final Map<DependencyNode, Integer> node2executionId = getContext().getNode2ExecutionId();
    for (DependencyNode node : getNodes()) {
      node2executionId.put(node, executionId);
    }
    if (getTail() != null) {
      for (GraphFragment tail : getTail()) {
        tail.markNodes(executionId);
      }
    }
  }

  public void executeImpl() {
    markNodes(_graphFragmentIdentifier);
    getContext().getExecutor().dispatchJob(createCalculationJob(_graphFragmentIdentifier), getContext());
  }

  public void execute() {
    executeImpl();
  }

  @Override
  public String toString() {
    return _graphFragmentIdentifier + ": " + _nodes.size() + " dep. node(s), earliestStart=" + _startTime + ", executionCost=" + _cycleCost;
  }

  public void resultReceived(final CalculationJobResult result) {
    // Release tree fragments up the tree
    getContext().addExecutionTime(result.getDuration());
    for (GraphFragment dependent : getDependencies()) {
      dependent.inputCompleted();
    }
  }

}
