/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatistics;

/* package */class GraphFragment {

  /**
   * Data input/output rate from shared cache. Assumes 1Gb/s. This needs to be tunable through the
   * executor.
   */
  private static final double NANOS_PER_BYTE = 1.0;

  private final int _graphFragmentIdentifier;
  private final GraphFragmentContext _context;
  private final LinkedList<DependencyNode> _nodes = new LinkedList<DependencyNode>();
  private final Map<ValueSpecification, Integer> _inputValues = new HashMap<ValueSpecification, Integer>();
  private final Map<ValueSpecification, Integer> _outputValues = new HashMap<ValueSpecification, Integer>();
  private final Set<GraphFragment> _inputFragments = new HashSet<GraphFragment>();
  private final Set<GraphFragment> _outputFragments = new HashSet<GraphFragment>();
  private Set<ValueSpecification> _localPrivateValues = new HashSet<ValueSpecification>();
  private CacheSelectHint _cacheSelectHint;
  private AtomicInteger _blockCount;
  private Collection<Long> _requiredJobs;
  private Collection<GraphFragment> _tail;
  private int _executionId;

  private long _startTime = -1;
  private long _invocationCost;
  private long _dataInputCost;
  private long _dataOutputCost;

  public GraphFragment(final GraphFragmentContext context) {
    _context = context;
    _graphFragmentIdentifier = context.nextIdentifier();
  }

  public GraphFragment(final GraphFragmentContext context, final DependencyNode node) {
    this(context);
    _nodes.add(node);
    final FunctionInvocationStatistics statistics = getContext().getFunctionStatistics(node.getFunction().getFunction());
    _invocationCost = (long) statistics.getInvocationCost();
    final Integer inputCost = (Integer) (int) (statistics.getDataInputCost() * NANOS_PER_BYTE);
    for (ValueSpecification input : node.getInputValues()) {
      _inputValues.put(input, inputCost);
    }
    _dataInputCost = node.getInputValues().size() * inputCost;
    final Integer outputCost = (Integer) (int) (statistics.getDataOutputCost() * NANOS_PER_BYTE);
    for (ValueSpecification output : node.getOutputValues()) {
      _outputValues.put(output, outputCost);
    }
    _dataOutputCost = node.getOutputValues().size() * outputCost;
  }

  public GraphFragment(final GraphFragmentContext context, final Collection<DependencyNode> nodes) {
    this(context);
    _nodes.addAll(nodes);
    _invocationCost = 0;
    for (DependencyNode node : nodes) {
      _invocationCost += (long) getContext().getFunctionStatistics(node.getFunction().getFunction()).getInvocationCost();
    }
  }

  public GraphFragmentContext getContext() {
    return _context;
  }

  public LinkedList<DependencyNode> getNodes() {
    return _nodes;
  }

  public void initBlockCount() {
    _blockCount = new AtomicInteger(getInputFragments().size());
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

  public Set<GraphFragment> getInputFragments() {
    return _inputFragments;
  }

  public Set<GraphFragment> getOutputFragments() {
    return _outputFragments;
  }

  private Map<ValueSpecification, Integer> getInputValues() {
    return _inputValues;
  }

  private Map<ValueSpecification, Integer> getOutputValues() {
    return _outputValues;
  }

  private Set<ValueSpecification> getPrivateValues() {
    return _localPrivateValues;
  }

  public int getJobItems() {
    return _nodes.size();
  }

  public long getJobInvocationCost() {
    return _invocationCost;
  }

  public long getJobDataInputCost() {
    return _dataInputCost;
  }

  public long getJobDataOutputCost() {
    return _dataOutputCost;
  }

  public long getJobCost() {
    return getJobInvocationCost() + getJobDataInputCost() + getJobDataOutputCost();
  }

  public void setExecutionId(final int executionId) {
    _executionId = executionId;
  }

  public int getExecutionId() {
    return _executionId;
  }

  public long getStartTime() {
    if (_startTime >= 0) {
      return _startTime;
    }
    long latest = 0;
    for (GraphFragment input : getInputFragments()) {
      final long finish = input.getStartTime() + input.getJobCost();
      if (finish > latest) {
        latest = finish;
      }
    }
    _startTime = latest;
    return latest;
  }

  /**
   * Merges input and invocation costs.
   */
  private void mergeFragmentCost(final GraphFragment fragment) {
    for (final Map.Entry<ValueSpecification, Integer> input : fragment.getInputValues().entrySet()) {
      if (!getInputValues().containsKey(input.getKey())) {
        // We don't already require the value
        getInputValues().put(input.getKey(), input.getValue());
        _dataInputCost += input.getValue();
      }
    }
    _invocationCost += fragment.getJobInvocationCost();
  }

  public boolean canPrependFragment(final GraphFragment fragment, final int maxItems, final long maxCost) {
    if (getJobItems() + fragment.getJobItems() > maxItems) {
      return false;
    }
    long cost = getJobInvocationCost() + getJobDataOutputCost() + fragment.getJobInvocationCost() + fragment.getJobDataInputCost();
    if (cost > maxCost) {
      return false;
    }
    if (cost + getJobDataInputCost() + fragment.getJobDataOutputCost() <= maxCost) {
      return true;
    }
    // In the interests of getting an answer quickly, outputs that need to go into shared cache are ignored so the prepend might end up exceeding our maximum
    for (final Map.Entry<ValueSpecification, Integer> input : getInputValues().entrySet()) {
      if (!fragment.getInputValues().containsKey(input.getKey()) && !fragment.getOutputValues().containsKey(input.getKey())) {
        cost += input.getValue();
      }
    }
    if (cost > maxCost) {
      return false;
    }
    for (final Map.Entry<ValueSpecification, Integer> output : fragment.getOutputValues().entrySet()) {
      if (!getInputValues().containsKey(output.getKey())) {
        cost += output.getValue();
      }
    }
    return cost <= maxCost;
  }

  /**
   * Prepends a fragment. A fragment can be prepended if it produces values needed by this. If output from
   * one doesn't feed into the other, use the cheaper append operation.
   */
  public void prependFragment(final GraphFragment fragment) {
    final Iterator<DependencyNode> nodeIterator = fragment.getNodes().descendingIterator();
    while (nodeIterator.hasNext()) {
      getNodes().addFirst(nodeIterator.next());
    }
    final Map<ValueSpecification, Boolean> sharedCacheValues = getContext().getSharedCacheValues();
    for (final Map.Entry<ValueSpecification, Integer> output : fragment.getOutputValues().entrySet()) {
      final Integer required = getInputValues().remove(output.getKey());
      if (required != null) {
        // This fragment requires the output the other fragment produces, so lower data input cost
        _dataInputCost -= required;
        // If the value is not needed by any other fragments it supplies it doesn't need to go into our output
        if (sharedCacheValues.get(output.getKey()) != Boolean.TRUE) {
          boolean isPrivate = true;
          for (GraphFragment outputFragment : fragment.getOutputFragments()) {
            if (outputFragment != this) {
              if (outputFragment.getInputValues().containsKey(output.getKey())) {
                isPrivate = false;
                break;
              }
            }
          }
          if (isPrivate) {
            // Don't need to add it to our output values, and now know it is a PRIVATE value only
            getPrivateValues().add(output.getKey());
            continue;
          }
        }
      }
      getOutputValues().put(output.getKey(), output.getValue());
      _dataOutputCost += output.getValue();
    }
    mergeFragmentCost(fragment);
  }

  /**
   * Tests whether {@link #appendFragment} would produce a fragment within the maximum limits.
   */
  public boolean canAppendFragment(final GraphFragment fragment, final int maxItems, final long maxCost) {
    if (getJobItems() + fragment.getJobItems() > maxItems) {
      return false;
    }
    long cost = getJobInvocationCost() + fragment.getJobInvocationCost() + getJobDataOutputCost() + fragment.getJobDataOutputCost() + getJobDataInputCost();
    if (cost > maxCost) {
      return false;
    }
    if (cost + fragment.getJobDataInputCost() <= maxCost) {
      return true;
    }
    for (final Map.Entry<ValueSpecification, Integer> input : fragment.getInputValues().entrySet()) {
      if (!getInputValues().containsKey(input.getKey())) {
        cost += input.getValue();
      }
    }
    return cost <= maxCost;
  }

  /**
   * Appends a fragment. A fragment can be appended if it does not require any input from this. If output from one
   * feeds into the other, use the more expensive prepend operation. 
   */
  public void appendFragment(final GraphFragment fragment) {
    getNodes().addAll(fragment.getNodes());
    getOutputValues().putAll(fragment.getOutputValues());
    _dataOutputCost += fragment.getJobDataOutputCost();
    mergeFragmentCost(fragment);
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
    final Map<CalculationJobItem, DependencyNode> item2Node = getContext().getItem2Node();
    for (DependencyNode node : getNodes()) {
      final Set<ValueSpecification> inputs = node.getInputValues();
      CalculationJobItem jobItem = new CalculationJobItem(node.getFunction().getFunction().getFunctionDefinition().getUniqueId(), node.getFunction().getParameters(), node
          .getComputationTarget().toSpecification(), inputs, node.getOutputRequirements());
      items.add(jobItem);
      item2Node.put(jobItem, node);
    }
    if (_cacheSelectHint == null) {
      final Map<ValueSpecification, Boolean> sharedValues = getContext().getSharedCacheValues();
      final Set<ValueSpecification> localPrivateValues = getPrivateValues();
      final Set<ValueSpecification> localSharedValues = new HashSet<ValueSpecification>();
      // If fragment has dependencies which aren't in the execution fragment, its outputs for those are "shared" values
      for (ValueSpecification outputValue : getOutputValues().keySet()) {
        // Output values are unique in the graph, so this code executes ONCE for each ValueSpecification
        if (sharedValues.containsKey(outputValue)) {
          // A terminal output
          localSharedValues.add(outputValue);
          continue;
        }
        boolean isPrivate = true;
        for (GraphFragment outputFragment : getOutputFragments()) {
          if (!outputFragment.getInputValues().containsKey(outputValue)) {
            // The fragment doesn't need this value
            continue;
          }
          if (outputFragment.getExecutionId() != getExecutionId()) {
            // Node is not part of our execution
            isPrivate = false;
            break;
          }
        }
        if (isPrivate) {
          localPrivateValues.add(outputValue);
          sharedValues.put(outputValue, Boolean.FALSE);
        } else {
          localSharedValues.add(outputValue);
          sharedValues.put(outputValue, Boolean.TRUE);
        }
      }
      // Any input graph fragments will have been processed (but maybe not executed), so inputs coming from
      // them will be in the shared value map. Anything not in the map is an input that will be in the
      // shared cache before the graph starts executing.
      for (ValueSpecification inputValue : getInputValues().keySet()) {
        if (!localSharedValues.contains(inputValue) && !localPrivateValues.contains(inputValue)) {
          final Boolean shared = sharedValues.get(inputValue);
          if (shared == Boolean.FALSE) {
            localPrivateValues.add(inputValue);
          } else {
            localSharedValues.add(inputValue);
          }
        }
      }
      // System.err.println(localPrivateValues.size() + " private value(s), " + localSharedValues.size() + " shared value(s)");
      if (localPrivateValues.size() < localSharedValues.size()) {
        _cacheSelectHint = CacheSelectHint.privateValues(localPrivateValues);
      } else {
        _cacheSelectHint = CacheSelectHint.sharedValues(localSharedValues);
      }
      // Won't need this set again, so help the GC out
      _localPrivateValues = null;
    }
    getContext().getExecutor().addJobToViewProcessorQuery(jobSpec, getContext().getGraph());
    final CalculationJob job = new CalculationJob(jobSpec, _requiredJobs, items, _cacheSelectHint);
    if (getTail() != null) {
      for (GraphFragment tail : getTail()) {
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

  public void executeImpl() {
    getContext().dispatchJob(createCalculationJob());
  }

  public void execute() {
    executeImpl();
  }

  public boolean reset(final Set<GraphFragment> processed) {
    if (!getInputFragments().isEmpty()) {
      initBlockCount();
      for (GraphFragment input : getInputFragments()) {
        if (processed.add(input)) {
          if (!input.reset(processed)) {
            return false;
          }
        }
      }
    }
    _requiredJobs = null;
    return true;
  }

  @Override
  public String toString() {
    return _graphFragmentIdentifier + ": " + _nodes.size() + " dep. node(s), earliestStart=" + _startTime + ", executionCost=" + _invocationCost;
  }

  public void resultReceived(final CalculationJobResult result) {
    // Release tree fragments up the tree
    getContext().addExecutionTime(result.getDuration());
    for (GraphFragment dependent : getOutputFragments()) {
      dependent.inputCompleted();
    }
  }

}
