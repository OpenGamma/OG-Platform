/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;

/**
 * A full model representing all operations that must be performed to
 * satisfy a particular logical computation model.
 *
 * @author kirk
 */
public class DependencyGraphModel {
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphModel.class);
  private final Map<Security, DependencyGraph> _graphForSecurity =
    new HashMap<Security, DependencyGraph>();
  private final Map<Position, DependencyGraph> _graphForPosition = 
    new HashMap<Position, DependencyGraph>();
  private final Map<PortfolioNode, DependencyGraph> _graphForAggregatePosition =
    new HashMap<PortfolioNode, DependencyGraph>();
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private AnalyticFunctionRepository _analyticFunctionRepository;
  
  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @param liveDataAvailabilityProvider the liveDataAvailabilityProvider to set
   */
  public void setLiveDataAvailabilityProvider(
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
  }

  /**
   * @return the analyticFunctionRepository
   */
  public AnalyticFunctionRepository getAnalyticFunctionRepository() {
    return _analyticFunctionRepository;
  }

  /**
   * @param analyticFunctionRepository the analyticFunctionRepository to set
   */
  public void setAnalyticFunctionRepository(
      AnalyticFunctionRepository analyticFunctionRepository) {
    _analyticFunctionRepository = analyticFunctionRepository;
  }

  public void addPosition(Position position, Collection<AnalyticValueDefinition<?>> requiredOutputValues) {
    if(position == null) {
      throw new NullPointerException("Must provide a valid position.");
    }
    if((getLiveDataAvailabilityProvider() == null)
        || (getAnalyticFunctionRepository() == null)) {
      throw new IllegalStateException("Must have provided a data availability provider and analytic function repository.");
    }
    if(_graphForPosition.containsKey(position)) {
      s_logger.debug("Already added position {}", position);
      return;
    }
    if (_graphForSecurity.containsKey(position.getSecurity())) {
      // we don't bother because it's already been done. 
      // REVIEW: jim 28-Oct-2009 -- we're making the assumption here that if it's security-specific for one position, it is for all with the same security.
      s_logger.debug("Already added position with this security, so assuming it's the same");
      return;
    } else {
      DependencyGraph depGraph = new DependencyGraph(requiredOutputValues, position.getSecurity(), position);
      depGraph.buildDependencyGraph(getAnalyticFunctionRepository(), getLiveDataAvailabilityProvider());
      switch (depGraph.getComputationTargetType()) {
      case PRIMITIVE:
        // REVIEW: jim 28-Oct-2009 -- not actually sure what should happen here.
        throw new OpenGammaRuntimeException("shouldn't encounter a primitive function here");
      case SECURITY:
        _graphForSecurity.put(position.getSecurity(), depGraph);
        break;
      case POSITION:
        _graphForPosition.put(position, depGraph);
        break;
      case MULTIPLE_POSITIONS:
        throw new OpenGammaRuntimeException("shouldn't encounter an aggregated position set here");
      }
    }
        
  }
  
  public void addAggregatePosition(PortfolioNode node, Collection<AnalyticValueDefinition<?>> requiredOutputValues) {
    if(node == null) {
      throw new NullPointerException("Must provide a valid portfolio node.");
    }
    if((getLiveDataAvailabilityProvider() == null)
        || (getAnalyticFunctionRepository() == null)) {
      throw new IllegalStateException("Must have provided a data availability provider and analytic function repository.");
    }
    if(_graphForAggregatePosition.containsKey(node)) {
      s_logger.debug("Already added portfolio node {}", node);
      return;
    }
    Collection<Position> positions = flattenPortfolio(node);
    DependencyGraph depGraph = new DependencyGraph(requiredOutputValues, positions);
    depGraph.buildDependencyGraph(getAnalyticFunctionRepository(), getLiveDataAvailabilityProvider());
    _graphForAggregatePosition.put(node, depGraph);
  }
  
  protected Collection<Position> flattenPortfolio(PortfolioNode node) {
    if (node.getSubNodes().size() == 0) { // just a little early optimization.
      return node.getPositions();
    } else {
      Collection<Position> positions = new ArrayList<Position>(node.getPositions());
      for (PortfolioNode subNode : node.getSubNodes()) {
        positions.addAll(flattenPortfolio(subNode));
      }
      return positions;
    }
  }
  
  public Set<AnalyticValueDefinition<?>> getAllRequiredLiveData() {
    Set<AnalyticValueDefinition<?>> result = new HashSet<AnalyticValueDefinition<?>>();
    for(DependencyGraph secTypeGraph : _graphForSecurity.values()) {
      result.addAll(secTypeGraph.getRequiredLiveData());
    }
    for(DependencyGraph posTypeGraph : _graphForPosition.values()) {
      result.addAll(posTypeGraph.getRequiredLiveData());
    }
    for(DependencyGraph aggTypeGraph : _graphForAggregatePosition.values()) {
      result.addAll(aggTypeGraph.getRequiredLiveData());
    }
    return result;
  }
  
  public DependencyGraph getDependencyGraph(Security security) {
    return _graphForSecurity.get(security);
  }
  
  public DependencyGraph getDependencyGraph(PortfolioNode node) {
    return _graphForAggregatePosition.get(node);
  }

  public DependencyGraph getDependencyGraph(Position position) {
    return _graphForPosition.get(position);
  }
  
}
