/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;

/**
 * This DependencyGraphExecutor executes the given dependency graph
 * on a number of calculation nodes.
 */
public class MultipleNodeExecutor implements DependencyGraphExecutor<Object> {

  private static final Logger s_logger = LoggerFactory.getLogger(MultipleNodeExecutor.class);

  private static final Runnable NO_OP = new Runnable() {
    @Override
    public void run() {
    }
  };

  private final SingleComputationCycle _cycle;
  private final int _minimumJobItems;
  private final int _maximumJobItems;
  private final int _minimumJobCost;
  private final int _maximumJobCost;
  private final int _maximumConcurrency;

  protected MultipleNodeExecutor(final SingleComputationCycle cycle, final int minimumJobItems, final int maximumJobItems, final int minimumJobCost, final int maximumJobCost, final int maximumConcurrency) {
    // ArgumentChecker.notNull(cycle, "cycle");
    _cycle = cycle;
    _minimumJobItems = minimumJobItems;
    _maximumJobItems = maximumJobItems;
    _minimumJobCost = minimumJobCost;
    _maximumJobCost = maximumJobCost;
    _maximumConcurrency = maximumConcurrency;
  }

  protected CalculationJobSpecification createJobSpecification(final DependencyGraph graph) {
    return new CalculationJobSpecification(_cycle.getViewName(), graph.getCalcConfName(), _cycle.getValuationTime().toEpochMillisLong(), JobIdSource.getId());
  }

  protected void addJobToViewProcessorQuery(final CalculationJobSpecification jobSpec, final DependencyGraph graph) {
    _cycle.getProcessingContext().getViewProcessorQueryReceiver().addJob(jobSpec, graph);
  }

  protected void dispatchJob(final CalculationJobSpecification jobSpec, final List<CalculationJobItem> items, final JobResultReceiver jobResultReceiver) {
    _cycle.getProcessingContext().getComputationJobDispatcher().dispatchJob(jobSpec, items, jobResultReceiver);
  }

  private class GraphFragment implements JobResultReceiver {

    private final List<DependencyNode> _nodes = new LinkedList<DependencyNode>();
    private final Set<GraphFragment> _inputs = new HashSet<GraphFragment>();
    private final Set<GraphFragment> _dependencies = new HashSet<GraphFragment>();
    private DependencyGraph _graph;
    private Map<CalculationJobItem, DependencyNode> _item2Node;
    private AtomicInteger _blockCount;

    private int _earliestStart;
    private int _executionCost;

    public void initBlockCount() {
      _blockCount = new AtomicInteger(_inputs.size());
    }

    public void addDependent(final GraphFragment dependent) {
      _dependencies.add(dependent);
      dependent._inputs.add(this);
    }

    public void inputCompleted(final DependencyGraph graph) {
      final int blockCount = _blockCount.decrementAndGet();
      s_logger.debug("{} blockCount={}", this, blockCount);
      if (blockCount == 0) {
        execute(graph);
        // Help out the GC - we don't need these any more
        _blockCount = null;
        _inputs.clear();
      }
    }

    public void execute(final DependencyGraph graph) {
      s_logger.debug("Execute {} - nodes {}", this, _nodes);
      _graph = graph;
      final CalculationJobSpecification jobSpec = createJobSpecification(graph);
      final List<CalculationJobItem> items = new ArrayList<CalculationJobItem>();
      _item2Node = new HashMap<CalculationJobItem, DependencyNode>();
      for (DependencyNode node : _nodes) {
        CalculationJobItem jobItem = new CalculationJobItem(node.getFunction().getFunction().getUniqueIdentifier(), node.getFunction().getParameters(), node.getComputationTarget().toSpecification(),
            node.getInputValues(), node.getOutputRequirements());
        items.add(jobItem);
        _item2Node.put(jobItem, node);
      }
      s_logger.info("Enqueuing {} to invoke {} functions", new Object[] {jobSpec, items.size()});
      addJobToViewProcessorQuery(jobSpec, graph);
      dispatchJob(jobSpec, items, this);
    }

    @Override
    public String toString() {
      return _nodes.size() + " dep. node(s), earliestStart=" + _earliestStart + ", executionCost=" + _executionCost;
    }

    @Override
    public void resultReceived(final CalculationJobResult result) {
      // Mark nodes as good or bad
      for (CalculationJobResultItem item : result.getResultItems()) {
        DependencyNode node = _item2Node.get(item.getItem());
        if (node == null) {
          s_logger.error("Got unexpected item {} on job {}", item, result.getSpecification().getJobId());
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

  private class RootGraphFragment extends GraphFragment {

    private final FutureTask<Object> _future = new FutureTask<Object>(NO_OP, null);

    public RootGraphFragment() {
    }

    @Override
    public void execute(final DependencyGraph graph) {
      s_logger.debug("Root node completed");
      _future.run();
    }

  }

  @Override
  public Future<Object> execute(final DependencyGraph graph) {
    // writeGraphForTestingPurposes(graph);
    final Collection<GraphFragment> roots = graphToFragments(graph);
    writeGraph("", roots);
    // TODO combine the graph fragments
    setBlockCount(roots);
    final RootGraphFragment logicalRoot = new RootGraphFragment();
    for (GraphFragment root : roots) {
      root.addDependent(logicalRoot);
    }
    logicalRoot.initBlockCount();
    final Collection<GraphFragment> leaves = getLeafFragments(roots);
    for (GraphFragment fragment : leaves) {
      fragment.execute(graph);
    }
    return logicalRoot._future;
  }

  private void writeGraphForTestingPurposes(final DependencyGraph graph) {
    try {
      final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("/tmp/graph.bin"));
      out.writeObject(graph);
      out.close();
      s_logger.error("Graph written; stopping JVM");
      System.exit(1);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Collection<GraphFragment> graphToFragments(final DependencyGraph graph) {
    final Map<DependencyNode, GraphFragment> node2fragment = new HashMap<DependencyNode, GraphFragment>();
    final Collection<DependencyNode> rootNodes = graph.getRootNodes();
    final Collection<GraphFragment> rootFragments = new ArrayList<GraphFragment>(rootNodes.size());
    graphToFragments(graph, rootFragments, node2fragment, rootNodes);
    return rootFragments;
  }

  private void graphToFragments(final DependencyGraph graph, final Collection<GraphFragment> output, final Map<DependencyNode, GraphFragment> node2fragment, final Collection<DependencyNode> nodes) {
    for (DependencyNode node : nodes) {
      if (!graph.containsNode(node)) {
        continue;
      }
      GraphFragment fragment = node2fragment.get(node);
      if (fragment == null) {
        fragment = new GraphFragment();
        fragment._nodes.add(node);
        // TODO this should be some metric relating to the computational overhead of the function
        fragment._executionCost = 1;
        node2fragment.put(node, fragment);
        if (!node.getInputNodes().isEmpty()) {
          graphToFragments(graph, fragment._inputs, node2fragment, node.getInputNodes());
          for (GraphFragment input : fragment._inputs) {
            input._dependencies.add(fragment);
            if (input._earliestStart + input._executionCost > fragment._earliestStart) {
              fragment._earliestStart = input._earliestStart + input._executionCost;
            }
          }
        }
      }
      output.add(fragment);
    }
  }

  private void setBlockCount(final Collection<GraphFragment> fragments) {
    for (GraphFragment fragment : fragments) {
      if (!fragment._inputs.isEmpty()) {
        fragment.initBlockCount();
        setBlockCount(fragment._inputs);
      }
    }
  }

  private Collection<GraphFragment> getLeafFragments(final Collection<GraphFragment> fragments) {
    final Set<GraphFragment> leaves = new HashSet<GraphFragment>();
    getLeafFragments(fragments, leaves);
    return leaves;
  }

  private void getLeafFragments(final Collection<GraphFragment> fragments, final Set<GraphFragment> leaves) {
    for (GraphFragment fragment : fragments) {
      if (fragment._inputs.isEmpty()) {
        leaves.add(fragment);
      } else {
        getLeafFragments(fragment._inputs, leaves);
      }
    }
  }

  private void writeGraph(final String indent, final Collection<GraphFragment> fragments) {
    for (GraphFragment fragment : fragments) {
      System.out.println(indent + " " + fragment);
      if (!fragment._inputs.isEmpty()) {
        writeGraph(indent + "  ", fragment._inputs);
      }
    }
  }

}
