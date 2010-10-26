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
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.resolver.CompiledFunctionResolver;
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
  private CompiledFunctionResolver _functionResolver;
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
  public void setLiveDataAvailabilityProvider(LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    _liveDataAvailabilityProvider = liveDataAvailabilityProvider;
  }

  /**
   * @return the functionResolver
   */
  public CompiledFunctionResolver getFunctionResolver() {
    return _functionResolver;
  }

  /**
   * @param functionResolver the functionResolver to set
   */
  public void setFunctionResolver(CompiledFunctionResolver functionResolver) {
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
      Pair<DependencyNode, ValueSpecification> terminalNode = addTargetRequirement(requirement, null);
      _graph.addTerminalOutputValue(terminalNode.getSecond());
    }
  }

  private DependencyNode createDependencyNode(final ComputationTarget target, final DependencyNode dependent) {
    DependencyNode node = new DependencyNode(target);
    if (dependent != null) {
      node.addDependentNode(dependent);
    }
    return node;
  }

  private Pair<DependencyNode, ValueSpecification> addTargetRequirement(ValueRequirement requirement, DependencyNode dependent) {
    ComputationTarget target = getTargetResolver().resolve(requirement.getTargetSpecification());
    if (target == null) {
      throw new UnsatisfiableDependencyGraphException("Unable to resolve target for " + requirement);
    }

    s_logger.info("Adding target requirement for {} on {}", requirement, target);

    Pair<DependencyNode, ValueSpecification> existingNode = _graph.getNodeSatisfying(requirement);
    if (existingNode != null) {
      s_logger.debug("Existing Node : {} on {}", requirement, target);
      final ValueSpecification originalOutput = existingNode.getSecond();
      final ValueSpecification resolvedOutput = originalOutput.compose(requirement);
      if (originalOutput.equals(resolvedOutput)) {
        return existingNode;
      }
      // TODO: update the node to reduce the specification & then return the node with the resolvedOutput
      throw new UnsatisfiableDependencyGraphException("In-situ specification reduction not implemented");
    }

    if (getLiveDataAvailabilityProvider().isAvailable(requirement)) {
      // this code to be moved to FunctionResolver?
      s_logger.debug("Live Data : {} on {}", requirement, target);
      LiveDataSourcingFunction function = new LiveDataSourcingFunction(requirement);
      return addTargetRequirement(requirement, createDependencyNode(target, dependent), target, Pair.of(new ParameterizedFunction(function, function.getDefaultParameters()), function.getResult()));
    } else {
      UnsatisfiableDependencyGraphException lastException = null;
      DependencyNode node = createDependencyNode(target, dependent);
      for (Pair<ParameterizedFunction, ValueSpecification> resolvedFunction : getFunctionResolver().resolveFunction(requirement, node)) {
        try {
          if (node == null) {
            node = createDependencyNode(target, dependent);
          }
          return addTargetRequirement(requirement, node, target, resolvedFunction);
        } catch (UnsatisfiableDependencyGraphException e) {
          s_logger.debug("Failed to resolve graph dependencies", e);
          // Instead of housekeeping to remove nodes that may have gone into the graph, we'll leave them in
          // as the sub-graph may yet be used. The removeUnusedValues will trim out unused nodes as well as
          // the values.
          node = null;
          lastException = e;
        }
      }
      throw new UnsatisfiableDependencyGraphException("Backtracking", lastException);
    }

  }

  private Pair<DependencyNode, ValueSpecification> addTargetRequirement(final ValueRequirement requirement, final DependencyNode node, final ComputationTarget target,
      final Pair<ParameterizedFunction, ValueSpecification> resolvedFunction) {

    node.setFunction(resolvedFunction.getFirst());
    CompiledFunctionDefinition functionDefinition = resolvedFunction.getFirst().getFunction();

    // Get the broad output values that we matched the function against, composing against the requested requirement
    Set<ValueSpecification> outputValues = functionDefinition.getResults(getCompilationContext(), target);
    final ValueSpecification originalOutput = resolvedFunction.getSecond();
    ValueSpecification resolvedOutput = originalOutput.compose(requirement);
    if (originalOutput.equals(resolvedOutput)) {
      node.addOutputValues(outputValues);
    } else {
      for (ValueSpecification outputValue : outputValues) {
        if (originalOutput.equals(outputValue)) {
          s_logger.debug("Substituting {} with {}", outputValue, resolvedOutput);
          node.addOutputValue(resolvedOutput);
        } else {
          node.addOutputValue(outputValue);
        }
      }
    }

    // TODO: pass the composed requirement in to allow downstream dynamic composition
    Set<ValueRequirement> inputRequirements = functionDefinition.getRequirements(getCompilationContext(), target);
    for (ValueRequirement inputRequirement : inputRequirements) {
      Pair<DependencyNode, ValueSpecification> input = addTargetRequirement(inputRequirement, node);
      node.addInputNode(input.getFirst());
      node.addInputValue(input.getSecond());
    }

    // TODO: only do this if there were wild-card or choices in any of the input requirements that might now be resolved
    Set<ValueSpecification> newOutputValues = functionDefinition.getResults(getCompilationContext(), target, node.getInputValues());
    if (!outputValues.equals(newOutputValues)) {
      node.clearOutputValues();
      resolvedOutput = null;
      for (ValueSpecification outputValue : newOutputValues) {
        if ((resolvedOutput == null) && requirement.isSatisfiedBy(outputValue)) {
          resolvedOutput = outputValue.compose(requirement);
          node.addOutputValue(resolvedOutput);
        } else {
          node.addOutputValue(outputValue);
        }
      }
      if (resolvedOutput == null) {
        throw new UnsatisfiableDependencyGraphException("Provisional specification " + originalOutput + " no longer in output after late resolution");
      }
    }

    _graph.addDependencyNode(node);
    return Pair.of(node, resolvedOutput);
  }

  public DependencyGraph getDependencyGraph() {
    return _graph;
  }

}
