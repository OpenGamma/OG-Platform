/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.AnalyticValueImpl;
import com.opengamma.engine.depgraph.RevisedDependencyGraph;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
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
  
  // State:
  private final long _startTime;
  private ViewComputationCache _computationCache;
  private final AtomicLong _jobIdSource = new AtomicLong(0l);
  
  // Outputs:
  private long _snapshotTime;
  private final ViewComputationResultModelImpl _resultModel;
  
  public SingleComputationCycle(
      String viewName,
      ViewProcessingContext processingContext,
      PortfolioEvaluationModel portfolioEvaluationModel,
      ViewComputationResultModelImpl resultModel,
      ViewDefinition viewDefinition) {
    assert viewName != null;
    assert processingContext != null;
    assert portfolioEvaluationModel != null;
    assert resultModel != null;
    assert viewDefinition != null;
    _viewName = viewName;
    _processingContext = processingContext;
    _portfolioEvaluationModel = portfolioEvaluationModel;
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

  public boolean prepareInputs() {
    setSnapshotTime(getProcessingContext().getLiveDataSnapshotProvider().snapshot());
    ViewComputationCache cache = getProcessingContext().getComputationCacheSource().getCache(getViewName(), getSnapshotTime());
    assert cache != null;
    setComputationCache(cache);
    getResultModel().setInputDataTimestamp(getSnapshotTime());
    getResultModel().setRootPopulatedNode(getPortfolioEvaluationModel().getPopulatedRootNode());
    
    Set<AnalyticValueDefinition<?>> requiredLiveData = getPortfolioEvaluationModel().getDependencyGraphModel().getAllRequiredLiveData();
    s_logger.debug("Populating {} market data items for snapshot {}", requiredLiveData.size(), getSnapshotTime());
    
    boolean missingData = false;
    for(AnalyticValueDefinition<?> requiredDataDefinition : requiredLiveData) {
      Object data = getProcessingContext().getLiveDataSnapshotProvider().querySnapshot(getSnapshotTime(), requiredDataDefinition);
      if(data == null) {
        s_logger.debug("Unable to load live data value for {} at snapshot {}.", requiredDataDefinition, getSnapshotTime());
        missingData = true;
      } else {
        @SuppressWarnings("unchecked")
        AnalyticValue<Object> dataAsValue = new AnalyticValueImpl(requiredDataDefinition, data);
        getComputationCache().putValue(dataAsValue);
      }
    }
    if(missingData) {
      s_logger.warn("Unable to load some input market data. Expect that nodes will fail to compute.");
    }
    return true;
  }
  
  public void executePlans() {
    PortfolioNode populatedRootNode = getPortfolioEvaluationModel().getPopulatedRootNode();
    executePrimitivePlan();
    executeSecuritySpecificPlans();
    executeAggregateAndPositionDependentPlans(populatedRootNode);
  }
  
  /**
   * 
   */
  private void executePrimitivePlan() {
    s_logger.debug("{} - Executing primitive plan", getSnapshotTime());
    RevisedDependencyGraph primitiveDepGraph = getPortfolioEvaluationModel().getDependencyGraphModel().getPrimitiveGraph();
    assert primitiveDepGraph != null;
    DependencyGraphExecutor depGraphExecutor = new DependencyGraphExecutor(
        getViewName(),
        primitiveDepGraph,
        getProcessingContext());
    depGraphExecutor.executeGraph(getSnapshotTime(), _jobIdSource);
  }

  // REVIEW kirk 2009-11-03 -- This is a database kernel. Act accordingly.
  public void executeSecuritySpecificPlans() {
    // REVIEW kirk 2009-11-02 -- These can actually run in parallel.
    for(Security security : getPortfolioEvaluationModel().getSecurities()) {
      s_logger.debug("{} - Executing security plan for {}", getSnapshotTime(), security);
      RevisedDependencyGraph secDepGraph = getPortfolioEvaluationModel().getDependencyGraphModel().getDependencyGraph(security);
      if(secDepGraph == null) {
        s_logger.debug("{} - No dep graph for {}. Must have been satisfied from lower-levels.", getSnapshotTime(), security);
        continue;
      }
      DependencyGraphExecutor depGraphExecutor = new DependencyGraphExecutor(
          getViewName(),
          security,
          secDepGraph,
          getProcessingContext()); 
      depGraphExecutor.executeGraph(getSnapshotTime(), _jobIdSource);
    }
  }

  public void executeAggregateAndPositionDependentPlans(PortfolioNode node) {
    // REVIEW kirk 2009-11-02 -- These can actually run in parallel.
    for(Position position : node.getPositions()) {
      RevisedDependencyGraph posDepGraph = getPortfolioEvaluationModel().getDependencyGraphModel().getDependencyGraph(position);
      if (posDepGraph != null) { // we might not have a portfolio specific graph here.
        DependencyGraphExecutor depGraphExecutor = new DependencyGraphExecutor(
            getViewName(),
            position,
            posDepGraph,
            getProcessingContext());
        depGraphExecutor.executeGraph(getSnapshotTime(), _jobIdSource);
      }
    }
    // NOTE: jim 28-Oct-2009 -- I've done this second because the first bit might have populated the cache or something - actually could work either way I guess.
    RevisedDependencyGraph aggDepGraph = getPortfolioEvaluationModel().getDependencyGraphModel().getDependencyGraph(node);
    if(aggDepGraph == null) {
      s_logger.debug("{} - No dep graph for aggregate node {}. Must have been satisfied from lower levels.", getSnapshotTime(), node);
      return;
    }
    DependencyGraphExecutor depGraphExecutor = new DependencyGraphExecutor(
        getViewName(),
        // TODO kirk 2009-11-02 -- Fix This
        new ArrayList<Position>(), //aggDepGraph.getPositions(), // I _think_ this is okay...
        aggDepGraph,
        getProcessingContext());
    depGraphExecutor.executeGraph(getSnapshotTime(), _jobIdSource);
    for(PortfolioNode subNode : node.getSubNodes()) {
      executeAggregateAndPositionDependentPlans(subNode);
    }
  }
  
  @SuppressWarnings("deprecation")
  public void populateResultModel() {
    populateResultModel(getPortfolioEvaluationModel().getPopulatedRootNode());
    s_logger.info("Computed result model {}", getResultModel().getPositionValuesAsText());
    // so viewer can access dependency graph values.
    getResultModel().setDependencyGraphModel(getPortfolioEvaluationModel().getDependencyGraphModel());
    getResultModel().setComputationCache(getProcessingContext().getComputationCacheSource().cloneCache( getViewName(), getSnapshotTime()));
    getResultModel().setSecurityMaster(getProcessingContext().getSecurityMaster()); // this is teh nasty.  We need some better way for the viewer to convert positions to securities.
  }
  
  public Set<String> populateResultModel(PortfolioNode node) {
    Map<String, Collection<AnalyticValueDefinition<?>>> valueDefsBySecTypes = getViewDefinition().getValueDefinitionsBySecurityTypes();
    Set<String> allSecurityTypesRecursive = new HashSet<String>();
    for (Position position : node.getPositions()) {
      getResultModel().addPosition(position);
      Security security = position.getSecurity();
      assert security != null;
      String securityType = security.getSecurityType();
      assert securityType != null;
      allSecurityTypesRecursive.add(securityType);
      
      Collection<AnalyticValueDefinition<?>> secTypeValueDefs = valueDefsBySecTypes.get(securityType);
      if (secTypeValueDefs == null) {
        // Nothing required for this security type for outputs, so no values to populate
        continue;
      }
      
      RevisedDependencyGraph depGraph = getPortfolioEvaluationModel().getDependencyGraphModel().getDependencyGraph(position);
      assert depGraph != null;
      for(AnalyticValueDefinition<?> analyticValueDefinition : secTypeValueDefs) {
        AnalyticValueDefinition<?> resolvedDefinition = depGraph.getResolvedRequirement(analyticValueDefinition);
        AnalyticValue<?> unscaledValue = getComputationCache().getValue(resolvedDefinition);
        if(unscaledValue != null) {
          // REVIEW kirk 2009-11-03 -- When we put scaling as function nodes at the Position
          // level, we won't scale here. It doesn't work for Position-specific values from a Position-based function.
          AnalyticValue<?> scaledValue = unscaledValue.scaleForPosition(position.getQuantity());
          getResultModel().addValue(position, scaledValue);
        }
      }      
    }
    for (PortfolioNode subNode : node.getSubNodes()) {
      allSecurityTypesRecursive.addAll(populateResultModel(subNode));
    }
    Collection<AnalyticValueDefinition<?>> commonValueDefsForPositionsUnder = new HashSet<AnalyticValueDefinition<?>>();
    for (String securityType : allSecurityTypesRecursive) {
      commonValueDefsForPositionsUnder.addAll(valueDefsBySecTypes.get(securityType));
    }
    RevisedDependencyGraph depGraph = getPortfolioEvaluationModel().getDependencyGraphModel().getDependencyGraph(node);
    for(AnalyticValueDefinition<?> analyticValueDefinition : commonValueDefsForPositionsUnder) {
      AnalyticValueDefinition<?> resolvedDefinition = depGraph.getResolvedRequirement(analyticValueDefinition);
      AnalyticValue<?> unscaledValue = getComputationCache().getValue(resolvedDefinition);
      if(unscaledValue != null) {
        getResultModel().addValue(node, unscaledValue);
      }
    }
    return allSecurityTypesRecursive;
  }
  
  public void releaseResources() {
    getProcessingContext().getLiveDataSnapshotProvider().releaseSnapshot(getSnapshotTime());
    getProcessingContext().getComputationCacheSource().releaseCache(getViewName(), getSnapshotTime());
  }

}
