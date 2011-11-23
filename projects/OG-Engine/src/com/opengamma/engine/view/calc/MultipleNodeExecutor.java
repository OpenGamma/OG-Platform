/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.engine.view.calcnode.stats.FunctionCosts;
import com.opengamma.util.Cancelable;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.Pair;

/**
 * This DependencyGraphExecutor executes the given dependency graph as a number of
 * dependent jobs suitable for a number of calculation nodes to exploit parallelism
 * from the dependency graph.
 */
public class MultipleNodeExecutor implements DependencyGraphExecutor<DependencyGraph> {

  private static final Logger s_logger = LoggerFactory.getLogger(MultipleNodeExecutor.class);

  private final SingleComputationCycle _cycle;
  private final int _minJobItems;
  private final int _maxJobItems;
  private final long _minJobCost;
  private final long _maxJobCost;
  private final int _maxConcurrency;
  private final FunctionCosts _functionCosts;
  private final ExecutionPlanCache _cache;

  protected MultipleNodeExecutor(final SingleComputationCycle cycle, final int minimumJobItems, final int maximumJobItems, final long minimumJobCost, final long maximumJobCost,
      final int maximumConcurrency, final FunctionCosts functionCosts, final ExecutionPlanCache cache) {
    // Don't check for null as the factory does this, plus for testing we don't have a cycle and override the methods that use it
    _cycle = cycle;
    _minJobItems = minimumJobItems;
    _maxJobItems = maximumJobItems;
    _minJobCost = minimumJobCost;
    _maxJobCost = maximumJobCost;
    _maxConcurrency = maximumConcurrency;
    _functionCosts = functionCosts;
    _cache = cache;
  }

  protected long getFunctionInitId() {
    return getCycle().getFunctionInitId();
  }

  protected SingleComputationCycle getCycle() {
    return _cycle;
  }

  protected ExecutionPlanCache getCache() {
    return _cache;
  }

  protected CalculationJobSpecification createJobSpecification(final DependencyGraph graph) {
    return new CalculationJobSpecification(getCycle().getUniqueId(), graph.getCalculationConfigurationName(), getCycle().getValuationTime(), JobIdSource.getId());
  }

  protected void addJobToViewProcessorQuery(final CalculationJobSpecification jobSpec, final DependencyGraph graph) {
    getCycle().getViewProcessContext().getViewProcessorQueryReceiver().addJob(jobSpec, graph);
  }

  protected Cancelable dispatchJob(final CalculationJob job, final JobResultReceiver jobResultReceiver) {
    return getCycle().getViewProcessContext().getComputationJobDispatcher().dispatchJob(job, jobResultReceiver);
  }

  protected void markExecuted(final DependencyNode node) {
    getCycle().markExecuted(node);
  }

  protected void markFailed(final DependencyNode node) {
    getCycle().markFailed(node);
  }

  protected CompleteGraphFragment executeSingleFragment(final MutableGraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics) {
    final Collection<DependencyNode> nodes = context.getGraph().getExecutionOrder();
    final CompleteGraphFragment fragment = new CompleteGraphFragment(context, statistics, nodes);
    long invocationCost = 0;
    for (DependencyNode node : nodes) {
      invocationCost += context.getFunctionStatistics(node.getFunction().getFunction()).getInvocationCost();
    }
    statistics.graphProcessed(context.getGraph().getCalculationConfigurationName(), 1, context.getGraph().getSize(), invocationCost, Double.NaN);
    context.allocateFragmentMap(1);
    final Set<ValueSpecification> sharedValues = new HashSet<ValueSpecification>(context.getGraph().getTerminalOutputSpecifications());
    final Set<ValueSpecification> privateValues = new HashSet<ValueSpecification>();
    for (DependencyNode node : nodes) {
      for (ValueSpecification output : node.getOutputValues()) {
        if (!sharedValues.contains(output)) {
          privateValues.add(output);
        }
      }
      for (ValueSpecification input : node.getInputValues()) {
        if (!privateValues.contains(input)) {
          sharedValues.add(input);
        }
      }
    }
    if (sharedValues.size() < privateValues.size()) {
      fragment.setCacheSelectHint(CacheSelectHint.sharedValues(sharedValues));
    } else {
      fragment.setCacheSelectHint(CacheSelectHint.privateValues(privateValues));
    }
    fragment.execute();
    return fragment;
  }

  protected MutableGraphFragment.Root executeMultipleFragments(final MutableGraphFragmentContext context, final GraphExecutorStatisticsGatherer statistics) {
    final Set<MutableGraphFragment> allFragments = Sets.newHashSetWithExpectedSize(context.getGraph().getSize());
    final MutableGraphFragment.Root logicalRoot = new MutableGraphFragment.Root(context, statistics);
    for (MutableGraphFragment root : graphToFragments(context, context.getGraph(), allFragments)) {
      root.getOutputFragments().add(logicalRoot);
      logicalRoot.getInputFragments().add(root);
    }
    int failCount = 0;
    do {
      if (mergeSharedInputs(logicalRoot, allFragments)) {
        failCount = 0;
      } else {
        if (++failCount >= 2) {
          break;
        }
      }
      if (mergeSingleDependencies(allFragments)) {
        failCount = 0;
      } else {
        if (++failCount >= 2) {
          break;
        }
      }
    } while (true);
    findTailFragments(allFragments);
    context.allocateFragmentMap(allFragments.size());
    // Set block counts on non-leaf nodes & leave only the leaves in the set
    logicalRoot.initBlockCount();
    final Iterator<MutableGraphFragment> fragmentIterator = allFragments.iterator();
    final int count = allFragments.size();
    int totalSize = 0;
    long totalInvocationCost = 0;
    long totalDataCost = 0;
    while (fragmentIterator.hasNext()) {
      final MutableGraphFragment fragment = fragmentIterator.next();
      totalSize += fragment.getJobItems();
      totalInvocationCost += fragment.getJobInvocationCost();
      totalDataCost += fragment.getJobDataInputCost() + fragment.getJobDataOutputCost();
      if (!fragment.getInputFragments().isEmpty()) {
        fragment.initBlockCount();
        fragmentIterator.remove();
      }
    }
    statistics.graphProcessed(context.getGraph().getCalculationConfigurationName(), count, (double) totalSize / (double) count,
        (double) totalInvocationCost / (double) count, (double) totalDataCost / (double) count);
    // printFragment(logicalRoot);
    // Execute anything left (leaf nodes)
    for (MutableGraphFragment fragment : allFragments) {
      fragment.execute();
    }
    return logicalRoot;
  }

  /**
   * Partitions the graph and starts it executing. The future returned corresponds to the whole graph. Once an execution plan is built
   * it is cached for future use.
   * 
   * @param graph the graph to execute, not null
   * @param calcJobResultQueue a queue to feed intermediate job result notifications to, not null
   * @param statistics the statistics reporter, not null
   * @return a future that indicates complete execution of the graph
   */
  protected Future<DependencyGraph> executeImpl(final DependencyGraph graph, final BlockingQueue<CalculationJobResult> calcJobResultQueue, final GraphExecutorStatisticsGatherer statistics) {
    final OperationTimer timer = new OperationTimer(s_logger, "Creating execution plan for {}", graph);
    final MutableGraphFragmentContext context = new MutableGraphFragmentContext(this, graph, calcJobResultQueue);
    // writeGraphForTestingPurposes(graph);
    if (graph.getSize() <= getMinJobItems()) {
      // If the graph is too small, run it as-is
      final CompleteGraphFragment fragment = executeSingleFragment(context, statistics);
      timer.finished();
      getCache().cachePlan(context.getGraph(), context.getFunctionInitId(), ExecutionPlan.of(fragment));
      return fragment.getFuture();
    } else {
      final MutableGraphFragment.Root fragment = executeMultipleFragments(context, statistics);
      timer.finished();
      // Note: don't cache the plan now as the "tails" and "hints" aren't produced until they are needed for dispatch
      return fragment.getFuture();
    }
  }

  /**
   * @param graph the graph to execute, not null
   * @param calcJobResultQueue the queue to report partial results to, not null
   * @param statistics the statistics gathering object, not null
   * @return a future that the caller can block on until the execution is complete. The future holds the graph (as passed to this method) that has been executed
   */
  @Override
  public Future<DependencyGraph> execute(final DependencyGraph graph, final BlockingQueue<CalculationJobResult> calcJobResultQueue, final GraphExecutorStatisticsGatherer statistics) {
    final ExecutionPlan plan = getCache().getCachedPlan(graph, getCycle().getFunctionInitId());
    if (plan != null) {
      s_logger.info("Using cached execution plan for {}", graph);
      return plan.run(new GraphFragmentContext(this, graph, calcJobResultQueue), statistics);
    } else {
      s_logger.debug("Creating new execution plan for {}", graph);
      return executeImpl(graph, calcJobResultQueue, statistics);
    }
  }

  public int getMinJobItems() {
    return _minJobItems;
  }

  public int getMaxJobItems() {
    return _maxJobItems;
  }

  public long getMinJobCost() {
    return _minJobCost;
  }

  public long getMaxJobCost() {
    return _maxJobCost;
  }

  public int getMaxConcurrency() {
    return _maxConcurrency;
  }

  public FunctionCosts getFunctionCosts() {
    return _functionCosts;
  }

  private Collection<MutableGraphFragment> graphToFragments(final MutableGraphFragmentContext context, final DependencyGraph graph, final Set<MutableGraphFragment> allFragments) {
    final Map<DependencyNode, MutableGraphFragment> node2fragment = new HashMap<DependencyNode, MutableGraphFragment>();
    final Collection<DependencyNode> rootNodes = graph.getRootNodes();
    final Collection<MutableGraphFragment> rootFragments = new ArrayList<MutableGraphFragment>(rootNodes.size());
    graphToFragments(context, graph, rootFragments, node2fragment, rootNodes);
    allFragments.addAll(node2fragment.values());
    return rootFragments;
  }

  private void graphToFragments(final MutableGraphFragmentContext context, final DependencyGraph graph, final Collection<MutableGraphFragment> output,
      final Map<DependencyNode, MutableGraphFragment> node2fragment, final Collection<DependencyNode> nodes) {
    // TODO Andrew 2010-09-02 -- Can we do this by iterating the graph nodes instead of walking the tree?
    for (DependencyNode node : nodes) {
      if (!graph.containsNode(node)) {
        continue;
      }
      MutableGraphFragment fragment = node2fragment.get(node);
      if (fragment == null) {
        fragment = new MutableGraphFragment(context, node);
        node2fragment.put(node, fragment);
        final Collection<DependencyNode> inputNodes = node.getInputNodes();
        if (!inputNodes.isEmpty()) {
          graphToFragments(context, graph, fragment.getInputFragments(), node2fragment, inputNodes);
          for (MutableGraphFragment input : fragment.getInputFragments()) {
            input.getOutputFragments().add(fragment);
          }
        }
      }
      output.add(fragment);
    }
  }

  /**
   * Finds pairs of nodes with the same input set (i.e. that would execute concurrently) that are below the minimum job size
   * and merge them together.
   */
  private boolean mergeSharedInputs(final MutableGraphFragment logicalRoot, final Set<MutableGraphFragment> allFragments) {
    final Map<Set<MutableGraphFragment>, MutableGraphFragment> possibleCandidates = new HashMap<Set<MutableGraphFragment>, MutableGraphFragment>();
    // REVIEW 2010-08-27 Andrew -- Should we only create validCandidates when we're ready to use it?
    final Map<MutableGraphFragment, MutableGraphFragment> validCandidates = new HashMap<MutableGraphFragment, MutableGraphFragment>();
    boolean result = false;
    do {
      // Scan all fragments for possible merges
      for (MutableGraphFragment fragment : allFragments) {
        if (fragment.getInputFragments().isEmpty()) {
          // No inputs to consider
          continue;
        }
        if ((fragment.getJobCost() >= getMinJobCost()) && (fragment.getJobItems() >= getMinJobItems())) {
          // We already meet the minimum requirement for the graph
          continue;
        }
        final MutableGraphFragment mergeCandidate = possibleCandidates.get(fragment.getInputFragments());
        if (mergeCandidate != null) {
          if (mergeCandidate.canAppendFragment(fragment, getMaxJobItems(), getMaxJobCost())) {
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
      for (Map.Entry<MutableGraphFragment, MutableGraphFragment> merge : validCandidates.entrySet()) {
        final MutableGraphFragment fragment = merge.getKey();
        final MutableGraphFragment mergeCandidate = merge.getValue();
        mergeCandidate.appendFragment(fragment);
        // Merge candidate already has the correct inputs by definition
        for (MutableGraphFragment dependency : fragment.getOutputFragments()) {
          dependency.getInputFragments().remove(fragment);
          if (mergeCandidate.getOutputFragments().add(dependency)) {
            dependency.getInputFragments().add(mergeCandidate);
          }
        }
        for (MutableGraphFragment input : fragment.getInputFragments()) {
          input.getOutputFragments().remove(fragment);
        }
        allFragments.remove(fragment);
      }
      // If deep nodes have merged with "root" nodes then we need to kill the roots
      final Iterator<MutableGraphFragment> fragmentIterator = logicalRoot.getInputFragments().iterator();
      while (fragmentIterator.hasNext()) {
        final MutableGraphFragment fragment = fragmentIterator.next();
        if (fragment.getOutputFragments().size() > 1) {
          fragment.getOutputFragments().remove(logicalRoot);
          fragmentIterator.remove();
        }
      }
      validCandidates.clear();
      possibleCandidates.clear();
      result = true;
    } while (true);
  }

  /**
   * If a fragment has only one dependency, and both it and its dependent are below the
   * maximum job size they are merged.
   */
  private boolean mergeSingleDependencies(final Set<MutableGraphFragment> allFragments) {
    int changes = 0;
    final Iterator<MutableGraphFragment> fragmentIterator = allFragments.iterator();
    while (fragmentIterator.hasNext()) {
      final MutableGraphFragment fragment = fragmentIterator.next();
      if (fragment.getOutputFragments().size() != 1) {
        continue;
      }
      final MutableGraphFragment dependency = fragment.getOutputFragments().iterator().next();
      if (dependency.getNodes().isEmpty()) {
        // Ignore the roots
        continue;
      }
      if (!dependency.canPrependFragment(fragment, getMaxJobItems(), getMaxJobCost())) {
        // Can't merge
        continue;
      }
      // Merge fragment with it's dependency and slice it out of the graph
      dependency.prependFragment(fragment);
      fragmentIterator.remove();
      dependency.getInputFragments().remove(fragment);
      for (MutableGraphFragment input : fragment.getInputFragments()) {
        dependency.getInputFragments().add(input);
        input.getOutputFragments().remove(fragment);
        input.getOutputFragments().add(dependency);
      }
      changes++;
    }
    return changes > 0;
  }

  /**
   * If a fragment has only a single input, it can be a tail to the fragment generating that input. A fragment with multiple inputs can
   * be a tail to all of them iff they are tails to a common fragment (i.e. all will end up at the same node).
   */
  private void findTailFragments(final Set<MutableGraphFragment> allFragments) {
    // Estimate start times based on fragment costs and dependencies
    final NavigableMap<Long, Pair<List<MutableGraphFragment>, List<MutableGraphFragment>>> concurrencyEvent = new TreeMap<Long, Pair<List<MutableGraphFragment>, List<MutableGraphFragment>>>();
    for (MutableGraphFragment fragment : allFragments) {
      Pair<List<MutableGraphFragment>, List<MutableGraphFragment>> event = concurrencyEvent.get(fragment.getStartTime());
      if (event == null) {
        event = Pair.of((List<MutableGraphFragment>) new LinkedList<MutableGraphFragment>(), null);
        concurrencyEvent.put(fragment.getStartTime(), event);
      } else {
        if (event.getFirst() == null) {
          event = Pair.of((List<MutableGraphFragment>) new LinkedList<MutableGraphFragment>(), event.getSecond());
          concurrencyEvent.put(fragment.getStartTime(), event);
        }
      }
      event.getFirst().add(fragment);
      event = concurrencyEvent.get(fragment.getStartTime() + fragment.getJobCost());
      if (event == null) {
        event = Pair.of(null, (List<MutableGraphFragment>) new LinkedList<MutableGraphFragment>());
        concurrencyEvent.put(fragment.getStartTime() + fragment.getJobCost(), event);
      } else {
        if (event.getSecond() == null) {
          event = Pair.of(event.getFirst(), (List<MutableGraphFragment>) new LinkedList<MutableGraphFragment>());
          concurrencyEvent.put(fragment.getStartTime() + fragment.getJobCost(), event);
        }
      }
      event.getSecond().add(fragment);
    }
    // Walk the execution plan, coloring the graph with potential invocation sites
    final Map<Integer, AtomicInteger> executing = new HashMap<Integer, AtomicInteger>();
    int nextExecutionId = 0;
    for (Map.Entry<Long, Pair<List<MutableGraphFragment>, List<MutableGraphFragment>>> eventEntry : concurrencyEvent.entrySet()) {
      final Pair<List<MutableGraphFragment>, List<MutableGraphFragment>> event = eventEntry.getValue();
      if (event.getSecond() != null) {
        for (MutableGraphFragment finishing : event.getSecond()) {
          // Decrement the concurrency count for the graph color
          executing.get(finishing.getExecutionId()).decrementAndGet();
        }
      }
      if (event.getFirst() != null) {
        for (MutableGraphFragment starting : event.getFirst()) {
          if (starting.getInputFragments().isEmpty()) {
            // No inputs, so we're a leaf node = new graph color
            nextExecutionId++;
            starting.setExecutionId(nextExecutionId);
            executing.put(nextExecutionId, new AtomicInteger(1));
          } else if (starting.getInputFragments().size() == 1) {
            // Single input, become the tail with the same graph color if below the concurrency limit
            final MutableGraphFragment tailOf = starting.getInputFragments().iterator().next();
            final AtomicInteger concurrency = executing.get(tailOf.getExecutionId());
            if (concurrency.get() >= getMaxConcurrency()) {
              // Concurrency limit exceeded so start a new color
              nextExecutionId++;
              starting.setExecutionId(nextExecutionId);
              executing.put(nextExecutionId, new AtomicInteger(1));
            } else {
              // Below concurrency limit so use same color and add as tail
              tailOf.addTail(starting);
              starting.setExecutionId(tailOf.getExecutionId());
              concurrency.incrementAndGet();
            }
          } else {
            final Iterator<MutableGraphFragment> inputIterator = starting.getInputFragments().iterator();
            int nodeColour = inputIterator.next().getExecutionId();
            while (inputIterator.hasNext()) {
              final MutableGraphFragment input = inputIterator.next();
              if (input.getExecutionId() != nodeColour) {
                // Inputs are from different colored graph fragments = new graph color
                nextExecutionId++;
                starting.setExecutionId(nextExecutionId);
                executing.put(nextExecutionId, new AtomicInteger(1));
                nodeColour = -1;
                break;
              }
            }
            if (nodeColour > 0) {
              // Inputs are all from the same colored graph fragments = become tail with the same color if below concurrency limit
              final AtomicInteger concurrency = executing.get(nodeColour);
              if (concurrency.get() >= getMaxConcurrency()) {
                // Concurrency limit exceeded so start a new color
                nextExecutionId++;
                starting.setExecutionId(nextExecutionId);
                executing.put(nextExecutionId, new AtomicInteger(1));
              } else {
                // Below concurrency limit so use same color and add as tails
                starting.setExecutionId(nodeColour);
                concurrency.incrementAndGet();
                for (MutableGraphFragment input : starting.getInputFragments()) {
                  input.addTail(starting);
                }
              }
            }
          }
        }
      }
    }
  }

  public void printFragment(final GraphFragment<?, ?> root) {
    printFragment("", Collections.singleton(root), new HashSet<GraphFragment<?, ?>>());
  }

  private void printFragment(final String indent, final Collection<? extends GraphFragment<?, ?>> fragments, final Set<GraphFragment<?, ?>> printed) {
    if (indent.length() > 16) {
      return;
    }
    for (GraphFragment<?, ?> fragment : fragments) {
      /*
       * if (!printed.add(fragment)) {
       * System.out.println(indent + " Fragments " + fragment.fragmentList());
       * continue;
       * }
       */
      System.out.println(indent + " " + fragment);
      if (!fragment.getInputFragments().isEmpty()) {
        printFragment(indent + "  ", fragment.getInputFragments(), printed);
      }
    }
  }

}
