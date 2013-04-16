/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.cache.ViewComputationCache;
import com.opengamma.engine.calcnode.CalculationJobResultItem;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.exec.DefaultAggregatedExecutionLog;
import com.opengamma.engine.exec.DependencyNodeJobExecutionResult;
import com.opengamma.engine.exec.DependencyNodeJobExecutionResultCache;
import com.opengamma.engine.exec.ExecutionResult;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.ExecutionLogWithContext;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.tuple.Pair;

/**
 * Consumes the results produced on completion of calculation jobs until all jobs have been executed.
 * <p>
 * The results are passed in batches to a {@link SingleComputationCycle} for processing.
 */
public class CalculationJobResultStreamConsumer extends TerminatableJob {

  private static final Logger s_logger = LoggerFactory.getLogger(CalculationJobResultStreamConsumer.class);

  private static final ExecutionResult POISON = new ExecutionResult(null, null);

  private final BlockingQueue<ExecutionResult> _resultQueue;
  private final SingleComputationCycle _computationCycle;

  public CalculationJobResultStreamConsumer(final BlockingQueue<ExecutionResult> resultQueue, final SingleComputationCycle computationCycle) {
    _resultQueue = resultQueue;
    _computationCycle = computationCycle;
  }

  @Override
  protected void runOneCycle() {
    try {
      final InMemoryViewComputationResultModel fragmentResultModel = _computationCycle.constructTemplateResultModel();
      final InMemoryViewComputationResultModel fullResultModel = _computationCycle.getResultModel();
      final ExecutionLogModeSource logModes = _computationCycle.getLogModeSource();
      final Map<ValueSpecification, Set<ValueRequirement>> deprecatedTerminalOutputs = new HashMap<ValueSpecification, Set<ValueRequirement>>();
      final Map<String, Set<ValueSpecification>> terminalOutputsByConfiguration = new HashMap<String, Set<ValueSpecification>>();
      // Block until at least one item is added, then drain any further items
      ExecutionResult result = _resultQueue.take();
      do {
        if (result == POISON) {
          super.terminate();
        } else {
          final String calcConfigurationName = result.getResult().getSpecification().getCalcConfigName();
          final DependencyGraph depGraph = _computationCycle.getDependencyGraph(calcConfigurationName);
          final DependencyNodeJobExecutionResultCache jobExecutionResultCache = _computationCycle.getJobExecutionResultCache(calcConfigurationName);
          final String computeNodeId = result.getResult().getComputeNodeId();
          final Iterator<CalculationJobResultItem> itrResultItem = result.getResult().getResultItems().iterator();
          final Iterator<DependencyNode> itrNode = result.getNodes().iterator();
          final Map<ValueSpecification, Set<ValueRequirement>> allTerminalOutputs = depGraph.getTerminalOutputs();
          Set<ValueSpecification> terminalOutputs = terminalOutputsByConfiguration.get(calcConfigurationName);
          if (terminalOutputs == null) {
            terminalOutputs = Sets.newHashSetWithExpectedSize(allTerminalOutputs.size());
            terminalOutputsByConfiguration.put(calcConfigurationName, terminalOutputs);
          }
          while (itrResultItem.hasNext()) {
            assert itrNode.hasNext();
            final CalculationJobResultItem jobResultItem = itrResultItem.next();
            final DependencyNode node = itrNode.next();
            final Set<AggregatedExecutionLog> inputLogs = new LinkedHashSet<AggregatedExecutionLog>();
            for (final ValueSpecification inputValueSpec : node.getInputValues()) {
              final DependencyNodeJobExecutionResult nodeResult = jobExecutionResultCache.get(inputValueSpec);
              if (nodeResult == null) {
                // Market data
                continue;
              }
              inputLogs.add(nodeResult.getAggregatedExecutionLog());
            }
            final ExecutionLogMode executionLogMode = logModes.getLogMode(node);
            final ExecutionLogWithContext executionLogWithContext = ExecutionLogWithContext.of(node, jobResultItem.getExecutionLog());
            final AggregatedExecutionLog aggregatedExecutionLog = new DefaultAggregatedExecutionLog(executionLogWithContext, new ArrayList<AggregatedExecutionLog>(inputLogs), executionLogMode);
            final DependencyNodeJobExecutionResult jobExecutionResult = new DependencyNodeJobExecutionResult(computeNodeId, jobResultItem, aggregatedExecutionLog);
            final Set<ValueSpecification> nodeTerminals = node.getTerminalOutputValues();
            // Not correct when there are multiple calculation configurations, but it will go soon!
            for (ValueSpecification terminalOutput : nodeTerminals) {
              deprecatedTerminalOutputs.put(terminalOutput, allTerminalOutputs.get(terminalOutput));
            }
            for (ValueSpecification output : node.getOutputValues()) {
              jobExecutionResultCache.put(output, jobExecutionResult);
            }
            terminalOutputs.addAll(nodeTerminals);
          }
        }
        result = _resultQueue.poll();
      } while (result != null);
      if (deprecatedTerminalOutputs.isEmpty()) {
        return;
      }
      fragmentResultModel.addRequirements(deprecatedTerminalOutputs);
      fullResultModel.addRequirements(deprecatedTerminalOutputs);
      for (Map.Entry<String, Set<ValueSpecification>> terminalOutputs : terminalOutputsByConfiguration.entrySet()) {
        if (terminalOutputs.getValue().isEmpty()) {
          continue;
        }
        final DependencyNodeJobExecutionResultCache jobExecutionResultCache = _computationCycle.getJobExecutionResultCache(terminalOutputs.getKey());
        final ViewComputationCache cache = _computationCycle.getComputationCache(terminalOutputs.getKey());
        for (Pair<ValueSpecification, Object> value : cache.getValues(terminalOutputs.getValue(), CacheSelectHint.allShared())) {
          final ValueSpecification valueSpec = value.getFirst();
          final Object calculatedValue = value.getSecond();
          if (calculatedValue != null) {
            final ComputedValueResult computedValueResult = SingleComputationCycle.createComputedValueResult(valueSpec, calculatedValue, jobExecutionResultCache.get(valueSpec));
            fragmentResultModel.addValue(terminalOutputs.getKey(), computedValueResult);
            fullResultModel.addValue(terminalOutputs.getKey(), computedValueResult);
          }
        }
      }
      if (!fragmentResultModel.isEmpty()) {
        _computationCycle.notifyFragmentCompleted(fragmentResultModel);
      }
    } catch (InterruptedException e) {
      s_logger.debug("Interrupted while waiting for computation job results");
      Thread.interrupted();
      terminate();
    }
  }

  @Override
  public void terminate() {
    try {
      // Poison the queue
      _resultQueue.put(POISON);
    } catch (InterruptedException e) {
      super.terminate();
    }
  }

}
