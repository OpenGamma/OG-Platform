/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.calcnode.stats.FunctionCosts;
import com.opengamma.engine.calcnode.stats.FunctionCostsPerConfiguration;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Produces an execution plan for a graph that will execute on multiple calculation nodes.
 * <p>
 * Note that the parameters used to control job partitioning are guidance values and not hard constraints. The planner may produce jobs that do not meet onr or more of the limits. It is therefore
 * possible, but perhaps not useful, to specify conflicting values.
 * <p>
 * Job cost estimates are in nanoseconds. These are using the (normalized) time estimate for the function execution and the estimated input/output data volumes using an approximate data rate. The
 * actual jobs produced may take longer to execute because of additional scheduling and housekeeping overheads.
 */
public class MultipleNodeExecutionPlanner implements GraphExecutionPlanner {

  private static final Logger s_logger = LoggerFactory.getLogger(MultipleNodeExecutionPlanner.class);
  private static final GraphExecutionPlanner s_smallJobPlanner = new SingleNodeExecutionPlanner();

  private int _minimumJobItems = 1;
  private int _maximumJobItems = Integer.MAX_VALUE;
  private long _minimumJobCost;
  private long _maximumJobCost = Long.MAX_VALUE;
  private int _maximumConcurrency = Integer.MAX_VALUE;
  private FunctionCosts _functionCosts = new FunctionCosts();

  /**
   * Sets the minimum number of items for each job.
   * <p>
   * The planner will do its best to honor this limit, but may produce jobs smaller than this if the maximum cost would be exceeded or there is not enough work to make a larger job.
   * 
   * @param minimumJobItems the number of items, must be more than 0
   */
  public void setMininumJobItems(final int minimumJobItems) {
    ArgumentChecker.isTrue(minimumJobItems > 0, "minimumJobItems");
    _minimumJobItems = minimumJobItems;
  }

  /**
   * Returns the minimum number of items for each job.
   * 
   * @return the number of items
   * @see #setMinimumJobItems
   */
  public int getMinimumJobItems() {
    return _minimumJobItems;
  }

  /**
   * Sets the maximum number of items for each job.
   * <p>
   * The planner will do its best to honor this limit, but may produce jobs larger than this if the minimum cost would not be met.
   * 
   * @param maximumJobItems the number of items, must be more than 0
   */
  public void setMaximimJobItems(final int maximumJobItems) {
    ArgumentChecker.isTrue(maximumJobItems > 0, "maximumJobItems");
    _maximumJobItems = maximumJobItems;
  }

  /**
   * Returns the maximum number of items for each job.
   * 
   * @return the number of items
   * @see #setMaximumJobItems
   */
  public int getMaximumJobItems() {
    return _maximumJobItems;
  }

  /**
   * Sets the minimum estimated cost of jobs.
   * <p>
   * The planner will do its best to honor this limit, but may produce jobs with a lower cost if the maximum number of job items would be exceeded or there is not enough work to make a larger job.
   * 
   * @param minimumJobCost the estimated cost in nanoseconds, must be at least 0
   */
  public void setMinimumJobCost(final long minimumJobCost) {
    ArgumentChecker.isTrue(minimumJobCost >= 0, "minimumJobCost");
    _minimumJobCost = minimumJobCost;
  }

  /**
   * Returns the minimum estimated cost of jobs.
   * 
   * @return the estimated cost
   * @see #setMinimumJobCost
   */
  public long getMinimumJobCost() {
    return _minimumJobCost;
  }

  /**
   * Sets the maximum estimated cost of jobs.
   * <p>
   * The planner will do its best to honor this limit, but may produce more expensive jobs if the minimum number of job items would be exceeded.
   * 
   * @param maximumJobCost the estimated cost in nanoseconds, must be at least 0
   */
  public void setMaximumJobCost(final long maximumJobCost) {
    ArgumentChecker.isTrue(maximumJobCost >= 0, "maximumJobCost");
    _maximumJobCost = maximumJobCost;
  }

  /**
   * Returns the maximum estimated cost of jobs.
   * 
   * @return the estimated cost
   * @see #setMaximumJobCost
   */
  public long getMaximumJobCost() {
    return _maximumJobCost;
  }

  /**
   * Sets the concurrency limit for job tails.
   * 
   * @param maximumConcurrency the number of job tails that are expected to be executing in parallel, must be more than 0 for tail execution. If set to 0, tail execution is disabled.
   */
  public void setMaximumConcurrency(final int maximumConcurrency) {
    ArgumentChecker.isTrue(maximumConcurrency >= 0, "maximumConcurrency");
    _maximumConcurrency = maximumConcurrency;
  }

  /**
   * Returns the concurrency limit for job tails.
   * 
   * @return the number of job tails that are expected to be executing in parallel
   * @see #setMaximumConcurrency
   */
  public int getMaximumConcurrency() {
    return _maximumConcurrency;
  }

  public void setFunctionCosts(final FunctionCosts functionCosts) {
    ArgumentChecker.notNull(functionCosts, "functionCosts");
    _functionCosts = functionCosts;
  }

  public FunctionCosts getFunctionCosts() {
    return _functionCosts;
  }

  private GraphExecutionPlan createSingleNodePlan(final DependencyGraph graph, final ExecutionLogModeSource logModeSource, final long functionInitializationId,
      final Set<ValueSpecification> sharedValues, final Map<ValueSpecification, FunctionParameters> parameters) {
    return s_smallJobPlanner.createPlan(graph, logModeSource, functionInitializationId, sharedValues, parameters);
  }

  private static final class FragmentGatherer {

    private final Map<DependencyNode, GraphFragment> _node2Fragment;
    private final Set<GraphFragment> _allFragments;
    private final FunctionCostsPerConfiguration _costs;
    private final Set<ValueSpecification> _sharedValues;

    public FragmentGatherer(final int size, final FunctionCostsPerConfiguration costs, final Set<ValueSpecification> sharedValues) {
      _node2Fragment = Maps.newHashMapWithExpectedSize(size);
      _allFragments = Sets.newHashSetWithExpectedSize(size);
      _costs = costs;
      _sharedValues = sharedValues;
    }

    public GraphFragment createFragments(final DependencyNode root) {
      final GraphFragment rootFragment = new GraphFragment(root, _costs.getStatistics(root.getFunction().getFunctionId()));
      _node2Fragment.put(root, rootFragment);
      _allFragments.add(rootFragment);
      final int inputs = root.getInputCount();
      for (int i = 0; i < inputs; i++) {
        final ValueSpecification inputValue = root.getInputValue(i);
        if (isShared(inputValue)) {
          // The input is in the shared cache, so don't create a fragment for that node
          continue;
        }
        final DependencyNode inputNode = root.getInputNode(i);
        GraphFragment inputFragment = _node2Fragment.get(inputNode);
        if (inputFragment == null) {
          inputFragment = createFragments(inputNode);
        }
        inputFragment.getOutputFragments().add(rootFragment);
        rootFragment.getInputFragments().add(inputFragment);
      }
      return rootFragment;
    }

    public Set<GraphFragment> getAllFragments() {
      return _allFragments;
    }

    public boolean isShared(final ValueSpecification value) {
      return _sharedValues.contains(value);
    }

  }

  private Set<GraphFragment> createGraphFragments(final DependencyGraph graph, final FragmentGatherer fragments) {
    final int rootCount = graph.getRootCount();
    final Set<GraphFragment> roots = Sets.newHashSetWithExpectedSize(rootCount);
    rootLoop: for (int i = 0; i < rootCount; i++) { //CSIGNORE
      final DependencyNode root = graph.getRootNode(i);
      final int outputs = root.getOutputCount();
      for (int j = 0; j < outputs; j++) {
        if (fragments.isShared(root.getOutputValue(j))) {
          // Don't create a fragment for the root; its output(s) are already in the shared cache
          continue rootLoop;
        }
      }
      roots.add(fragments.createFragments(root));
    }
    return roots;
  }

  /**
   * Finds pairs of nodes with the same input set (i.e. that would execute concurrently) that are below the minimum job size and merge them together.
   */
  private boolean mergeSharedInputs(final Set<GraphFragment> rootFragments, final Set<GraphFragment> allFragments) {
    final Map<Set<GraphFragment>, GraphFragment> possibleCandidates = new HashMap<Set<GraphFragment>, GraphFragment>();
    final Map<GraphFragment, GraphFragment> validCandidates = new HashMap<GraphFragment, GraphFragment>();
    boolean result = false;
    do {
      // Scan all fragments for possible merges
      for (final GraphFragment fragment : allFragments) {
        if (fragment.getInputFragments().isEmpty()) {
          // No inputs to consider
          continue;
        }
        if ((fragment.getJobCost() >= getMinimumJobCost()) && (fragment.getJobItems() >= getMinimumJobItems())) {
          // We already meet the minimum requirement for the graph
          continue;
        }
        final GraphFragment mergeCandidate = possibleCandidates.get(fragment.getInputFragments());
        if (mergeCandidate != null) {
          if (mergeCandidate.canAppendFragment(fragment, getMaximumJobItems(), getMaximumJobCost())) {
            // Defer the merge because we're iterating through the dependent's inputs at the moment
            validCandidates.put(fragment, mergeCandidate);
            // Stop using the merge candidate
            possibleCandidates.remove(fragment.getInputFragments());
            continue;
          }
          if (fragment.getJobCost() >= mergeCandidate.getJobCost()) {
            // We are a worse possible candidate as we're already more expensive
            continue;
          }
        }
        possibleCandidates.put(fragment.getInputFragments(), fragment);
      }
      if (validCandidates.isEmpty()) {
        return result;
      }
      for (final Map.Entry<GraphFragment, GraphFragment> merge : validCandidates.entrySet()) {
        final GraphFragment fragment = merge.getKey();
        final GraphFragment mergeCandidate = merge.getValue();
        final Set<GraphFragment> outputFragments = mergeCandidate.getOutputFragments();
        boolean wasRoot = outputFragments.isEmpty();
        mergeCandidate.appendFragment(fragment);
        // Merge candidate already has the correct inputs by definition
        for (final GraphFragment dependency : fragment.getOutputFragments()) {
          dependency.getInputFragments().remove(fragment);
          if (outputFragments.add(dependency)) {
            dependency.getInputFragments().add(mergeCandidate);
            if (wasRoot) {
              // Deep node has merged with a root node; it is no longer a root
              rootFragments.remove(mergeCandidate);
              wasRoot = false;
            }
          }
        }
        for (final GraphFragment input : fragment.getInputFragments()) {
          input.getOutputFragments().remove(fragment);
        }
        allFragments.remove(fragment);
      }
      validCandidates.clear();
      possibleCandidates.clear();
      result = true;
    } while (true);
  }

  /**
   * If a fragment has only one dependency, and both it and its dependent are below the maximum job size they are merged.
   */
  private boolean mergeSingleDependencies(final GraphFragmentContext context, final Set<GraphFragment> allFragments) {
    int changes = 0;
    final Iterator<GraphFragment> fragmentIterator = allFragments.iterator();
    while (fragmentIterator.hasNext()) {
      final GraphFragment fragment = fragmentIterator.next();
      if (fragment.getOutputFragments().size() != 1) {
        continue;
      }
      final GraphFragment dependency = fragment.getOutputFragments().iterator().next();
      if (!dependency.canPrependFragment(fragment, getMaximumJobItems(), getMaximumJobCost())) {
        // Can't merge
        continue;
      }
      // Merge fragment with it's dependency and slice it out of the graph
      dependency.prependFragment(context, fragment);
      fragmentIterator.remove();
      dependency.getInputFragments().remove(fragment);
      for (final GraphFragment input : fragment.getInputFragments()) {
        dependency.getInputFragments().add(input);
        input.getOutputFragments().remove(fragment);
        input.getOutputFragments().add(dependency);
      }
      changes++;
    }
    return changes > 0;
  }

  /**
   * If a fragment has only a single input, it can be a tail to the fragment generating that input. A fragment with multiple inputs can be a tail to all of them iff they are tails to a common fragment
   * (ie all will end up at the same calculation node).
   */
  private void findTailFragments(final Set<GraphFragment> allFragments) {
    // Estimate start times based on fragment costs and dependencies
    final NavigableMap<Long, Pair<List<GraphFragment>, List<GraphFragment>>> concurrencyEvent = new TreeMap<Long, Pair<List<GraphFragment>, List<GraphFragment>>>();
    for (final GraphFragment fragment : allFragments) {
      Pair<List<GraphFragment>, List<GraphFragment>> event = concurrencyEvent.get(fragment.getStartTime());
      if (event == null) {
        event = Pairs.of((List<GraphFragment>) new LinkedList<GraphFragment>(), (List<GraphFragment>) null);
        concurrencyEvent.put(fragment.getStartTime(), event);
      } else {
        if (event.getFirst() == null) {
          event = Pairs.of((List<GraphFragment>) new LinkedList<GraphFragment>(), event.getSecond());
          concurrencyEvent.put(fragment.getStartTime(), event);
        }
      }
      event.getFirst().add(fragment);
      event = concurrencyEvent.get(fragment.getStartTime() + fragment.getJobCost());
      if (event == null) {
        event = Pairs.of((List<GraphFragment>) null, (List<GraphFragment>) new LinkedList<GraphFragment>());
        concurrencyEvent.put(fragment.getStartTime() + fragment.getJobCost(), event);
      } else {
        if (event.getSecond() == null) {
          event = Pairs.of(event.getFirst(), (List<GraphFragment>) new LinkedList<GraphFragment>());
          concurrencyEvent.put(fragment.getStartTime() + fragment.getJobCost(), event);
        }
      }
      event.getSecond().add(fragment);
    }
    // Walk the execution plan, coloring the graph with potential invocation sites
    int[] executing = new int[1024];
    int nextExecutionId = 0;
    for (final Map.Entry<Long, Pair<List<GraphFragment>, List<GraphFragment>>> eventEntry : concurrencyEvent.entrySet()) {
      final Pair<List<GraphFragment>, List<GraphFragment>> event = eventEntry.getValue();
      if (event.getSecond() != null) {
        for (final GraphFragment finishing : event.getSecond()) {
          // Decrement the concurrency count for the graph color
          executing[finishing.getExecutionId()]--;
        }
      }
      if (event.getFirst() != null) {
        eventFragment: for (final GraphFragment starting : event.getFirst()) { //CSIGNORE
          if (starting.getInputFragments().isEmpty()) {
            // No inputs, so we're a leaf node = new graph color
            starting.setExecutionId(nextExecutionId);
            executing[nextExecutionId] = 1;
            if ((++nextExecutionId) >= executing.length) {
              executing = Arrays.copyOf(executing, executing.length * 2);
            }
          } else if (starting.getInputFragments().size() == 1) {
            // Single input, become the tail with the same graph color if below the concurrency limit
            final GraphFragment tailOf = starting.getInputFragments().iterator().next();
            if (executing[tailOf.getExecutionId()] >= getMaximumConcurrency()) {
              // Concurrency limit exceeded so start a new color
              starting.setExecutionId(nextExecutionId);
              executing[nextExecutionId] = 1;
              if ((++nextExecutionId) >= executing.length) {
                executing = Arrays.copyOf(executing, executing.length * 2);
              }
            } else {
              // Below concurrency limit so use same color and add as tail
              tailOf.addTail(starting);
              starting.setExecutionId(tailOf.getExecutionId());
              executing[tailOf.getExecutionId()]++;
            }
          } else {
            final Iterator<GraphFragment> inputIterator = starting.getInputFragments().iterator();
            int nodeColour = inputIterator.next().getExecutionId();
            while (inputIterator.hasNext()) {
              final GraphFragment input = inputIterator.next();
              if (input.getExecutionId() != nodeColour) {
                // Inputs are from different colored graph fragments = new graph color
                starting.setExecutionId(nextExecutionId);
                executing[nextExecutionId] = 1;
                if ((++nextExecutionId) >= executing.length) {
                  executing = Arrays.copyOf(executing, executing.length * 2);
                }
                continue eventFragment;
              }
            }
            // Inputs are all from the same colored graph fragments = become tail with the same color if below concurrency limit
            if (executing[nodeColour] >= getMaximumConcurrency()) {
              // Concurrency limit exceeded so start a new color
              starting.setExecutionId(nextExecutionId);
              executing[nextExecutionId] = 1;
              if ((++nextExecutionId) >= executing.length) {
                executing = Arrays.copyOf(executing, executing.length * 2);
              }
            } else {
              // Below concurrency limit so use same color and add as tails
              starting.setExecutionId(nodeColour);
              executing[nodeColour]++;
              for (final GraphFragment input : starting.getInputFragments()) {
                input.addTail(starting);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Updates the shared value cache with details of any "private" values that, as a result of tail jobs, do not need to leave the calculation node.
   * 
   * @param context the operating context, not null
   * @param allFragments all discovered fragments, not null
   */
  private void exportPrivateValues(final GraphFragmentContext context, final Collection<GraphFragment> allFragments) {
    final Map<ValueSpecification, Boolean> sharedValues = context.getSharedCacheValues();
    for (GraphFragment fragment : allFragments) {
      fragment.exportPrivateValues(sharedValues);
    }
  }

  private GraphExecutionPlan createMultipleNodePlan(final DependencyGraph graph, final ExecutionLogModeSource logModeSource, final long functionInitializationId,
      final Set<ValueSpecification> sharedValues, final Map<ValueSpecification, FunctionParameters> parameters) {
    final GraphFragmentContext context = new GraphFragmentContext(graph.getCalculationConfigurationName(), logModeSource, functionInitializationId, sharedValues, parameters);
    context.setTerminalOutputs(DependencyGraphImpl.getTerminalOutputSpecifications(graph));
    FragmentGatherer gatherer = new FragmentGatherer(graph.getSize(), getFunctionCosts().getStatistics(graph.getCalculationConfigurationName()), sharedValues);
    final Set<GraphFragment> rootFragments = createGraphFragments(graph, gatherer);
    final Set<GraphFragment> allFragments = gatherer.getAllFragments();
    gatherer = null;
    int failCount = 0;
    do {
      if (mergeSharedInputs(rootFragments, allFragments)) {
        failCount = 0;
      } else {
        if (++failCount >= 2) {
          break;
        }
      }
      if (mergeSingleDependencies(context, allFragments)) {
        failCount = 0;
      } else {
        if (++failCount >= 2) {
          break;
        }
      }
    } while (true);
    findTailFragments(allFragments);
    exportPrivateValues(context, allFragments);
    long totalSize = 0;
    long totalInvocationCost = 0;
    long totalDataCost = 0;
    final Collection<PlannedJob> jobs = new LinkedList<PlannedJob>();
    for (GraphFragment fragment : allFragments) {
      final Collection<GraphFragment> inputs = fragment.getInputFragments();
      totalSize += fragment.getJobItems();
      totalInvocationCost += fragment.getInvocationCost();
      totalDataCost += fragment.getDataIOCost();
      if ((inputs != null) && inputs.isEmpty()) {
        jobs.add(fragment.getOrCreateJob(context));
      }
    }
    final int totalJobs = allFragments.size();
    return new GraphExecutionPlan(graph.getCalculationConfigurationName(), functionInitializationId, jobs, allFragments.size(), (double) totalSize / (double) totalJobs,
        (double) totalInvocationCost / (double) totalJobs, (double) totalDataCost / (double) totalJobs);
  }

  // GraphExecutionPlanner

  @Override
  public GraphExecutionPlan createPlan(final DependencyGraph graph, final ExecutionLogModeSource logModeSource, final long functionInitialisationId,
      final Set<ValueSpecification> sharedValues, final Map<ValueSpecification, FunctionParameters> parameters) {
    final OperationTimer timer = new OperationTimer(s_logger, "Creating execution plan for {}", graph);
    try {
      if (graph.getSize() <= getMinimumJobItems()) {
        // If the graph is too small, run it as-is
        return createSingleNodePlan(graph, logModeSource, functionInitialisationId, sharedValues, parameters);
      } else {
        // Split the graph into multiple fragments
        return createMultipleNodePlan(graph, logModeSource, functionInitialisationId, sharedValues, parameters);
      }
    } finally {
      timer.finished();
    }
  }

}
