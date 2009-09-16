/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.security.Security;

// TODO kirk 2009-09-14 -- Do we need some type of progress monitor?

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
  private final ViewComputationCache _computationCache;
  private final PortfolioEvaluationModel _portfolioEvaluationModel;
  private final LiveDataSnapshotProvider _snapshotProvider;
  private final ViewDefinition _viewDefinition;
  
  // State:
  private final long _startTime;
  
  // Outputs:
  private long _snapshotTime;
  private final ViewComputationResultModelImpl _resultModel;
  
  public SingleComputationCycle(
      ViewComputationCache cache,
      PortfolioEvaluationModel portfolioEvaluationModel,
      LiveDataSnapshotProvider snapshotProvider,
      ViewComputationResultModelImpl resultModel,
      ViewDefinition viewDefinition) {
    assert cache != null;
    assert portfolioEvaluationModel != null;
    assert snapshotProvider != null;
    assert resultModel != null;
    assert viewDefinition != null;
    _computationCache = cache;
    _portfolioEvaluationModel = portfolioEvaluationModel;
    _snapshotProvider = snapshotProvider;
    _resultModel = resultModel;
    _viewDefinition = viewDefinition;
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
   * @return the portfolioEvaluationModel
   */
  public PortfolioEvaluationModel getPortfolioEvaluationModel() {
    return _portfolioEvaluationModel;
  }

  /**
   * @return the snapshotProvider
   */
  public LiveDataSnapshotProvider getSnapshotProvider() {
    return _snapshotProvider;
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

  public void prepareInputs() {
    setSnapshotTime(getSnapshotProvider().snapshot());
    getResultModel().setInputDataTimestamp(getSnapshotTime());
    
    Set<AnalyticValueDefinition> requiredLiveData = getPortfolioEvaluationModel().getDependencyGraphModel().getAllRequiredLiveData();
    s_logger.debug("Populating {} market data items for snapshot {}", requiredLiveData.size(), getSnapshotTime());
    
    for(AnalyticValueDefinition requiredDataDefinition : requiredLiveData) {
      AnalyticValue value = getSnapshotProvider().querySnapshot(getSnapshotTime(), requiredDataDefinition);
      if(value == null) {
        s_logger.warn("Unable to load live data value for {} at snapshot {}", requiredDataDefinition, getSnapshotTime());
      } else {
        getComputationCache().putValue(value);
      }
    }
  }
  
  public void executePlans() {
    for(PerSecurityExecutionPlan executionPlan : getPortfolioEvaluationModel().getPlansBySecurity().values()) {
      // TODO kirk 2009-09-14 -- Yep, need some concurrency here as well.
      executePlan(executionPlan);
    }
  }

  /**
   * @param executionPlan
   */
  private void executePlan(PerSecurityExecutionPlan executionPlan) {
    // TODO kirk 2009-09-14 -- Yep, this sucks. Totally first-gen code.
    for(DependencyNode node : executionPlan.getOrderedNodes()) {
      // First of all, check that we don't have the outputs already ready.
      boolean allFound = true;
      for(AnalyticValueDefinition outputDefinition : node.getOutputValues()) {
        if(getComputationCache().getValue(outputDefinition) == null) {
          allFound = false;
          break;
        }
      }
      
      if(allFound) {
        s_logger.debug("Able to skip a node because it was already computed.");
        continue;
      }
      
      Collection<AnalyticValue> inputs = new HashSet<AnalyticValue>();
      for(AnalyticValueDefinition inputDefinition : node.getInputValues()) {
        inputs.add(getComputationCache().getValue(inputDefinition));
      }
      Collection<AnalyticValue> outputs = node.getFunction().execute(inputs, executionPlan.getSecurity());
      for(AnalyticValue outputValue : outputs) {
        getComputationCache().putValue(outputValue);
      }
    }
  }
  
  public void populateResultModel() {
    Map<String, Collection<AnalyticValueDefinition>> valueDefsBySecTypes = getViewDefinition().getValueDefinitionsBySecurityTypes(); 
    for(FullyPopulatedPosition position : getPortfolioEvaluationModel().getPopulatedPositions()) {
      // REVIEW kirk 2009-09-14 -- Could be parallelized if we need to.
      Security security = position.getSecurity();
      String securityType = security.getSecurityType();
      Collection<AnalyticValueDefinition> secTypeValueDefs = valueDefsBySecTypes.get(securityType);
      
      for(AnalyticValueDefinition analyticValueDefinition : secTypeValueDefs) {
        AnalyticValue unscaledValue = getComputationCache().getValue(analyticValueDefinition);
        if(unscaledValue != null) {
          AnalyticValue scaledValue = unscaledValue.scaleForPosition(position.getPosition().getQuantity());
          getResultModel().addValue(position.getPosition(), scaledValue);
        }
      }
    }
  }
  
  public void releaseResources() {
    getSnapshotProvider().releaseSnapshot(getSnapshotTime());
  }

}
