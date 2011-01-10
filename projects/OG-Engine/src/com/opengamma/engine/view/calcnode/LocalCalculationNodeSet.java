/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
 * Creates a set of more-or-less identical nodes, e.g. one for each core or a fixed number.
 */
public class LocalCalculationNodeSet extends AbstractCollection<LocalCalculationNode> implements InitializingBean {

  private ViewComputationCacheSource _viewComputationCache;
  private CompiledFunctionService _functionCompilationService;
  private FunctionExecutionContext _functionExecutionContext;
  private ComputationTargetResolver _computationTargetResolver;
  private ViewProcessorQuerySender _viewProcessorQuery;
  private ExecutorService _writeBehindExecutorService;
  private FunctionInvocationStatisticsGatherer _statisticsGatherer = new DiscardingInvocationStatisticsGatherer();
  private String _nodeIdentifier;

  private int _nodeCount;
  private double _nodesPerCore;

  private Collection<LocalCalculationNode> _nodes;

  /**
   * Gets the viewComputationCache field.
   * @return the viewComputationCache
   */
  public ViewComputationCacheSource getViewComputationCache() {
    return _viewComputationCache;
  }

  /**
   * Sets the viewComputationCache field.
   * @param viewComputationCache  the viewComputationCache
   */
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

  /**
   * Gets the functionExecutionContext field.
   * @return the functionExecutionContext
   */
  public FunctionExecutionContext getFunctionExecutionContext() {
    return _functionExecutionContext;
  }

  /**
   * Sets the functionExecutionContext field.
   * @param functionExecutionContext  the functionExecutionContext
   */
  public void setFunctionExecutionContext(FunctionExecutionContext functionExecutionContext) {
    ArgumentChecker.notNull(functionExecutionContext, "functionExecutionContext");
    _functionExecutionContext = functionExecutionContext;
  }

  /**
   * Gets the computationTargetResolver field.
   * @return the computationTargetResolver
   */
  public ComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }

  /**
   * Sets the computationTargetResolver field.
   * @param computationTargetResolver  the computationTargetResolver
   */
  public void setComputationTargetResolver(ComputationTargetResolver computationTargetResolver) {
    ArgumentChecker.notNull(computationTargetResolver, "computationTargetResolver");
    _computationTargetResolver = computationTargetResolver;
  }

  /**
   * Gets the viewProcessorQuery field.
   * @return the viewProcessorQuery
   */
  public ViewProcessorQuerySender getViewProcessorQuery() {
    return _viewProcessorQuery;
  }

  /**
   * Sets the viewProcessorQuery field.
   * @param viewProcessorQuery  the viewProcessorQuery
   */
  public void setViewProcessorQuery(ViewProcessorQuerySender viewProcessorQuery) {
    ArgumentChecker.notNull(viewProcessorQuery, "viewProcessorQuery");
    _viewProcessorQuery = viewProcessorQuery;
  }

  /**
   * Gets the writeBehindExecutorService field.
   * @return the writeBehindExecutorService
   */
  public ExecutorService getWriteBehindExecutorService() {
    return _writeBehindExecutorService;
  }

  /**
   * Sets the writeBehindExecutorService field.
   * @param writeBehindExecutorService  the writeBehindExecutorService
   */
  public void setWriteBehindExecutorService(ExecutorService writeBehindExecutorService) {
    ArgumentChecker.notNull(writeBehindExecutorService, "writeBehindExecutorService");
    _writeBehindExecutorService = writeBehindExecutorService;
  }

  public void setNodeCount(final int nodeCount) {
    ArgumentChecker.notNegative(nodeCount, "nodeCount");
    _nodeCount = nodeCount;
  }

  public int getNodeCount() {
    return _nodeCount;
  }

  public void setNodesPerCore(final double nodesPerCore) {
    ArgumentChecker.notNegativeOrZero(nodesPerCore, "nodesPerCore");
    _nodesPerCore = nodesPerCore;
  }

  public double getNodesPerCore() {
    return _nodesPerCore;
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

  protected int getCores() {
    return Runtime.getRuntime().availableProcessors();
  }

  @Override
  public Iterator<LocalCalculationNode> iterator() {
    return _nodes.iterator();
  }

  @Override
  public int size() {
    return _nodes.size();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ArgumentChecker.notNull(getViewComputationCache(), "viewComputationCache");
    ArgumentChecker.notNull(getFunctionCompilationService(), "functionCompilationService");
    ArgumentChecker.notNull(getFunctionExecutionContext(), "functionExecutionContext");
    ArgumentChecker.notNull(getComputationTargetResolver(), "computationTargetResolver");
    ArgumentChecker.notNull(getViewProcessorQuery(), "viewProcessorQuery");
    ArgumentChecker.notNull(getWriteBehindExecutorService(), "writeBehindExecutorService");
    final int nodes;
    if (getNodeCount() == 0) {
      if (getNodesPerCore() == 0) {
        throw new IllegalStateException("Either nodeCount or nodesPerCore must be set");
      }
      nodes = (int) Math.ceil(getNodesPerCore() * (double) getCores());
    } else {
      nodes = getNodeCount();
    }
    _nodes = new ArrayList<LocalCalculationNode>(nodes);
    for (int i = 0; i < nodes; i++) {
      final LocalCalculationNode node = new LocalCalculationNode(getViewComputationCache(), getFunctionCompilationService(), getFunctionExecutionContext(), getComputationTargetResolver(),
          getViewProcessorQuery(), getWriteBehindExecutorService(), getStatisticsGatherer());
      if (getNodeIdentifier() != null) {
        if (nodes > 1) {
          node.setNodeId(getNodeIdentifier() + ":" + (i + 1));
        } else {
          node.setNodeId(getNodeIdentifier());
        }
      }
      _nodes.add(node);
    }
  }

}
