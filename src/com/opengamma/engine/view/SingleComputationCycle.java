/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.ViewComputationCache;

/**
 * Holds all data and actions for a single pass through a computation cycle.
 * In general, each invocation of {@link ViewRecalculationJob#runOneCycle()}
 * will create an instance of this class.
 * <p/>
 * At the moment, the concurrency metaphor is:
 * <ul>
 *   <li>Each distinct security has its own execution plan</li>
 *   <li>The cycle will schedule each node in the execution plan sequentially</li>
 *   <li>If there are shared sub-graphs that aren't security specific, they'll be captured at execution time.</li>
 * </ul>
 * This is, of course, not optimal, and later on we can fix that.
 *
 * @author kirk
 */
public class SingleComputationCycle {
  private static final Logger s_logger = LoggerFactory.getLogger(SingleComputationCycle.class);
  // Injected Inputs:
  private final String _viewName;
  private final ViewProcessingContext _processingContext;
  private final PortfolioEvaluationModel _portfolioEvaluationModel;
  
  // State:
  private final long _startTime;
  private final AtomicLong _jobIdSource = new AtomicLong(0l);
  private final ReentrantReadWriteLock _nodeExecutionLock = new ReentrantReadWriteLock();
  private final Set<DependencyNode> _executedNodes = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _failedNodes = new HashSet<DependencyNode>();
  private final Map<String, ViewComputationCache> _cachesByCalculationConfiguration =
    new HashMap<String, ViewComputationCache>();
  
  // Outputs:
  private long _snapshotTime;
  private final ViewComputationResultModelImpl _resultModel;
  
  public SingleComputationCycle(
      String viewName,
      ViewProcessingContext processingContext,
      PortfolioEvaluationModel portfolioEvaluationModel,
      ViewComputationResultModelImpl resultModel,
      ViewDefinition viewDefinition) {
    // TODO kirk 2010-03-02 -- Convert to proper arg checks.
    assert viewName != null;
    assert processingContext != null;
    assert portfolioEvaluationModel != null;
    assert resultModel != null;
    _viewName = viewName;
    _processingContext = processingContext;
    _portfolioEvaluationModel = portfolioEvaluationModel;
    _resultModel = resultModel;
    _startTime = System.currentTimeMillis();
  }
  
  /**
   * @return the snapshotTime
   */
  public long getSnapshotTime() {
    return _snapshotTime;
  }

  /**
   * @param snapshotTime the snapshotTime to set
   */
  public void setSnapshotTime(long snapshotTime) {
    _snapshotTime = snapshotTime;
  }

  /**
   * @return the viewName
   */
  public String getViewName() {
    return _viewName;
  }

  /**
   * @return the processingContext
   */
  public ViewProcessingContext getProcessingContext() {
    return _processingContext;
  }

  /**
   * @return the portfolioEvaluationModel
   */
  public PortfolioEvaluationModel getPortfolioEvaluationModel() {
    return _portfolioEvaluationModel;
  }

  /**
   * @return the startTime
   */
  public long getStartTime() {
    return _startTime;
  }

  /**
   * @return the resultModel
   */
  public ViewComputationResultModelImpl getResultModel() {
    return _resultModel;
  }
  
  public ViewComputationCache getComputationCache(String calcConfigName) {
    return _cachesByCalculationConfiguration.get(calcConfigName);
  }
  
  public boolean prepareInputs() {
    long snapshotTime = getProcessingContext().getLiveDataSnapshotProvider().snapshot();
    setSnapshotTime(snapshotTime);
    getResultModel().setInputDataTimestamp(getSnapshotTime());
    
    createAllCaches();
    
    Set<ValueRequirement> allLiveDataRequirements = getPortfolioEvaluationModel().getAllLiveDataRequirements();
    s_logger.debug("Populating {} market data items for snapshot {}", allLiveDataRequirements.size(), getSnapshotTime());
    
    Set<ValueRequirement> missingLiveData = new HashSet<ValueRequirement>();
    for(ValueRequirement liveDataRequirement : allLiveDataRequirements) {
      Object data = getProcessingContext().getLiveDataSnapshotProvider().querySnapshot(getSnapshotTime(), liveDataRequirement);
      if(data == null) {
        s_logger.debug("Unable to load live data value for {} at snapshot {}.", liveDataRequirement, getSnapshotTime());
        missingLiveData.add(liveDataRequirement);
      } else {
        ComputedValue dataAsValue = new ComputedValue(new ValueSpecification(liveDataRequirement), data);
        addToAllCaches(dataAsValue);
      }
    }
    if(!missingLiveData.isEmpty()) {
      s_logger.warn("Missing live data: {}", missingLiveData);
    }
    return true;
  }
  
  /**
   * 
   */
  private void createAllCaches() {
    for(DependencyGraphModel depGraphModel : getPortfolioEvaluationModel().getAllDependencyGraphModels()) {
      ViewComputationCache cache =  getProcessingContext().getComputationCacheSource().getCache(getViewName(), depGraphModel.getCalculationConfigurationName(), getSnapshotTime());
      _cachesByCalculationConfiguration.put(depGraphModel.getCalculationConfigurationName(), cache);
    }
  }

  /**
   * @param dataAsValue
   */
  private void addToAllCaches(ComputedValue dataAsValue) {
    for(DependencyGraphModel depGraphModel : getPortfolioEvaluationModel().getAllDependencyGraphModels()) {
      getComputationCache(depGraphModel.getCalculationConfigurationName()).putValue(dataAsValue);
    }
  }

  // REVIEW kirk 2009-11-03 -- This is a database kernel. Act accordingly.
  public void executePlans() {
    for(DependencyGraphModel depGraphModel : getPortfolioEvaluationModel().getAllDependencyGraphModels()) {
      s_logger.info("Executing plans for calculation configuration {}", depGraphModel.getCalculationConfigurationName());
      executePlans(depGraphModel, ComputationTargetType.PRIMITIVE);
      executePlans(depGraphModel, ComputationTargetType.SECURITY);
      executePlans(depGraphModel, ComputationTargetType.POSITION);
      executePlans(depGraphModel, ComputationTargetType.MULTIPLE_POSITIONS);
    }
  }
  
  /**
   * @param primitive
   */
  protected void executePlans(DependencyGraphModel depGraphModel, ComputationTargetType targetType) {
    Collection<DependencyGraph> depGraphs = depGraphModel.getDependencyGraphs(targetType);
    for(DependencyGraph depGraph : depGraphs) {
      s_logger.info("{} - Executing dependency graph for {}", depGraphModel.getCalculationConfigurationName(), depGraph.getComputationTarget());
      DependencyGraphExecutor depGraphExecutor = new DependencyGraphExecutor(
          getViewName(),
          depGraphModel.getCalculationConfigurationName(),
          depGraph,
          getProcessingContext(),
          this);
      depGraphExecutor.executeGraph(getSnapshotTime(), _jobIdSource);
    }
  }

  
  public void populateResultModel() {
    for(DependencyGraphModel depGraphModel : getPortfolioEvaluationModel().getAllDependencyGraphModels()) {
      populateResultModel(depGraphModel, ComputationTargetType.POSITION);
      populateResultModel(depGraphModel, ComputationTargetType.MULTIPLE_POSITIONS);
    }
  }
  
  protected void populateResultModel(DependencyGraphModel depGraphModel, ComputationTargetType targetType) {
    Collection<DependencyGraph> depGraphs = depGraphModel.getDependencyGraphs(targetType);
    for(DependencyGraph depGraph : depGraphs) {
      for(ValueSpecification outputSpec : depGraph.getOutputValues()) {
        ComputedValue value = getComputationCache(depGraphModel.getCalculationConfigurationName()).getValue(outputSpec);
        if(value != null) {
          getResultModel().addValue(depGraphModel.getCalculationConfigurationName(), value);
        }
      }
    }
  }
  
  public void releaseResources() {
    getProcessingContext().getLiveDataSnapshotProvider().releaseSnapshot(getSnapshotTime());
    getProcessingContext().getComputationCacheSource().releaseCaches(getViewName(), getSnapshotTime());
  }
  
  // Dependency Node Maintenance:
  public boolean isExecuted(DependencyNode node) {
    if(node == null) {
      return true;
    }
    _nodeExecutionLock.readLock().lock();
    try {
      return _executedNodes.contains(node);
    } finally {
      _nodeExecutionLock.readLock().unlock();
    }
  }
  
  public void markExecuted(DependencyNode node) {
    if(node == null) {
      return;
    }
    _nodeExecutionLock.writeLock().lock();
    try {
      _executedNodes.add(node);
    } finally {
      _nodeExecutionLock.writeLock().unlock();
    }
  }

  public boolean isFailed(DependencyNode node) {
    if(node == null) {
      return true;
    }
    _nodeExecutionLock.readLock().lock();
    try {
      return _failedNodes.contains(node);
    } finally {
      _nodeExecutionLock.readLock().unlock();
    }
  }
  
  public void markFailed(DependencyNode node) {
    if(node == null) {
      return;
    }
    _nodeExecutionLock.writeLock().lock();
    try {
      _failedNodes.add(node);
    } finally {
      _nodeExecutionLock.writeLock().unlock();
    }
  }
}
