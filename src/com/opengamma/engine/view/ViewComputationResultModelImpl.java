/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.depgraph.NewDependencyGraphModel;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.value.AnalyticValueDefinition;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.cache.ViewComputationCache;

/**
 * A simple in-memory implementation of {@link ViewComputatinResultModel}.
 * REVIEW: jim 29-Oct-2009 -- at the moment this model uses references to PortfolioNodes and Positions that will break if we
 * start incrementally updating the portfolio.  It also won't travel well over the wire.  We're going to need to deal with this
 * once we start doing that.
 * @author kirk
 */
public class ViewComputationResultModelImpl implements
    ViewComputationResultModel, Serializable {
  private final Map<Position, PositionResultModel> _perPositionResults = new HashMap<Position, PositionResultModel>();
  private final Map<PortfolioNode, AggregatePositionResultModel> _perNodeResults = new HashMap<PortfolioNode, AggregatePositionResultModel>();
  private long _inputDataTimestamp;
  private long _resultTimestamp;
  private ViewComputationCache _cache;
  private NewDependencyGraphModel _dependencyGraphModel;
  private SecurityMaster _securityMaster;
  private Portfolio _portfolio;
  private PortfolioNode _rootPopulatedNode;
  
  @Override
  public long getInputDataTimestamp() {
    return _inputDataTimestamp;
  }

  /**
   * @param inputDataTimestamp the inputDataTimestamp to set
   */
  public void setInputDataTimestamp(long inputDataTimestamp) {
    _inputDataTimestamp = inputDataTimestamp;
  }

  /**
   * @returns flat collection of all the positions in the portfolio
   */
  @Override
  public Collection<Position> getPositions() {
    return Collections.unmodifiableSet(_perPositionResults.keySet());
  }

  @Override
  public long getResultTimestamp() {
    return _resultTimestamp;
  }

  /**
   * @param resultTimestamp the resultTimestamp to set
   */
  public void setResultTimestamp(long resultTimestamp) {
    _resultTimestamp = resultTimestamp;
  }

  @Override
  public ComputedValue<?> getValue(Position position,
      AnalyticValueDefinition<?> valueDefinition) {
    PositionResultModel perPositionModel = _perPositionResults.get(position);
    if(perPositionModel == null) {
      return null;
    } else {
      return perPositionModel.get(valueDefinition);
    }
  }
  
  @Override 
  public ComputedValue<?> getValue(PortfolioNode node, AnalyticValueDefinition<?> valueDefinition) {
    AggregatePositionResultModel perAggregatePositionModel = _perNodeResults.get(node);
    if(perAggregatePositionModel == null) {
      return null;
    } else {
      return perAggregatePositionModel.get(valueDefinition);
    }
  }

  @Override
  public Map<AnalyticValueDefinition<?>, ComputedValue<?>> getValues(Position position) {
    PositionResultModel perPositionModel = _perPositionResults.get(position);
    if(perPositionModel == null) {
      return Collections.emptyMap();
    } else {
      return perPositionModel.getAllResults();
    }
  }
  
  @Override
  public Map<AnalyticValueDefinition<?>, ComputedValue<?>> getValues(PortfolioNode node) {
    AggregatePositionResultModel perAggregatePositionModel = _perNodeResults.get(node);
    if(perAggregatePositionModel == null) {
      return Collections.emptyMap();
    } else {
      return perAggregatePositionModel.getAllResults();
    }
  }
  
  public void addValue(Position position, ComputedValue<?> value) {
    PositionResultModel perPositionModel = _perPositionResults.get(position);
    assert perPositionModel != null;
    perPositionModel.add(value);
  }
  
  public void addPosition(Position position) {
    PositionResultModel perPositionModel = _perPositionResults.get(position);
    if(perPositionModel == null) {
      perPositionModel = new PositionResultModel(position);
      _perPositionResults.put(position, perPositionModel);
    }
  }
  
  public void addValue(PortfolioNode node, ComputedValue<?> value) {
    AggregatePositionResultModel perAggregatePositionModel = _perNodeResults.get(node);
    assert perAggregatePositionModel != null;
    perAggregatePositionModel.add(value);
  }
  
  public void addAggregatePosition(PortfolioNode node) {
    AggregatePositionResultModel perAggregatePositionModel = _perNodeResults.get(node);
    if(perAggregatePositionModel == null) {
      perAggregatePositionModel = new AggregatePositionResultModel(node);
      _perNodeResults.put(node, perAggregatePositionModel);
    }
  }
  
  public void setPortfolio(Portfolio portfolio, PortfolioNode populatedRootNode) {
    _portfolio = portfolio; 
    recursiveAddPortfolio(populatedRootNode);
  }
  
  public Portfolio getPortfolio() {
    return _portfolio;
  }
  
  /**
   * @param rootPopulatedNode the rootPopulatedNode to set
   */
  public void setRootPopulatedNode(PortfolioNode rootPopulatedNode) {
    _rootPopulatedNode = rootPopulatedNode;
  }

  @Override
  public PortfolioNode getRootPopulatedNode() {
    return _rootPopulatedNode;
  }
  
  private void recursiveAddPortfolio(PortfolioNode node) {
    for (Position position : node.getPositions()) {
      addPosition(position);
    }
    for (PortfolioNode subNode : node.getSubNodes()) {
      recursiveAddPortfolio(subNode);
    }
    addAggregatePosition(node);
  }
  
  public void setComputationCache(ViewComputationCache cache) {
    _cache = cache;
  }  
  
  public ViewComputationCache getComputationCache() {
    return _cache;  
  }

  /**
   * @param dependencyGraphModel
   */
  public void setDependencyGraphModel(NewDependencyGraphModel dependencyGraphModel) {
    _dependencyGraphModel = dependencyGraphModel;
  }
  
  public NewDependencyGraphModel getDependencyGraphModel() {
    return _dependencyGraphModel;
  }

  // BIG REVIEW: jim 12-Oct-09 -- this is a super-big hack so that the viewer can convert from Positions to securities and pull nodes from the dep graph.
  /**
   * @param securityMaster
   */
  @Deprecated
  public void setSecurityMaster(SecurityMaster securityMaster) {
    _securityMaster = securityMaster;
  }
  
  @Deprecated
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }
  
  public String getPositionValuesAsText() {
    StringBuilder sb = new StringBuilder();
    for(Map.Entry<Position, PositionResultModel> entry : _perPositionResults.entrySet()) {
      sb.append(entry.getValue().debugToString());
      sb.append("\n");
    }
    return sb.toString();
  }

}
