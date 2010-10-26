/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
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
      final ResolutionState resolutionState = resolveValueRequirement(requirement, null);
      Pair<DependencyNode, ValueSpecification> terminalNode = addTargetRequirement(resolutionState);
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

  // Note the order requirements are considered can affect function choices and resultant graph construction (see [ENG-259]).
  private ResolutionState resolveValueRequirement(final ValueRequirement requirement, final DependencyNode dependent) {
    final ComputationTarget target = getTargetResolver().resolve(requirement.getTargetSpecification());
    if (target == null) {
      throw new UnsatisfiableDependencyGraphException("Unable to resolve target for " + requirement);
    }
    s_logger.info("Resolving target requirement for {} on {}", requirement, target);
    // Find existing nodes in the graph
    final Collection<Pair<DependencyNode, ValueSpecification>> existingNodes = _graph.getNodesSatisfying(requirement);
    final ResolutionState resolutionState;
    if (existingNodes != null) {
      s_logger.debug("{} existing nodes found", existingNodes.size());
      resolutionState = new ResolutionState(requirement);
      resolutionState.addExistingNodes(existingNodes);
      // It it's live data, stop now. Otherwise fall through to the functions as they may be more generic than the
      // composed specifications attached to the nodes found
      if (getLiveDataAvailabilityProvider().isAvailable(requirement)) {
        return resolutionState;
      }
    } else {
      // Find live data
      if (getLiveDataAvailabilityProvider().isAvailable(requirement)) {
        s_logger.debug("Live Data : {} on {}", requirement, target);
        LiveDataSourcingFunction function = new LiveDataSourcingFunction(requirement);
        return new ResolutionState(requirement, function.getResult(), new ParameterizedFunction(function, function.getDefaultParameters()), createDependencyNode(target, dependent));
      }
      resolutionState = new ResolutionState(requirement);
    }
    // Find functions that can do this
    DependencyNode node = createDependencyNode(target, dependent);
    for (Pair<ParameterizedFunction, ValueSpecification> resolvedFunction : getFunctionResolver().resolveFunction(requirement, node)) {
      final CompiledFunctionDefinition functionDefinition = resolvedFunction.getFirst().getFunction();
      final Set<ValueSpecification> outputValues = functionDefinition.getResults(getCompilationContext(), target);
      final ValueSpecification originalOutput = resolvedFunction.getSecond();
      final ValueSpecification resolvedOutput = originalOutput.compose(requirement);
      if (_graph.getNodeProducing(resolvedOutput) != null) {
        // Resolved function output already in the graph, so would have been added to the resolution state when we checked existing nodes
        s_logger.debug("Skipping {} - already in graph", resolvedFunction.getSecond());
        continue;
      }
      if (node == null) {
        node = createDependencyNode(target, dependent);
      }
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
      resolutionState.addFunction(resolvedOutput, resolvedFunction.getFirst(), node);
      node = null;
    }
    return resolutionState;
  }

  private Pair<DependencyNode, ValueSpecification> addTargetRequirement(final ResolutionState resolved) {
    do {
      if (resolved.isEmpty()) {
        return resolved.getLastValid();
      }
      final ResolutionState.Node node = resolved.getFirst();
      boolean strictConstraints = true;
      if (node.getInputStates() == null) {
        if (node.getParameterizedFunction() != null) {
          // TODO factor the guarded code into a method and only set up the try/catch overhead if we're not a single resolve
          try {
            final CompiledFunctionDefinition functionDefinition = node.getParameterizedFunction().getFunction();
            // TODO: pass the composed requirement in to allow downstream dynamic composition
            Set<ValueRequirement> inputRequirements = functionDefinition.getRequirements(getCompilationContext(), node.getDependencyNode().getComputationTarget());
            node.dimInputState(inputRequirements.size());
            boolean pendingInputStates = false;
            for (ValueRequirement inputRequirement : inputRequirements) {
              final ResolutionState inputState = resolveValueRequirement(inputRequirement, node.getDependencyNode());
              node.addInputState(inputState);
              final Pair<DependencyNode, ValueSpecification> inputValue = addTargetRequirement(inputState);
              node.getDependencyNode().addInputNode(inputValue.getFirst());
              node.getDependencyNode().addInputValue(inputValue.getSecond());
              if (!inputState.isEmpty()) {
                pendingInputStates = true;
              }
              if (!inputState.getValueRequirement().getConstraints().isStrict()) {
                strictConstraints = false;
              }
            }
            if (!pendingInputStates && resolved.isSingle()) {
              resolved.removeFirst();
            }
          } catch (UnsatisfiableDependencyGraphException e) {
            s_logger.debug("Backtracking on dependency graph error", e);
            resolved.removeFirst();
            if (resolved.isEmpty()) {
              throw new UnsatisfiableDependencyGraphException("Unable to satisfy requirement " + resolved.getValueRequirement(), e);
            }
            continue;
          }
        } else {
          s_logger.debug("Existing Node : {} on {}", resolved.getValueRequirement(), node.getDependencyNode().getComputationTarget());
          final ValueSpecification originalOutput = node.getValueSpecification();
          final ValueSpecification resolvedOutput = originalOutput.compose(resolved.getValueRequirement());
          if (originalOutput.equals(resolvedOutput)) {
            final Pair<DependencyNode, ValueSpecification> result = Pair.of(node.getDependencyNode(), resolvedOutput);
            if (resolved.isSingle()) {
              resolved.removeFirst();
              resolved.setLastValid(result);
            }
            return result;
          }
          // TODO: update the node to reduce the specification & then return the node with the resolvedOutput
          throw new UnsatisfiableDependencyGraphException("In-situ specification reduction not implemented");
        }
      } else {
        // TODO factor the guarded code into a method and only set up the try/catch overhead if we're not a single resolve
        try {
          boolean pendingInputStates = false;
          for (ResolutionState inputState : node.getInputStates()) {
            final Pair<DependencyNode, ValueSpecification> inputValue = addTargetRequirement(inputState);
            node.getDependencyNode().addInputNode(inputValue.getFirst());
            node.getDependencyNode().addInputValue(inputValue.getSecond());
            if (!inputState.isEmpty()) {
              pendingInputStates = true;
            }
            if (!inputState.getValueRequirement().getConstraints().isStrict()) {
              strictConstraints = false;
            }
          }
          if (!pendingInputStates && resolved.isSingle()) {
            resolved.removeFirst();
          }
        } catch (UnsatisfiableDependencyGraphException e) {
          s_logger.debug("Backtracking on dependency graph error", e);
          resolved.removeFirst();
          if (resolved.isEmpty()) {
            throw new UnsatisfiableDependencyGraphException("Unable to satisfy requirement " + resolved.getValueRequirement(), e);
          }
          continue;
        }
      }
      ValueSpecification resolvedOutput = node.getValueSpecification();
      if (!strictConstraints) {
        final CompiledFunctionDefinition functionDefinition = node.getParameterizedFunction().getFunction();
        final Set<ValueSpecification> newOutputValues = functionDefinition.getResults(getCompilationContext(), node.getDependencyNode().getComputationTarget(), node.getDependencyNode()
            .getInputValues());
        if (!node.getDependencyNode().getOutputValues().equals(newOutputValues)) {
          node.getDependencyNode().clearOutputValues();
          resolvedOutput = null;
          for (ValueSpecification outputValue : newOutputValues) {
            if ((resolvedOutput == null) && resolved.getValueRequirement().isSatisfiedBy(outputValue)) {
              resolvedOutput = outputValue.compose(resolved.getValueRequirement());
              s_logger.debug("Substituting {} with {}", outputValue, resolvedOutput);
              node.getDependencyNode().addOutputValue(resolvedOutput);
            } else {
              node.getDependencyNode().addOutputValue(outputValue);
            }
          }
          if (resolvedOutput == null) {
            s_logger.debug("Provisional specification {} no longer in output after late resolution of {}", node.getValueSpecification(), resolved.getValueRequirement());
            if (resolved.isEmpty() || !resolved.removeDeepest()) {
              throw new UnsatisfiableDependencyGraphException("Provisional specification " + node.getValueSpecification() + " no longer in output after late resolution of "
                  + resolved.getValueRequirement());
            }
            node.getDependencyNode().clearInputs();
            continue;
          }
        }
      }
      _graph.addDependencyNode(node.getDependencyNode());
      final Pair<DependencyNode, ValueSpecification> result = Pair.of(node.getDependencyNode(), resolvedOutput);
      if (resolved.isEmpty()) {
        resolved.setLastValid(result);
      }
      return result;
    } while (true);
  }

  public DependencyGraph getDependencyGraph() {
    return _graph;
  }

}
