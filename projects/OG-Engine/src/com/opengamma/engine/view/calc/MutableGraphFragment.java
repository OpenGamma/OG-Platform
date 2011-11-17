/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatistics;

/**
 * Subclass of {@link GraphFragment} to include additional state for the fragmentation
 * algorithm.
 */
/* package */class MutableGraphFragment extends GraphFragment<MutableGraphFragmentContext, MutableGraphFragment> {

  /**
   * Data input/output rate from shared cache. Assumes 1Gb/s. This needs to be tunable through the
   * executor.
   */
  private static final double NANOS_PER_BYTE = 1.0;

  private final Map<ValueSpecification, Integer> _inputValues = new HashMap<ValueSpecification, Integer>();
  private final Map<ValueSpecification, Integer> _outputValues = new HashMap<ValueSpecification, Integer>();
  private Set<ValueSpecification> _localPrivateValues = new HashSet<ValueSpecification>();
  private long _startTime = -1;
  private long _invocationCost;
  private long _dataInputCost;
  private long _dataOutputCost;
  private int _executionId;

  private MutableGraphFragment(final MutableGraphFragmentContext context) {
    super(context);
  }

  public MutableGraphFragment(final MutableGraphFragmentContext context, final DependencyNode node) {
    super(context, node);
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

  private Map<ValueSpecification, Integer> getInputValues() {
    return _inputValues;
  }

  private Map<ValueSpecification, Integer> getOutputValues() {
    return _outputValues;
  }

  private Set<ValueSpecification> getPrivateValues() {
    return _localPrivateValues;
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

  public long getStartTime() {
    if (_startTime >= 0) {
      return _startTime;
    }
    long latest = 0;
    for (MutableGraphFragment input : getInputFragments()) {
      final long finish = input.getStartTime() + input.getJobCost();
      if (finish > latest) {
        latest = finish;
      }
    }
    _startTime = latest;
    return latest;
  }

  public void addTail(final MutableGraphFragment fragment) {
    Collection<MutableGraphFragment> tail = getTail();
    if (tail == null) {
      tail = new LinkedList<MutableGraphFragment>();
      setTail(tail);
    }
    tail.add(fragment);
  }

  /**
   * Merges input and invocation costs.
   */
  private void mergeFragmentCost(final MutableGraphFragment fragment) {
    for (final Map.Entry<ValueSpecification, Integer> input : fragment.getInputValues().entrySet()) {
      if (!getInputValues().containsKey(input.getKey())) {
        // We don't already require the value
        getInputValues().put(input.getKey(), input.getValue());
        _dataInputCost += input.getValue();
      }
    }
    _localPrivateValues.addAll(fragment.getPrivateValues());
    _invocationCost += fragment.getJobInvocationCost();
  }

  public boolean canPrependFragment(final MutableGraphFragment fragment, final int maxItems, final long maxCost) {
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

  @Override
  public LinkedList<DependencyNode> getNodes() {
    return (LinkedList<DependencyNode>) super.getNodes();
  }

  /**
   * Prepends a fragment. A fragment can be prepended if it produces values needed by this. If output from
   * one doesn't feed into the other, use the cheaper append operation.
   */
  public void prependFragment(final MutableGraphFragment fragment) {
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
          for (MutableGraphFragment outputFragment : fragment.getOutputFragments()) {
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
  public boolean canAppendFragment(final MutableGraphFragment fragment, final int maxItems, final long maxCost) {
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
  public void appendFragment(final MutableGraphFragment fragment) {
    getNodes().addAll(fragment.getNodes());
    getOutputValues().putAll(fragment.getOutputValues());
    _dataOutputCost += fragment.getJobDataOutputCost();
    mergeFragmentCost(fragment);
  }

  public void setExecutionId(final int executionId) {
    _executionId = executionId;
  }

  public int getExecutionId() {
    return _executionId;
  }

  @Override
  public CalculationJob createCalculationJob() {
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
      for (MutableGraphFragment outputFragment : getOutputFragments()) {
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
      setCacheSelectHint(CacheSelectHint.privateValues(localPrivateValues));
    } else {
      setCacheSelectHint(CacheSelectHint.sharedValues(localSharedValues));
    }
    // Won't need this set again, so help the GC out
    _localPrivateValues = null;
    return super.createCalculationJob();
  }

  @Override
  public String toString() {
    return super.toString() + ", earliestStart=" + _startTime + ", executionCost=" + _invocationCost;
  }

  public static class Root extends MutableGraphFragment {

    private final RootGraphFragmentFuture _future;

    public Root(final MutableGraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics) {
      super(context);
      _future = new RootGraphFragmentFuture(this, statistics);
    }

    @Override
    public void execute() {
      getContext().getExecutor().getCache().cachePlan(getContext().getGraph(), getContext().getFunctionInitId(), ExecutionPlan.of(this));
      _future.executed();
    }

    public Future<DependencyGraph> getFuture() {
      return _future;
    }

  }

}
