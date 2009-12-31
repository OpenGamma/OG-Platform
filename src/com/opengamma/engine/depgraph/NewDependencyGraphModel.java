/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.NewFunctionDefinition;
import com.opengamma.engine.function.NewLiveDataSourcingFunction;
import com.opengamma.engine.livedata.NewLiveDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.Pair;

/**
 * 
 *
 * @author kirk
 */
public class NewDependencyGraphModel {
  private static final Logger s_logger = LoggerFactory.getLogger(NewDependencyGraphModel.class);
  // Injected Inputs:
  private NewLiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private FunctionRepository _functionRepository;
  private ComputationTargetResolver _targetResolver;
  // State:
  private final Map<ComputationTarget, NewDependencyGraph> _graphsByTarget =
    new HashMap<ComputationTarget, NewDependencyGraph>();
  private final Set<ValueRequirement> _allRequiredLiveData = new HashSet<ValueRequirement>();
    
  
  /**
   * @return the liveDataAvailabilityProvider
   */
  public NewLiveDataAvailabilityProvider getLiveDataAvailabilityProvider() {
    return _liveDataAvailabilityProvider;
  }
  /**
   * @param liveDataAvailabilityProvider the liveDataAvailabilityProvider to set
   */
  public void setLiveDataAvailabilityProvider(
      NewLiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
  }
  /**
   * @return the analyticFunctionRepository
   */
  public FunctionRepository getFunctionRepository() {
    return _functionRepository;
  }
  /**
   * @param functionRepository the analyticFunctionRepository to set
   */
  public void setFunctionRepository(
      FunctionRepository functionRepository) {
    _functionRepository = functionRepository;
  }
  
  /**
   * @return the targetResolver
   */
  public ComputationTargetResolver getTargetResolver() {
    return _targetResolver;
  }
  /**
   * @param targetResolver the targetResolver to set
   */
  public void setTargetResolver(ComputationTargetResolver targetResolver) {
    _targetResolver = targetResolver;
  }
  
  public Set<ValueRequirement> getAllRequiredLiveData() {
    return Collections.unmodifiableSet(_allRequiredLiveData);
  }
  
  public void addTarget(ComputationTarget target, Set<ValueRequirement> requirements) {
    ArgumentChecker.checkNotNull(target, "Computation Target");
    ArgumentChecker.checkNotNull(requirements, "Value requirements");
    
    for(ValueRequirement requirement : requirements) {
      // TODO kirk 2009-12-30 -- Need to do something with the top-most mappings.
      addTargetRequirement(target, requirement);
    }
  }
  
  protected Pair<NewDependencyNode, ValueSpecification> addTargetRequirement(
      ComputationTarget target, ValueRequirement requirement) {
    s_logger.info("Adding target requirement for {} on {}", target, requirement);
    Pair<NewDependencyNode, ValueSpecification> existingNode = resolveRequirement(target, requirement);
    if(existingNode == null) {
      s_logger.debug("Satisfied requirement for {} on {} via existing node", target, requirement);
      return existingNode;
    }
    
    NewDependencyGraph depGraph = getDependencyGraph(target);
    
    if(getLiveDataAvailabilityProvider().isAvailable(requirement)) {
      s_logger.debug("Satisfied requirement for {} on {} via live data", target, requirement);
      _allRequiredLiveData.add(requirement);
      NewLiveDataSourcingFunction function = new NewLiveDataSourcingFunction(requirement);
      NewDependencyNode node = new NewDependencyNode(function, target);
      depGraph.addDependencyNode(node);
      return new Pair<NewDependencyNode, ValueSpecification>(node, function.getResult());
    }
    
    Pair<NewFunctionDefinition, ValueSpecification> resolvedFunction = resolveFunction(target, requirement);
    if(resolvedFunction == null) {
      // Couldn't resolve.
      // TODO kirk 2009-12-30 -- Gather up all the errors in some way.
      throw new UnsatisfiableDependencyGraphException("Could not satisfy requirement " + requirement + " for target " + target);
    }
    NewDependencyNode node = new NewDependencyNode(resolvedFunction.getFirst(), target);
    depGraph.addDependencyNode(node);
    
    for(ValueRequirement inputRequirement : node.getInputRequirements()) {
      ComputationTarget inputTarget = getTargetResolver().resolve(inputRequirement.getTargetSpecification());
      if(inputTarget == null) {
        throw new UnsatisfiableDependencyGraphException("Unable to resolve target for " + inputRequirement);
      }
      Pair<NewDependencyNode, ValueSpecification> resolvedInput = addTargetRequirement(inputTarget, inputRequirement);
      node.addInputNode(resolvedInput.getFirst());
      node.addRequirementMapping(inputRequirement, resolvedInput.getSecond());
    }
    
    return new Pair<NewDependencyNode, ValueSpecification>(node, resolvedFunction.getSecond());
  }
  
  protected NewDependencyGraph getDependencyGraph(ComputationTarget target) {
    NewDependencyGraph depGraph = _graphsByTarget.get(target);
    if(depGraph == null) {
      depGraph = new NewDependencyGraph(target);
      _graphsByTarget.put(target, depGraph);
    }
    return depGraph;
  }
  
  // TODO kirk 2009-12-30 -- This belongs elsewhere.
  // TODO kirk 2009-12-30 -- This also needs better indexing on the function repository when
  // we get past the current time.
  protected Pair<NewFunctionDefinition, ValueSpecification> resolveFunction(ComputationTarget target, ValueRequirement requirement) {
    for(FunctionDefinition function : getFunctionRepository().getAllFunctions()) {
      if(function instanceof NewFunctionDefinition) {
        NewFunctionDefinition newFunction = (NewFunctionDefinition) function;
        if(!newFunction.canApplyTo(target)) {
          continue;
        }
        Set<ValueSpecification> resultSpecs = newFunction.getResults(target, Collections.singleton(requirement));
        for(ValueSpecification resultSpec : resultSpecs) {
          if(ObjectUtils.equals(resultSpec, requirement)) {
            return new Pair<NewFunctionDefinition, ValueSpecification>(newFunction, resultSpec);
          }
        }
      }
      
    }
    return null;
  }
  
  protected Pair<NewDependencyNode, ValueSpecification> resolveRequirement(ComputationTarget target, ValueRequirement requirement) {
    for(NewDependencyGraph depGraph : _graphsByTarget.values()) {
      Pair<NewDependencyNode, ValueSpecification> satisfiedRequirement = depGraph.getNodeProducing(requirement);
      if(satisfiedRequirement != null) {
        return satisfiedRequirement;
      }
    }
    return null;
  }
  
  public Collection<NewDependencyGraph> getAllDependencyGraphs() {
    return new ArrayList<NewDependencyGraph>(_graphsByTarget.values());
  }

}
