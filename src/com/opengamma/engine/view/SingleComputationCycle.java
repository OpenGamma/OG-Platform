/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.depgraph.SecurityDependencyGraph;
import com.opengamma.engine.security.Security;

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
  private final ViewDefinition _viewDefinition;
  private final ExecutorService _computationExecutorService;
  
  // State:
  private final long _startTime;
  private ViewComputationCache _computationCache;
  
  // Outputs:
  private long _snapshotTime;
  private final ViewComputationResultModelImpl _resultModel;
  
  public SingleComputationCycle(
      String viewName,
      ViewProcessingContext processingContext,
      PortfolioEvaluationModel portfolioEvaluationModel,
      ViewComputationResultModelImpl resultModel,
      ViewDefinition viewDefinition,
      ExecutorService computationExecutorService) {
    assert viewName != null;
    assert processingContext != null;
    assert portfolioEvaluationModel != null;
    assert resultModel != null;
    assert viewDefinition != null;
    assert computationExecutorService != null;
    _viewName = viewName;
    _processingContext = processingContext;
    _portfolioEvaluationModel = portfolioEvaluationModel;
    _resultModel = resultModel;
    _viewDefinition = viewDefinition;
    _computationExecutorService = computationExecutorService;
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
   * @return the computationCache
   */
  public ViewComputationCache getComputationCache() {
    return _computationCache;
  }

  /**
   * @param computationCache the computationCache to set
   */
  public void setComputationCache(ViewComputationCache computationCache) {
    _computationCache = computationCache;
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

  /**
   * @return the viewDefinition
   */
  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  /**
   * @return the computationExecutorService
   */
  public ExecutorService getComputationExecutorService() {
    return _computationExecutorService;
  }

  public void prepareInputs() {
    setSnapshotTime(getProcessingContext().getLiveDataSnapshotProvider().snapshot());
    ViewComputationCache cache = getProcessingContext().getComputationCacheSource().getCache(getViewName(), getSnapshotTime());
    assert cache != null;
    setComputationCache(cache);
    getResultModel().setInputDataTimestamp(getSnapshotTime());
    
    Set<AnalyticValueDefinition<?>> requiredLiveData = getPortfolioEvaluationModel().getDependencyGraphModel().getAllRequiredLiveData();
    s_logger.debug("Populating {} market data items for snapshot {}", requiredLiveData.size(), getSnapshotTime());
    
    for(AnalyticValueDefinition<?> requiredDataDefinition : requiredLiveData) {
      AnalyticValue<?> value = getProcessingContext().getLiveDataSnapshotProvider().querySnapshot(getSnapshotTime(), requiredDataDefinition);
      if(value == null) {
        s_logger.warn("Unable to load live data value for {} at snapshot {}", requiredDataDefinition, getSnapshotTime());
      } else {
        getComputationCache().putValue(value);
      }
    }
  }
  
  public void executePlans() {
    for(Security security : getPortfolioEvaluationModel().getSecurities()) {
      SecurityDependencyGraph secDepGraph = getPortfolioEvaluationModel().getDependencyGraphModel().getDependencyGraph(security);
      assert secDepGraph != null;
      DependencyGraphExecutor depGraphExecutor = new DependencyGraphExecutor(
          security, secDepGraph, getProcessingContext(), getComputationCache(),
          getComputationExecutorService(),
          getProcessingContext().getAnalyticFunctionRepository());
      depGraphExecutor.executeGraph();
    }
  }

  public void populateResultModel() {
    Map<String, Collection<AnalyticValueDefinition<?>>> valueDefsBySecTypes = getViewDefinition().getValueDefinitionsBySecurityTypes(); 
    for(FullyPopulatedPosition position : getPortfolioEvaluationModel().getPopulatedPositions()) {
      // REVIEW kirk 2009-09-14 -- Could be parallelized if we need to.
      Security security = position.getSecurity();
      String securityType = security.getSecurityType();
      Collection<AnalyticValueDefinition<?>> secTypeValueDefs = valueDefsBySecTypes.get(securityType);

      if(secTypeValueDefs == null) {
        // Nothing required for this sec type for outputs, so no values
        // to populate.
        continue;
      }
      SecurityDependencyGraph depGraph = getPortfolioEvaluationModel().getDependencyGraphModel().getDependencyGraph(security);
      
      for(AnalyticValueDefinition<?> analyticValueDefinition : secTypeValueDefs) {
        AnalyticValueDefinition<?> resolvedDefinition = depGraph.getResolvedOutputs().get(analyticValueDefinition);
        AnalyticValue<?> unscaledValue = getComputationCache().getValue(resolvedDefinition);
        if(unscaledValue != null) {
          AnalyticValue<?> scaledValue = unscaledValue.scaleForPosition(position.getPosition().getQuantity());
          getResultModel().addValue(position.getPosition(), scaledValue);
        }
      }
    }
  }
  
  public void releaseResources() {
    getProcessingContext().getLiveDataSnapshotProvider().releaseSnapshot(getSnapshotTime());
    getProcessingContext().getComputationCacheSource().releaseCache(getViewName(), getSnapshotTime());
  }

}
