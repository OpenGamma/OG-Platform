/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class DependencyGraphBuilder {
  private static final Logger s_logger = LoggerFactory.getLogger(DependencyGraphBuilder.class);
  // Injected Inputs:
  private String _calculationConfigurationName;
  private LiveDataAvailabilityProvider _liveDataAvailabilityProvider;
  private ComputationTargetResolver _targetResolver;
  private FunctionResolver _functionResolver;
  private FunctionCompilationContext _compilationContext;
  // State:
  private DependencyGraph _graph;
    
  /**
   * @return the calculationConfigurationName
   */
  public String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }
  /**
   * @param calculationConfigurationName the calculationConfigurationName to set
   */
  public void setCalculationConfigurationName(String calculationConfigurationName) {
    _calculationConfigurationName = calculationConfigurationName;
    _graph = new DependencyGraph(_calculationConfigurationName);
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
  public void setLiveDataAvailabilityProvider(
      LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
  }
  
  /**
   * @return the functionResolver
   */
  public FunctionResolver getFunctionResolver() {
    return _functionResolver;
  }
  
  /**
   * @param functionResolver the functionResolver to set
   */
  public void setFunctionResolver(FunctionResolver functionResolver) {
    _functionResolver = functionResolver;
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
  
  /**
   * @return the compilationContext
   */
  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
  }
  /**
   * @param compilationContext the compilationContext to set
   */
  public void setCompilationContext(FunctionCompilationContext compilationContext) {
    _compilationContext = compilationContext;
  }
  
  protected void checkInjectedInputs() {
    ArgumentChecker.notNullInjected(getLiveDataAvailabilityProvider(), "liveDataAvailabilityProvider");
    ArgumentChecker.notNullInjected(getFunctionResolver(), "functionResolver");
    ArgumentChecker.notNullInjected(getTargetResolver(), "targetResolver");
    ArgumentChecker.notNullInjected(getCalculationConfigurationName(), "calculationConfigurationName");
  }
  
  public void addTarget(ComputationTarget target, ValueRequirement requirement) {
    addTarget(target, Collections.singleton(requirement));
  }
  
  public void addTarget(ComputationTarget target, Set<ValueRequirement> requirements) {
    ArgumentChecker.notNull(target, "Computation Target");
    ArgumentChecker.notNull(requirements, "Value requirements");
    checkInjectedInputs();
    
    for (ValueRequirement requirement : requirements) {
      Pair<DependencyNode, ValueSpecification> terminalNode = addTargetRequirement(requirement);
      _graph.addTerminalOutputValue(terminalNode.getSecond());
    }
  }
  
  private Pair<DependencyNode, ValueSpecification> addTargetRequirement(ValueRequirement requirement) {
    ComputationTarget target = getTargetResolver().resolve(requirement.getTargetSpecification());
    if (target == null) {
      throw new UnsatisfiableDependencyGraphException("Unable to resolve target for " + requirement);
    }
    
    s_logger.info("Adding target requirement for {} on {}", requirement, target);

    Pair<DependencyNode, ValueSpecification> existingNode = _graph.getNodeProducing(requirement);
    if (existingNode != null) {
      s_logger.debug("Existing Node : {} on {}", requirement, target);
      return existingNode;
    }
    
    DependencyNode node = new DependencyNode(target);

    Pair<ParameterizedFunction, ValueSpecification> resolvedFunction;
    
    if (getLiveDataAvailabilityProvider().isAvailable(requirement)) {
      
      // this code to be moved to FunctionResolver?
      s_logger.debug("Live Data : {} on {}", requirement, target);
      LiveDataSourcingFunction function = new LiveDataSourcingFunction(requirement);
      resolvedFunction = Pair.of(new ParameterizedFunction(function, function.getDefaultParameters()), function.getResult());
    
    } else {
      
      resolvedFunction = getFunctionResolver().resolveFunction(requirement, node, getCompilationContext());
    
    }
    
    node.setFunction(resolvedFunction.getFirst());

    FunctionDefinition functionDefinition = resolvedFunction.getFirst().getFunction(); 
    Set<ValueSpecification> outputValues = functionDefinition.getResults(getCompilationContext(), target);
    node.addOutputValues(outputValues);
    
    Set<ValueRequirement> inputRequirements = functionDefinition.getRequirements(getCompilationContext(), target);
    for (ValueRequirement inputRequirement : inputRequirements) {
      Pair<DependencyNode, ValueSpecification> input = addTargetRequirement(inputRequirement);
      node.addInputNode(input.getFirst());
      node.addInputValue(input.getSecond());
    }
    
    _graph.addDependencyNode(node);
    return Pair.of(node, resolvedFunction.getSecond());
  }
  
  public DependencyGraph getDependencyGraph() {
    return _graph;
  }
  
}
