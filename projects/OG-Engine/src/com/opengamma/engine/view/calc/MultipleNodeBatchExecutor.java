/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.stats.FunctionCosts;
import com.opengamma.id.UniqueId;
import com.opengamma.util.functional.Function1;

import javax.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import static com.opengamma.util.functional.Functional.map;

/**
 * This DependencyGraphExecutor executes the given dependency graph
 * on a number of calculation nodes.
 */
public class MultipleNodeBatchExecutor extends MultipleNodeExecutor implements DependencyGraphExecutor<Object> {

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

  protected GraphFragmentContext createContext(final DependencyGraph graph, final BlockingQueue<CalculationJobResult> calcJobResultQueue) {
    
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
    return new GraphFragmentContext(this, graph, calcJobResultQueue) {
      @Override
      public void resultReceived(final CalculationJobResult result) {
        writeResultToBatchDb(result);
        super.resultReceived(result);
      }

      private void writeResultToBatchDb(CalculationJobResult result) {
        resultWriter.write(result, graph);
      }
    };
  }

}
