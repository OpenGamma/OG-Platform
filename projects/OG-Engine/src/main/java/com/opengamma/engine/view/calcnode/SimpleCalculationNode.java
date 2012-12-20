/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInputsImpl;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.blacklist.DummyFunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.DummyFunctionBlacklistQuery;
import com.opengamma.engine.function.blacklist.FunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.engine.function.blacklist.FunctionBlacklistedException;
import com.opengamma.engine.target.LazyComputationTargetResolver;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.cache.DeferredViewComputationCache;
import com.opengamma.engine.view.cache.DirectWriteViewComputationCache;
import com.opengamma.engine.view.cache.NotCalculatedSentinel;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.cache.WriteBehindViewComputationCache;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatisticsGatherer;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.InetAddressUtils;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.async.AsynchronousHandle;
import com.opengamma.util.async.AsynchronousHandleExecution;
import com.opengamma.util.async.AsynchronousHandleOperation;
import com.opengamma.util.async.AsynchronousOperation;
import com.opengamma.util.async.AsynchronousResult;
import com.opengamma.util.async.ResultListener;
import com.opengamma.util.time.DateUtils;
import com.opengamma.lambdava.tuple.Pair;

/**
 * A calculation node implementation. The node can only be used by one thread - i.e. executeJob cannot be called concurrently to do multiple jobs. To execute multiple jobs concurrently separate
 * calculation nodes must be used.
 * <p>
 * The function repository (and anything else) must be properly initialized and ready for use by the node when it receives its first job. Responsibility for initialization should therefore lie with
 * whatever will logically be dispatching jobs. This is typically a {@link ViewProcessor} for local nodes or a {@link RemoteNodeClient} for remote nodes.
 */
public class SimpleCalculationNode extends SimpleCalculationNodeState implements CalculationNode {

  private static final String ERROR_CANT_RESOLVE = "com.opengamma.engine.view.calcnode.InvalidTargetException";
  private static final String ERROR_BAD_FUNCTION = "com.opengamma.engine.view.calcnode.InvalidFunctionException";
  private static final String ERROR_INVOKING = "com.opengamma.engine.view.calcnode.InvalidInvocationException";

  private static final Logger s_logger = LoggerFactory.getLogger(SimpleCalculationNode.class);
  private static int s_nodeUniqueID;

  /**
   * The deferred caches for performing write-behind and late write completion (asynchronous flush) to the value cache. The map contains weak values so that entries can be cleared when the deferred
   * cache is dead (i.e. no calculation node is currently using it and its writer is not running).
   */
  private static final ConcurrentMap<ViewComputationCache, DeferredViewComputationCache> s_deferredCaches = new MapMaker().weakValues().makeMap();

  private final ViewComputationCacheSource _cacheSource;
  private final CompiledFunctionService _functionCompilationService;
  private final ComputationTargetResolver _targetResolver;
  private final ViewProcessorQuerySender _viewProcessorQuerySender;
  private final FunctionInvocationStatisticsGatherer _functionInvocationStatistics;
  private final String _nodeId;
  private final ExecutorService _executorService;
  private final CalculationNodeLogEventListener _logListener;
  private boolean _writeBehindSharedCache;
  private boolean _writeBehindPrivateCache;
  private boolean _asynchronousTargetResolve;
  private FunctionBlacklistQuery _blacklistQuery = new DummyFunctionBlacklistQuery();
  private FunctionBlacklistMaintainer _blacklistUpdate = new DummyFunctionBlacklistMaintainer();
  private MaximumJobItemExecutionWatchdog _maxJobItemExecution = new MaximumJobItemExecutionWatchdog();

  public SimpleCalculationNode(ViewComputationCacheSource cacheSource, CompiledFunctionService functionCompilationService,
      FunctionExecutionContext functionExecutionContext, ComputationTargetResolver targetResolver, ViewProcessorQuerySender calcNodeQuerySender, String nodeId,
      ExecutorService executorService, FunctionInvocationStatisticsGatherer functionInvocationStatistics, CalculationNodeLogEventListener logListener) {
    super(functionExecutionContext);
    ArgumentChecker.notNull(cacheSource, "cacheSource");
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    ArgumentChecker.notNull(functionExecutionContext, "functionExecutionContext");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    ArgumentChecker.notNull(calcNodeQuerySender, "calcNodeQuerySender");
    ArgumentChecker.notNull(nodeId, "nodeId");
    ArgumentChecker.notNull(functionInvocationStatistics, "functionInvocationStatistics");
    ArgumentChecker.notNull(logListener, "logListener");
    _cacheSource = cacheSource;
    _functionCompilationService = functionCompilationService;
    _targetResolver = targetResolver;
    _viewProcessorQuerySender = calcNodeQuerySender;
    _nodeId = nodeId;
    _executorService = executorService;
    _functionInvocationStatistics = functionInvocationStatistics;
    _logListener = logListener;
  }

  //-------------------------------------------------------------------------
  public ViewComputationCacheSource getCacheSource() {
    return _cacheSource;
  }

  public CompiledFunctionService getFunctionCompilationService() {
    return _functionCompilationService;
  }

  public ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }

  public ViewProcessorQuerySender getViewProcessorQuerySender() {
    return _viewProcessorQuerySender;
  }

  public boolean isUseWriteBehindSharedCache() {
    return _writeBehindSharedCache;
  }

  public boolean isUseWriteBehindPrivateCache() {
    return _writeBehindPrivateCache;
  }

  /**
   * Sets whether to use a write-behind strategy when writing to the shared value cache. Write-behind can work well if the cost of writing is high (e.g. network overhead). If the write is cheap (e.g.
   * to an in-process, in-memory store) then the overheads of the write-behind become a burden. An executor service must be available if this is selected.
   * 
   * @param writeBehind true to use write-behind on the shared value cache, false not to
   */
  public void setUseWriteBehindSharedCache(final boolean writeBehind) {
    if (writeBehind) {
      if (getExecutorService() == null) {
        throw new IllegalArgumentException("Can't use write behind cache without an executor service");
      }
    }
    _writeBehindSharedCache = writeBehind;
  }

  /**
   * Sets whether to use a write-behind strategy when writing to the private value cache. Write-behind can work well if the cost of writing is high (e.g. disk overhead). If the write is cheap (e.g. to
   * an in-process, in-memory store) then the overheads of the write-behind become a burden. An executor service must be available if this is selected.
   * 
   * @param writeBehind true to use write-behind on the private value cache, false not to
   */
  public void setUseWriteBehindPrivateCache(final boolean writeBehind) {
    if (writeBehind) {
      if (getExecutorService() == null) {
        throw new IllegalArgumentException("Can't use write behind cache without an executor service");
      }
    }
    _writeBehindPrivateCache = writeBehind;
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

  public ExecutorService getExecutorService() {
    return _executorService;
  }

  public FunctionInvocationStatisticsGatherer getFunctionInvocationStatistics() {
    return _functionInvocationStatistics;
  }

  /**
   * Sets a function blacklist to use for checking each job invocation. The blacklist(s) used may suppress particular functions on this node, the logical group of nodes or at all nodes.
   * 
   * @param query the blacklist to query against, not null
   */
  public void setFunctionBlacklistQuery(final FunctionBlacklistQuery query) {
    ArgumentChecker.notNull(query, "query");
    _blacklistQuery = query;
  }

  /**
   * Returns the function blacklist used for checking each job invocation.
   * 
   * @return the blacklist queried, not null
   */
  public FunctionBlacklistQuery getFunctionBlacklistQuery() {
    return _blacklistQuery;
  }

  /**
   * Sets a maintenance policy for updating one or more blacklist(s) when a job item throws an exception. The application of a policy at this point will depend on the nature of the function library.
   * Issuing updates from the function's {@link FunctionInvoker} can allow more specific control over how a function failure should be reported.
   * 
   * @param update the maintainer to notify of a job item that threw an exception
   */
  public void setFunctionBlacklistUpdate(final FunctionBlacklistMaintainer update) {
    ArgumentChecker.notNull(update, "update");
    _blacklistUpdate = update;
  }

  public FunctionBlacklistMaintainer getFunctionBlacklistUpdate() {
    return _blacklistUpdate;
  }

  public void setMaxJobItemExecution(final MaximumJobItemExecutionWatchdog maxJobItemExecution) {
    ArgumentChecker.notNull(maxJobItemExecution, "maxJobItemExecution");
    _maxJobItemExecution = maxJobItemExecution;
  }

  public MaximumJobItemExecutionWatchdog getMaxJobItemExecution() {
    return _maxJobItemExecution;
  }
  
  public CalculationNodeLogEventListener getLogListener() {
    return _logListener;
  }

  @Override
  public String getNodeId() {
    return _nodeId;
  }

  public static synchronized String createNodeId() {
    final StringBuilder sb = new StringBuilder();
    sb.append(InetAddressUtils.getLocalHostName());
    sb.append('/');
    sb.append(System.getProperty("opengamma.node.id", "0"));
    sb.append('/');
    sb.append(++s_nodeUniqueID);
    return sb.toString();
  }

  //-------------------------------------------------------------------------
  private CalculationJobResult executeJobResult(final List<CalculationJobResultItem> resultItems) throws AsynchronousExecution {
    if (resultItems == null) {
      return null;
    }
    final long executionTime = System.nanoTime() - getExecutionStartTime();
    final CalculationJobResult jobResult = new CalculationJobResult(getJob().getSpecification(), executionTime, resultItems, getNodeId());
    s_logger.info("Executed {} in {}ns", getJob(), executionTime);
    try {
      getCache().flush();
    } catch (AsynchronousExecution e) {
      final AsynchronousOperation<CalculationJobResult> async = new AsynchronousOperation<CalculationJobResult>();
      e.setResultListener(new ResultListener<Void>() {
        @Override
        public void operationComplete(final AsynchronousResult<Void> result) {
          try {
            result.getResult();
            async.getCallback().setResult(jobResult);
          } catch (RuntimeException e) {
            async.getCallback().setException(e);
          }
        }
      });
      return async.getResult();
    }
    return jobResult;
  }

  /**
   * Invokes all of the items from a calculation job on this node. If asynchronous execution occurs, the {@link AsynchronousHandleExecution} will report a handle so that it can be resumed when the job
   * next becomes runnable. If the write behind cache is being used then the {@link AsynchronousExecution} will contain the deferred completion job. This is to allow tail job executions on the local
   * nodes to start immediately but block the central dispatcher until the cache has been flushed.
   * 
   * @param job the job to execute
   * @return the job result
   * @throws AsynchronousHandleExecution if the job is completing asynchronously
   */
  public CalculationJobResult executeJob(final CalculationJob job) throws AsynchronousHandleExecution, AsynchronousExecution {
    s_logger.info("Executing {} on {}", job, _nodeId);
    setJob(job);
    final CalculationJobSpecification spec = job.getSpecification();
    getFunctionExecutionContext().setViewProcessorQuery(new ViewProcessorQuery(getViewProcessorQuerySender(), spec));
    getFunctionExecutionContext().setValuationTime(spec.getValuationTime());
    getFunctionExecutionContext().setValuationClock(DateUtils.fixedClockUTC(spec.getValuationTime()));
    setFunctions(getFunctionCompilationService().compileFunctionRepository(spec.getValuationTime()));
    setCache(getDeferredViewComputationCache(getCache(spec)));
    setExecutionStartTime(System.nanoTime());
    setConfiguration(spec.getCalcConfigName());
    List<CalculationJobResultItem> jobItems; 
    try {
      jobItems = executeJobItems();
    } catch (AsynchronousHandleExecution ex) {
      return executeJobAsyncResult(ex);
    }
    return executeJobResult(jobItems);
  }
  
  private CalculationJobResult executeJobAsyncResult(final AsynchronousHandleExecution ex) throws AsynchronousHandleExecution {
    final AsynchronousHandleOperation<CalculationJobResult> async = new AsynchronousHandleOperation<CalculationJobResult>();
    ex.setResultHandleListener(new ResultListener<AsynchronousHandle<List<CalculationJobResultItem>>>() {
      @Override
      public void operationComplete(final AsynchronousResult<AsynchronousHandle<List<CalculationJobResultItem>>> result) {
        try {
          final AsynchronousHandle<List<CalculationJobResultItem>> resultHandle = result.getResult();
          async.getCallback().setResult(new AsynchronousHandle<CalculationJobResult>() {
            @Override
            public CalculationJobResult get() throws AsynchronousHandleExecution {
              try {
                return executeJobResult(resultHandle.get());
              } catch (AsynchronousHandleExecution e) {
                return executeJobAsyncResult(e);
              } catch (AsynchronousExecution e) {
                return AsynchronousOperation.getResult(e);
              }
            }
          });
        } catch (RuntimeException e) {
          async.getCallback().setException(e);
        }
      }
    });
    return async.getHandleResult();
  }

  //-------------------------------------------------------------------------
  private void postEvaluationErrors(final Set<ValueSpecification> outputs, final NotCalculatedSentinel type) {
    final Collection<ComputedValue> results = new ArrayList<ComputedValue>(outputs.size());
    for (ValueSpecification output : outputs) {
      results.add(new ComputedValue(output, type));
    }
    getCache().putValues(results, getJob().getCacheSelectHint());
  }

  private void invocationBlacklisted(final CalculationJobItem jobItem, final CalculationJobResultItemBuilder resultItemBuilder) {
    final Set<ValueSpecification> outputs = jobItem.getOutputs();
    postEvaluationErrors(outputs, NotCalculatedSentinel.SUPPRESSED);
    resultItemBuilder.withSuppression();
  }

  private void invocationFailure(final Throwable t, final CalculationJobItem jobItem, final CalculationJobResultItemBuilder resultItemBuilder) {
    s_logger.error("Caught exception", t);
    getFunctionBlacklistUpdate().failedJobItem(jobItem);
    final Set<ValueSpecification> outputs = jobItem.getOutputs();
    postEvaluationErrors(outputs, NotCalculatedSentinel.EVALUATION_ERROR);
    resultItemBuilder.withException(t);
  }
  
  //-------------------------------------------------------------------------
  private void attachLog(MutableExecutionLog log) {
    getLogListener().attach(log);
  }
  
  private void detachLog() {
    getLogListener().detach();
  }

  //-------------------------------------------------------------------------
  private List<CalculationJobResultItem> executeJobItems(final Iterator<CalculationJobItem> jobItemItr, final List<CalculationJobResultItem> resultItems) throws AsynchronousHandleExecution {
    while (jobItemItr.hasNext()) {
      if (getJob().isCancelled()) {
        return null;
      }
      final CalculationJobItem jobItem = jobItemItr.next();
      // TODO: start resolving the next target while this item executes -- can we "poll" an iterator?
      final MutableExecutionLog executionLog = new MutableExecutionLog(jobItem.getLogMode());
      final CalculationJobResultItemBuilder resultItemBuilder = CalculationJobResultItemBuilder.of(executionLog);
      if (getFunctionBlacklistQuery().isBlacklisted(jobItem)) {
        invocationBlacklisted(jobItem, resultItemBuilder);
      } else {
        getMaxJobItemExecution().jobExecutionStarted(jobItem);
        try {
          // Can only use this thread's logs during the synchronous attempt
          attachLog(executionLog);
          try {
            invoke(jobItem, new DeferredInvocationStatistics(getFunctionInvocationStatistics(), getConfiguration()), resultItemBuilder);
          } finally {
            detachLog();
          }
        } catch (AsynchronousExecution e) {
          final AsynchronousHandleOperation<List<CalculationJobResultItem>> async = new AsynchronousHandleOperation<List<CalculationJobResultItem>>();
          e.setResultListener(new ResultListener<Void>() {
            @Override
            public void operationComplete(final AsynchronousResult<Void> result) {
              try {
                result.getResult();
              } catch (Throwable t) {
                invocationFailure(t, jobItem, resultItemBuilder);
              }
              resultItems.add(resultItemBuilder.toResultItem());
              async.getCallback().setResult(new AsynchronousHandle<List<CalculationJobResultItem>>() {
                @Override
                public List<CalculationJobResultItem> get() throws AsynchronousHandleExecution {
                  return executeJobItems(jobItemItr, resultItems);
                }
              });
            }
          });
          // Discard the handle -- it contains the same state that this loop already has
          async.getResultHandle();
          // Completed successfully so result item would have been added in the callback
          continue;
        } catch (Throwable t) {
          invocationFailure(t, jobItem, resultItemBuilder);
        } finally {
          getMaxJobItemExecution().jobExecutionStopped();
        }
      }
      resultItems.add(resultItemBuilder.toResultItem());
    }
    return resultItems;
  }

  private List<CalculationJobResultItem> executeJobItems() throws AsynchronousHandleExecution {
    return executeJobItems(getJob().getJobItems().iterator(), new ArrayList<CalculationJobResultItem>());
  }

  private DeferredViewComputationCache getDeferredViewComputationCache(final ViewComputationCache cache) {
    DeferredViewComputationCache deferred = s_deferredCaches.get(cache);
    if (deferred == null) {
      if (isUseWriteBehindSharedCache() || isUseWriteBehindPrivateCache()) {
        deferred = new WriteBehindViewComputationCache(cache, getExecutorService(), isUseWriteBehindSharedCache(), isUseWriteBehindPrivateCache());
      } else {
        deferred = new DirectWriteViewComputationCache(cache);
      }
      final DeferredViewComputationCache existing = s_deferredCaches.putIfAbsent(cache, deferred);
      if (existing != null) {
        return existing;
      }
    }
    return deferred;
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

  private void invokeResult(FunctionInvoker invoker, DeferredInvocationStatistics statistics,
      Set<ValueSpecification> missing, Set<ValueSpecification> outputs, Collection<ComputedValue> results, CalculationJobResultItemBuilder resultItemBuilder) {
    if (results == null) {
      postEvaluationErrors(outputs, NotCalculatedSentinel.EVALUATION_ERROR);
      resultItemBuilder.withException(ERROR_INVOKING, "No results returned by invoker " + invoker);
      return;
    }
    statistics.endInvocation();
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
      resultItemBuilder.withMissingOutputs(missing);
    }
    getCache().putValues(results, getJob().getCacheSelectHint(), statistics);
  }

  private void invoke(final CalculationJobItem jobItem, final DeferredInvocationStatistics statistics,
      final CalculationJobResultItemBuilder resultItemBuilder) throws AsynchronousExecution {
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
      target = LazyComputationTargetResolver.resolve(getTargetResolver(), jobItem.getComputationTargetSpecification());
      if (target == null) {
        resultItemBuilder.withException(ERROR_CANT_RESOLVE, "Unable to resolve target " + jobItem.getComputationTargetSpecification());
        return;
      }
    }
    final FunctionInvoker invoker = getFunctions().getInvoker(functionUniqueId);
    if (invoker == null) {
      resultItemBuilder.withException(ERROR_BAD_FUNCTION, "Unable to locate " + functionUniqueId + " in function repository");
      return;
    }
    // set parameters
    getFunctionExecutionContext().setFunctionParameters(jobItem.getFunctionParameters());
    // assemble inputs
    final Collection<ComputedValue> inputs = new HashSet<ComputedValue>();
    final Set<ValueSpecification> missing = new HashSet<ValueSpecification>();
    int inputBytes = 0;
    int inputSamples = 0;
    final DeferredViewComputationCache cache = getCache();
    for (Pair<ValueSpecification, Object> input : cache.getValues(jobItem.getInputs(), getJob().getCacheSelectHint())) {
      if ((input._2() == null) || (input._2() instanceof MissingInput)) {
        missing.add(input._1());
      } else {
        final ComputedValue value = new ComputedValue(input._1(), input._2());
        inputs.add(value);
        final Integer bytes = cache.estimateValueSize(value);
        if (bytes != null) {
          inputBytes += bytes;
          inputSamples++;
        }
      }
    }
    statistics.setDataInputBytes(inputBytes, inputSamples);
    if (!missing.isEmpty()) {
      if (invoker.canHandleMissingInputs()) {
        s_logger.debug("Executing even with missing inputs {}", missing);
        resultItemBuilder.withPartialInputs(new HashSet<ValueSpecification>(missing));
      } else {
        s_logger.info("Not able to execute as missing inputs {}", missing);
        if (targetFuture != null) {
          // Cancelling doesn't do anything so we have to block and clear the result
          try {
            targetFuture.get();
          } catch (Throwable t) {
            s_logger.warn("Error resolving target", t);
            resultItemBuilder.withException(t);
            return;
          }
        }
        postEvaluationErrors(jobItem.getOutputs(), NotCalculatedSentinel.MISSING_INPUTS);
        resultItemBuilder.withMissingInputs(missing);
        return;
      }
    }
    final FunctionInputs functionInputs = new FunctionInputsImpl(inputs, missing);
    if (target == null) {
      try {
        target = targetFuture.get();
      } catch (Throwable t) {
        s_logger.warn("Error resolving target", t);
        resultItemBuilder.withException(t);
        return;
      }
      if (target == null) {
        resultItemBuilder.withException(ERROR_CANT_RESOLVE, "Unable to resolve target " + jobItem.getComputationTargetSpecification());
        return;
      }
    }
    // Execute
    statistics.beginInvocation(functionUniqueId);
    final Set<ValueSpecification> outputs = jobItem.getOutputs();
    try {
      invokeResult(invoker, statistics, missing, outputs, invoker.execute(getFunctionExecutionContext(), functionInputs, target, plat2290(outputs)), resultItemBuilder);
    } catch (AsynchronousExecution e) {
      e.setResultListener(new ResultListener<Set<ComputedValue>>() {
        @Override
        public void operationComplete(final AsynchronousResult<Set<ComputedValue>> result) {
          try {
            invokeResult(invoker, statistics, missing, outputs, result.getResult(), resultItemBuilder);
          } catch (FunctionBlacklistedException e) {
            invocationBlacklisted(jobItem, resultItemBuilder);
          } catch (RuntimeException e) {
            resultItemBuilder.withException(e);
          }
        }
      });
    } catch (FunctionBlacklistedException e) {
      invocationBlacklisted(jobItem, resultItemBuilder);
    } catch (Throwable t) {
      s_logger.error("Invocation error: {}", t.getMessage());
      s_logger.warn("Caught exception", t);
      postEvaluationErrors(outputs, NotCalculatedSentinel.EVALUATION_ERROR);
      resultItemBuilder.withException(t);
    }
  }
}
