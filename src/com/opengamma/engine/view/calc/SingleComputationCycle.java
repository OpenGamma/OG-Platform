/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.time.Instant;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.PortfolioEvaluationModel;
import com.opengamma.engine.view.View;
import com.opengamma.engine.view.ViewComputationResultModelImpl;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessingContext;
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
  private final View _view;
  private final Instant _valuationTime;
  
  private final DependencyGraphExecutor<?> _dependencyGraphExecutor;
  
  // State:
  
  /** Current state of the cycle */
  private enum State {
    CREATED, INPUTS_PREPARED, EXECUTING, FINISHED, CLEANED  
  }
  
  private State _state;
  
  /**
   * Nanoseconds, see System.nanoTime()
   */
  private long _startTime;
  
  /**
   * Nanoseconds, see System.nanoTime()
   */
  private long _endTime;
  
  private final ReentrantReadWriteLock _nodeExecutionLock = new ReentrantReadWriteLock();
  private final Set<DependencyNode> _executedNodes = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _failedNodes = new HashSet<DependencyNode>();
  private final Map<String, ViewComputationCache> _cachesByCalculationConfiguration =
    new HashMap<String, ViewComputationCache>();
  
  // Outputs:
  private final ViewComputationResultModelImpl _resultModel;
  
  public SingleComputationCycle(View view,
      long valuationTime) {
    ArgumentChecker.notNull(view, "View");
    
    _view = view;
    _valuationTime = Instant.ofEpochMillis(valuationTime);
    
    _resultModel =  new ViewComputationResultModelImpl();
    _resultModel.setCalculationConfigurationNames(getPortfolioEvaluationModel().getAllCalculationConfigurationNames());
    _resultModel.setPortfolio(getPortfolioEvaluationModel().getPortfolio());
    
    _dependencyGraphExecutor = getProcessingContext().getDependencyGraphExecutorFactory().createExecutor(this);

    _state = State.CREATED;
  }
  
  public View getView() {
    return _view;
  }
  
  public Instant getValuationTime() {
    return _valuationTime;
  }
  
  /**
   * @return the viewName
   */
  public String getViewName() {
    return getView().getDefinition().getName();
  }

  /**
   * @return the processingContext
   */
  public ViewProcessingContext getProcessingContext() {
    return getView().getProcessingContext();
  }

  /**
   * @return the portfolioEvaluationModel
   */
  public PortfolioEvaluationModel getPortfolioEvaluationModel() {
    return getView().getPortfolioEvaluationModel();
  }

  /**
   * @return the start time. Nanoseconds, see {@link System#nanoTime()}. 
   */
  public long getStartTime() {
    return _startTime;
  }
  
  /**
   * @return the end time. Nanoseconds, see {@link System#nanoTime()}. 
   */
  public long getEndTime() {
    return _endTime;
  }
  
  /**
   * @return How many nanoseconds the cycle took
   */
  public long getDurationNanos() {
    return getEndTime() - getStartTime();  
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
  
  /**
   * @return the viewDefinition
   */
  public ViewDefinition getViewDefinition() {
    return getView().getDefinition();
  }
  
  public DependencyGraphExecutor<?> getDependencyGraphExecutor() {
    return _dependencyGraphExecutor;    
  }
  
  public Map<String, ViewComputationCache> getCachesByCalculationConfiguration() {
    return Collections.unmodifiableMap(_cachesByCalculationConfiguration);
  }

  
  // --------------------------------------------------------------------------
  

  public void prepareInputs() {
    if (_state != State.CREATED) {
      throw new IllegalStateException("State must be " + State.CREATED);
    }
    
    _startTime = System.nanoTime();
    
    getResultModel().setValuationTime(getValuationTime());
    
    createAllCaches();
    
    Set<ValueRequirement> allLiveDataRequirements = getPortfolioEvaluationModel().getAllLiveDataRequirements();
    s_logger.debug("Populating {} market data items for snapshot {}", allLiveDataRequirements.size(), getValuationTime());
    
    Set<ValueRequirement> missingLiveData = new HashSet<ValueRequirement>();
    for (ValueRequirement liveDataRequirement : allLiveDataRequirements) {
      Object data = getProcessingContext().getLiveDataSnapshotProvider().querySnapshot(getValuationTime().toEpochMillisLong(), liveDataRequirement);
      if (data == null) {
        s_logger.debug("Unable to load live data value for {} at snapshot {}.", liveDataRequirement, getValuationTime());
        missingLiveData.add(liveDataRequirement);
      } else {
        ComputedValue dataAsValue = new ComputedValue(new ValueSpecification(liveDataRequirement), data);
        //s_logger.warn("Live Data Requirement: {}", dataAsValue);
        addToAllCaches(dataAsValue);
      }
    }
    if (!missingLiveData.isEmpty()) {
      s_logger.warn("Missing {} live data elements: {}", missingLiveData.size(), formatMissingLiveData(missingLiveData));
    }
    
    _state = State.INPUTS_PREPARED;
  }
  
  protected static String formatMissingLiveData(Set<ValueRequirement> missingLiveData) {
    StringBuilder sb = new StringBuilder();
    for (ValueRequirement req : missingLiveData) {
      sb.append("[").append(req.getValueName()).append(" on ");
      sb.append(req.getTargetSpecification().getType());
      if (req.getTargetSpecification().getType() == ComputationTargetType.PRIMITIVE) {
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
    for (String calcConfigurationName : getPortfolioEvaluationModel().getAllCalculationConfigurationNames()) {
      ViewComputationCache cache = getProcessingContext().getComputationCacheSource().getCache(getViewName(), calcConfigurationName, getValuationTime().toEpochMillisLong());
      _cachesByCalculationConfiguration.put(calcConfigurationName, cache);
    }
  }

  /**
   * @param dataAsValue
   */
  private void addToAllCaches(ComputedValue dataAsValue) {
    for (String calcConfigurationName : getPortfolioEvaluationModel().getAllCalculationConfigurationNames()) {
      getComputationCache(calcConfigurationName).putValue(dataAsValue);
    }
  }
  
  // --------------------------------------------------------------------------

  /**
   * Determine which live data inputs have changed between iterations, and:
   * <ul>
   * <li>Copy over all values that can be demonstrated to be the same from the previous iteration (because no input has changed)
   * <li>Only recompute the values that could have changed based on live data inputs
   * </ul> 
   * 
   * @param previousCycle Previous iteration. It must not have been cleaned yet ({@link #releaseResources()}).
   */
  public void computeDelta(SingleComputationCycle previousCycle) {
    if (_state != State.INPUTS_PREPARED) {
      throw new IllegalStateException("State must be " + State.INPUTS_PREPARED);
    }
    if (previousCycle._state != State.FINISHED) {
      throw new IllegalArgumentException("State of previous cycle must be " + State.FINISHED);
    }
    
    for (String calcConfigurationName : getPortfolioEvaluationModel().getAllCalculationConfigurationNames()) {
      DependencyGraph depGraph = getPortfolioEvaluationModel().getDependencyGraph(calcConfigurationName);
      
      ViewComputationCache cache = this.getComputationCache(calcConfigurationName); 
      ViewComputationCache previousCache = previousCycle.getComputationCache(calcConfigurationName);
      
      LiveDataDeltaCalculator deltaCalculator = new LiveDataDeltaCalculator(depGraph,
          cache,
          previousCache);
      deltaCalculator.computeDelta();
      
      s_logger.info("Computed delta for calc conf {}. Of {} nodes, {} require recomputation.", 
          new Object[] {calcConfigurationName, depGraph.getSize(), deltaCalculator.getChangedNodes().size()});
      
      for (DependencyNode unchangedNode : deltaCalculator.getUnchangedNodes()) {
        markExecuted(unchangedNode);
        
        for (ValueSpecification spec : unchangedNode.getOutputValues()) {
          Object previousValue = previousCache.getValue(spec);
          if (previousValue != null) {
            cache.putValue(new ComputedValue(spec, previousValue));
          }
        }
      }
    }
  }
  
  // REVIEW kirk 2009-11-03 -- This is a database kernel. Act accordingly.
  public void executePlans() {
    if (_state != State.INPUTS_PREPARED) {
      throw new IllegalStateException("State must be " + State.INPUTS_PREPARED);
    }
    _state = State.EXECUTING;
    
    LinkedList<Future<?>> futures = new LinkedList<Future<?>>();
    
    for (String calcConfigurationName : getPortfolioEvaluationModel().getAllCalculationConfigurationNames()) {
      s_logger.info("Executing plans for calculation configuration {}", calcConfigurationName);
      DependencyGraph depGraph = getDependencyGraph(calcConfigurationName);
      
      s_logger.info("Submitting {} for execution by {}", depGraph, getDependencyGraphExecutor());
      
      Future<?> future = getDependencyGraphExecutor().execute(depGraph);
      futures.add(future);
    }

    while (!futures.isEmpty()) {
      Future<?> future = futures.poll();
      try {
        future.get(5, TimeUnit.SECONDS);
      } catch (TimeoutException e) {
        s_logger.info("Waiting for " + future);
        futures.add(future);
      } catch (InterruptedException e) {
        Thread.interrupted();
        s_logger.info("Interrupted while waiting for completion of " + future);
        futures.add(future);
      } catch (ExecutionException e) {
        s_logger.error("Unable to execute dependency graph", e);
        // Should we be swallowing this or not?
        throw new OpenGammaRuntimeException("Unable to execute dependency graph", e);
      }
    }
    
    _state = State.FINISHED;
  }

  /**
   * @return A dependency graph with nodes already executed stripped out.
   * See {@link #computeDelta} and how it calls {@link #markExecuted}.
   */
  private DependencyGraph getDependencyGraph(String calcConfName) {
    DependencyGraph originalDepGraph = getPortfolioEvaluationModel().getDependencyGraph(calcConfName);
    
    DependencyGraph dependencyGraph = originalDepGraph.subGraph(new DependencyNodeFilter() {
      public boolean accept(DependencyNode node) {
        // LiveData functions do not need to be computed. 
        if (node.getFunctionDefinition() instanceof LiveDataSourcingFunction) {
          markExecuted(node);        
        }
        
        return !isExecuted(node);        
      }
    });
    return dependencyGraph;
  }
  
  // --------------------------------------------------------------------------

  public void populateResultModel() {
    Instant resultTimestamp = Instant.nowSystemClock();
    getResultModel().setResultTimestamp(resultTimestamp);

    for (String calcConfigurationName : getPortfolioEvaluationModel().getAllCalculationConfigurationNames()) {
      DependencyGraph depGraph = getPortfolioEvaluationModel().getDependencyGraph(calcConfigurationName);
      populateResultModel(calcConfigurationName, depGraph);
    }
    
    _endTime = System.nanoTime();
  }
  
  protected void populateResultModel(String calcConfigurationName, DependencyGraph depGraph) {
    ViewComputationCache computationCache = getComputationCache(calcConfigurationName);
    for (ValueSpecification outputSpec : depGraph.getOutputValues()) {
      ComputationTargetType type = outputSpec.getRequirementSpecification().getTargetSpecification().getType();
      
      // REVIEW kirk 2010-07-05 -- WARNING! GROSS HACK!
      if (type == ComputationTargetType.PRIMITIVE
          && !ObjectUtils.equals(ValueRequirementNames.FUNDING_CURVE, outputSpec.getRequirementSpecification().getValueName())) {
        continue;
      }
      
      if (!getViewDefinition().getResultModelDefinition().shouldWriteResults(type)) {
        continue;
      }
      
      Object value = computationCache.getValue(outputSpec);
      if (value != null) {
        getResultModel().addValue(calcConfigurationName, new ComputedValue(outputSpec, value));
      }
    }
  }
  
  public void releaseResources() {
    if (_state != State.FINISHED) {
      throw new IllegalStateException("State must be " + State.FINISHED);
    }
    
    getProcessingContext().getLiveDataSnapshotProvider().releaseSnapshot(getValuationTime().toEpochMillisLong());
    getProcessingContext().getComputationCacheSource().releaseCaches(getViewName(), getValuationTime().toEpochMillisLong());
    
    _state = State.CLEANED;
  }
  
  // --------------------------------------------------------------------------
  
  public boolean isExecuted(DependencyNode node) {
    if (node == null) {
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
    if (node == null) {
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
    if (node == null) {
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
    if (node == null) {
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
