/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.analytics.AnalyticFunction;
import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.analytics.AnalyticFunctionResolver;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.DefaultAnalyticFunctionResolver;
import com.opengamma.engine.analytics.LiveDataSourcingFunction;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.security.Security;

/**
 * A logical dependency graph capable of satisfying computations
 * for a particular security type.
 *
 * @author kirk
 */
public class SecurityDependencyGraph {
  private static final Logger s_logger = LoggerFactory.getLogger(SecurityDependencyGraph.class);
  private final Security _security;
  private final Set<AnalyticValueDefinition<?>> _requiredOutputValues =
    new HashSet<AnalyticValueDefinition<?>>();
  private final Set<AnalyticValueDefinition<?>> _requiredLiveData =
    new HashSet<AnalyticValueDefinition<?>>();
  private final Set<DependencyNode> _topLevelNodes =
    new HashSet<DependencyNode>();
  
  public SecurityDependencyGraph(
      Security security,
      Collection<AnalyticValueDefinition<?>> requiredOutputValues) {
    if(security == null) {
      throw new NullPointerException("Must specify a valid security.");
    }
    if((requiredOutputValues == null) || requiredOutputValues.isEmpty()) {
      throw new IllegalArgumentException("Required output values must not be null or empty.");
    }
    _security = security;
    _requiredOutputValues.addAll(requiredOutputValues);
  }

  /**
   * @return the securityType
   */
  public Security getSecurity() {
    return _security;
  }
  
  /**
   * @return the requiredOutputValues
   */
  public Set<AnalyticValueDefinition<?>> getRequiredOutputValues() {
    return _requiredOutputValues;
  }

  /**
   * @return the requiredLiveData
   */
  public Set<AnalyticValueDefinition<?>> getRequiredLiveData() {
    return _requiredLiveData;
  }

  /**
   * @return the topLevelNodes
   */
  public Set<DependencyNode> getTopLevelNodes() {
    return _topLevelNodes;
  }

  public void buildDependencyGraph(
      AnalyticFunctionRepository functionRepository,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    if(functionRepository == null) {
      throw new NullPointerException("Function repository cannot be null.");
    }
    if(liveDataAvailabilityProvider == null) {
      throw new NullPointerException("Live data availability provider cannot be null.");
    }
    buildDependencyGraphImpl(functionRepository, liveDataAvailabilityProvider);
  }
  
  // TODO kirk 2009-09-04 -- Determine how to specify all the various failed dependencies.
  protected void buildDependencyGraphImpl(
      AnalyticFunctionRepository functionRepository,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    assert functionRepository != null;
    assert liveDataAvailabilityProvider != null;
    
    DefaultAnalyticFunctionResolver functionResolver = new DefaultAnalyticFunctionResolver(functionRepository);
    
    Set<DependencyNode> nodes = new HashSet<DependencyNode>();
    Set<AnalyticValueDefinition<?>> requiredLiveData = new HashSet<AnalyticValueDefinition<?>>();
    for(AnalyticValueDefinition<?> outputValue : getRequiredOutputValues()) {
      satisfyDependency(outputValue, nodes, requiredLiveData, functionResolver, liveDataAvailabilityProvider);
    }
    getRequiredLiveData().clear();
    getRequiredLiveData().addAll(requiredLiveData);
    s_logger.info("{} built graph with {} top-level nodes", getSecurity(), nodes.size());
    getTopLevelNodes().addAll(nodes);
  }
  
  protected DependencyNode satisfyDependency(
      AnalyticValueDefinition<?> outputValue,
      Set<DependencyNode> nodes,
      Set<AnalyticValueDefinition<?>> requiredLiveData,
      AnalyticFunctionResolver functionResolver,
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    DependencyNode node = null;
    for(DependencyNode existingNode : nodes) {
      node = existingNode.getMatchingNode(outputValue);
      if(node != null) {
        break;
      }
    }
    if(node != null) {
      s_logger.debug("Satisfied output value {} with existing dependency", outputValue);
      return node;
    }
    
    // Can we satisfy this item using live data?
    if(liveDataAvailabilityProvider.isAvailable(outputValue)) {
      assert !requiredLiveData.contains(outputValue) : "Should have found existing dependency graph node.";
      requiredLiveData.add(outputValue);
      node = new DependencyNode(new LiveDataSourcingFunction(outputValue), getSecurity());
      nodes.add(node);
      return node;
    }
    
    AnalyticFunction function = functionResolver.resolve(outputValue, getSecurity());
    assert function != null : "This is a bad assertion. Do something better.";
    
    node = new DependencyNode(function, getSecurity());
    nodes.add(node);
    for(AnalyticValueDefinition<?> inputValue : node.getInputValues()) {
      DependencyNode inputNode = satisfyDependency(inputValue, nodes, requiredLiveData, functionResolver, liveDataAvailabilityProvider);
      assert inputNode != null : "This is a bad assertion. Do something better.";
      node.addInputNode(inputValue, inputNode);
    }
    return node;
  }
}
