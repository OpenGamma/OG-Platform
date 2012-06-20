/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatisticsGatherer;
import com.opengamma.util.ArgumentChecker;

/**
 * Creates more-or-less identical nodes.
 */
public class SimpleCalculationNodeFactory implements InitializingBean {

  private ViewComputationCacheSource _viewComputationCache;
  private CompiledFunctionService _functionCompilationService;
  private FunctionExecutionContext _functionExecutionContext;
  private ComputationTargetResolver _computationTargetResolver;
  private ViewProcessorQuerySender _viewProcessorQuery;
  private ExecutorService _executorService;
  private FunctionInvocationStatisticsGatherer _statisticsGatherer = new DiscardingInvocationStatisticsGatherer();
  private String _nodeIdentifier;
  private boolean _useWriteBehindSharedCache;
  private boolean _useWriteBehindPrivateCache;
  private boolean _useAsynchronousTargetResolve;

  private int _uid;

  public ViewComputationCacheSource getViewComputationCache() {
    return _viewComputationCache;
  }

  public void setViewComputationCache(ViewComputationCacheSource viewComputationCache) {
    ArgumentChecker.notNull(viewComputationCache, "viewComputationCache");
    _viewComputationCache = viewComputationCache;
  }

  public CompiledFunctionService getFunctionCompilationService() {
    return _functionCompilationService;
  }

  public void setFunctionCompilationService(CompiledFunctionService functionCompilationService) {
    ArgumentChecker.notNull(functionCompilationService, "functionCompilationService");
    _functionCompilationService = functionCompilationService;
  }

  public FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }

  public void setFunctionExecutionContext(FunctionExecutionContext functionExecutionContext) {
    ArgumentChecker.notNull(functionExecutionContext, "functionExecutionContext");
    _functionExecutionContext = functionExecutionContext;
  }

  public ComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }

  public void setComputationTargetResolver(ComputationTargetResolver computationTargetResolver) {
    ArgumentChecker.notNull(computationTargetResolver, "computationTargetResolver");
    _computationTargetResolver = computationTargetResolver;
  }

  public ViewProcessorQuerySender getViewProcessorQuery() {
    return _viewProcessorQuery;
  }

  public void setViewProcessorQuery(ViewProcessorQuerySender viewProcessorQuery) {
    ArgumentChecker.notNull(viewProcessorQuery, "viewProcessorQuery");
    _viewProcessorQuery = viewProcessorQuery;
  }

  public ExecutorService getExecutorService() {
    return _executorService;
  }

  public void setExecutorService(ExecutorService executorService) {
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

  public synchronized SimpleCalculationNode createNode() {
    final String identifier;
    if (getNodeIdentifier() != null) {
      identifier = getNodeIdentifier() + ":" + (++_uid);
    } else {
      identifier = SimpleCalculationNode.createNodeId();
    }
    final SimpleCalculationNode node = new SimpleCalculationNode(getViewComputationCache(), getFunctionCompilationService(), getFunctionExecutionContext(), getComputationTargetResolver(),
          getViewProcessorQuery(), identifier, getExecutorService(), getStatisticsGatherer());
    node.setUseWriteBehindSharedCache(isUseWriteBehindSharedCache());
    node.setUseWriteBehindPrivateCache(isUseWriteBehindPrivateCache());
    node.setUseAsynchronousTargetResolve(isUseAsynchronousTargetResolve());
    return node;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(getViewComputationCache(), "viewComputationCache");
    ArgumentChecker.notNull(getFunctionCompilationService(), "functionCompilationService");
    ArgumentChecker.notNull(getFunctionExecutionContext(), "functionExecutionContext");
    ArgumentChecker.notNull(getComputationTargetResolver(), "computationTargetResolver");
    ArgumentChecker.notNull(getViewProcessorQuery(), "viewProcessorQuery");
  }

}
