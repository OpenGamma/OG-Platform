/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec.plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;

/**
 * Produces an execution plan for a graph that will execute on a single calculation node in a single thread.
 */
public class SingleNodeExecutionPlanner implements GraphExecutionPlanner {

  private static final Logger s_logger = LoggerFactory.getLogger(SingleNodeExecutionPlanner.class);

  protected PlannedJob createJob(final DependencyGraph graph, final ExecutionLogModeSource logModeSource) {
    final List<DependencyNode> order = graph.getExecutionOrder();
    final List<CalculationJobItem> items = new ArrayList<CalculationJobItem>();
    final Set<ValueSpecification> privateValues = new HashSet<ValueSpecification>();
    final Set<ValueSpecification> sharedValues = new HashSet<ValueSpecification>(graph.getTerminalOutputSpecifications());
    for (final DependencyNode node : order) {
      final Set<ValueSpecification> inputs = node.getInputValues();
      final ExecutionLogMode logMode = logModeSource.getLogMode(node);
      final CalculationJobItem jobItem = new CalculationJobItem(node.getFunction().getFunction().getFunctionDefinition().getUniqueId(), node.getFunction().getParameters(),
          node.getComputationTarget(), inputs, node.getOutputValues(), logMode);
      items.add(jobItem);
      // If node has dependencies which AREN'T in the graph, its outputs for those nodes are "shared" values
      for (final ValueSpecification specification : node.getOutputValues()) {
        if (sharedValues.contains(specification)) {
          continue;
        }
        boolean isPrivate = true;
        for (final DependencyNode dependent : node.getDependentNodes()) {
          if (!graph.containsNode(dependent)) {
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
      // If node has inputs which haven't been seen already, they can't have been generated within this graph so are "shared"
      for (final ValueSpecification specification : inputs) {
        if (sharedValues.contains(specification) || privateValues.contains(specification)) {
          continue;
        }
        sharedValues.add(specification);
      }
    }
    s_logger.debug("{} private values, {} shared values in graph", privateValues.size(), sharedValues.size());
    final CacheSelectHint cacheHint;
    if (privateValues.size() < sharedValues.size()) {
      cacheHint = CacheSelectHint.privateValues(privateValues);
    } else {
      cacheHint = CacheSelectHint.sharedValues(sharedValues);
    }
    return new PlannedJob(0, items, cacheHint, null, null);
  }

  // GraphExecutionPlanner

  @Override
  public GraphExecutionPlan createPlan(final DependencyGraph graph, final ExecutionLogModeSource logModeSource, final long functionInitializationId) {
    final PlannedJob job = createJob(graph, logModeSource);
    return new GraphExecutionPlan(graph.getCalculationConfigurationName(), functionInitializationId, Collections.singleton(job), 1, job.getItems().size(), Double.NaN, Double.NaN);
  }

}
