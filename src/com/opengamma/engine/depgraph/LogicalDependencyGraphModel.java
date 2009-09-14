/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.LiveDataAvailabilityProvider;
import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.analytics.AnalyticValueDefinition;

/**
 * A full model representing all operations that must be performed to
 * satisfy a particular logical computation model.
 *
 * @author kirk
 */
public class LogicalDependencyGraphModel {
  private static final Logger s_logger = LoggerFactory.getLogger(LogicalDependencyGraphModel.class);
  private final Map<String, SecurityTypeLogicalDependencyGraph> _graphForSecurityTypes =
    new TreeMap<String, SecurityTypeLogicalDependencyGraph>();
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

  public void addSecurityType(String securityType, Collection<AnalyticValueDefinition> requiredOutputValues) {
    if(securityType == null) {
      throw new NullPointerException("Must provide a valid security type.");
    }
    if((getLiveDataAvailabilityProvider() == null)
        || (getAnalyticFunctionRepository() == null)) {
      throw new IllegalStateException("Must have provided a data availability provider and analytic function repository.");
    }
    if(_graphForSecurityTypes.containsKey(securityType)) {
      s_logger.debug("Already added security type {}", securityType);
      return;
    }
    SecurityTypeLogicalDependencyGraph depGraph = new SecurityTypeLogicalDependencyGraph(securityType, requiredOutputValues);
    _graphForSecurityTypes.put(securityType, depGraph);
  }
  
  public Set<AnalyticValueDefinition> getAllRequiredLiveData() {
    Set<AnalyticValueDefinition> result = new HashSet<AnalyticValueDefinition>();
    for(SecurityTypeLogicalDependencyGraph secTypeGraph : _graphForSecurityTypes.values()) {
      result.addAll(secTypeGraph.getRequiredLiveData());
    }
    return result;
  }
  
  public SecurityTypeLogicalDependencyGraph getLogicalGraph(String securityType) {
    return _graphForSecurityTypes.get(securityType);
  }

}
