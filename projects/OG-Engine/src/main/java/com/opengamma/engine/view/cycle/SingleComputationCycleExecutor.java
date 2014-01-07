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
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.cache.ViewComputationCache;
import com.opengamma.engine.calcnode.CalculationJob;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.calcnode.CalculationJobResult;
import com.opengamma.engine.calcnode.CalculationJobResultItem;
import com.opengamma.engine.calcnode.MutableExecutionLog;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.exec.DefaultAggregatedExecutionLog;
import com.opengamma.engine.exec.DependencyGraphExecutionFuture;
import com.opengamma.engine.exec.DependencyGraphExecutor;
import com.opengamma.engine.exec.DependencyNodeJobExecutionResult;
import com.opengamma.engine.exec.DependencyNodeJobExecutionResultCache;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionParameters;
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

    public abstract void run(SingleComputationCycleExecutor executorComputation);

  }

  private static class GraphExecutionComplete extends Event {

    private final String _calculationConfiguration;

    public GraphExecutionComplete(final String calculationConfiguration) {
      _calculationConfiguration = calculationConfiguration;
    }

    @Override
    public void run(final SingleComputationCycleExecutor executor) {
      s_logger.info("Execution of {} complete", _calculationConfiguration);
      final ExecutingCalculationConfiguration calcConfig = executor._executing.remove(_calculationConfiguration);

      if (calcConfig != null) {
        SingleComputationCycle cycle = executor.getCycle();
        final InMemoryViewComputationResultModel fragmentResultModel = cycle.constructTemplateResultModel();
        calcConfig.buildResults(fragmentResultModel, cycle.getResultModel());
        // TODO: Populate with durations from the component jobs
        fragmentResultModel.setCalculationTime(Instant.now());
        cycle.notifyFragmentCompleted(fragmentResultModel);
      }
    }

  }

  private static class CalculationJobComplete extends Event {

    private final CalculationJob _job;
    private final CalculationJobResult _jobResult;

    public CalculationJobComplete(final CalculationJob job, final CalculationJobResult jobResult) {
      _job = job;
      _jobResult = jobResult;
    }

    @Override
    public void run(final SingleComputationCycleExecutor executor) {
      s_logger.debug("Execution of {} complete", _job);
      executor.buildResults(_job, _jobResult);
    }

  }

  private static class ExecutingCalculationConfiguration {

    private final Cancelable _handle;
    private final DependencyGraph _graph;
    private final DependencyNodeJobExecutionResultCache _resultCache;
    private final ViewComputationCache _computationCache;
    private final Set<ValueSpecification> _terminalOutputs = new HashSet<ValueSpecification>();

    public ExecutingCalculationConfiguration(final SingleComputationCycle cycle, final DependencyGraph graph, final Cancelable handle) {
      _handle = handle;
      _graph = graph;
      _resultCache = cycle.getJobExecutionResultCache(graph.getCalculationConfigurationName());
      _computationCache = cycle.getComputationCache(graph.getCalculationConfigurationName());
    }

    public void cancel() {
      _handle.cancel(true);
    }

    public DependencyGraph getDependencyGraph() {
      return _graph;
    }

    public DependencyNodeJobExecutionResultCache getResultCache() {
      return _resultCache;
    }

    public Set<ValueSpecification> getTerminalOutputs() {
      return _terminalOutputs;
    }

    public void buildResults(final InMemoryViewComputationResultModel fragmentResultModel, final InMemoryViewComputationResultModel fullResultModel) {
      if (_terminalOutputs.isEmpty()) {
        return;
      }
      final String calculationConfiguration = _graph.getCalculationConfigurationName();
      for (Pair<ValueSpecification, Object> value : _computationCache.getValues(_terminalOutputs, CacheSelectHint.allShared())) {
        final ValueSpecification valueSpec = value.getFirst();
        final Object calculatedValue = value.getSecond();
        if (calculatedValue != null) {
          final ComputedValueResult computedValueResult = SingleComputationCycle.createComputedValueResult(valueSpec, calculatedValue, _resultCache.get(valueSpec));
          fragmentResultModel.addValue(calculationConfiguration, computedValueResult);
          fullResultModel.addValue(calculationConfiguration, computedValueResult);
        }
      }
      _terminalOutputs.clear();
    }
  }

  private final BlockingQueue<Event> _events = new LinkedBlockingQueue<Event>();
  private final Map<String, ExecutingCalculationConfiguration> _executing = new HashMap<String, ExecutingCalculationConfiguration>();
  private final SingleComputationCycle _cycle;
  private boolean _issueFragmentResults;

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
      final DependencyGraph depGraph = getCycle().getDependencyGraph(calcConfigurationName);
      final Set<ValueSpecification> sharedData = getCycle().getSharedValues(calcConfigurationName);
      final Map<ValueSpecification, FunctionParameters> parameters = getCycle().createFunctionParameters(calcConfigurationName);
      s_logger.info("Submitting {} for execution by {}", depGraph, executor);
      final DependencyGraphExecutionFuture future = executor.execute(depGraph, sharedData, parameters);
      _executing.put(calcConfigurationName, new ExecutingCalculationConfiguration(getCycle(), depGraph, future));
      future.setListener(this);
    }
    try {
      while (!_executing.isEmpty()) {
        // Block for the first event
        _events.take().run(this);
        // Then run through any others as quickly as possible before dispatching a notification
        Event e = _events.poll();
        while (e != null) {
          e.run(this);
          e = _events.poll();
        }
        if (_issueFragmentResults) {
          if (_executing.isEmpty()) {
            s_logger.info("Discarding fragment completion message - overall execution is complete");
          } else {
            s_logger.debug("Building result fragment");
            final InMemoryViewComputationResultModel fragmentResultModel = getCycle().constructTemplateResultModel();
            final InMemoryViewComputationResultModel fullResultModel = getCycle().getResultModel();
            for (ExecutingCalculationConfiguration calcConfig : _executing.values()) {
              calcConfig.buildResults(fragmentResultModel, fullResultModel);
            }
            s_logger.info("Fragment execution complete");
            // TODO: Populate the calculation duration with information from the component jobs
            fragmentResultModel.setCalculationTime(Instant.now());
            getCycle().notifyFragmentCompleted(fragmentResultModel);
          }
          _issueFragmentResults = false;
        }
      }
    } catch (final InterruptedException e) {
      Thread.interrupted();
      // Cancel all outstanding jobs to free up resources
      for (ExecutingCalculationConfiguration execution : _executing.values()) {
        execution.cancel();
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

  private void buildResults(final CalculationJob job, final CalculationJobResult jobResult) {
    final String calculationConfiguration = jobResult.getSpecification().getCalcConfigName();
    final ExecutingCalculationConfiguration calcConfig = _executing.get(calculationConfiguration);
    if (calcConfig == null) {
      s_logger.warn("Job fragment result for already completed configuration {}", calculationConfiguration);
      return;
    }
    final DependencyGraph graph = calcConfig.getDependencyGraph();
    final Map<ValueSpecification, ?> graphTerminalOutputs = graph.getTerminalOutputs();
    final Iterator<CalculationJobItem> jobItemItr = job.getJobItems().iterator();
    final Iterator<CalculationJobResultItem> jobResultItr = jobResult.getResultItems().iterator();
    final ExecutionLogModeSource logModes = getCycle().getLogModeSource();
    final DependencyNodeJobExecutionResultCache jobExecutionResultCache = calcConfig.getResultCache();
    final Set<ValueSpecification> executedTerminalOutputs = calcConfig.getTerminalOutputs();
    final String computeNodeId = jobResult.getComputeNodeId();
    while (jobItemItr.hasNext()) {
      assert jobResultItr.hasNext();
      final CalculationJobItem jobItem = jobItemItr.next();
      final CalculationJobResultItem jobResultItem = jobResultItr.next();
      // Process the streamed result fragment
      final ExecutionLogMode executionLogMode = logModes.getLogMode(calculationConfiguration, jobItem.getOutputs()[0]);
      final AggregatedExecutionLog aggregatedExecutionLog;
      if (executionLogMode == ExecutionLogMode.FULL) {
        final ExecutionLog log = jobResultItem.getExecutionLog();
        MutableExecutionLog logCopy = null;
        final Set<AggregatedExecutionLog> inputLogs = new LinkedHashSet<AggregatedExecutionLog>();
        Set<ValueSpecification> missing = jobResultItem.getMissingInputs();
        if (!missing.isEmpty()) {
          logCopy = new MutableExecutionLog(log);
          logCopy.add(new SimpleLogEvent(log.hasException() ? LogLevel.WARN : LogLevel.INFO, toString("Missing input", missing)));
        }
        missing = jobResultItem.getMissingOutputs();
        if (!missing.isEmpty()) {
          if (logCopy == null) {
            logCopy = new MutableExecutionLog(log);
          }
          logCopy.add(new SimpleLogEvent(LogLevel.WARN, toString("Failed to produce output", missing)));
        }
        for (final ValueSpecification inputValueSpec : jobItem.getInputs()) {
          final DependencyNodeJobExecutionResult nodeResult = jobExecutionResultCache.get(inputValueSpec);
          if (nodeResult == null) {
            // Market data
            continue;
          }
          inputLogs.add(nodeResult.getAggregatedExecutionLog());
        }
        final String functionName;
        final FunctionDefinition function = getCycle().getViewProcessContext().getFunctionResolver().getFunction(jobItem.getFunctionUniqueIdentifier());
        if (function != null) {
          functionName = function.getShortName();
        } else {
          functionName = jobItem.getFunctionUniqueIdentifier();
        }
        aggregatedExecutionLog = DefaultAggregatedExecutionLog.fullLogMode(functionName, jobItem.getComputationTargetSpecification(), (logCopy != null) ? logCopy : log, inputLogs);
      } else {
        EnumSet<LogLevel> logs = jobResultItem.getExecutionLog().getLogLevels();
        boolean copied = false;
        for (final ValueSpecification inputValueSpec : jobItem.getInputs()) {
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
      for (final ValueSpecification outputValueSpec : jobItem.getOutputs()) {
        if (graphTerminalOutputs.containsKey(outputValueSpec)) {
          executedTerminalOutputs.add(outputValueSpec);
        }
        jobExecutionResultCache.put(outputValueSpec, jobExecutionResult);
      }
    }
    _issueFragmentResults |= !executedTerminalOutputs.isEmpty();
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
