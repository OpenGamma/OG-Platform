/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.calc.stats.DiscardingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.engine.view.calcnode.JobResultReceiver;
import com.opengamma.engine.view.calcnode.stats.FunctionCosts;
import com.opengamma.id.UniqueId;
import com.opengamma.util.Cancelable;
import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link ExecutionPlan} class.
 */
@Test
public class ExecutionPlanTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ExecutionPlanTest.class);

  private MultipleNodeExecutor createExecutor() {
    return new MultipleNodeExecutor(null, 0, 0, 0, 0, 0, new FunctionCosts(), null) {

      @Override
      protected long getFunctionInitId() {
        return 0;
      }

      @Override
      protected CalculationJobSpecification createJobSpecification(final DependencyGraph graph) {
        return new CalculationJobSpecification(UniqueId.of("Test", "ViewProcess"), graph.getCalculationConfigurationName(), Instant.now(), JobIdSource.getId());
      }

      @Override
      protected void addJobToViewProcessorQuery(final CalculationJobSpecification jobSpec, final DependencyGraph graph) {
        // Nothing
      }

      @Override
      protected void markExecuted(final DependencyNode node) {
        s_logger.info("Node {} executed", node);
      }

      @Override
      protected Cancelable dispatchJob(final CalculationJob job, final JobResultReceiver jobResultReceiver) {
        s_logger.info("Dispatch job {}", job);
        final List<CalculationJobResultItem> resultItems = new ArrayList<CalculationJobResultItem>(job.getJobItems().size());
        for (CalculationJobItem jobItem : job.getJobItems()) {
          s_logger.debug("Job item {}", jobItem);
          resultItems.add(new CalculationJobResultItem(jobItem));
        }
        final CalculationJobResult result = new CalculationJobResult(job.getSpecification(), 0, resultItems, "");
        jobResultReceiver.resultReceived(result);
        if (job.getTail() != null) {
          for (CalculationJob tail : job.getTail()) {
            dispatchJob(tail, jobResultReceiver);
          }
        }
        return new Cancelable() {
          @Override
          public boolean cancel(final boolean mayInterrupt) {
            return false;
          }
        };
      }

    };
  }

  /**
   * Creates a graph:
   * 
   *       N3 N4
   *       | \ |
   *       N1 N2
   */
  private DependencyGraph createDependencyGraph() {
    final DependencyGraph graph = new DependencyGraph("Default");
    final DependencyNode node1 = new DependencyNode(new ComputationTarget(1));
    node1.setFunction(new MockFunction("1", node1.getComputationTarget()));
    final DependencyNode node2 = new DependencyNode(new ComputationTarget(2));
    node2.setFunction(new MockFunction("2", node2.getComputationTarget()));
    final DependencyNode node3 = new DependencyNode(new ComputationTarget(3));
    node3.setFunction(new MockFunction("3", node3.getComputationTarget()));
    final DependencyNode node4 = new DependencyNode(new ComputationTarget(4));
    node4.setFunction(new MockFunction("4", node4.getComputationTarget()));
    node3.addInputNode(node1);
    node3.addInputNode(node2);
    node4.addInputNode(node2);
    graph.addDependencyNode(node1);
    graph.addDependencyNode(node2);
    graph.addDependencyNode(node3);
    graph.addDependencyNode(node4);
    return graph;
  }

  private MutableGraphFragmentContext createMutableGraphFragmentContext() {
    final MutableGraphFragmentContext context = new MutableGraphFragmentContext(createExecutor(), createDependencyGraph(), new LinkedBlockingQueue<CalculationJobResult>());
    return context;
  }

  private GraphFragmentContext createGraphFragmentContext() {
    final GraphFragmentContext context = new GraphFragmentContext(createExecutor(), createDependencyGraph(), new LinkedBlockingQueue<CalculationJobResult>());
    return context;
  }

  private GraphExecutorStatisticsGatherer createStatisticsGatherer() {
    return DiscardingGraphStatisticsGathererProvider.GATHERER_INSTANCE;
  }

  public void testSingleFragment() throws Exception {
    final MutableGraphFragmentContext mContext = createMutableGraphFragmentContext();
    final CompleteGraphFragment fragment = new CompleteGraphFragment(mContext, createStatisticsGatherer(), mContext.getGraph().getExecutionOrder());
    fragment.setCacheSelectHint(CacheSelectHint.allShared());
    final ExecutionPlan plan = ExecutionPlan.of(fragment);
    final GraphFragmentContext context = createGraphFragmentContext();
    final Future<?> future = plan.run(context, createStatisticsGatherer());
    assertEquals(future.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS), context.getGraph());
  }

  public void testMultipleFragments() throws Exception {
    final MutableGraphFragmentContext mContext = createMutableGraphFragmentContext();
    final MutableGraphFragment.Root root = new MutableGraphFragment.Root(mContext, createStatisticsGatherer());
    final MutableGraphFragment[] fragment = new MutableGraphFragment[4];
    for (DependencyNode node : mContext.getGraph().getDependencyNodes()) {
      final MutableGraphFragment f = new MutableGraphFragment(mContext, node);
      fragment[(Integer) node.getComputationTarget().getValue() - 1] = f;
      f.setCacheSelectHint(CacheSelectHint.allShared());
    }
    fragment[0].getOutputFragments().add(fragment[2]);
    fragment[2].getInputFragments().add(fragment[0]);
    fragment[1].getOutputFragments().add(fragment[2]);
    fragment[2].getInputFragments().add(fragment[1]);
    fragment[1].addTail(fragment[3]);
    fragment[3].getInputFragments().add(fragment[1]);
    fragment[2].getOutputFragments().add(root);
    fragment[3].getOutputFragments().add(root);
    root.getInputFragments().add(fragment[2]);
    root.getInputFragments().add(fragment[3]);
    final ExecutionPlan plan = ExecutionPlan.of(root);
    final GraphFragmentContext context = createGraphFragmentContext();
    final Future<?> future = plan.run(context, createStatisticsGatherer());
    assertEquals(future.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS), context.getGraph());
  }

}
