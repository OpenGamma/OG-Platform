/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.cache.ViewComputationCacheSource;
import com.opengamma.engine.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.engine.calcnode.stats.FunctionInvocationStatisticsGatherer;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.blacklist.FunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.log.LogBridge;
import com.opengamma.util.log.ThreadLocalLogEventListener;

/**
 * Creates more-or-less identical nodes.
 */
public class SimpleCalculationNodeFactory implements InitializingBean {

  private ViewComputationCacheSource _viewComputationCache;
  private CompiledFunctionService _functionCompilationService;
  private FunctionExecutionContext _functionExecutionContext;
  private ExecutorService _executorService;
  private FunctionInvocationStatisticsGatherer _statisticsGatherer = new DiscardingInvocationStatisticsGatherer();
  private String _nodeIdentifier;
  private boolean _useWriteBehindSharedCache;
  private boolean _useWriteBehindPrivateCache;
  private boolean _useAsynchronousTargetResolve;
  private FunctionBlacklistQuery _blacklistQuery;
  private FunctionBlacklistMaintainer _blacklistUpdate;
  private MaximumJobItemExecutionWatchdog _maxJobItemExecution;
  private final ThreadLocalLogEventListener _threadLocalLogListener;

  public SimpleCalculationNodeFactory() {
    _threadLocalLogListener = new ThreadLocalLogEventListener();
    LogBridge.getInstance().addListener(_threadLocalLogListener);
  }

  private int _uid;

  public ViewComputationCacheSource getViewComputationCache() {
    return _viewComputationCache;
  }

  public void setViewComputationCache(final ViewComputationCacheSource viewComputationCache) {
    ArgumentChecker.notNull(viewComputationCache, "viewComputationCache");
    _viewComputationCache = viewComputationCache;
  }

  public CompiledFunctionService getFunctionCompilationService() {
    return _functionCompilationService;
  }

  public void setFunctionCompilationService(final CompiledFunctionService functionCompilationService) {
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    _functionCompilationService = functionCompilationService;
  }

  public FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }

  public void setFunctionExecutionContext(final FunctionExecutionContext functionExecutionContext) {
    ArgumentChecker.notNull(functionExecutionContext, "functionExecutionContext");
    _functionExecutionContext = functionExecutionContext;
  }

  public ExecutorService getExecutorService() {
    return _executorService;
  }

  public void setExecutorService(final ExecutorService executorService) {
    _executorService = executorService;
  }

  public boolean isUseWriteBehindSharedCache() {
    return _useWriteBehindSharedCache;
  }

  public void setUseWriteBehindSharedCache(final boolean useWriteBehindCache) {
    _useWriteBehindSharedCache = useWriteBehindCache;
  }

  public boolean isUseWriteBehindPrivateCache() {
    return _useWriteBehindPrivateCache;
  }

  public void setUseWriteBehindPrivateCache(final boolean useWriteBehindCache) {
    _useWriteBehindPrivateCache = useWriteBehindCache;
  }

  public boolean isUseAsynchronousTargetResolve() {
    return _useAsynchronousTargetResolve;
  }

  public void setUseAsynchronousTargetResolve(final boolean useAsynchronousTargetResolve) {
    _useAsynchronousTargetResolve = useAsynchronousTargetResolve;
  }

  public void setNodeIdentifier(final String nodeIdentifier) {
    _nodeIdentifier = nodeIdentifier;
  }

  public String getNodeIdentifier() {
    return _nodeIdentifier;
  }

  public void setStatisticsGatherer(final FunctionInvocationStatisticsGatherer statisticsGatherer) {
    ArgumentChecker.notNull(statisticsGatherer, "statisticsGatherer");
    _statisticsGatherer = statisticsGatherer;
  }

  public FunctionInvocationStatisticsGatherer getStatisticsGatherer() {
    return _statisticsGatherer;
  }

  public void setFunctionBlacklistQuery(final FunctionBlacklistQuery blacklistQuery) {
    _blacklistQuery = blacklistQuery;
  }

  public FunctionBlacklistQuery getFunctionBlacklistQuery() {
    return _blacklistQuery;
  }

  public void setFunctionBlacklistUpdate(final FunctionBlacklistMaintainer blacklistUpdate) {
    _blacklistUpdate = blacklistUpdate;
  }

  public FunctionBlacklistMaintainer getFunctionBlacklistUpdate() {
    return _blacklistUpdate;
  }

  public void setMaxJobItemExecution(final MaximumJobItemExecutionWatchdog maxJobItemExecution) {
    _maxJobItemExecution = maxJobItemExecution;
  }

  public MaximumJobItemExecutionWatchdog getMaxJobItemExecution() {
    return _maxJobItemExecution;
  }

  public synchronized SimpleCalculationNode createNode() {
    final String identifier;
    if (getNodeIdentifier() != null) {
      identifier = getNodeIdentifier() + ":" + (++_uid);
    } else {
      identifier = SimpleCalculationNode.createNodeId();
    }
    final CalculationNodeLogEventListener logListener = new CalculationNodeLogEventListener(_threadLocalLogListener);
    final SimpleCalculationNode node = new SimpleCalculationNode(getViewComputationCache(), getFunctionCompilationService(), getFunctionExecutionContext(), identifier, getExecutorService(),
        getStatisticsGatherer(), logListener);
    node.setUseWriteBehindSharedCache(isUseWriteBehindSharedCache());
    node.setUseWriteBehindPrivateCache(isUseWriteBehindPrivateCache());
    node.setUseAsynchronousTargetResolve(isUseAsynchronousTargetResolve());
    if (getFunctionBlacklistQuery() != null) {
      node.setFunctionBlacklistQuery(getFunctionBlacklistQuery());
    }
    if (getFunctionBlacklistUpdate() != null) {
      node.setFunctionBlacklistUpdate(getFunctionBlacklistUpdate());
    }
    if (getMaxJobItemExecution() != null) {
      node.setMaxJobItemExecution(getMaxJobItemExecution());
    }
    return node;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(getViewComputationCache(), "viewComputationCache");
    ArgumentChecker.notNull(getFunctionCompilationService(), "functionCompilationService");
    ArgumentChecker.notNull(getFunctionExecutionContext(), "functionExecutionContext");
  }

}
