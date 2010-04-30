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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.util.ArgumentChecker;

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
    ArgumentChecker.notNull(viewName, "View name");
    ArgumentChecker.notNull(processingContext, "View processing context");
    ArgumentChecker.notNull(portfolioEvaluationModel, "Portfolio evaluation model");
    ArgumentChecker.notNull(resultModel, "Result model");

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
  
  public void prepareInputs() {
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
        //s_logger.warn("Live Data Requirement: {}", dataAsValue);
        addToAllCaches(dataAsValue);
      }
    }
    if(!missingLiveData.isEmpty()) {
      s_logger.warn("Missing {} live data elements: {}", missingLiveData.size(), formatMissingLiveData(missingLiveData));
    }
  }
  
  protected static String formatMissingLiveData(Set<ValueRequirement> missingLiveData) {
    StringBuilder sb = new StringBuilder();
    for(ValueRequirement req : missingLiveData) {
      sb.append("[").append(req.getValueName()).append(" on ");
      sb.append(req.getTargetSpecification().getType());
      if(req.getTargetSpecification().getType() == ComputationTargetType.PRIMITIVE) {
        sb.append("-").append(req.getTargetSpecification().getIdentifier().getScheme().getName());
      }
      sb.append(":").append(req.getTargetSpecification().getIdentifier().getValue()).append("] ");
    }
    return sb.toString();
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
      // NOTE kirk 2010-04-01 -- Yes, this is correct. Yes, it's counterintuitive.
      // 1) Because we always generate the top-level aggregate nodes right now, we guarantee that by hitting
      //    MULTIPLE_POSITIONS we're guaranteed to hit the POSITION-level nodes because dependency graphs
      //    link to the lower levels that they require. Since we only output at the POSITION level, we're set.
      // 2) There's a massive benefit to having larger dependency graphs with lots of stuff to do when executing.
      //    If you have a small graph, or a large graph where most stuff is already done, just processing the
      //    dependency graph itself is costly and limits your parallelism.
      // This optimization took multi-calc-node performance up by a factor of 2. Don't mess with it
      // lightly.
      // I've left the commented out nodes there so that if we ARE in a situation where we need to
      // evaluate per-lower-level item (for example, if we allow for raw per-unit greeks calculations to flow
      // out of a result model), then this will ultimately have to be changed.
      
      // There are two ways to execute the different dependency graphs: sequentially or in parallel.
      // Both are implemented, and both work.
      // However, right now on the scope that we're working on, we don't actually need the parallel form,
      // and the additional overhead of thread management slows us down, so I've hard coded it to the sequential
      // form. This should ultimately change if we discover that as graphs get bigger we want to have them
      // running in parallel.
      executePlansSequential(depGraphModel, ComputationTargetType.MULTIPLE_POSITIONS);
      //executePlansParallel(depGraphModel, ComputationTargetType.POSITION);
      //executePlansParallel(depGraphModel, ComputationTargetType.SECURITY);
      //executePlansParallel(depGraphModel, ComputationTargetType.PRIMITIVE);
    }
  }
  
  /**
   * @param primitive
   */
  protected void executePlansParallel(DependencyGraphModel depGraphModel, ComputationTargetType targetType) {
    ExecutorCompletionService<Object> completionService = new ExecutorCompletionService<Object>(getProcessingContext().getExecutorService());
    int nSubmitted = 0;
    Collection<DependencyGraph> depGraphs = depGraphModel.getDependencyGraphs(targetType);
    for(DependencyGraph depGraph : depGraphs) {
      s_logger.info("{} - Submitting dependency graph for {} for execution", depGraphModel.getCalculationConfigurationName(), depGraph.getComputationTarget());
      final DependencyGraphExecutor depGraphExecutor = new DependencyGraphExecutor(
          getViewName(),
          depGraphModel.getCalculationConfigurationName(),
          depGraph,
          getProcessingContext(),
          this);
      completionService.submit(new Runnable() {
        @Override
        public void run() {
          depGraphExecutor.executeGraph(getSnapshotTime(), _jobIdSource);
        }
      }, depGraphExecutor);
      nSubmitted++;
    }
    for(int i = 0; i < nSubmitted; i++) {
      Future<Object> result = null;
      try {
        result = completionService.take();
        result.get();
      } catch (InterruptedException e) {
        Thread.interrupted();
        s_logger.info("Interrupted while waiting for completion of all execution plans");
      } catch (ExecutionException e) {
        s_logger.error("Unable to execute dependency graph", e);
        // Should we be swallowing this or not?
        throw new OpenGammaRuntimeException("Unable to execute dependency graph", e);
      }
    }
  }

  /**
   * @param primitive
   */
  protected void executePlansSequential(DependencyGraphModel depGraphModel, ComputationTargetType targetType) {
    Collection<DependencyGraph> depGraphs = depGraphModel.getDependencyGraphs(targetType);
    for(DependencyGraph depGraph : depGraphs) {
      s_logger.info("{} - Submitting dependency graph for {} for execution", depGraphModel.getCalculationConfigurationName(), depGraph.getComputationTarget());
      final DependencyGraphExecutor depGraphExecutor = new DependencyGraphExecutor(
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
    ViewComputationCache computationCache = getComputationCache(depGraphModel.getCalculationConfigurationName());
    Collection<DependencyGraph> depGraphs = depGraphModel.getDependencyGraphs(targetType);
    for(DependencyGraph depGraph : depGraphs) {
      for(ValueSpecification outputSpec : depGraph.getOutputValues()) {
        Object value = computationCache.getValue(outputSpec);
        if(value != null) {
          getResultModel().addValue(depGraphModel.getCalculationConfigurationName(), new ComputedValue (outputSpec, value));
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
