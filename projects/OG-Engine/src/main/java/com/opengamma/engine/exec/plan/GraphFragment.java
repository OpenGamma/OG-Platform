/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.calcnode.stats.FunctionInvocationStatistics;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;

/**
 * A subset of an executable dependency graph that corresponds to a single computation job.
 * <p>
 * Fragments are linked to create a graph of fragments that corresponds to the original graph. At the extreme, there could be a fragment for each node in the original graph and the graph of fragments
 * will be the same shape as the graph of nodes.
 */
/* package */final class GraphFragment {

  /**
   * Data input/output rate from shared cache. Assumes 1Gb/s. This needs to be tunable through the executor.
   */
  private static final double NANOS_PER_BYTE = 1.0;

  /**
   * The nodes that are within this fragment of the graph.
   */
  private LinkedList<DependencyNode> _nodes = new LinkedList<DependencyNode>();

  /**
   * The fragments that feed into this subgraph. For any node in this fragment, its input nodes are either also in this fragment or in one of the input fragments.
   */
  private Set<GraphFragment> _inputFragments = new HashSet<GraphFragment>();

  /**
   * All of the input values for this fragment that will be produced as outputs by the input fragments. This is a map of the value to its cost.
   */
  private Map<ValueSpecification, Integer> _inputValues = new HashMap<ValueSpecification, Integer>();

  /**
   * The fragments that consume the outputs of this subgraph. For any node in this fragment, its dependent nodes are either also in this fragment or in one of the output fragments.
   */
  private Set<GraphFragment> _outputFragments = new HashSet<GraphFragment>();

  /**
   * All of the output values for this fragment that are either terminal outputs or required as inputs by the output fragments. This is a map of the value to its cost.
   */
  private Map<ValueSpecification, Integer> _outputValues = new HashMap<ValueSpecification, Integer>();

  /**
   * The estimated invocation cost of this fragment. This is the sum of the estimated invocation costs of all of the job items.
   */
  private long _invocationCost;

  /**
   * The estimated data input cost of this fragment. This is the sum of the estimated cost of all values that will be sourced from the shared value cache.
   */
  private long _dataInputCost;

  /**
   * The estimated data output cost of this fragment. This is the sum of the estimated cost of all values that will be written to the shared value cache.
   */
  private long _dataOutputCost;

  /**
   * The values that are needed for edges between nodes in this subgraph (or a tail) only. These are intermediate values that can be confined to the private caches of the calculation nodes.
   * <p>
   * This is not allocated initially to avoid heap churn - at most, only half of the allocations will be used.
   */
  private Set<ValueSpecification> _privateValues;

  /**
   * The estimated start time as used by the graph coloring algorithm used to identify job tails.
   */
  private long _startTime = -1;

  /**
   * The execution group identifier, written by the graph coloring algorithm. If an output fragment has the same execution identifier then it will be streamed to the same node.
   */
  private int _executionId;

  /**
   * The fragments that are logical tails to this one. These are fragments that will consume the outputs of this fragment and should be streamed to the same node as tail jobs.
   */
  private Collection<GraphFragment> _tail;

  /**
   * The job created for this fragment.
   */
  private PlannedJob _job;

  public GraphFragment(final DependencyNode node, final FunctionInvocationStatistics statistics) {
    _nodes.add(node);
    _invocationCost = (long) statistics.getInvocationCost();
    final Integer inputCost = (Integer) (int) (statistics.getDataInputCost() * NANOS_PER_BYTE);
    int count = node.getInputCount();
    for (int i = 0; i < count; i++) {
      _inputValues.put(node.getInputValue(i), inputCost);
    }
    _dataInputCost = count * inputCost;
    final Integer outputCost = (Integer) (int) (statistics.getDataOutputCost() * NANOS_PER_BYTE);
    count = node.getOutputCount();
    for (int i = 0; i < count; i++) {
      _outputValues.put(node.getOutputValue(i), outputCost);
    }
    _dataOutputCost = count * outputCost;
  }

  private LinkedList<DependencyNode> getNodes() {
    return _nodes;
  }

  public void addTail(final GraphFragment fragment) {
    if (_tail == null) {
      _tail = new LinkedList<GraphFragment>();
    }
    _tail.add(fragment);
  }

  private Collection<GraphFragment> getTail() {
    return _tail;
  }

  public Set<GraphFragment> getInputFragments() {
    return _inputFragments;
  }

  private Map<ValueSpecification, Integer> getInputValues() {
    return _inputValues;
  }

  public Set<GraphFragment> getOutputFragments() {
    return _outputFragments;
  }

  private Map<ValueSpecification, Integer> getOutputValues() {
    return _outputValues;
  }

  public int getJobItems() {
    if (_nodes != null) {
      return _nodes.size();
    }
    return _job.getItems().size();
  }

  public long getInvocationCost() {
    return _invocationCost;
  }

  public long getDataInputCost() {
    return _dataInputCost;
  }

  public long getDataOutputCost() {
    return _dataOutputCost;
  }

  public long getDataIOCost() {
    return getDataInputCost() + getDataOutputCost();
  }

  public long getJobCost() {
    return getInvocationCost() + getDataIOCost();
  }

  private Set<ValueSpecification> getPrivateValues() {
    return _privateValues;
  }

  private void addPrivateValues(final Set<ValueSpecification> privateValues) {
    if (privateValues != null) {
      if (_privateValues == null) {
        _privateValues = privateValues;
      } else {
        _privateValues.addAll(privateValues);
      }
    }
  }

  private void addPrivateValue(final ValueSpecification privateValue) {
    if (_privateValues == null) {
      _privateValues = new HashSet<ValueSpecification>();
    }
    _privateValues.add(privateValue);
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

  public int getExecutionId() {
    return _executionId;
  }

  public void setExecutionId(final int executionId) {
    _executionId = executionId;
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
    addPrivateValues(fragment.getPrivateValues());
    _invocationCost += fragment.getInvocationCost();
  }

  /**
   * Tests whether {@link #appendFragment} would produce a fragment within the maximum limits.
   */
  public boolean canAppendFragment(final GraphFragment fragment, final int maxItems, final long maxCost) {
    if (getJobItems() + fragment.getJobItems() > maxItems) {
      return false;
    }
    long cost = getInvocationCost() + fragment.getInvocationCost() + getDataOutputCost() + fragment.getDataOutputCost() + getDataInputCost();
    if (cost > maxCost) {
      return false;
    }
    if (cost + fragment.getDataInputCost() <= maxCost) {
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
   * Appends a fragment. A fragment can be appended if it does not require any input from this. If output from one feeds into the other, use the more expensive prepend operation.
   */
  public void appendFragment(final GraphFragment fragment) {
    getNodes().addAll(fragment.getNodes());
    getOutputValues().putAll(fragment.getOutputValues());
    _dataOutputCost += fragment.getDataOutputCost();
    mergeFragmentCost(fragment);
  }

  /**
   * Tests whether {@link #prependFragment} would produce a fragment within the maximum limits
   */
  public boolean canPrependFragment(final GraphFragment fragment, final int maxItems, final long maxCost) {
    if (getJobItems() + fragment.getJobItems() > maxItems) {
      return false;
    }
    long cost = getInvocationCost() + getDataOutputCost() + fragment.getInvocationCost() + fragment.getDataInputCost();
    if (cost > maxCost) {
      return false;
    }
    if (cost + getDataInputCost() + fragment.getDataOutputCost() <= maxCost) {
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
   * Prepends a fragment. A fragment can be prepended if it produces values needed by this. If output from one doesn't feed into the other, use the cheaper append operation.
   */
  public void prependFragment(final GraphFragmentContext context, final GraphFragment fragment) {
    final Iterator<DependencyNode> nodeIterator = fragment.getNodes().descendingIterator();
    while (nodeIterator.hasNext()) {
      getNodes().addFirst(nodeIterator.next());
    }
    final Map<ValueSpecification, Boolean> sharedCacheValues = context.getSharedCacheValues();
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
            addPrivateValue(output.getKey());
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
   * Updates the shared value map with details of any outputs this fragment produces that are destined for its tails only so can be considered "private". Once identified the output is removed from the
   * costed output set and added to the "private" set.
   * <p>
   * This must be called for all fragments prior to job construction as the data collected is needed to construct the cache select hints.
   * 
   * @param sharedValues the shared value map to be updated with any outputs that should be private
   */
  public void exportPrivateValues(final Map<ValueSpecification, Boolean> sharedValues) {
    if (getTail() != null) {
      // Only applies if there is a tail. A job without a tail can only produce shared values.
      final Iterator<ValueSpecification> outputValueItr = getOutputValues().keySet().iterator();
      while (outputValueItr.hasNext()) {
        final ValueSpecification outputValue = outputValueItr.next();
        if (sharedValues.containsKey(outputValue)) {
          // A terminal output
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
          // Output is only consumed by our own tail
          addPrivateValue(outputValue);
          sharedValues.put(outputValue, Boolean.FALSE);
          outputValueItr.remove();
        }
      }
    }
  }

  /**
   * Creates the cache hint, indicating which of the input/output values for this job will be in the shared/private caches. Terminal outputs are explicitly added to the shared value map. Outputs
   * private to a job are flagged internally when fragments get merged. Outputs consumed only by a tail will be flagged by the calls to {@link #exportPrivateValues} before job construction.
   * 
   * @param context the current context, not null
   * @return the cache hint, not null
   */
  private CacheSelectHint createCacheSelectHint(final GraphFragmentContext context) {
    final Map<ValueSpecification, Boolean> sharedValues = context.getSharedCacheValues();
    Set<ValueSpecification> localPrivateValues = getPrivateValues();
    // If there are no private intermediate values, the member will still be null at this point.
    if (localPrivateValues == null) {
      localPrivateValues = new HashSet<ValueSpecification>();
    }
    // Anything in the output set is shared following a previous call to {@link #exportPrivateValues}. Inputs may be either shared or private.
    final Set<ValueSpecification> localSharedValues = new HashSet<ValueSpecification>(getOutputValues().keySet());
    // If this is a tail job and the input is private then it will have a FALSE entry in the shared value map, produced by a previous call
    // to {@link #exportPrivateValues} on the fragment that produces it. If this is a terminal output the map will have a TRUE entry.
    // Anything not in the map is either a shared output from another fragment or will be provided when the graph is executed (eg market data).
    for (ValueSpecification inputValue : getInputValues().keySet()) {
      final Boolean shared = sharedValues.get(inputValue);
      if (shared == Boolean.FALSE) {
        localPrivateValues.add(inputValue);
      } else {
        localSharedValues.add(inputValue);
      }
    }
    // Create the cheapest hint object
    if (localPrivateValues.size() < localSharedValues.size()) {
      return CacheSelectHint.privateValues(localPrivateValues);
    } else {
      return CacheSelectHint.sharedValues(localSharedValues);
    }
  }

  private PlannedJob createJob(final GraphFragmentContext context) {
    final String calculationConfig = context.getCalculationConfig();
    final List<DependencyNode> nodes = getNodes();
    final List<CalculationJobItem> items = new ArrayList<CalculationJobItem>(nodes.size());
    final Map<ValueSpecification, FunctionParameters> parameters = context.getParameters();
    for (final DependencyNode node : nodes) {
      final ExecutionLogMode logMode = context.getLogModeSource().getLogMode(calculationConfig, node.getOutputValue(0));
      FunctionParameters functionParameters = node.getFunction().getParameters();
      final ValueSpecification[] outputs = DependencyNodeImpl.getOutputValueArray(node);
      for (ValueSpecification output : outputs) {
        FunctionParameters newParameters = parameters.get(output);
        if (newParameters != null) {
          functionParameters = newParameters;
        }
      }
      final CalculationJobItem jobItem = new CalculationJobItem(node.getFunction().getFunctionId(), functionParameters, node.getTarget(), DependencyNodeImpl.getInputValueArray(node),
          outputs, logMode);
      items.add(jobItem);
    }
    final CacheSelectHint hint = createCacheSelectHint(context);
    final Set<GraphFragment> outputFragments = getOutputFragments();
    final Collection<GraphFragment> tailFragments = getTail();
    final PlannedJob[] tailJobs;
    if (tailFragments != null) {
      int index = 0;
      tailJobs = new PlannedJob[tailFragments.size()];
      for (GraphFragment tailFragment : tailFragments) {
        tailJobs[index++] = tailFragment.getOrCreateJob(context);
        outputFragments.remove(tailFragment);
      }
    } else {
      tailJobs = null;
    }
    final PlannedJob[] dependentJobs;
    final int dependentJobCount = outputFragments.size();
    if (dependentJobCount > 0) {
      int index = 0;
      dependentJobs = new PlannedJob[dependentJobCount];
      for (GraphFragment outputFragment : outputFragments) {
        dependentJobs[index++] = outputFragment.getOrCreateJob(context);
        final Set<GraphFragment> outputInputs = outputFragment.getInputFragments();
        if (outputInputs.size() == 1) {
          // We are the last fragment referencing this, help out the GC
          outputFragment._inputFragments = null;
          outputFragment._inputValues = null;
        } else {
          outputInputs.remove(this);
        }
      }
    } else {
      dependentJobs = null;
    }
    return new PlannedJob(getInputFragments().size(), items, hint, tailJobs, dependentJobs);
  }

  public PlannedJob getOrCreateJob(final GraphFragmentContext context) {
    if (_job == null) {
      _job = createJob(context);
      // Help out the GC
      _nodes = null;
      _outputFragments = null;
      _outputValues = null;
      _tail = null;
      _privateValues = null;
    }
    return _job;
  }

  @Override
  public String toString() {
    return hashCode() + ": " + getJobItems() + " dep. node(s)";
  }

}
