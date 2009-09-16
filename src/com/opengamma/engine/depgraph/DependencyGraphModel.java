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

import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.security.Security;

/**
 * A full model representing all operations that must be performed to
 * satisfy a particular logical computation model.
 *
 * @author kirk
 */
public class DependencyGraphModel {
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphModel.class);
  private final Map<Security, SecurityDependencyGraph> _graphForSecurity =
    new TreeMap<Security, SecurityDependencyGraph>();
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

  public void addSecurity(Security security, Collection<AnalyticValueDefinition> requiredOutputValues) {
    if(security == null) {
      throw new NullPointerException("Must provide a valid security.");
    }
    if((getLiveDataAvailabilityProvider() == null)
        || (getAnalyticFunctionRepository() == null)) {
      throw new IllegalStateException("Must have provided a data availability provider and analytic function repository.");
    }
    if(_graphForSecurity.containsKey(security)) {
      s_logger.debug("Already added security {}", security);
      return;
    }
    SecurityDependencyGraph depGraph = new SecurityDependencyGraph(security, requiredOutputValues);
    _graphForSecurity.put(security, depGraph);
  }
  
  public Set<AnalyticValueDefinition> getAllRequiredLiveData() {
    Set<AnalyticValueDefinition> result = new HashSet<AnalyticValueDefinition>();
    for(SecurityDependencyGraph secTypeGraph : _graphForSecurity.values()) {
      result.addAll(secTypeGraph.getRequiredLiveData());
    }
    return result;
  }
  
  public SecurityDependencyGraph getDependencyGraph(Security security) {
    return _graphForSecurity.get(security);
  }

}
