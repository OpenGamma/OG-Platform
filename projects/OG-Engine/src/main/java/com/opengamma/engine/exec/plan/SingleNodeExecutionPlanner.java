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
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;

/**
 * Produces an execution plan for a graph that will execute on a single calculation node in a single thread.
 */
public class SingleNodeExecutionPlanner implements GraphExecutionPlanner {

  private static final Logger s_logger = LoggerFactory.getLogger(SingleNodeExecutionPlanner.class);

  private static final class JobBuilder {

    private final List<CalculationJobItem> _items;
    private final String _calcConfigName;
    private final ExecutionLogModeSource _logModeSource;
    private final Set<ValueSpecification> _sharedValues;
    private final Set<ValueSpecification> _privateValues;
    private final Map<ValueSpecification, ?> _terminals;
    private final Map<ValueSpecification, FunctionParameters> _parameters;
    private final Set<DependencyNode> _executed;

    public JobBuilder(final DependencyGraph graph, final ExecutionLogModeSource logModeSource, final Set<ValueSpecification> sharedValues, final Map<ValueSpecification, FunctionParameters> parameters) {
      final int size = graph.getSize();
      _items = new ArrayList<CalculationJobItem>(size);
      _calcConfigName = graph.getCalculationConfigurationName();
      _logModeSource = logModeSource;
      _sharedValues = sharedValues;
      _privateValues = new HashSet<ValueSpecification>();
      _terminals = graph.getTerminalOutputs();
      _parameters = parameters;
      _executed = Sets.newHashSetWithExpectedSize(size);
    }

    public void addNodes(final DependencyNode root) {
      if (!_executed.add(root)) {
        return;
      }
      final ValueSpecification[] inputs = DependencyNodeImpl.getInputValueArray(root);
      for (int i = 0; i < inputs.length; i++) {
        final ValueSpecification input = inputs[i];
        if (!_sharedValues.contains(input)) {
          addNodes(root.getInputNode(i));
        }
      }
      final ExecutionLogMode logMode = _logModeSource.getLogMode(_calcConfigName, root.getOutputValue(0));
      final ValueSpecification[] outputs = DependencyNodeImpl.getOutputValueArray(root);
      FunctionParameters functionParameters = root.getFunction().getParameters();
      for (ValueSpecification output : outputs) {
        if (_terminals.containsKey(output)) {
          _sharedValues.add(output);
        } else {
          _privateValues.add(output);
        }
        FunctionParameters newParameters = _parameters.get(output);
        if (newParameters != null) {
          functionParameters = newParameters;
        }
      }
      _items.add(new CalculationJobItem(root.getFunction().getFunctionId(), functionParameters, root.getTarget(), inputs, outputs, logMode));
    }

    public List<CalculationJobItem> getJobItems() {
      return _items;
    }

    public CacheSelectHint getCacheHint() {
      s_logger.debug("{} private values, {} shared values in graph", _privateValues.size(), _sharedValues.size());
      if (_privateValues.size() < _sharedValues.size()) {
        return CacheSelectHint.privateValues(_privateValues);
      } else {
        return CacheSelectHint.sharedValues(_sharedValues);
      }
    }

  }

  protected static PlannedJob createJob(final DependencyGraph graph, final ExecutionLogModeSource logModeSource, final Set<ValueSpecification> sharedValues,
      final Map<ValueSpecification, FunctionParameters> parameters) {
    final JobBuilder builder = new JobBuilder(graph, logModeSource, sharedValues, parameters);
    final int roots = graph.getRootCount();
    iLoop: for (int i = 0; i < roots; i++) { //CSIGNORE
      final DependencyNode root = graph.getRootNode(i);
      final int outputs = root.getOutputCount();
      for (int j = 0; j < outputs; j++) {
        if (sharedValues.contains(root.getOutputValue(j))) {
          continue iLoop;
        }
      }
      builder.addNodes(root);
    }
    return new PlannedJob(0, builder.getJobItems(), builder.getCacheHint(), null, null);
  }

  // GraphExecutionPlanner

  @Override
  public GraphExecutionPlan createPlan(final DependencyGraph graph, final ExecutionLogModeSource logModeSource, final long functionInitialisationId, final Set<ValueSpecification> sharedValues,
      final Map<ValueSpecification, FunctionParameters> parameters) {
    final PlannedJob job = createJob(graph, logModeSource, sharedValues, parameters);
    return new GraphExecutionPlan(graph.getCalculationConfigurationName(), functionInitialisationId, Collections.singleton(job), 1, job.getItems().size(), Double.NaN, Double.NaN);
  }

}
