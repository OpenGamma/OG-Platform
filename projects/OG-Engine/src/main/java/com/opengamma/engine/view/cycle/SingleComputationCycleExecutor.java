/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cycle;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.cache.ViewComputationCache;
import com.opengamma.engine.calcnode.CalculationJob;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.calcnode.CalculationJobResult;
import com.opengamma.engine.calcnode.CalculationJobResultItem;
import com.opengamma.engine.calcnode.MutableExecutionLog;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.exec.DefaultAggregatedExecutionLog;
import com.opengamma.engine.exec.DependencyGraphExecutionFuture;
import com.opengamma.engine.exec.DependencyGraphExecutor;
import com.opengamma.engine.exec.DependencyNodeJobExecutionResult;
import com.opengamma.engine.exec.DependencyNodeJobExecutionResultCache;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.impl.ExecutionLogModeSource;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.util.async.Cancelable;
import com.opengamma.util.log.LogLevel;
import com.opengamma.util.log.SimpleLogEvent;
import com.opengamma.util.tuple.Pair;

/**
 * State required by {@link SingleComputationCycle} during its execution only.
 * <p>
 * An instance of this object, owned by the cycle, will only exist while the {@link SingleComputationCycle#execute} method is being called.
 */
/* package */class SingleComputationCycleExecutor implements DependencyGraphExecutionFuture.Listener {

  private static final Logger s_logger = LoggerFactory.getLogger(SingleComputationCycleExecutor.class);

  private abstract static class Event {

    public abstract void run(SingleComputationCycleExecutor executorComputation, InMemoryViewComputationResultModel fragmentResultModel, InMemoryViewComputationResultModel fullResultModel);

    public abstract void run(SingleComputationCycleExecutor executorComputation);

  }

  private static class GraphExecutionComplete extends Event {

    private final String _calculationConfiguration;

    public GraphExecutionComplete(final String calculationConfiguration) {
      _calculationConfiguration = calculationConfiguration;
    }

    @Override
    public void run(final SingleComputationCycleExecutor executor, final InMemoryViewComputationResultModel fragmentResultModel, final InMemoryViewComputationResultModel fullResultModel) {
      run(executor);
    }

    @Override
    public void run(final SingleComputationCycleExecutor executor) {
      s_logger.info("Execution of {} complete", _calculationConfiguration);
      executor._executing.remove(_calculationConfiguration);
    }

  }

  private static class CalculationJobComplete extends Event {

    private CalculationJob _job;
    private CalculationJobResult _jobResult;

    public CalculationJobComplete(final CalculationJob job, final CalculationJobResult jobResult) {
      _job = job;
      _jobResult = jobResult;
    }

    @Override
    public void run(final SingleComputationCycleExecutor executor, final InMemoryViewComputationResultModel fragmentResultModel, final InMemoryViewComputationResultModel fullResultModel) {
      s_logger.debug("Execution of {} complete", _job);
      executor.buildResults(_job, _jobResult, fragmentResultModel, fullResultModel);
      _job = null;
      _jobResult = null;
    }

    @Override
    public void run(final SingleComputationCycleExecutor executor) {
      final InMemoryViewComputationResultModel fragmentResultModel = executor.getCycle().constructTemplateResultModel();
      final InMemoryViewComputationResultModel fullResultModel = executor.getCycle().getResultModel();
      run(executor, fragmentResultModel, fullResultModel);
      executor.mergeResults(fragmentResultModel, fullResultModel);
      s_logger.info("Fragment execution complete");
      executor.getCycle().notifyFragmentCompleted(fragmentResultModel);
    }

  }

  private final BlockingQueue<Event> _events = new LinkedTransferQueue<Event>();
  private final Map<String, Cancelable> _executing = new HashMap<String, Cancelable>();
  private final SingleComputationCycle _cycle;

  public SingleComputationCycleExecutor(final SingleComputationCycle cycle) {
    _cycle = cycle;
  }

  private SingleComputationCycle getCycle() {
    return _cycle;
  }

  public void execute() throws InterruptedException {
    final DependencyGraphExecutor executor = getCycle().getViewProcessContext().getDependencyGraphExecutorFactory().createExecutor(getCycle());
    for (final String calcConfigurationName : getCycle().getAllCalculationConfigurationNames()) {
      s_logger.info("Executing plans for calculation configuration {}", calcConfigurationName);
      final DependencyGraph depGraph = getCycle().createExecutableDependencyGraph(calcConfigurationName);
      s_logger.info("Submitting {} for execution by {}", depGraph, executor);
      final DependencyGraphExecutionFuture future = executor.execute(depGraph);
      _executing.put(calcConfigurationName, future);
      future.setListener(this);
    }
    try {
      while (!_executing.isEmpty()) {
        _events.take().run(this);
      }
    } catch (final InterruptedException e) {
      Thread.interrupted();
      // Cancel all outstanding jobs to free up resources
      for (Cancelable execution : _executing.values()) {
        execution.cancel(true);
      }
      throw e;
    }
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

  private void buildResults(final CalculationJob job, final CalculationJobResult jobResult, final InMemoryViewComputationResultModel fragmentResultModel,
      final InMemoryViewComputationResultModel fullResultModel) {
    final String calculationConfiguration = jobResult.getSpecification().getCalcConfigName();
    final DependencyGraph graph = getCycle().getDependencyGraph(calculationConfiguration);
    final Iterator<CalculationJobItem> jobItemItr = job.getJobItems().iterator();
    final Iterator<CalculationJobResultItem> jobResultItr = jobResult.getResultItems().iterator();
    final Set<ValueSpecification> terminalOutputs = new HashSet<ValueSpecification>();
    final ExecutionLogModeSource logModes = getCycle().getLogModeSource();
    final DependencyNodeJobExecutionResultCache jobExecutionResultCache = getCycle().getJobExecutionResultCache(calculationConfiguration);
    final ViewComputationCache cache = getCycle().getComputationCache(calculationConfiguration);
    final String computeNodeId = jobResult.getComputeNodeId();
    while (jobItemItr.hasNext()) {
      assert jobResultItr.hasNext();
      final CalculationJobItem jobItem = jobItemItr.next();
      final CalculationJobResultItem jobResultItem = jobResultItr.next();
      // Mark the node that corresponds to this item
      final DependencyNode node = graph.getNodeProducing(jobItem.getOutputs().iterator().next());
      if (jobResultItem.isFailed()) {
        getCycle().markFailed(node);
      } else {
        getCycle().markExecuted(node);
      }
      // Process the streamed result fragment
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
      for (Pair<ValueSpecification, Object> value : cache.getValues(terminalOutputs, CacheSelectHint.allShared())) {
        final ValueSpecification valueSpec = value.getFirst();
        final Object calculatedValue = value.getSecond();
        if (calculatedValue != null) {
          final ComputedValueResult computedValueResult = SingleComputationCycle.createComputedValueResult(valueSpec, calculatedValue, jobExecutionResult);
          fragmentResultModel.addValue(calculationConfiguration, computedValueResult);
          fullResultModel.addValue(calculationConfiguration, computedValueResult);
        }
      }
    }
  }

  private void mergeResults(final InMemoryViewComputationResultModel fragmentResultModel, final InMemoryViewComputationResultModel fullResultModel) {
    Event next = _events.poll();
    while (next != null) {
      next.run(this, fragmentResultModel, fullResultModel);
      next = _events.poll();
    }
  }

  /**
   * Receives a job result fragment. These will be streamed in by the execution framework. Only one notification per job will be received (for example the execution framework might have
   * repeated/duplicated jobs to handle node failures).
   * 
   * @param job the job that was executed, not null
   * @param jobResult the job result, not null
   */
  public void jobCompleted(final CalculationJob job, final CalculationJobResult jobResult) {
    _events.add(new CalculationJobComplete(job, jobResult));
  }

  @Override
  public void graphCompleted(final String calculationConfiguration) {
    _events.add(new GraphExecutionComplete(calculationConfiguration));
  }

}
