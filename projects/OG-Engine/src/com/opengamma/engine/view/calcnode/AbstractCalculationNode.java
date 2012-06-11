/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInputsImpl;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.cache.DeferredViewComputationCache;
import com.opengamma.engine.view.cache.DirectWriteViewComputationCache;
import com.opengamma.engine.view.cache.FilteredViewComputationCache;
import com.opengamma.engine.view.cache.NotCalculatedSentinel;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.cache.WriteBehindViewComputationCache;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatisticsGatherer;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.monitor.InvocationCount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * A calculation node implementation. The node can only be used by one thread - i.e. executeJob cannot be called concurrently to do multiple jobs. To execute multiple jobs concurrently separate
 * calculation nodes must be used.
 * <p>
 * The function repository (and anything else) must be properly initialized and ready for use by the node when it receives its first job. Responsibility for initialization should therefore lie with
 * whatever will logically be dispatching jobs. This is typically a {@link ViewProcessor} for local nodes or a {@link RemoteNodeClient} for remote nodes.
 */
public abstract class AbstractCalculationNode implements CalculationNode {

  private static final String ERROR_CANT_RESOLVE = "com.opengamma.engine.view.calcnode.InvalidTargetException";
  private static final String ERROR_BAD_FUNCTION = "com.opengamma.engine.view.calcnode.InvalidFunctionException";
  private static final String ERROR_INVOKING = "com.opengamma.engine.view.calcnode.InvalidInvocationException";

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractCalculationNode.class);

  private final ViewComputationCacheSource _cacheSource;
  private final CompiledFunctionService _functionCompilationService;
  private final FunctionExecutionContext _functionExecutionContext;
  private final ComputationTargetResolver _targetResolver;
  private final ViewProcessorQuerySender _viewProcessorQuerySender;
  private final FunctionInvocationStatisticsGatherer _functionInvocationStatistics;
  private String _nodeId;
  private final ExecutorService _executorService;

  private boolean _writeBehindCache;
  private boolean _asynchronousTargetResolve;

  protected AbstractCalculationNode(ViewComputationCacheSource cacheSource, CompiledFunctionService functionCompilationService,
      FunctionExecutionContext functionExecutionContext, ComputationTargetResolver targetResolver, ViewProcessorQuerySender calcNodeQuerySender, String nodeId,
      final ExecutorService executorService, FunctionInvocationStatisticsGatherer functionInvocationStatistics) {
    ArgumentChecker.notNull(cacheSource, "cacheSource");
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    ArgumentChecker.notNull(functionExecutionContext, "functionExecutionContext");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    ArgumentChecker.notNull(calcNodeQuerySender, "calcNodeQuerySender");
    ArgumentChecker.notNull(nodeId, "nodeId");
    ArgumentChecker.notNull(functionInvocationStatistics, "functionInvocationStatistics");
    _cacheSource = cacheSource;
    _functionCompilationService = functionCompilationService;
    // Take a copy of the execution context as we will modify it during execution which isn't good if there are other CalcNodes in our JVM
    _functionExecutionContext = functionExecutionContext.clone();
    _targetResolver = targetResolver;
    _viewProcessorQuerySender = calcNodeQuerySender;
    _nodeId = nodeId;
    _executorService = executorService;
    _functionInvocationStatistics = functionInvocationStatistics;
  }

  public ViewComputationCacheSource getCacheSource() {
    return _cacheSource;
  }

  public CompiledFunctionService getFunctionCompilationService() {
    return _functionCompilationService;
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

  public boolean isUseWriteBehindCache() {
    return _writeBehindCache;
  }

  public void setUseWriteBehindCache(final boolean writeBehind) {
    if (writeBehind) {
      if (getExecutorService() == null) {
        throw new IllegalArgumentException("Can't use write behind cache without an executor service");
      }
    }
    _writeBehindCache = writeBehind;
  }

  public boolean isUseAsynchronousTargetResolve() {
    return _asynchronousTargetResolve;
  }

  public void setUseAsynchronousTargetResolve(final boolean asynchronousTargetResolve) {
    if (asynchronousTargetResolve) {
      if (getExecutorService() == null) {
        throw new IllegalArgumentException("Can't use asynchronous target resolution without an executor service");
      }
    }
    _asynchronousTargetResolve = asynchronousTargetResolve;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
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

  private void postNotCalculated(final Collection<ValueSpecification> outputs, final FilteredViewComputationCache cache, final MissingInput error) {
    final Collection<ComputedValue> results = new ArrayList<ComputedValue>(outputs.size());
    for (ValueSpecification output : outputs) {
      results.add(new ComputedValue(output, error));
    }
    cache.putValues(results);
  }

  protected List<CalculationJobResultItem> executeJobItems(final CalculationJob job, final DeferredViewComputationCache cache,
      final CompiledFunctionRepository functions, final String calculationConfiguration) {
    final List<CalculationJobResultItem> resultItems = new ArrayList<CalculationJobResultItem>();
    for (CalculationJobItem jobItem : job.getJobItems()) {
      if (job.isCancelled()) {
        return null;
      }
      CalculationJobResultItem resultItem = invoke(functions, jobItem, cache, new DeferredInvocationStatistics(getFunctionInvocationStatistics(), calculationConfiguration));
      try {
        resultItem = invoke(functions, jobItem, cache, new DeferredInvocationStatistics(getFunctionInvocationStatistics(), calculationConfiguration));
      } catch (Throwable t) {
        s_logger.error("Caught exception", t);
        postNotCalculated(jobItem.getOutputs(), cache, NotCalculatedSentinel.EVALUATION_ERROR);
        resultItem = CalculationJobResultItem.failure(t);
      }
      resultItems.add(resultItem);
    }
    return resultItems;
  }

  private static final InvocationCount s_executeJob = new InvocationCount("executeJob");
  private static final InvocationCount s_executeJobItems = new InvocationCount("executeJobItems");
  private static final InvocationCount s_writeBack = new InvocationCount("writeBack");

  public CalculationJobResult executeJob(final CalculationJob job) {
    s_logger.info("Executing {} on {}", job, _nodeId);
    s_executeJob.enter();
    try {
      final CalculationJobSpecification spec = job.getSpecification();
      getFunctionExecutionContext().setViewProcessorQuery(new ViewProcessorQuery(getViewProcessorQuerySender(), spec));
      getFunctionExecutionContext().setValuationTime(spec.getValuationTime());
      getFunctionExecutionContext().setValuationClock(DateUtils.fixedClockUTC(spec.getValuationTime()));
      final CompiledFunctionRepository functions = getFunctionCompilationService().compileFunctionRepository(spec.getValuationTime());
      // TODO: don't create a new cache instance each time -- if there are peer nodes then we may be able to share
      final DeferredViewComputationCache cache = getDeferredViewComputationCache(getCache(spec), job.getCacheSelectHint());
      long executionTime = System.nanoTime();
      final String calculationConfiguration = spec.getCalcConfigName();
      final List<CalculationJobResultItem> resultItems;
      s_executeJobItems.enter();
      try {
        resultItems = executeJobItems(job, cache, functions, calculationConfiguration);
        if (resultItems == null) {
          return null;
        }
      } finally {
        s_executeJobItems.leave();
      }
      s_writeBack.enter();
      try {
        // [PLAT-2293]: we don't have to wait for pending writes locally - our tail jobs can run immediately - we just have to wait before sending the job completion message back to the dispatcher
        cache.waitForPendingWrites();
      } finally {
        s_writeBack.leave();
      }
      executionTime = System.nanoTime() - executionTime;
      final CalculationJobResult jobResult = new CalculationJobResult(spec, executionTime, resultItems, getNodeId());
      s_logger.info("Executed {} in {}ns", job, executionTime);
      return jobResult;
    } finally {
      if (s_executeJob.leave() > 500) {
        System.exit(1);
      }
    }
  }

  private DeferredViewComputationCache getDeferredViewComputationCache(ViewComputationCache cache,
      CacheSelectHint cacheSelectHint) {
    if (isUseWriteBehindCache()) {
      return new WriteBehindViewComputationCache(cache, cacheSelectHint, getExecutorService());
    } else {
      return new DirectWriteViewComputationCache(cache, cacheSelectHint);
    }
  }

  @Override
  public ViewComputationCache getCache(CalculationJobSpecification spec) {
    ViewComputationCache cache = getCacheSource().getCache(spec.getViewCycleId(), spec.getCalcConfigName());
    return cache;
  }

  private static Set<ValueRequirement> plat2290(final Set<ValueSpecification> outputs) {
    final Set<ValueRequirement> result = Sets.newHashSetWithExpectedSize(outputs.size());
    for (ValueSpecification output : outputs) {
      result.add(output.toRequirementSpecification());
    }
    return result;
  }

  private static final InvocationCount s_resolveTarget = new InvocationCount("resolveTarget");
  private static final InvocationCount s_invoke = new InvocationCount("invoke");
  private static final InvocationCount s_prepareInputs = new InvocationCount("prepareInputs");

  private CalculationJobResultItem invoke(final CompiledFunctionRepository functions, final CalculationJobItem jobItem, final DeferredViewComputationCache cache,
      final DeferredInvocationStatistics statistics) {
    // TODO: can we do the target resolution in advance ? (i.e. for future items in the queue)
    final String functionUniqueId = jobItem.getFunctionUniqueIdentifier();
    Future<ComputationTarget> targetFuture = null;
    ComputationTarget target = null;
    if (isUseAsynchronousTargetResolve()) {
      targetFuture = getExecutorService().submit(new Callable<ComputationTarget>() {
        @Override
        public ComputationTarget call() {
          return getTargetResolver().resolve(jobItem.getComputationTargetSpecification());
        }
      });
    } else {
      s_resolveTarget.enter();
      target = getTargetResolver().resolve(jobItem.getComputationTargetSpecification());
      s_resolveTarget.leave();
      if (target == null) {
        return CalculationJobResultItem.failure(ERROR_CANT_RESOLVE, "Unable to resolve target " + jobItem.getComputationTargetSpecification());
      }
    }
    final FunctionInvoker invoker = functions.getInvoker(functionUniqueId);
    if (invoker == null) {
      return CalculationJobResultItem.failure(ERROR_BAD_FUNCTION, "Unable to locate " + functionUniqueId + " in function repository");
    }
    // set parameters
    getFunctionExecutionContext().setFunctionParameters(jobItem.getFunctionParameters());
    // assemble inputs
    final Collection<ComputedValue> inputs = new HashSet<ComputedValue>();
    final Set<ValueSpecification> missing = new HashSet<ValueSpecification>();
    int inputBytes = 0;
    int inputSamples = 0;
    s_prepareInputs.enter();
    try {
      for (Pair<ValueSpecification, Object> input : cache.getValues(jobItem.getInputs())) {
        if ((input.getValue() == null) || (input.getValue() instanceof MissingInput)) {
          missing.add(input.getKey());
        } else {
          final ComputedValue value = new ComputedValue(input.getKey(), input.getValue());
          inputs.add(value);
          final Integer bytes = cache.estimateValueSize(value);
          if (bytes != null) {
            inputBytes += bytes;
            inputSamples++;
          }
        }
      }
      statistics.setDataInputBytes(inputBytes, inputSamples);
    } finally {
      s_prepareInputs.leave();
    }
    CalculationJobResultItem itemResult;
    if (missing.isEmpty()) {
      itemResult = CalculationJobResultItem.success();
    } else {
      if (invoker.canHandleMissingInputs()) {
        s_logger.debug("Executing even with missing inputs {}", missing);
        itemResult = CalculationJobResultItem.partialInputs(new HashSet<ValueSpecification>(missing));
      } else {
        s_logger.info("Not able to execute as missing inputs {}", missing);
        if (targetFuture != null) {
          // Cancelling doesn't do anything so we have to block and clear the result
          s_resolveTarget.enter();
          try {
            targetFuture.get();
          } catch (Throwable t) {
            return CalculationJobResultItem.failure(t);
          } finally {
            s_resolveTarget.leave();
          }
        }
        return CalculationJobResultItem.missingInputs(missing);
      }
    }
    final FunctionInputs functionInputs = new FunctionInputsImpl(inputs, missing);
    if (target == null) {
      s_resolveTarget.enter();
      try {
        target = targetFuture.get();
      } catch (Throwable t) {
        return CalculationJobResultItem.failure(t);
      } finally {
        s_resolveTarget.leave();
      }
      if (target == null) {
        return CalculationJobResultItem.failure(ERROR_CANT_RESOLVE, "Unable to resolve target " + jobItem.getComputationTargetSpecification());
      }
    }
    // execute
    statistics.beginInvocation();
    final Set<ValueSpecification> outputs = jobItem.getOutputs();
    Collection<ComputedValue> results;
    try {
      s_invoke.enter();
      results = invoker.execute(getFunctionExecutionContext(), functionInputs, target, plat2290(outputs));
    } catch (Throwable t) {
      return itemResult.withFailure(t);
    } finally {
      s_invoke.leave();
    }
    if (results == null) {
      return itemResult.withFailure(ERROR_INVOKING, "No results returned by invoker " + invoker);
    }
    statistics.endInvocation();
    statistics.setFunctionIdentifier(functionUniqueId);
    statistics.setExpectedDataOutputSamples(results.size());
    // store results
    missing.clear();
    missing.addAll(outputs);
    for (ComputedValue result : results) {
      final ValueSpecification resultSpec = result.getSpecification();
      if (!missing.remove(resultSpec)) {
        s_logger.debug("Function produced non-requested result {}", resultSpec);
      }
    }
    if (!missing.isEmpty()) {
      final Collection<ComputedValue> newResults = new ArrayList<ComputedValue>(results.size() + missing.size());
      newResults.addAll(results);
      for (ValueSpecification output : missing) {
        newResults.add(new ComputedValue(output, NotCalculatedSentinel.EVALUATION_ERROR));
      }
      results = newResults;
      itemResult = itemResult.withMissingOutputs(missing);
    }
    cache.putValues(results, statistics);
    return itemResult;
  }
}
