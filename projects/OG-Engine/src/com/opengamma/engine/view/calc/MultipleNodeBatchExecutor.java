/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import static com.opengamma.util.functional.Functional.map;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

import javax.time.Instant;

import com.google.common.util.concurrent.ForwardingBlockingQueue;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.stats.FunctionCosts;
import com.opengamma.id.UniqueId;
import com.opengamma.util.functional.Function1;

/**
 * This DependencyGraphExecutor executes the given dependency graph
 * on a number of calculation nodes.
 */
public class MultipleNodeBatchExecutor extends MultipleNodeExecutor {

  /**
   * The batch result writer.
   */
  private final ResultWriterFactory _resultWriterFactory;
  
  private final SingleComputationCycle _cycle;

  protected MultipleNodeBatchExecutor(final SingleComputationCycle cycle, final int minimumJobItems, final int maximumJobItems, final long minimumJobCost, final long maximumJobCost,
                                      final int maximumConcurrency, final FunctionCosts functionCosts, final ExecutionPlanCache cache, final ResultWriterFactory writerFactory) {
    super(cycle, minimumJobItems, maximumJobItems, minimumJobCost, maximumJobCost, maximumConcurrency, functionCosts, cache);
    _resultWriterFactory = writerFactory;
    _cycle = cycle;
  }

  @Override
  public Future<DependencyGraph> execute(final DependencyGraph graph, final BlockingQueue<CalculationJobResult> calcJobResultQueue, final GraphExecutorStatisticsGatherer statistics) {
    
    Set<String> configNames = _cycle.getAllCalculationConfigurationNames();
    Instant valuationTime = _cycle.getValuationTime();
    ViewDefinition viewDefinition = _cycle.getViewDefinition();
    UniqueId batchProcessId = _cycle.getViewProcessId();
    Map<String, ViewComputationCache> cachesByCalculationConfiguration = _cycle.getCachesByCalculationConfiguration();
    
    Set<ComputationTarget> computationTargets = graph.getAllComputationTargets();
    
    Iterable<ComputationTargetSpecification> computationTargetSpecifications = map(graph.getAllComputationTargets(), new Function1<ComputationTarget, ComputationTargetSpecification>() {
      @Override
      public ComputationTargetSpecification execute(ComputationTarget computationTarget) {
        return computationTarget.toSpecification();
      }
    });

    Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs = graph.getTerminalOutputs();

    /*ResultModelDefinition resultModelDefinition,
          Map<String, ViewComputationCache> cachesByCalculationConfiguration,
          Set<ComputationTarget> computationTargets,
          RiskRun riskRun,
          Set<RiskValueName> valueNames,
          Set<RiskValueRequirement> valueRequirements,
          Set<RiskValueSpecification> valueSpecifications) {
    }*/


    final ResultWriter resultWriter = _resultWriterFactory.createResultWriter(graph);
    final BlockingQueue<CalculationJobResult> wrappedResultQueue = new ForwardingBlockingQueue<CalculationJobResult>() {

      @Override
      public boolean offer(final CalculationJobResult result) {
        resultWriter.write(result, graph);
        return super.offer(result);
      }

      @Override
      protected BlockingQueue<CalculationJobResult> delegate() {
        return calcJobResultQueue;
      }

    };
    return super.execute(graph, wrappedResultQueue, statistics);
  }

}
