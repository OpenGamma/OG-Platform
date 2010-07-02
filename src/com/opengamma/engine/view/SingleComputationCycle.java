/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
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
  
  /** Current state of the cycle */
  private enum State {
    CREATED, INPUTS_PREPARED, EXECUTING, FINISHED, CLEANED  
  }
  
  private State _state;
  
  /**
   * Milliseconds, see System.currentTimeMillis()
   */
  private final long _valuationTime;
  
  /**
   * Nanoseconds, see System.nanoTime()
   */
  private final long _startTime;
  private final AtomicLong _jobIdSource = new AtomicLong(0L);
  private final ReentrantReadWriteLock _nodeExecutionLock = new ReentrantReadWriteLock();
  private final ViewDefinition _viewDefinition;
  private final Set<DependencyNode> _executedNodes = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _failedNodes = new HashSet<DependencyNode>();
  private final Map<String, ViewComputationCache> _cachesByCalculationConfiguration =
    new HashMap<String, ViewComputationCache>();
  
  // Outputs:
  private final ViewComputationResultModelImpl _resultModel;
  
  public SingleComputationCycle(
      String viewName,
      ViewProcessingContext processingContext,
      PortfolioEvaluationModel portfolioEvaluationModel,
      ViewComputationResultModelImpl resultModel,
      ViewDefinition viewDefinition,
      long valuationTime) {
    ArgumentChecker.notNull(viewName, "View name");
    ArgumentChecker.notNull(processingContext, "View processing context");
    ArgumentChecker.notNull(portfolioEvaluationModel, "Portfolio evaluation model");
    ArgumentChecker.notNull(resultModel, "Result model");
    ArgumentChecker.notNull(viewDefinition, "View Definition");

    _viewName = viewName;
    _processingContext = processingContext;
    _portfolioEvaluationModel = portfolioEvaluationModel;
    _resultModel = resultModel;
    _startTime = System.nanoTime();
    _viewDefinition = viewDefinition;
    _valuationTime = valuationTime;
    
    _state = State.CREATED;
  }
  
  /**
   * @return Milliseconds from epoch
   */
  public Long getValuationTime() {
    return _valuationTime;
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
   * @return the start time. Nanoseconds, see {@link System#nanoTime()}. 
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
  
  /**
   * @return the viewDefinition
   */
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }
  
  // --------------------------------------------------------------------------
  
  public void prepareInputs() {
    if (_state != State.CREATED) {
      throw new IllegalStateException("State must be " + State.CREATED);
    }
    
    getResultModel().setValuationTime(Instant.ofEpochMillis(getValuationTime()));
    
    createAllCaches();
    
    Set<ValueRequirement> allLiveDataRequirements = getPortfolioEvaluationModel().getAllLiveDataRequirements();
    s_logger.debug("Populating {} market data items for snapshot {}", allLiveDataRequirements.size(), getValuationTime());
    
    Set<ValueRequirement> missingLiveData = new HashSet<ValueRequirement>();
    for (ValueRequirement liveDataRequirement : allLiveDataRequirements) {
      Object data = getProcessingContext().getLiveDataSnapshotProvider().querySnapshot(getValuationTime(), liveDataRequirement);
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
      ViewComputationCache cache = getProcessingContext().getComputationCacheSource().getCache(getViewName(), calcConfigurationName, getValuationTime());
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
    
    // There are two ways to execute the different dependency graphs: sequentially or in parallel.
    // Both are implemented, and both work.
    // However, right now on the scope that we're working on, we don't actually need the parallel form,
    // and the additional overhead of thread management slows us down, so I've hard coded it to the sequential
    // form. This should ultimately change if we discover that as graphs get bigger we want to have them
    // running in parallel.
    executePlansSequential();
    //executePlansParallel();
    
    _state = State.FINISHED;
  }
  
  protected void executePlansParallel() {
    ExecutorCompletionService<Object> completionService = new ExecutorCompletionService<Object>(getProcessingContext().getExecutorService());
    int nSubmitted = 0;
    
    for (String calcConfigurationName : getPortfolioEvaluationModel().getAllCalculationConfigurationNames()) {
      s_logger.info("Executing plans for calculation configuration {}", calcConfigurationName);
      DependencyGraph depGraph = getPortfolioEvaluationModel().getDependencyGraph(calcConfigurationName);
      
      s_logger.info("Submitting {} for execution", depGraph);
      final DependencyGraphExecutor depGraphExecutor = new DependencyGraphExecutor(
          getViewName(),
          calcConfigurationName,
          depGraph,
          getProcessingContext(),
          this);
      completionService.submit(new Runnable() {
        @Override
        public void run() {
          depGraphExecutor.executeGraph(getValuationTime(), _jobIdSource);
        }
      }, depGraphExecutor);
      nSubmitted++;
    }

    for (int i = 0; i < nSubmitted; i++) {
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

  protected void executePlansSequential() {
    for (String calcConfigurationName : getPortfolioEvaluationModel().getAllCalculationConfigurationNames()) {
      s_logger.info("Executing plans for calculation configuration {}", calcConfigurationName);
      DependencyGraph depGraph = getPortfolioEvaluationModel().getDependencyGraph(calcConfigurationName);
      
      s_logger.info("Submitting {} for execution", depGraph);
      final DependencyGraphExecutor depGraphExecutor = new DependencyGraphExecutor(
          getViewName(),
          calcConfigurationName,
          depGraph,
          getProcessingContext(),
          this);
      depGraphExecutor.executeGraph(getValuationTime(), _jobIdSource);
    }
  }
  
  // --------------------------------------------------------------------------

  public void populateResultModel() {
    Instant resultTimestamp = Instant.nowSystemClock();
    getResultModel().setResultTimestamp(resultTimestamp);

    for (String calcConfigurationName : getPortfolioEvaluationModel().getAllCalculationConfigurationNames()) {
      DependencyGraph depGraph = getPortfolioEvaluationModel().getDependencyGraph(calcConfigurationName);

      populateResultModel(calcConfigurationName, depGraph, ComputationTargetType.POSITION);
      populateResultModel(calcConfigurationName, depGraph, ComputationTargetType.MULTIPLE_POSITIONS);
    }
  }
  
  protected void populateResultModel(String calcConfigurationName, DependencyGraph depGraph, ComputationTargetType type) {
    ViewComputationCache computationCache = getComputationCache(calcConfigurationName);
    for (ValueSpecification outputSpec : depGraph.getOutputValues(type)) {
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
    
    getProcessingContext().getLiveDataSnapshotProvider().releaseSnapshot(getValuationTime());
    getProcessingContext().getComputationCacheSource().releaseCaches(getViewName(), getValuationTime());
    
    _state = State.CLEANED;
  }
  
  // --------------------------------------------------------------------------
  
  // Dependency Node Maintenance:
  // REVIEW kirk 2010-04-30 -- Is this good locking? I'm not entirely sure.
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
