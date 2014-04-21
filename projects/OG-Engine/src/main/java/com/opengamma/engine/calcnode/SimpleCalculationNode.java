/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.DeferredViewComputationCache;
import com.opengamma.engine.cache.DirectWriteViewComputationCache;
import com.opengamma.engine.cache.MissingOutput;
import com.opengamma.engine.cache.ViewComputationCache;
import com.opengamma.engine.cache.ViewComputationCacheSource;
import com.opengamma.engine.cache.WriteBehindViewComputationCache;
import com.opengamma.engine.calcnode.stats.FunctionInvocationStatisticsGatherer;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInputsImpl;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.function.TargetSourcingFunction;
import com.opengamma.engine.function.blacklist.DummyFunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.DummyFunctionBlacklistQuery;
import com.opengamma.engine.function.blacklist.FunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.engine.function.blacklist.FunctionBlacklistedException;
import com.opengamma.engine.target.ComputationTargetResolverUtils;
import com.opengamma.engine.target.lazy.LazyComputationTargetResolver;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.InetAddressUtils;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.async.AsynchronousOperation;
import com.opengamma.util.async.AsynchronousResult;
import com.opengamma.util.async.ResultListener;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * A calculation node implementation. The node can only be used by one thread - i.e. executeJob cannot be called concurrently to do multiple jobs. To execute multiple jobs concurrently separate
 * calculation nodes must be used.
 * <p>
 * The function repository (and anything else) must be properly initialized and ready for use by the node when it receives its first job. Responsibility for initialization should therefore lie with
 * whatever will logically be dispatching jobs. This is typically a {@link ViewProcessor} for local nodes or a {@link RemoteNodeClient} for remote nodes.
 */
public class SimpleCalculationNode extends SimpleCalculationNodeState implements CalculationNode {

  private static final class InputSpecificationsWrapper extends AbstractCollection<ValueSpecification> {

    private ValueSpecification[] _inputs;

    @Override
    public Iterator<ValueSpecification> iterator() {
      return new Iterator<ValueSpecification>() {
        private int _index;

        @Override
        public boolean hasNext() {
          return _index < _inputs.length;
        }

        @Override
        public ValueSpecification next() {
          return _inputs[_index++];
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }

      };
    }

    @Override
    public int size() {
      return _inputs.length;
    }

  };

  /**
   * Interface used by deferred executions. Any {@link AsynchronousExecution} exceptions thrown by this class will supply either an instance of {@code Deferred} on the original method type, or the
   * original method type. Instances of {@code Deferred} are not bound (tightly) to their original calculation node instance allowing them to run on another (for example if the original node is now
   * being reused for another job). Thus the caller must supply the node that will now host the deferred operation. The state from the original node must be preserved and then restored into the new
   * host node.
   * 
   * @param <T> the original return type of the asynchronous method
   */
  public interface Deferred<T> {

    T call(SimpleCalculationNode self) throws AsynchronousExecution;

  }

  private static final String ERROR_CANT_RESOLVE = "com.opengamma.engine.calcnode.InvalidTargetException";
  private static final String ERROR_BAD_FUNCTION = "com.opengamma.engine.calcnode.InvalidFunctionException";
  private static final String ERROR_INVOKING = "com.opengamma.engine.calcnode.InvalidInvocationException";

  private static final Logger s_logger = LoggerFactory.getLogger(SimpleCalculationNode.class);
  private static int s_nodeUniqueID;

  /**
   * The deferred caches for performing write-behind and late write completion (asynchronous flush) to the value cache. The map contains weak values so that entries can be cleared when the deferred
   * cache is dead (i.e. no calculation node is currently using it and its writer is not running).
   */
  private static final ConcurrentMap<ViewComputationCache, DeferredViewComputationCache> s_deferredCaches = new MapMaker().weakValues().makeMap();

  private final ViewComputationCacheSource _cacheSource;
  private final CompiledFunctionService _functionCompilationService;
  private final FunctionInvocationStatisticsGatherer _functionInvocationStatistics;
  private final String _nodeId;
  private final ExecutorService _executorService;
  private final CalculationNodeLogEventListener _logListener;
  private final InputSpecificationsWrapper _inputs = new InputSpecificationsWrapper();
  private boolean _writeBehindSharedCache;
  private boolean _writeBehindPrivateCache;
  private boolean _asynchronousTargetResolve;
  private FunctionBlacklistQuery _blacklistQuery = new DummyFunctionBlacklistQuery();
  private FunctionBlacklistMaintainer _blacklistUpdate = new DummyFunctionBlacklistMaintainer();
  private MaximumJobItemExecutionWatchdog _maxJobItemExecution = new MaximumJobItemExecutionWatchdog();

  public SimpleCalculationNode(final ViewComputationCacheSource cacheSource, final CompiledFunctionService functionCompilationService,
      final FunctionExecutionContext functionExecutionContext, final String nodeId, final ExecutorService executorService, final FunctionInvocationStatisticsGatherer functionInvocationStatistics,
      final CalculationNodeLogEventListener logListener) {
    super(functionExecutionContext);
    ArgumentChecker.notNull(cacheSource, "cacheSource");
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    ArgumentChecker.notNull(functionExecutionContext, "functionExecutionContext");
    ArgumentChecker.notNull(nodeId, "nodeId");
    ArgumentChecker.notNull(functionInvocationStatistics, "functionInvocationStatistics");
    ArgumentChecker.notNull(logListener, "logListener");
    _cacheSource = cacheSource;
    _functionCompilationService = functionCompilationService;
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

  public ComputationTargetResolver getRawTargetResolver() {
    return getFunctionCompilationService().getFunctionCompilationContext().getRawComputationTargetResolver();
  }

  protected ComputationTargetResolver.AtVersionCorrection getTargetResolver() {
    return getFunctionExecutionContext().getComputationTargetResolver();
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
    final long executionTime = System.nanoTime() - getExecutionStartTime();
    final CalculationJobResult jobResult = new CalculationJobResult(getJob().getSpecification(), executionTime, resultItems, getNodeId());
    s_logger.info("Executed {} in {}ns", getJob(), executionTime);
    try {
      getCache().flush();
    } catch (final AsynchronousExecution e) {
      s_logger.info("Starting cache flush at {}", _nodeId);
      final AsynchronousOperation<CalculationJobResult> async = AsynchronousOperation.create(CalculationJobResult.class);
      e.setResultListener(new ResultListener<Void>() {
        @Override
        public void operationComplete(final AsynchronousResult<Void> result) {
          try {
            result.getResult();
            async.getCallback().setResult(jobResult);
          } catch (final RuntimeException e) {
            async.getCallback().setException(e);
          }
          s_logger.info("Finished cache flush at {}", _nodeId);
        }
      });
      return async.getResult();
    }
    return jobResult;
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  private static <T> AsynchronousOperation<Deferred<T>> deferredOperation() {
    return (AsynchronousOperation) AsynchronousOperation.create(Deferred.class);
  }

  /**
   * Invokes all of the items from a calculation job on this node. If asynchronous execution occurs, the {@link AsynchronousExecution} will report either a {@code Deferred&lt;CalculationJobResult&gt;}
   * or a {@code CalculationJobResult}. This is to allow the node to be reused if calculations are running externally and for tail job executions on the local nodes to start immediately but block the
   * central dispatcher until the cache has been flushed.
   * <p>
   * If a deferred result is returned, and this node will be used for other work, its state must be saved and then later restored into a node that will be used to complete the deferred action.
   * 
   * @param job the job to execute
   * @return the job result
   * @throws AsynchronousExecution if the job is completing asynchronously
   */
  public CalculationJobResult executeJob(final CalculationJob job) throws AsynchronousExecution {
    s_logger.info("Executing {} on {}", job, _nodeId);
    setJob(job);
    final CalculationJobSpecification spec = job.getSpecification();
    getFunctionExecutionContext().setValuationTime(spec.getValuationTime());
    getFunctionExecutionContext().setValuationClock(DateUtils.fixedClockUTC(spec.getValuationTime()));
    getFunctionExecutionContext().setComputationTargetResolver(getRawTargetResolver().atVersionCorrection(job.getResolverVersionCorrection()));
    setFunctions(getFunctionCompilationService().compileFunctionRepository(spec.getValuationTime()));
    setCache(getDeferredViewComputationCache(getCache(spec)));
    setExecutionStartTime(System.nanoTime());
    setConfiguration(spec.getCalcConfigName());
    List<CalculationJobResultItem> jobItems;
    try {
      jobItems = executeJobItems();
    } catch (final AsynchronousExecution e) {
      s_logger.debug("Asynchronous job execution at {}", _nodeId);
      final AsynchronousOperation<Deferred<CalculationJobResult>> async = deferredOperation();
      e.setResultListener(new ResultListener<Deferred<List<CalculationJobResultItem>>>() {
        @Override
        public void operationComplete(final AsynchronousResult<Deferred<List<CalculationJobResultItem>>> result) {
          s_logger.debug("Asynchronous job execution result available");
          try {
            async.getCallback().setResult(new ExecuteJobFinish(result.getResult()));
          } catch (final RuntimeException e) {
            async.getCallback().setException(e);
          }
        }
      });
      return async.getResult().call(this);
    }
    return executeJobResult(jobItems);
  }

  private static class ExecuteJobFinish implements Deferred<CalculationJobResult> {

    private final Deferred<List<CalculationJobResultItem>> _deferredExecute;

    public ExecuteJobFinish(final Deferred<List<CalculationJobResultItem>> deferredExecute) {
      _deferredExecute = deferredExecute;
    }

    @Override
    public CalculationJobResult call(final SimpleCalculationNode self) throws AsynchronousExecution {
      s_logger.debug("Asynchronous job execution result at {}", self._nodeId);
      List<CalculationJobResultItem> jobItems;
      try {
        jobItems = _deferredExecute.call(self);
      } catch (final AsynchronousExecution e) {
        s_logger.debug("Asynchronous execution of job remainder at {}", self._nodeId);
        final AsynchronousOperation<Deferred<CalculationJobResult>> async = deferredOperation();
        e.setResultListener(new ResultListener<Deferred<List<CalculationJobResultItem>>>() {
          @Override
          public void operationComplete(final AsynchronousResult<Deferred<List<CalculationJobResultItem>>> result) {
            s_logger.debug("Asynchronous job execution result available");
            try {
              async.getCallback().setResult(new ExecuteJobFinish(result.getResult()));
            } catch (final RuntimeException e) {
              async.getCallback().setException(e);
            }
          }
        });
        return async.getResult().call(self);
      }
      return self.executeJobResult(jobItems);
    }

  }

  private void postEvaluationErrors(final ValueSpecification[] outputs, final MissingOutput type) {
    final Collection<ComputedValue> results = new ArrayList<ComputedValue>(outputs.length);
    for (final ValueSpecification output : outputs) {
      results.add(new ComputedValue(output, type));
    }
    getCache().putValues(results, getJob().getCacheSelectHint());
  }

  private void invocationBlacklisted(final CalculationJobItem jobItem, final CalculationJobResultItemBuilder resultItemBuilder) {
    postEvaluationErrors(jobItem.getOutputs(), MissingOutput.SUPPRESSED);
    resultItemBuilder.withSuppression();
  }

  private void invocationFailure(final Throwable t, final CalculationJobItem jobItem, final CalculationJobResultItemBuilder resultItemBuilder) {
    s_logger.error("Caught exception", t);
    getFunctionBlacklistUpdate().failedJobItem(jobItem);
    postEvaluationErrors(jobItem.getOutputs(), MissingOutput.EVALUATION_ERROR);
    resultItemBuilder.withException(t);
  }

  //-------------------------------------------------------------------------
  private void attachLog(final MutableExecutionLog log) {
    getLogListener().attach(log);
  }

  private void detachLog() {
    getLogListener().detach();
  }

  /**
   * Executes one or more items from the supplied iterator, populating the supplied list. If a job item starts running asynchronously, an exception will be thrown. At resumption of the operation,
   * another call to this method will occur with the same parameters allowing it to continue with the remaining items the iterator has.
   * 
   * @param jobItemItr the job items to execute, not null
   * @param resultItems the list to populate with results, not null
   */
  private void executeJobItems(final Iterator<CalculationJobItem> jobItemItr, final List<CalculationJobResultItem> resultItems) throws AsynchronousExecution {
    while (jobItemItr.hasNext()) {
      if (getJob().isCancelled()) {
        throw new CancellationException();
      }
      final CalculationJobItem jobItem = jobItemItr.next();
      // TODO: start resolving the next target while this item executes -- can we "poll" an iterator?
      final MutableExecutionLog executionLog = new MutableExecutionLog(jobItem.getLogMode());
      final CalculationJobResultItemBuilder resultItemBuilder = CalculationJobResultItemBuilder.of(executionLog);
      if (getFunctionBlacklistQuery().isBlacklisted(jobItem)) {
        invocationBlacklisted(jobItem, resultItemBuilder);
      } else {
        getMaxJobItemExecution().jobExecutionStarted(jobItem);
        attachLog(executionLog);
        boolean logAttached = true;
        try {
          invoke(jobItem, new DeferredInvocationStatistics(getFunctionInvocationStatistics(), getConfiguration(), jobItem.getFunctionUniqueIdentifier()), resultItemBuilder);
        } catch (final AsynchronousExecution e) {
          s_logger.debug("Asynchronous job item invocation at {}", _nodeId);
          detachLog();
          getMaxJobItemExecution().jobExecutionStopped();
          logAttached = false;
          final AsynchronousOperation<Deferred<Void>> async = deferredOperation();
          final ExecuteJobItemsInvoke invoke = new ExecuteJobItemsInvoke(jobItem, executionLog, resultItemBuilder);
          e.setResultListener(new ResultListener<Deferred<Void>>() {
            @Override
            public void operationComplete(final AsynchronousResult<Deferred<Void>> result) {
              s_logger.debug("Asynchronous job item result available");
              try {
                async.getCallback().setResult(new ExecuteJobItemsResume(resultItems, invoke, result.getResult(), jobItemItr));
              } catch (final RuntimeException e) {
                async.getCallback().setException(e);
              }
            }
          });
          final Deferred<Void> inline = async.getResult();
          // If the result is already available we can call the deferred block inline. The log and watchdog are already attached,
          // and we don't want it to make a recursive call into this method (which means it won't throw AsynchronousExecution).
          invoke.inline();
          inline.call(this);
          continue;
        } catch (final Throwable t) {
          invocationFailure(t, jobItem, resultItemBuilder);
        } finally {
          if (logAttached) {
            detachLog();
            getMaxJobItemExecution().jobExecutionStopped();
          }
        }
      }
      resultItems.add(resultItemBuilder.toResultItem());
    }
  }

  private static class ExecuteJobItemsInvoke {

    private final CalculationJobItem _jobItem;
    private MutableExecutionLog _executionLog;
    private final CalculationJobResultItemBuilder _resultItemBuilder;

    public ExecuteJobItemsInvoke(final CalculationJobItem jobItem, final MutableExecutionLog executionLog, final CalculationJobResultItemBuilder resultItemBuilder) {
      _jobItem = jobItem;
      _executionLog = executionLog;
      _resultItemBuilder = resultItemBuilder;
    }

    public void inline() {
      _executionLog = null;
    }

    public boolean isInline() {
      return _executionLog == null;
    }

    public CalculationJobResultItem call(final SimpleCalculationNode self, final Deferred<Void> deferredInvoke) {
      if (_executionLog != null) {
        self.getMaxJobItemExecution().jobExecutionStarted(_jobItem);
        self.attachLog(_executionLog);
      }
      try {
        deferredInvoke.call(self);
      } catch (final Throwable t) {
        self.invocationFailure(t, _jobItem, _resultItemBuilder);
      } finally {
        if (_executionLog != null) {
          self.detachLog();
          self.getMaxJobItemExecution().jobExecutionStopped();
        }
      }
      return _resultItemBuilder.toResultItem();
    }

  }

  private static class ExecuteJobItemsResume implements Deferred<Void> {

    private final List<CalculationJobResultItem> _resultItems;
    private ExecuteJobItemsInvoke _invoke;
    private Deferred<Void> _deferredInvoke;
    private final Iterator<CalculationJobItem> _jobItemItr;

    public ExecuteJobItemsResume(final List<CalculationJobResultItem> resultItems, final ExecuteJobItemsInvoke invoke, final Deferred<Void> deferredInvoke,
        final Iterator<CalculationJobItem> jobItemItr) {
      _resultItems = resultItems;
      _invoke = invoke;
      _deferredInvoke = deferredInvoke;
      _jobItemItr = jobItemItr;
    }

    @Override
    public Void call(final SimpleCalculationNode self) throws AsynchronousExecution {
      s_logger.debug("Asynchronous job item result at {}", self._nodeId);
      _resultItems.add(_invoke.call(self, _deferredInvoke));
      if (!_invoke.isInline()) {
        _invoke = null;
        _deferredInvoke = null;
        self.executeJobItems(_jobItemItr, _resultItems);
      }
      return null;
    }

  }

  private List<CalculationJobResultItem> executeJobItems() throws AsynchronousExecution {
    final List<CalculationJobItem> jobItems = getJob().getJobItems();
    final List<CalculationJobResultItem> resultItems = new ArrayList<CalculationJobResultItem>(jobItems.size());
    try {
      executeJobItems(jobItems.iterator(), resultItems);
      return resultItems;
    } catch (final AsynchronousExecution e) {
      s_logger.debug("Asynchronous execution of remaining job items at {}", _nodeId);
      final AsynchronousOperation<Deferred<List<CalculationJobResultItem>>> async = deferredOperation();
      e.setResultListener(new ResultListener<Deferred<Void>>() {
        @Override
        public void operationComplete(final AsynchronousResult<Deferred<Void>> result) {
          s_logger.debug("Asynchronous job item results available");
          try {
            async.getCallback().setResult(new ExecuteJobItemsFinish(resultItems, result.getResult()));
          } catch (final RuntimeException e) {
            async.getCallback().setException(e);
          }
        }
      });
      return async.getResult().call(this);
    }
  }

  private static class ExecuteJobItemsFinish implements Deferred<List<CalculationJobResultItem>> {

    private final List<CalculationJobResultItem> _results;
    private final Deferred<Void> _deferredResult;

    public ExecuteJobItemsFinish(final List<CalculationJobResultItem> results, final Deferred<Void> deferredResult) {
      _results = results;
      _deferredResult = deferredResult;
    }

    @Override
    public List<CalculationJobResultItem> call(final SimpleCalculationNode self) throws AsynchronousExecution {
      s_logger.debug("Asynchronous job item results at {}", self._nodeId);
      try {
        _deferredResult.call(self);
        return _results;
      } catch (final AsynchronousExecution e) {
        s_logger.debug("Asynchronous execution of remaining job items at {}", self._nodeId);
        final AsynchronousOperation<Deferred<List<CalculationJobResultItem>>> async = deferredOperation();
        e.setResultListener(new ResultListener<Deferred<Void>>() {
          @Override
          public void operationComplete(final AsynchronousResult<Deferred<Void>> result) {
            s_logger.debug("Asynchronous job item results available");
            try {
              async.getCallback().setResult(new ExecuteJobItemsFinish(_results, result.getResult()));
            } catch (final RuntimeException e) {
              async.getCallback().setException(e);
            }
          }
        });
        return async.getResult().call(self);
      }
    }

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
  public ViewComputationCache getCache(final CalculationJobSpecification spec) {
    final ViewComputationCache cache = getCacheSource().getCache(spec.getViewCycleId(), spec.getCalcConfigName());
    return cache;
  }

  @SuppressWarnings("deprecation")
  private static Set<ValueRequirement> plat2290(final ValueSpecification[] outputs) {
    final Set<ValueRequirement> result = Sets.newHashSetWithExpectedSize(outputs.length);
    for (final ValueSpecification output : outputs) {
      result.add(output.toRequirementSpecification());
    }
    return result;
  }

  private void invokeResult(final FunctionInvoker invoker, final DeferredInvocationStatistics statistics,
      final Set<ValueSpecification> missing, final ValueSpecification[] outputs, final Collection<ComputedValue> results, final CalculationJobResultItemBuilder resultItemBuilder) {
    if (results == null) {
      postEvaluationErrors(outputs, MissingOutput.EVALUATION_ERROR);
      resultItemBuilder.withException(ERROR_INVOKING, "No results returned by invoker " + invoker);
      return;
    }
    statistics.endInvocation();
    statistics.setExpectedDataOutputSamples(results.size());
    removeInvocationLoggingInfo();
    // store results
    missing.clear();
    for (ValueSpecification output : outputs) {
      missing.add(output);
    }
    final Collection<ComputedValue> newResults = new ArrayList<ComputedValue>(outputs.length);
    for (ComputedValue result : results) {
      ValueSpecification resultSpec = result.getSpecification();
      final ComputationTargetSpecification targetSpec = ComputationTargetResolverUtils.simplifyType(resultSpec.getTargetSpecification(), getRawTargetResolver());
      if (targetSpec != resultSpec.getTargetSpecification()) {
        resultSpec = new ValueSpecification(resultSpec.getValueName(), targetSpec, resultSpec.getProperties());
        result = new ComputedValue(resultSpec, result.getValue());
      }
      if (missing.remove(resultSpec)) {
        newResults.add(result);
      } else {
        s_logger.debug("Function {} produced non-requested result {}", invoker, resultSpec);
      }
    }
    if (!missing.isEmpty()) {
      for (final ValueSpecification output : missing) {
        s_logger.debug("Function {} didn't produce required result {}", invoker, output);
        newResults.add(new ComputedValue(output, MissingOutput.EVALUATION_ERROR));
      }
      resultItemBuilder.withMissingOutputs(missing);
    }
    getCache().putValues(newResults, getJob().getCacheSelectHint(), statistics);
  }

  private void invokeException(final ValueSpecification[] outputs, final Throwable t, final CalculationJobResultItemBuilder resultItemBuilder) {
    s_logger.error("Invocation error: {}", t.getMessage());
    s_logger.warn("Caught exception", t);
    postEvaluationErrors(outputs, MissingOutput.EVALUATION_ERROR);
    resultItemBuilder.withException(t);
  }

  private void invoke(final CalculationJobItem jobItem, final DeferredInvocationStatistics statistics, final CalculationJobResultItemBuilder resultItemBuilder) throws AsynchronousExecution {
    final ValueSpecification[] outputs = jobItem.getOutputs();
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
    }
    final FunctionInvoker invoker = getFunctions().getInvoker(functionUniqueId);
    if (invoker == null) {
      postEvaluationErrors(outputs, MissingOutput.EVALUATION_ERROR);
      resultItemBuilder.withException(ERROR_BAD_FUNCTION, "Unable to locate " + functionUniqueId + " in function repository");
      return;
    }
    // set parameters
    getFunctionExecutionContext().setFunctionParameters(jobItem.getFunctionParameters());
    // assemble inputs
    final ValueSpecification[] inputValueSpecs = jobItem.getInputs();
    final Set<ValueSpecification> missing = Sets.newHashSetWithExpectedSize(inputValueSpecs.length);
    if (!isUseAsynchronousTargetResolve() && (target == null)) {
      if (invoker.canHandleMissingInputs()) {
        // A missing target is just a special case of missing input
        missing.add(TargetSourcingFunction.createSpecification(jobItem.getComputationTargetSpecification()));
      } else {
        postEvaluationErrors(outputs, MissingOutput.EVALUATION_ERROR);
        resultItemBuilder.withException(ERROR_CANT_RESOLVE, "Unable to resolve target " + jobItem.getComputationTargetSpecification());
        return;
      }
    }
    final Collection<ComputedValue> inputs = new ArrayList<ComputedValue>(inputValueSpecs.length);
    int inputBytes = 0;
    int inputSamples = 0;
    final DeferredViewComputationCache cache = getCache();
    _inputs._inputs = inputValueSpecs;
    for (final Pair<ValueSpecification, Object> input : cache.getValues(_inputs, getJob().getCacheSelectHint())) {
      if ((input.getSecond() == null) || (input.getSecond() instanceof MissingValue)) {
        missing.add(input.getFirst());
      } else {
        final ComputedValue value = new ComputedValue(input.getFirst(), input.getSecond());
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
        resultItemBuilder.withPartialInputs(missing);
      } else {
        s_logger.info("Not able to execute as missing inputs {}", missing);
        if (targetFuture != null) {
          // Cancelling doesn't do anything so we have to block and clear the result
          try {
            targetFuture.get();
          } catch (final Throwable t) {
            s_logger.warn("Error resolving target", t);
            postEvaluationErrors(outputs, MissingOutput.EVALUATION_ERROR);
            resultItemBuilder.withException(t);
            return;
          }
        }
        postEvaluationErrors(jobItem.getOutputs(), MissingOutput.MISSING_INPUTS);
        resultItemBuilder.withMissingInputs(missing);
        return;
      }
    }
    final FunctionInputs functionInputs = new FunctionInputsImpl(getTargetResolver().getSpecificationResolver(), inputs, missing);
    if (target == null) {
      try {
        target = targetFuture.get();
      } catch (final Throwable t) {
        s_logger.warn("Error resolving target", t);
        postEvaluationErrors(outputs, MissingOutput.EVALUATION_ERROR);
        resultItemBuilder.withException(t);
        return;
      }
      if (target == null) {
        if (invoker.canHandleMissingInputs()) {
          // A missing target is just a special case of missing input
          missing.add(new ValueSpecification(ValueRequirementNames.TARGET, jobItem.getComputationTargetSpecification(), ValueProperties.with(ValuePropertyNames.FUNCTION, "TargetSourcingFunction")
              .get()));
        } else {
          postEvaluationErrors(outputs, MissingOutput.EVALUATION_ERROR);
          resultItemBuilder.withException(ERROR_CANT_RESOLVE, "Unable to resolve target " + jobItem.getComputationTargetSpecification());
          return;
        }
      }
    }
    // Execute
    statistics.beginInvocation();
    recordInvocationLoggingInfo(target);
    Set<ComputedValue> result;
    try {
      result = invoker.execute(getFunctionExecutionContext(), functionInputs, target, plat2290(outputs));
    } catch (final AsynchronousExecution e) {
      s_logger.debug("Asynchronous execution of {} at {}", jobItem, _nodeId);
      final AsynchronousOperation<Deferred<Void>> async = deferredOperation();
      e.setResultListener(new ResultListener<Set<ComputedValue>>() {
        @Override
        public void operationComplete(final AsynchronousResult<Set<ComputedValue>> result) {
          s_logger.debug("Job item {} result available", jobItem);
          async.getCallback().setResult(new Deferred<Void>() {
            @Override
            public Void call(final SimpleCalculationNode self) {
              s_logger.debug("Asynchronous result for {} at {}", jobItem, self._nodeId);
              Set<ComputedValue> results;
              try {
                results = result.getResult();
              } catch (final FunctionBlacklistedException e) {
                self.invocationBlacklisted(jobItem, resultItemBuilder);
                return null;
              } catch (final Throwable t) {
                self.invokeException(outputs, t, resultItemBuilder);
                return null;
              }
              self.invokeResult(invoker, statistics, missing, outputs, results, resultItemBuilder);
              return null;
            }
          });
        }
      });
      async.getResult().call(this);
      return;
    } catch (final FunctionBlacklistedException e) {
      invocationBlacklisted(jobItem, resultItemBuilder);
      return;
    } catch (final Throwable t) {
      invokeException(outputs, t, resultItemBuilder);
      return;
    }
    invokeResult(invoker, statistics, missing, outputs, result, resultItemBuilder);
  }

  private void recordInvocationLoggingInfo(ComputationTarget target) {
    if (target != null && target.getUniqueId() != null) {
      try {      // make target available to logging
        MDC.put("target", target.getUniqueId().toString());
      } catch (Throwable ex) {
        // pass
      }
    }
  }

  private void removeInvocationLoggingInfo() {
    try {
      MDC.remove("target");
    } catch (Throwable ex) {
      // pass
    }
  }

}
