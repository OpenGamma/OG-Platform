/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.opengamma.engine.LiveDataAvailabilityProvider;
import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.depgraph.LogicalDependencyGraphModel;
import com.opengamma.engine.position.AggregatePosition;

/**
 * The base implementation of the {@link View} interface.
 *
 * @author kirk
 */
public class ViewImpl implements View {
  private final ViewDefinition _definition;
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private AnalyticFunctionRepository _analyticFunctionRepository;
  
  public ViewImpl(ViewDefinition definition) {
    if(definition == null) {
      throw new NullPointerException("Must provide a definition.");
    }
    _definition = definition;
  }
  
  /**
   * @return the liveDataAvailabilityProvider
   */
  public LiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }

  /**
   * @param liveDataAvailabilityProvider the liveDataAvailabilityProvider to set
   */
  @Required
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
  @Required
  public void setAnalyticFunctionRepository(
      AnalyticFunctionRepository analyticFunctionRepository) {
    _analyticFunctionRepository = analyticFunctionRepository;
  }

  /**
   * @return the definition
   */
  public ViewDefinition getDefinition() {
    return _definition;
  }

  // TODO kirk 2009-09-03 -- Flesh out a bootstrap system:
  // - Walk through an AnalyticFunctionRepository to get all functions
  // - Load up the contents of the portfolio from a PortfolioMaster
  // - Load up the securities of the portfolio from a SecurityMaster
  // - Gather up all problems if anything isn't available
  
  public void init() {
    checkInjectedDependencies();
    LogicalDependencyGraphModel logicalDepGraph = new LogicalDependencyGraphModel();
    logicalDepGraph.setAnalyticFunctionRepository(getAnalyticFunctionRepository());
    logicalDepGraph.setLiveDataAvailabilityProvider(getLiveDataAvailabilityProvider());
    
    Map<String, Collection<AnalyticValueDefinition>> outputsBySecurityType = getDefinition().getValueDefinitionsBySecurityTypes();
    for(Map.Entry<String, Collection<AnalyticValueDefinition>> entry : outputsBySecurityType.entrySet()) {
      // REVIEW kirk 2009-09-04 -- This is potentially a VERY computationally expensive
      // operation. We could/should do them in parallel.
      logicalDepGraph.addSecurityType(entry.getKey(), entry.getValue());
    }
  }
  
  /**
   * 
   */
  private void checkInjectedDependencies() {
    if(getAnalyticFunctionRepository() == null) {
      throw new IllegalStateException("Must have an Analytic Function Repository");
    }
    if(getLiveDataAvailabilityProvider() == null) {
      throw new IllegalStateException("Must have a Live Data Availability Provider");
    }
  }

  @Override
  public ViewComputationResultModel getMostRecentResult() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AggregatePosition getPositionRoot() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public void recalculationPerformed(ViewComputationResultModel result) {
  }

}
