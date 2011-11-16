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

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
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
 * This DependencyGraphExecutor executes the given dependency graph
 * on a number of calculation nodes.
 */
public class MultipleNodeExecutor implements DependencyGraphExecutor<Object> {

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

  protected GraphFragmentContext createContext(DependencyGraph graph, final BlockingQueue<CalculationJobResult> calcJobResultQueue) {
    return new GraphFragmentContext(this, graph, calcJobResultQueue);
  }

  protected RootGraphFragment createExecutionPlan(final DependencyGraph graph, final BlockingQueue<CalculationJobResult> calcJobResultQueue, final GraphExecutorStatisticsGatherer statistics) {
    final OperationTimer timer = new OperationTimer(s_logger, "Creating execution plan for {}", graph);
    final GraphFragmentContext context = createContext(graph, calcJobResultQueue);
    // writeGraphForTestingPurposes(graph);
    if (graph.getSize() <= getMinJobItems()) {
      // If the graph is too small, run it as-is
      final RootGraphFragment fragment = new RootGraphFragment(context, statistics, graph.getExecutionOrder());
      statistics.graphProcessed(graph.getCalculationConfigurationName(), 1, graph.getSize(), fragment.getJobInvocationCost(), Double.NaN);
      context.allocateFragmentMap(1);
      fragment.executeImpl();
      timer.finished();
      return fragment;
    }
    final Set<GraphFragment> allFragments = new HashSet<GraphFragment>((graph.getSize() * 4) / 3);
    final RootGraphFragment logicalRoot = new RootGraphFragment(context, statistics);
    for (GraphFragment root : graphToFragments(context, graph, allFragments)) {
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
    final Iterator<GraphFragment> fragmentIterator = allFragments.iterator();
    final int count = allFragments.size();
    int totalSize = 0;
    long totalInvocationCost = 0;
    long totalDataCost = 0;
    while (fragmentIterator.hasNext()) {
      final GraphFragment fragment = fragmentIterator.next();
      totalSize += fragment.getJobItems();
      totalInvocationCost += fragment.getJobInvocationCost();
      totalDataCost += fragment.getJobDataInputCost() + fragment.getJobDataOutputCost();
      if (!fragment.getInputFragments().isEmpty()) {
        fragment.initBlockCount();
        fragmentIterator.remove();
      }
    }
    statistics.graphProcessed(graph.getCalculationConfigurationName(), count, (double) totalSize / (double) count,
        (double) totalInvocationCost / (double) count, (double) totalDataCost / (double) count);
    // printFragment(logicalRoot);
    // Execute anything left (leaf nodes)
    for (GraphFragment fragment : allFragments) {
      fragment.executeImpl();
    }
    timer.finished();
    return logicalRoot;
  }

  private void executeLeafNodes(final GraphFragment fragment, final Set<GraphFragment> visited) {
    final Set<GraphFragment> inputs = fragment.getInputFragments();
    if (inputs.isEmpty()) {
      fragment.executeImpl();
    } else {
      for (GraphFragment input : inputs) {
        if (visited.add(input)) {
          executeLeafNodes(input, visited);
        }
      }
    }
  }

  @Override
  public Future<Object> execute(final DependencyGraph graph, final BlockingQueue<CalculationJobResult> calcJobResultQueue, final GraphExecutorStatisticsGatherer statistics) {
    final RootGraphFragment execution = _cache.getCachedExecutionPlan(graph);
    if (execution != null) {
      if (execution.getFunctionInitializationTimestamp() != getCycle().getFunctionInitId()) {
        s_logger.warn("Invalid cached execution plan for {} due to re-initialization", graph);
      } else {
        final Set<GraphFragment> visited = new HashSet<GraphFragment>();
        if (execution.reset(this, visited, calcJobResultQueue)) {
          s_logger.info("Using cached execution plan for {}", graph);
          visited.clear();
          executeLeafNodes(execution, visited);
          return execution;
        } else {
          s_logger.warn("Invalid cached execution plan for {}", graph);
        }
      }
    }
    return createExecutionPlan(graph, calcJobResultQueue, statistics);
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

  private Collection<GraphFragment> graphToFragments(final GraphFragmentContext context, final DependencyGraph graph, final Set<GraphFragment> allFragments) {
    final Map<DependencyNode, GraphFragment> node2fragment = new HashMap<DependencyNode, GraphFragment>();
    final Collection<DependencyNode> rootNodes = graph.getRootNodes();
    final Collection<GraphFragment> rootFragments = new ArrayList<GraphFragment>(rootNodes.size());
    graphToFragments(context, graph, rootFragments, node2fragment, rootNodes);
    allFragments.addAll(node2fragment.values());
    return rootFragments;
  }

  private void graphToFragments(final GraphFragmentContext context, final DependencyGraph graph, final Collection<GraphFragment> output, final Map<DependencyNode, GraphFragment> node2fragment,
      final Collection<DependencyNode> nodes) {
    // TODO Andrew 2010-09-02 -- Can we do this by iterating the graph nodes instead of walking the tree?
    for (DependencyNode node : nodes) {
      if (!graph.containsNode(node)) {
        continue;
      }
      GraphFragment fragment = node2fragment.get(node);
      if (fragment == null) {
        fragment = new GraphFragment(context, node);
        node2fragment.put(node, fragment);
        final Collection<DependencyNode> inputNodes = node.getInputNodes();
        if (!inputNodes.isEmpty()) {
          graphToFragments(context, graph, fragment.getInputFragments(), node2fragment, inputNodes);
          for (GraphFragment input : fragment.getInputFragments()) {
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
  private boolean mergeSharedInputs(final GraphFragment logicalRoot, final Set<GraphFragment> allFragments) {
    final Map<Set<GraphFragment>, GraphFragment> possibleCandidates = new HashMap<Set<GraphFragment>, GraphFragment>();
    // REVIEW 2010-08-27 Andrew -- Should we only create validCandidates when we're ready to use it?
    final Map<GraphFragment, GraphFragment> validCandidates = new HashMap<GraphFragment, GraphFragment>();
    boolean result = false;
    do {
      // Scan all fragments for possible merges
      for (GraphFragment fragment : allFragments) {
        if (fragment.getInputFragments().isEmpty()) {
          // No inputs to consider
          continue;
        }
        if ((fragment.getJobCost() >= getMinJobCost()) && (fragment.getJobItems() >= getMinJobItems())) {
          // We already meet the minimum requirement for the graph
          continue;
        }
        final GraphFragment mergeCandidate = possibleCandidates.get(fragment.getInputFragments());
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
      for (Map.Entry<GraphFragment, GraphFragment> merge : validCandidates.entrySet()) {
        final GraphFragment fragment = merge.getKey();
        final GraphFragment mergeCandidate = merge.getValue();
        mergeCandidate.appendFragment(fragment);
        // Merge candidate already has the correct inputs by definition
        for (GraphFragment dependency : fragment.getOutputFragments()) {
          dependency.getInputFragments().remove(fragment);
          if (mergeCandidate.getOutputFragments().add(dependency)) {
            dependency.getInputFragments().add(mergeCandidate);
          }
        }
        for (GraphFragment input : fragment.getInputFragments()) {
          input.getOutputFragments().remove(fragment);
        }
        allFragments.remove(fragment);
      }
      // If deep nodes have merged with "root" nodes then we need to kill the roots
      final Iterator<GraphFragment> fragmentIterator = logicalRoot.getInputFragments().iterator();
      while (fragmentIterator.hasNext()) {
        final GraphFragment fragment = fragmentIterator.next();
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
  private boolean mergeSingleDependencies(final Set<GraphFragment> allFragments) {
    int changes = 0;
    final Iterator<GraphFragment> fragmentIterator = allFragments.iterator();
    while (fragmentIterator.hasNext()) {
      final GraphFragment fragment = fragmentIterator.next();
      if (fragment.getOutputFragments().size() != 1) {
        continue;
      }
      final GraphFragment dependency = fragment.getOutputFragments().iterator().next();
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
      for (GraphFragment input : fragment.getInputFragments()) {
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
  private void findTailFragments(final Set<GraphFragment> allFragments) {
    // Estimate start times based on fragment costs and dependencies
    final NavigableMap<Long, Pair<List<GraphFragment>, List<GraphFragment>>> concurrencyEvent = new TreeMap<Long, Pair<List<GraphFragment>, List<GraphFragment>>>();
    for (GraphFragment fragment : allFragments) {
      Pair<List<GraphFragment>, List<GraphFragment>> event = concurrencyEvent.get(fragment.getStartTime());
      if (event == null) {
        event = Pair.of((List<GraphFragment>) new LinkedList<GraphFragment>(), null);
        concurrencyEvent.put(fragment.getStartTime(), event);
      } else {
        if (event.getFirst() == null) {
          event = Pair.of((List<GraphFragment>) new LinkedList<GraphFragment>(), event.getSecond());
          concurrencyEvent.put(fragment.getStartTime(), event);
        }
      }
      event.getFirst().add(fragment);
      event = concurrencyEvent.get(fragment.getStartTime() + fragment.getJobCost());
      if (event == null) {
        event = Pair.of(null, (List<GraphFragment>) new LinkedList<GraphFragment>());
        concurrencyEvent.put(fragment.getStartTime() + fragment.getJobCost(), event);
      } else {
        if (event.getSecond() == null) {
          event = Pair.of(event.getFirst(), (List<GraphFragment>) new LinkedList<GraphFragment>());
          concurrencyEvent.put(fragment.getStartTime() + fragment.getJobCost(), event);
        }
      }
      event.getSecond().add(fragment);
    }
    // Walk the execution plan, coloring the graph with potential invocation sites
    final Map<Integer, AtomicInteger> executing = new HashMap<Integer, AtomicInteger>();
    int nextExecutionId = 0;
    for (Map.Entry<Long, Pair<List<GraphFragment>, List<GraphFragment>>> eventEntry : concurrencyEvent.entrySet()) {
      final Pair<List<GraphFragment>, List<GraphFragment>> event = eventEntry.getValue();
      if (event.getSecond() != null) {
        for (GraphFragment finishing : event.getSecond()) {
          // Decrement the concurrency count for the graph color
          executing.get(finishing.getExecutionId()).decrementAndGet();
        }
      }
      if (event.getFirst() != null) {
        for (GraphFragment starting : event.getFirst()) {
          if (starting.getInputFragments().isEmpty()) {
            // No inputs, so we're a leaf node = new graph color
            nextExecutionId++;
            starting.setExecutionId(nextExecutionId);
            executing.put(nextExecutionId, new AtomicInteger(1));
          } else if (starting.getInputFragments().size() == 1) {
            // Single input, become the tail with the same graph color if below the concurrency limit
            final GraphFragment tailOf = starting.getInputFragments().iterator().next();
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
            final Iterator<GraphFragment> inputIterator = starting.getInputFragments().iterator();
            int nodeColour = inputIterator.next().getExecutionId();
            while (inputIterator.hasNext()) {
              final GraphFragment input = inputIterator.next();
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
                for (GraphFragment input : starting.getInputFragments()) {
                  input.addTail(starting);
                }
              }
            }
          }
        }
      }
    }
  }

  public void printFragment(final GraphFragment root) {
    printFragment("", Collections.singleton(root), new HashSet<GraphFragment>());
  }

  private void printFragment(final String indent, final Collection<GraphFragment> fragments, final Set<GraphFragment> printed) {
    if (indent.length() > 16) {
      return;
    }
    for (GraphFragment fragment : fragments) {
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
