/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogModeSource;
import com.opengamma.engine.view.calcnode.stats.FunctionCostsPerConfiguration;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatistics;

/**
 * Additional state required by {@link MutableGraphFragment} instances.
 */
/* package */class MutableGraphFragmentContext extends GraphFragmentContext {

  private final FunctionCostsPerConfiguration _functionCost;
  private Map<ValueSpecification, Boolean> _sharedCacheValues;

  public MutableGraphFragmentContext(final MultipleNodeExecutor executor, final DependencyGraph graph,
      final Queue<ExecutionResult> executionResultQueue, final ExecutionLogModeSource logModeSource) {
    super(executor, graph, executionResultQueue, logModeSource);
    final Set<ValueSpecification> terminalOutputs = graph.getTerminalOutputSpecifications();
    _sharedCacheValues = createMap(terminalOutputs.size());
    for (ValueSpecification specification : terminalOutputs) {
      _sharedCacheValues.put(specification, Boolean.TRUE);
    }
    _functionCost = executor.getFunctionCosts().getStatistics(graph.getCalculationConfigurationName());
  }

  public Map<ValueSpecification, Boolean> getSharedCacheValues() {
    return _sharedCacheValues;
  }

  public FunctionInvocationStatistics getFunctionStatistics(final CompiledFunctionDefinition function) {
    return _functionCost.getStatistics(function.getFunctionDefinition().getUniqueId());
  }

}
