/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.util.tuple.Pair;

/**
 * This DependencyGraphExecutor executes the given dependency graph
 * on a number of calculation nodes.
 */
public class MultipleNodeExecutor implements DependencyGraphExecutor<Object> {

  private static final Runnable NO_OP = new Runnable() {
    @Override
    public void run() {
    }
  };

  private final SingleComputationCycle _cycle;
  private final int _minJobItems;
  private final int _maxJobItems;
  private final int _minJobCost;
  private final int _maxJobCost;
  private final int _maxConcurrency;

  protected MultipleNodeExecutor(final SingleComputationCycle cycle, final int minimumJobItems, final int maximumJobItems, final int minimumJobCost, final int maximumJobCost,
      final int maximumConcurrency) {
    // Don't check for null as the factory does this, plus for testing we don't have a cycle and override the methods that use it
    _cycle = cycle;
    _minJobItems = minimumJobItems;
    _maxJobItems = maximumJobItems;
    _minJobCost = minimumJobCost;
    _maxJobCost = maximumJobCost;
    _maxConcurrency = maximumConcurrency;
  }

  protected CalculationJobSpecification createJobSpecification(final DependencyGraph graph) {
    return new CalculationJobSpecification(_cycle.getViewName(), graph.getCalcConfName(), _cycle.getValuationTime().toEpochMillisLong(), JobIdSource.getId());
  }

  protected void addJobToViewProcessorQuery(final CalculationJobSpecification jobSpec, final DependencyGraph graph) {
    _cycle.getProcessingContext().getViewProcessorQueryReceiver().addJob(jobSpec, graph);
  }

  protected void dispatchJob(final CalculationJob job, final JobResultReceiver jobResultReceiver) {
    _cycle.getProcessingContext().getComputationJobDispatcher().dispatchJob(job, jobResultReceiver);
  }

  private final AtomicInteger _graphFragmentIdentifiers = new AtomicInteger();

  /* package */class GraphFragment implements JobResultReceiver {

    private final int _graphFragmentIdentifier = _graphFragmentIdentifiers.incrementAndGet();

    private final LinkedList<DependencyNode> _nodes = new LinkedList<DependencyNode>();
    private final Set<GraphFragment> _inputs = new HashSet<GraphFragment>();
    private final Set<GraphFragment> _dependencies = new HashSet<GraphFragment>();
    private DependencyGraph _graph;
    private Map<CalculationJobItem, DependencyNode> _item2Node;
    private AtomicInteger _blockCount;

    private int _startTime;
    private int _startTimeCache;
    private int _cycleCost;

    public GraphFragment() {
    }

    public GraphFragment(final DependencyNode node) {
      _nodes.add(node);
      // TODO [ENG-201] this should be some metric relating to the computational overhead of the function
      _cycleCost = 1;
    }

    public GraphFragment(final Collection<DependencyNode> nodes) {
      _nodes.addAll(nodes);
    }

    public Collection<DependencyNode> getNodes() {
      return _nodes;
    }

    public void initBlockCount() {
      _blockCount = new AtomicInteger(_inputs.size());
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

    public int getJobCost() {
      return _cycleCost;
    }

    public int getStartTime(final int startTimeCache) {
      if (startTimeCache == _startTimeCache) {
        return _startTime;
      }
      _startTimeCache = startTimeCache;
      int latest = 0;
      for (GraphFragment input : _inputs) {
        final int finish = input.getStartTime(startTimeCache) + input._cycleCost;
        if (finish > latest) {
          latest = finish;
        }
      }
      _startTime = latest;
      return latest;
    }

    public void prependFragment(final GraphFragment fragment) {
      final Iterator<DependencyNode> nodeIterator = fragment._nodes.descendingIterator();
      while (nodeIterator.hasNext()) {
        _nodes.addFirst(nodeIterator.next());
      }
      _cycleCost += fragment._cycleCost;
    }

    public void appendFragment(final GraphFragment fragment) {
      _nodes.addAll(fragment._nodes);
      _cycleCost += fragment._cycleCost;
    }

    public void inputCompleted(final DependencyGraph graph) {
      final int blockCount = _blockCount.decrementAndGet();
      if (blockCount == 0) {
        execute(graph);
        // Help out the GC - we don't need these any more
        _blockCount = null;
        _inputs.clear();
      }
    }

    public void executeImpl(final DependencyGraph graph) {
      _graph = graph;
      final CalculationJobSpecification jobSpec = createJobSpecification(graph);
      final List<CalculationJobItem> items = new ArrayList<CalculationJobItem>();
      _item2Node = new HashMap<CalculationJobItem, DependencyNode>();
      final Set<ValueSpecification> privateValues = new HashSet<ValueSpecification>();
      final Set<ValueSpecification> sharedValues = new HashSet<ValueSpecification>(graph.getTerminalOutputValues());
      for (DependencyNode node : _nodes) {
        final Set<ValueSpecification> inputs = node.getInputValues();
        CalculationJobItem jobItem = new CalculationJobItem(node.getFunction().getFunction().getUniqueIdentifier(), node.getFunction().getParameters(), node.getComputationTarget().toSpecification(),
            inputs, node.getOutputRequirements());
        items.add(jobItem);
        _item2Node.put(jobItem, node);
        // If node has dependencies which AREN'T in the graph fragment, its outputs for those nodes are "shared" values
        for (ValueSpecification specification : node.getOutputValues()) {
          if (sharedValues.contains(specification)) {
            continue;
          }
          boolean isPrivate = true;
          for (DependencyNode dependent : node.getDependentNodes()) {
            if (!_nodes.contains(dependent)) {
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
      addJobToViewProcessorQuery(jobSpec, graph);
      dispatchJob(new CalculationJob(jobSpec, items, cacheHint), this);
    }

    public void execute(final DependencyGraph graph) {
      executeImpl(graph);
    }

    @Override
    public String toString() {
      return _graphFragmentIdentifier + ": " + _nodes.size() + " dep. node(s), earliestStart=" + _startTime + ", executionCost=" + _cycleCost;
    }

    @Override
    public void resultReceived(final CalculationJobResult result) {
      // Mark nodes as good or bad
      for (CalculationJobResultItem item : result.getResultItems()) {
        DependencyNode node = _item2Node.get(item.getItem());
        if (node == null) {
          continue;
        }
        _cycle.markExecuted(node);

        if (item.failed()) {
          _cycle.markFailed(node);
        }
      }
      // Release tree fragments up the tree
      for (GraphFragment dependent : _dependencies) {
        dependent.inputCompleted(_graph);
      }
      // Release memory we don't need any more
      _item2Node = null;
      _graph = null;
    }

  }

  /* package */class RootGraphFragment extends GraphFragment {

    private final FutureTask<Object> _future = new FutureTask<Object>(NO_OP, null);

    public RootGraphFragment() {
    }

    public RootGraphFragment(final Collection<DependencyNode> nodes) {
      super(nodes);
    }

    @Override
    public void execute(final DependencyGraph graph) {
      _future.run();
    }

    /**
     * Only gets called if this was the only node created because the dep graph was
     * too small.
     */
    @Override
    public void resultReceived(final CalculationJobResult result) {
      super.resultReceived(result);
      execute(null);
    }

  }

  protected RootGraphFragment executeImpl(final DependencyGraph graph) {
    // writeGraphForTestingPurposes(graph);
    if (graph.getSize() <= getMinJobItems()) {
      // If the graph is too small, run it as-is
      final RootGraphFragment fragment = new RootGraphFragment(graph.getExecutionOrder());
      fragment.executeImpl(graph);
      return fragment;
    }
    final Set<GraphFragment> allFragments = new HashSet<GraphFragment>((graph.getSize() * 4) / 3);
    final RootGraphFragment logicalRoot = new RootGraphFragment();
    for (GraphFragment root : graphToFragments(graph, allFragments)) {
      root.getDependencies().add(logicalRoot);
      logicalRoot.getInputs().add(root);
    }
    int failCount = 0;
    do {
      if (mergeSharedInputs(logicalRoot, allFragments)) {
        failCount = 0;
      } else {
        if (++failCount >= 3) {
          break;
        }
      }
      if (mergeSingleDependencies(allFragments)) {
        failCount = 0;
      } else {
        if (++failCount >= 3) {
          break;
        }
      }
      if (reduceConcurrency(logicalRoot, allFragments)) {
        failCount = 0;
      } else {
        if (++failCount >= 3) {
          break;
        }
      }
    } while (true);
    // Set block counts on non-leaf nodes
    logicalRoot.initBlockCount();
    final Iterator<GraphFragment> fragmentIterator = allFragments.iterator();
    while (fragmentIterator.hasNext()) {
      final GraphFragment fragment = fragmentIterator.next();
      if (!fragment.getInputs().isEmpty()) {
        fragment.initBlockCount();
        fragmentIterator.remove();
      }
    }
    // Execute anything left (leaf nodes)
    for (GraphFragment fragment : allFragments) {
      fragment.execute(graph);
    }
    return logicalRoot;
  }

  @Override
  public Future<Object> execute(final DependencyGraph graph, final GraphExecutorStatisticsGatherer statistics) {
    return executeImpl(graph)._future;
  }

  public int getMinJobItems() {
    return _minJobItems;
  }

  public int getMaxJobItems() {
    return _maxJobItems;
  }

  public int getMinJobCost() {
    return _minJobCost;
  }

  public int getMaxJobCost() {
    return _maxJobCost;
  }

  public int getMaxConcurrency() {
    return _maxConcurrency;
  }

  /*
   * private void writeGraphForTestingPurposes(final DependencyGraph graph) {
   * try {
   * final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("/tmp/graph.bin"));
   * out.writeObject(graph);
   * out.close();
   * System.exit(1);
   * } catch (IOException e) {
   * e.printStackTrace();
   * }
   * }
   */

  private Collection<GraphFragment> graphToFragments(final DependencyGraph graph, final Set<GraphFragment> allFragments) {
    final Map<DependencyNode, GraphFragment> node2fragment = new HashMap<DependencyNode, GraphFragment>();
    final Collection<DependencyNode> rootNodes = graph.getRootNodes();
    final Collection<GraphFragment> rootFragments = new ArrayList<GraphFragment>(rootNodes.size());
    graphToFragments(graph, rootFragments, node2fragment, rootNodes);
    allFragments.addAll(node2fragment.values());
    return rootFragments;
  }

  private void graphToFragments(final DependencyGraph graph, final Collection<GraphFragment> output, final Map<DependencyNode, GraphFragment> node2fragment, final Collection<DependencyNode> nodes) {
    for (DependencyNode node : nodes) {
      if (!graph.containsNode(node)) {
        continue;
      }
      GraphFragment fragment = node2fragment.get(node);
      if (fragment == null) {
        fragment = new GraphFragment(node);
        node2fragment.put(node, fragment);
        final Collection<DependencyNode> inputNodes = node.getInputNodes();
        if (!inputNodes.isEmpty()) {
          graphToFragments(graph, fragment.getInputs(), node2fragment, inputNodes);
          for (GraphFragment input : fragment.getInputs()) {
            input.getDependencies().add(fragment);
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
        if (fragment.getInputs().isEmpty()) {
          // No inputs to consider
          continue;
        }
        if ((fragment.getJobCost() >= getMinJobCost()) && (fragment.getJobItems() >= getMinJobItems())) {
          // We already meet the minimum requirement for the graph
          continue;
        }
        final GraphFragment mergeCandidate = possibleCandidates.get(fragment.getInputs());
        if (mergeCandidate != null) {
          if ((mergeCandidate.getJobCost() + fragment.getJobCost() <= getMaxJobCost()) && (mergeCandidate.getJobItems() + fragment.getJobItems() <= getMaxJobItems())) {
            // Defer the merge because we're iterating through the dependent's inputs at the moment
            validCandidates.put(fragment, mergeCandidate);
            // Stop using the merge candidate
            possibleCandidates.remove(fragment.getInputs());
            continue;
          }
          if (fragment.getJobCost() >= mergeCandidate.getJobCost()) {
            // We are a worse possible candidate as we're already more expensive
            continue;
          }
        }
        possibleCandidates.put(fragment.getInputs(), fragment);
      }
      if (validCandidates.isEmpty()) {
        return result;
      }
      for (Map.Entry<GraphFragment, GraphFragment> merge : validCandidates.entrySet()) {
        final GraphFragment fragment = merge.getKey();
        final GraphFragment mergeCandidate = merge.getValue();
        mergeCandidate.appendFragment(fragment);
        // Merge candidate already has the correct inputs by definition
        for (GraphFragment dependency : fragment.getDependencies()) {
          dependency.getInputs().remove(fragment);
          if (mergeCandidate.getDependencies().add(dependency)) {
            dependency.getInputs().add(mergeCandidate);
          }
        }
        for (GraphFragment input : fragment.getInputs()) {
          input.getDependencies().remove(fragment);
        }
        allFragments.remove(fragment);
      }
      // If deep nodes have merged with "root" nodes then we need to kill the roots
      final Iterator<GraphFragment> fragmentIterator = logicalRoot.getInputs().iterator();
      while (fragmentIterator.hasNext()) {
        final GraphFragment fragment = fragmentIterator.next();
        if (fragment.getDependencies().size() > 1) {
          fragment.getDependencies().remove(logicalRoot);
          fragmentIterator.remove();
        }
      }
      validCandidates.clear();
      possibleCandidates.clear();
      result = true;
    } while (true);
  }

  /**
   * Traverses the tree from root to leaves. If a fragment has only one dependency, and both it and
   * its dependent are below the minimum job size they are merged.
   */
  private boolean mergeSingleDependencies(final Set<GraphFragment> allFragments) {
    int changes = 0;
    final Iterator<GraphFragment> fragmentIterator = allFragments.iterator();
    while (fragmentIterator.hasNext()) {
      final GraphFragment fragment = fragmentIterator.next();
      if (fragment.getDependencies().size() != 1) {
        continue;
      }
      final GraphFragment dependency = fragment.getDependencies().iterator().next();
      if (dependency.getNodes().isEmpty()) {
        // Ignore the roots
        continue;
      }
      if ((fragment.getJobItems() + dependency.getJobItems() > getMaxJobItems()) || (fragment.getJobCost() + dependency.getJobCost() > getMaxJobCost())) {
        // Can't merge
        continue;
      }
      // Merge fragment with it's dependency and slice it out of the graph
      dependency.prependFragment(fragment);
      fragmentIterator.remove();
      dependency.getInputs().remove(fragment);
      for (GraphFragment input : fragment.getInputs()) {
        dependency.getInputs().add(input);
        input.getDependencies().remove(fragment);
        input.getDependencies().add(dependency);
      }
      changes++;
    }
    return changes > 0;
  }

  /**
   * If max concurrency is less than Integer.MAX_VALUE, any nodes that would execute concurrently above
   * this limit are merged if possible within the maximum job size constraint.
   */
  private boolean reduceConcurrency(final GraphFragment logicalRoot, final Set<GraphFragment> allFragments) {
    if (getMaxConcurrency() == Integer.MAX_VALUE) {
      return false;
    }
    final NavigableMap<Integer, Pair<List<GraphFragment>, List<GraphFragment>>> concurrencyEvent = new TreeMap<Integer, Pair<List<GraphFragment>, List<GraphFragment>>>();
    final int cacheKey = allFragments.size(); // Any changes to the graph reduce this, so we use it to cache the start time
    for (GraphFragment fragment : allFragments) {
      Pair<List<GraphFragment>, List<GraphFragment>> event = concurrencyEvent.get(fragment.getStartTime(cacheKey));
      if (event == null) {
        event = Pair.of((List<GraphFragment>) new LinkedList<GraphFragment>(), null);
        concurrencyEvent.put(fragment.getStartTime(cacheKey), event);
      } else {
        if (event.getFirst() == null) {
          event = Pair.of((List<GraphFragment>) new LinkedList<GraphFragment>(), event.getSecond());
          concurrencyEvent.put(fragment.getStartTime(cacheKey), event);
        }
      }
      event.getFirst().add(fragment);
      event = concurrencyEvent.get(fragment.getStartTime(cacheKey) + fragment.getJobCost());
      if (event == null) {
        event = Pair.of(null, (List<GraphFragment>) new LinkedList<GraphFragment>());
        concurrencyEvent.put(fragment.getStartTime(cacheKey) + fragment.getJobCost(), event);
      } else {
        if (event.getSecond() == null) {
          event = Pair.of(event.getFirst(), (List<GraphFragment>) new LinkedList<GraphFragment>());
          concurrencyEvent.put(fragment.getStartTime(cacheKey) + fragment.getJobCost(), event);
        }
      }
      event.getSecond().add(fragment);
    }
    final Set<GraphFragment> executing = new HashSet<GraphFragment>();
    int changes = 0;
    for (Map.Entry<Integer, Pair<List<GraphFragment>, List<GraphFragment>>> eventEntry : concurrencyEvent.entrySet()) {
      final Pair<List<GraphFragment>, List<GraphFragment>> event = eventEntry.getValue();
      if (event.getFirst() != null) {
        executing.addAll(event.getFirst());
      }
      if (event.getSecond() != null) {
        executing.removeAll(event.getSecond());
      }
      int displace = executing.size() - getMaxConcurrency();
      if (displace > 0) {
        // TODO should sort the fragments by lowest cost
        final Iterator<GraphFragment> fragmentIterator = executing.iterator();
        GraphFragment mergeCandidate = fragmentIterator.next();
        while (fragmentIterator.hasNext()) {
          final GraphFragment fragment = fragmentIterator.next();
          if ((mergeCandidate.getJobCost() + fragment.getJobCost() <= getMaxJobCost()) && (mergeCandidate.getJobItems() + fragment.getJobItems() <= getMaxJobItems())) {
            mergeCandidate.appendFragment(fragment);
            for (GraphFragment input : fragment.getInputs()) {
              input.getDependencies().remove(fragment);
              if (mergeCandidate.getInputs().add(input)) {
                input.getDependencies().add(mergeCandidate);
              }
            }
            for (GraphFragment dependency : fragment.getDependencies()) {
              dependency.getInputs().remove(fragment);
              if (mergeCandidate.getDependencies().add(dependency)) {
                dependency.getInputs().add(mergeCandidate);
              }
            }
            allFragments.remove(fragment);
            changes++;
            fragmentIterator.remove();
            if (--displace == 0) {
              // We've done enough
              break;
            }
          } else {
            if (fragment.getJobCost() < mergeCandidate.getJobCost()) {
              mergeCandidate = fragment;
            }
          }
        }
      }
    }
    // If any "root" nodes were merged with non-root nodes, we need to kill the roots
    final Iterator<GraphFragment> fragmentIterator = logicalRoot.getInputs().iterator();
    while (fragmentIterator.hasNext()) {
      final GraphFragment fragment = fragmentIterator.next();
      if (fragment.getDependencies().size() > 1) {
        fragment.getDependencies().remove(logicalRoot);
        fragmentIterator.remove();
      }
    }
    return changes > 0;
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
      if (!fragment.getInputs().isEmpty()) {
        printFragment(indent + "  ", fragment.getInputs(), printed);
      }
    }
  }

}
