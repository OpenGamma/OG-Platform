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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.LiveDataSnapshotProvider;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.LogicalDependencyGraphModel;
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
  private final FullyPopulatedPortfolioNode _rootNode;
  private final LiveDataSnapshotProvider _snapshotProvider;
  private final LogicalDependencyGraphModel _logicalDependencyGraph;
  private final ViewDefinition _viewDefinition;
  
  // State:
  private final long _startTime;
  // REVIEW kirk 2009-09-14 -- HashSet is almost certainly the wrong set here.
  private final Set<FullyPopulatedPosition> _populatedPositions = new HashSet<FullyPopulatedPosition>();
  private final Map<Security, PerSecurityExecutionPlan> _plansBySecurity = new HashMap<Security, PerSecurityExecutionPlan>();
  
  // Outputs:
  private long _snapshotTime;
  private final ViewComputationResultModelImpl _resultModel;
  
  public SingleComputationCycle(
      ViewComputationCache cache,
      FullyPopulatedPortfolioNode rootNode,
      LiveDataSnapshotProvider snapshotProvider,
      LogicalDependencyGraphModel logicalDependencyGraph,
      ViewComputationResultModelImpl resultModel,
      ViewDefinition viewDefinition) {
    assert cache != null;
    assert rootNode != null;
    assert snapshotProvider != null;
    assert logicalDependencyGraph != null;
    assert resultModel != null;
    assert viewDefinition != null;
    _computationCache = cache;
    _rootNode = rootNode;
    _snapshotProvider = snapshotProvider;
    _logicalDependencyGraph = logicalDependencyGraph;
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
   * @return the rootNode
   */
  public FullyPopulatedPortfolioNode getRootNode() {
    return _rootNode;
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
   * @return the logicalDependencyGraph
   */
  public LogicalDependencyGraphModel getLogicalDependencyGraph() {
    return _logicalDependencyGraph;
  }

  /**
   * @return the resultModel
   */
  public ViewComputationResultModelImpl getResultModel() {
    return _resultModel;
  }

  /**
   * @return the positions
   */
  public Set<FullyPopulatedPosition> getPopulatedPositions() {
    return _populatedPositions;
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
    
    Set<AnalyticValueDefinition> requiredLiveData = getLogicalDependencyGraph().getAllRequiredLiveData();
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
  
  public void loadPositions() {
    loadPositions(getRootNode());
    s_logger.debug("Operating on {} positions this cycle", getPopulatedPositions().size());
  }
  
  protected void loadPositions(FullyPopulatedPortfolioNode node) {
    for(FullyPopulatedPortfolioNode child : node.getPopulatedSubNodes()) {
      loadPositions(child);
    }
  }
  
  public void buildExecutionPlans() {
    Set<Security> securities = new HashSet<Security>();
    for(FullyPopulatedPosition position : getPopulatedPositions()) {
      securities.add(position.getSecurity());
    }
    s_logger.debug("Building execution plans for {} distinct securities", securities.size());
    for(Security security : securities) {
      PerSecurityExecutionPlan executionPlan = new PerSecurityExecutionPlan(security, getLogicalDependencyGraph().getLogicalGraph(security.getSecurityType()));
      _plansBySecurity.put(security, executionPlan);
    }
    for(PerSecurityExecutionPlan executionPlan : _plansBySecurity.values()) {
      // TODO kirk 2009-09-14 -- This might be expensive in the future, so should be done
      // in a parallel form.
      executionPlan.buildExecutionPlan();
    }
  }
  
  public void executePlans() {
    for(PerSecurityExecutionPlan executionPlan : _plansBySecurity.values()) {
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
    for(FullyPopulatedPosition position : getPopulatedPositions()) {
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

}
