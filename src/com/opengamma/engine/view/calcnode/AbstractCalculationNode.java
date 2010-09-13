/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInputsImpl;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.cache.WriteBehindViewComputationCache;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatisticsGatherer;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.Pair;

/**
 * A calculation node implementation. The node can only be used by one thread - i.e. executeJob cannot be called concurrently to do
 * multiple jobs. To execute multiple jobs concurrently separate calculation nodes must be used.
 * <p>
 * The function repository (and anything else) must be properly initialized and ready for use by the node when it receives its first
 * job. Responsibility for initialization should therefore lie with whatever will logically be dispatching jobs. This is typically a
 * {@link ViewProcessor} for local nodes or a {@link RemoteNodeClient} for remote nodes.
 */
public abstract class AbstractCalculationNode implements CalculationNode {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractCalculationNode.class);
  private final ViewComputationCacheSource _cacheSource;
  private final FunctionRepository _functionRepository;
  private final FunctionExecutionContext _functionExecutionContext;
  private final ComputationTargetResolver _targetResolver;
  private final ViewProcessorQuerySender _viewProcessorQuerySender;
  private final FunctionInvocationStatisticsGatherer _functionInvocationStatistics;
  private String _nodeId;
  private final ExecutorService _writeBehindExecutorService;

  protected AbstractCalculationNode(ViewComputationCacheSource cacheSource, FunctionRepository functionRepository, FunctionExecutionContext functionExecutionContext,
      ComputationTargetResolver targetResolver, ViewProcessorQuerySender calcNodeQuerySender, String nodeId, final ExecutorService writeBehindExecutorService,
      FunctionInvocationStatisticsGatherer functionInvocationStatistics) {
    ArgumentChecker.notNull(cacheSource, "Cache Source");
    ArgumentChecker.notNull(functionRepository, "Function repository");
    ArgumentChecker.notNull(functionExecutionContext, "Function Execution Context");
    ArgumentChecker.notNull(targetResolver, "Target Resolver");
    ArgumentChecker.notNull(calcNodeQuerySender, "Calc Node Query Sender");
    ArgumentChecker.notNull(nodeId, "Calculation node ID");
    ArgumentChecker.notNull(writeBehindExecutorService, "Write behind Executor Service");
    ArgumentChecker.notNull(functionInvocationStatistics, "function invocation statistics");

    _cacheSource = cacheSource;
    _functionRepository = functionRepository;
    // Take a copy of the execution context as we will modify it during execution which isn't good if there are other CalcNodes in our JVM
    _functionExecutionContext = functionExecutionContext.clone();
    _targetResolver = targetResolver;
    _viewProcessorQuerySender = calcNodeQuerySender;
    _nodeId = nodeId;
    _writeBehindExecutorService = writeBehindExecutorService;
    _functionInvocationStatistics = functionInvocationStatistics;
  }

  public ViewComputationCacheSource getCacheSource() {
    return _cacheSource;
  }

  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  public FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }

  public ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }

  protected ViewProcessorQuerySender getViewProcessorQuerySender() {
    return _viewProcessorQuerySender;
  }

  protected ExecutorService getWriteBehindExecutorService() {
    return _writeBehindExecutorService;
  }

  protected FunctionInvocationStatisticsGatherer getFunctionInvocationStatistics() {
    return _functionInvocationStatistics;
  }

  @Override
  public String getNodeId() {
    return _nodeId;
  }

  public void setNodeId(final String nodeId) {
    ArgumentChecker.notNull(nodeId, "nodeId");
    _nodeId = nodeId;
  }

  public CalculationJobResult executeJob(final CalculationJob job) {
    s_logger.info("Executing {} on {}", job, _nodeId);
    final CalculationJobSpecification spec = job.getSpecification();
    getFunctionExecutionContext().setViewProcessorQuery(new ViewProcessorQuery(getViewProcessorQuerySender(), spec));
    getFunctionExecutionContext().setSnapshotEpochTime(spec.getIterationTimestamp());
    getFunctionExecutionContext().setSnapshotClock(DateUtil.epochFixedClockUTC(spec.getIterationTimestamp()));
    final WriteBehindViewComputationCache cache = new WriteBehindViewComputationCache(getCache(spec), job.getCacheSelectHint(), getWriteBehindExecutorService());
    long executionTime = System.nanoTime();
    final List<CalculationJobResultItem> resultItems = new ArrayList<CalculationJobResultItem>();
    final String calculationConfiguration = spec.getCalcConfigName();
    for (CalculationJobItem jobItem : job.getJobItems()) {
      CalculationJobResultItem resultItem;
      try {
        invoke(jobItem, cache, new DeferredInvocationStatistics(getFunctionInvocationStatistics(), calculationConfiguration));
        resultItem = new CalculationJobResultItem(jobItem);
      } catch (MissingInputException e) {
        // NOTE kirk 2009-10-20 -- We intentionally only do the message here so that we don't
        // litter the logs with stack traces.
        s_logger.info("Unable to invoke {} due to missing inputs: {}", jobItem, e.getMessage());
        resultItem = new CalculationJobResultItem(jobItem, e);
      } catch (Exception e) {
        s_logger.warn("Invoking " + jobItem.getFunctionUniqueIdentifier() + " threw exception.", e);
        resultItem = new CalculationJobResultItem(jobItem, e);
      }
      resultItems.add(resultItem);
    }
    cache.waitForPendingWrites();
    executionTime = System.nanoTime() - executionTime;
    CalculationJobResult jobResult = new CalculationJobResult(spec, executionTime, resultItems, getNodeId());
    s_logger.info("Executed {}", job);
    return jobResult;
  }

  @Override
  public ViewComputationCache getCache(CalculationJobSpecification spec) {
    ViewComputationCache cache = getCacheSource().getCache(spec.getViewName(), spec.getCalcConfigName(), spec.getIterationTimestamp());
    return cache;
  }

  private void invoke(final CalculationJobItem jobItem, final WriteBehindViewComputationCache cache, final DeferredInvocationStatistics statistics) {
    final String functionUniqueId = jobItem.getFunctionUniqueIdentifier();
    final ComputationTarget target = getTargetResolver().resolve(jobItem.getComputationTargetSpecification());
    if (target == null) {
      throw new OpenGammaRuntimeException("Unable to resolve specification " + jobItem.getComputationTargetSpecification());
    }
    s_logger.debug("Invoking {} on target {}", functionUniqueId, target);
    final FunctionInvoker invoker = getFunctionRepository().getInvoker(functionUniqueId);
    if (invoker == null) {
      throw new NullPointerException("Unable to locate " + functionUniqueId + " in function repository.");
    }
    // set parameters
    getFunctionExecutionContext().setFunctionParameters(jobItem.getFunctionParameters());
    // assemble inputs
    final Collection<ComputedValue> inputs = new HashSet<ComputedValue>();
    final Collection<ValueSpecification> missingInputs = new HashSet<ValueSpecification>();
    int inputBytes = 0;
    int inputSamples = 0;
    for (Pair<ValueSpecification, Object> input : cache.getValues(jobItem.getInputs())) {
      if ((input.getValue() == null) || (input.getValue() instanceof MissingInput)) {
        missingInputs.add(input.getKey());
      } else {
        inputs.add(new ComputedValue(input.getKey(), input.getValue()));
        // [ENG-220] Record the byte -size of this value
        inputBytes += 1;
        inputSamples++;
      }
    }
    statistics.setDataInputBytes(inputBytes, inputSamples);
    if (!missingInputs.isEmpty()) {
      s_logger.info("Not able to execute as missing inputs {}", missingInputs);
      throw new MissingInputException(missingInputs, functionUniqueId);
    }
    final FunctionInputs functionInputs = new FunctionInputsImpl(inputs);
    // execute
    statistics.beginInvocation();
    final Set<ComputedValue> results = invoker.execute(getFunctionExecutionContext(), functionInputs, target, jobItem.getDesiredValues());
    if (results == null) {
      throw new NullPointerException("No results returned by invoker " + invoker);
    }
    statistics.endInvocation();
    statistics.setFunctionIdentifier(functionUniqueId);
    statistics.setExpectedDataOutputSamples(results.size());
    // store results
    cache.putValues(results, statistics);
  }
}
