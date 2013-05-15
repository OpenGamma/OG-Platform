/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import java.util.Collection;
import java.util.EnumSet;
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
import com.opengamma.engine.calcnode.MutableExecutionLog;
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
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.util.TerminatableJob;
import com.opengamma.util.log.LogLevel;
import com.opengamma.util.log.SimpleLogEvent;
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

  private String toString(final String prefix, final Collection<?> values) {
    final StringBuilder sb = new StringBuilder(prefix);
    if (values.size() > 1) {
      sb.append("s - { ");
      int count = 0;
      for (Object value : values) {
        count++;
        if (count > 1) {
          sb.append(", ");
          if (count > 10) {
            sb.append("...");
            break;
          }
        }
        sb.append(value.toString());
      }
      sb.append(" }");
    } else {
      sb.append(" - ").append(values.iterator().next().toString());
    }
    return sb.toString();
  }

  @Override
  protected void runOneCycle() {
    try {
      final InMemoryViewComputationResultModel fragmentResultModel = _computationCycle.constructTemplateResultModel();
      final InMemoryViewComputationResultModel fullResultModel = _computationCycle.getResultModel();
      final ExecutionLogModeSource logModes = _computationCycle.getLogModeSource();
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
            final ExecutionLogMode executionLogMode = logModes.getLogMode(node);
            final AggregatedExecutionLog aggregatedExecutionLog;
            if (executionLogMode == ExecutionLogMode.FULL) {
              final ExecutionLog log = jobResultItem.getExecutionLog();
              MutableExecutionLog logCopy = null;
              final Set<AggregatedExecutionLog> inputLogs = new LinkedHashSet<AggregatedExecutionLog>();
              Set<ValueSpecification> missing = jobResultItem.getMissingInputs();
              if (!missing.isEmpty()) {
                if (logCopy == null) {
                  logCopy = new MutableExecutionLog(log);
                }
                logCopy.add(new SimpleLogEvent(log.hasException() ? LogLevel.WARN : LogLevel.INFO, toString("Missing input", missing)));
              }
              missing = jobResultItem.getMissingOutputs();
              if (!missing.isEmpty()) {
                if (logCopy == null) {
                  logCopy = new MutableExecutionLog(log);
                }
                logCopy.add(new SimpleLogEvent(LogLevel.WARN, toString("Failed to produce output", missing)));
              }
              for (final ValueSpecification inputValueSpec : node.getInputValues()) {
                final DependencyNodeJobExecutionResult nodeResult = jobExecutionResultCache.get(inputValueSpec);
                if (nodeResult == null) {
                  // Market data
                  continue;
                }
                inputLogs.add(nodeResult.getAggregatedExecutionLog());
              }
              aggregatedExecutionLog = DefaultAggregatedExecutionLog.fullLogMode(node, (logCopy != null) ? logCopy : log, inputLogs);
            } else {
              EnumSet<LogLevel> logs = jobResultItem.getExecutionLog().getLogLevels();
              boolean copied = false;
              for (final ValueSpecification inputValueSpec : node.getInputValues()) {
                final DependencyNodeJobExecutionResult nodeResult = jobExecutionResultCache.get(inputValueSpec);
                if (nodeResult == null) {
                  // Market data
                  continue;
                }
                if (logs.containsAll(nodeResult.getAggregatedExecutionLog().getLogLevels())) {
                  continue;
                }
                if (!copied) {
                  copied = true;
                  logs = EnumSet.copyOf(logs);
                }
                logs.addAll(nodeResult.getAggregatedExecutionLog().getLogLevels());
              }
              aggregatedExecutionLog = DefaultAggregatedExecutionLog.indicatorLogMode(logs);
            }
            final DependencyNodeJobExecutionResult jobExecutionResult = new DependencyNodeJobExecutionResult(computeNodeId, jobResultItem, aggregatedExecutionLog);
            node.gatherTerminalOutputValues(terminalOutputs);
            for (ValueSpecification output : node.getOutputValues()) {
              jobExecutionResultCache.put(output, jobExecutionResult);
            }
          }
        }
        result = _resultQueue.poll();
      } while (result != null);
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
      super.terminate();
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
