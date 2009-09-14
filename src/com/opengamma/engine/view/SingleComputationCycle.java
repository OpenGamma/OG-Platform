/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.LiveDataSnapshotProvider;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.depgraph.LogicalDependencyGraphModel;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;

/**
 * Holds all data and actions for a single pass through a computation cycle.
 * In general, each invocation of {@link ViewRecalculationJob#runOneCycle()}
 * will create an instance of this class.
 *
 * @author kirk
 */
public class SingleComputationCycle {
  private static final Logger s_logger = LoggerFactory.getLogger(SingleComputationCycle.class);
  // Injected Inputs:
  private final ViewComputationCache _computationCache;
  private final PortfolioNode _rootNode;
  private final LiveDataSnapshotProvider _snapshotProvider;
  private final LogicalDependencyGraphModel _logicalDependencyGraph;
  
  // State:
  private final long _startTime;
  // REVIEW kirk 2009-09-14 -- HashSet is almost certainly the wrong set here.
  private final Set<Position> _positions = new HashSet<Position>();
  
  // Outputs:
  private long _snapshotTime;
  private final ViewComputationResultModelImpl _resultModel;
  
  public SingleComputationCycle(
      ViewComputationCache cache,
      PortfolioNode rootNode,
      LiveDataSnapshotProvider snapshotProvider,
      LogicalDependencyGraphModel logicalDependencyGraph,
      ViewComputationResultModelImpl resultModel) {
    assert cache != null;
    assert rootNode != null;
    assert snapshotProvider != null;
    assert logicalDependencyGraph != null;
    assert resultModel != null;
    _computationCache = cache;
    _rootNode = rootNode;
    _snapshotProvider = snapshotProvider;
    _logicalDependencyGraph = logicalDependencyGraph;
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
   * @return the computationCache
   */
  public ViewComputationCache getComputationCache() {
    return _computationCache;
  }

  /**
   * @return the rootNode
   */
  public PortfolioNode getRootNode() {
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
  public Set<Position> getPositions() {
    return _positions;
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
    s_logger.debug("Operating on {} positions this cycle", getPositions().size());
  }
  
  protected void loadPositions(PortfolioNode node) {
    getPositions().addAll(node.getPositions());
    for(PortfolioNode child : node.getSubNodes()) {
      loadPositions(child);
    }
  }

}
