/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/* package */class FunctionApplicationStep extends NextFunctionStep {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionApplicationStep.class);

  private final ParameterizedFunction _function;
  private final ValueSpecification _originalOutput;
  private final ValueSpecification _resolvedOutput;

  public FunctionApplicationStep(final ResolveTask task, final Iterator<Pair<ParameterizedFunction, ValueSpecification>> functions, final ParameterizedFunction function,
      final ValueSpecification originalOutput, final ValueSpecification resolvedOutput) {
    super(task, functions);
    assert function != null;
    assert originalOutput != null;
    assert resolvedOutput != null;
    _function = function;
    _originalOutput = originalOutput;
    _resolvedOutput = resolvedOutput;
  }

  protected ParameterizedFunction getFunction() {
    return _function;
  }

  protected ValueSpecification getOriginalOutput() {
    return _originalOutput;
  }

  protected ValueSpecification getResolvedOutput() {
    return _resolvedOutput;
  }

  private static class DelegateState extends NextFunctionStep implements ResolvedValueCallback {

    private final DependencyGraphBuilder _builder;
    private ResolutionPump _pump;

    public DelegateState(final ResolveTask task, final Iterator<Pair<ParameterizedFunction, ValueSpecification>> functions, final DependencyGraphBuilder builder) {
      super(task, functions);
      _builder = builder;
    }

    @Override
    public void failed(final ValueRequirement value) {
      _pump = null;
      setRunnableTaskState(this, _builder);
    }

    @Override
    public void resolved(final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
      pushResult(value);
      _pump = pump;
    }

    @Override
    protected void pump() {
      if (_pump == null) {
        // Either pump called twice for a resolve, called before the first resolve, or after failed
        throw new IllegalStateException();
      } else {
        s_logger.debug("Pumping underlying delegate");
        _pump.pump();
        _pump = null;
      }
    }

    @Override
    public String toString() {
      return "APPLY_FUNCTION_DELEGATE";
    }

  }

  protected static final class PumpingState extends NextFunctionStep {

    private final ValueSpecification _valueSpecification;
    private final ParameterizedFunction _function;
    private final DependencyGraphBuilder _builder;
    private final FunctionApplicationWorker _worker;

    private PumpingState(final ResolveTask task, final Iterator<Pair<ParameterizedFunction, ValueSpecification>> functions, final ValueSpecification valueSpecification,
        final ParameterizedFunction function, final DependencyGraphBuilder builder, final FunctionApplicationWorker worker) {
      super(task, functions);
      _valueSpecification = valueSpecification;
      _function = function;
      _builder = builder;
      _worker = worker;
    }

    public void inputsAvailable(final Set<ValueSpecification> inputs) {
      s_logger.debug("Function inputs available {} for {}", inputs, _valueSpecification);
      // TODO: getResults () and getAdditionalRequirements () before we can push the values
      // TODO: we could be pushing a DependencyNode object to the builder that has all the second stage results values
      ResolvedValue result = createResult(_valueSpecification, _function, inputs);
      _worker.pushResult(result);
      pushResult(result);
    }
    
    public void finished() {
      setRunnableTaskState(this, _builder);
    }

    @Override
    protected void pump() {
      _worker.pumpImpl();
    }

    @Override
    public String toString() {
      return "APPLY_FUNCTION_ENUMERATE";
    }

  }

  @Override
  protected void run(final DependencyGraphBuilder builder) {
    final FunctionApplicationWorker worker = new FunctionApplicationWorker(getValueRequirement());
    final ResolvedValueProducer producer = builder.declareTaskProducing(getResolvedOutput(), getTask(), worker);
    if (producer == worker) {
      // Populate the worker and position this task in the chain for pumping alternative resolutions to dependents
      s_logger.debug("Registered worker {} for {} production", worker, getResolvedOutput());
      final CompiledFunctionDefinition functionDefinition = getFunction().getFunction();
      Set<ValueSpecification> originalOutputValues = null;
      try {
        originalOutputValues = functionDefinition.getResults(builder.getCompilationContext(), getComputationTarget());
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getResults", t);
        builder.postException(t);
      }
      if (originalOutputValues == null) {
        s_logger.debug("Function {} returned NULL for getResults on {}", functionDefinition, getComputationTarget());
        setRunnableTaskState(new NextFunctionStep(getTask(), getFunctions()), builder);
        return;
      }
      final Set<ValueSpecification> resolvedOutputValues;
      if (getOriginalOutput().equals(getResolvedOutput())) {
        resolvedOutputValues = originalOutputValues;
      } else {
        resolvedOutputValues = Sets.newHashSetWithExpectedSize(originalOutputValues.size());
        for (ValueSpecification outputValue : originalOutputValues) {
          if (getOriginalOutput().equals(outputValue)) {
            s_logger.debug("Substituting {} with {}", outputValue, getResolvedOutput());
            resolvedOutputValues.add(getResolvedOutput());
          } else {
            resolvedOutputValues.add(outputValue);
          }
        }
      }
      Set<ValueRequirement> inputRequirements = null;
      try {
        inputRequirements = functionDefinition.getRequirements(builder.getCompilationContext(), getComputationTarget(), getValueRequirement());
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getRequirements", t);
        builder.postException(t);
      }
      if (inputRequirements == null) {
        s_logger.debug("Function {} returned NULL for getResults on {}", functionDefinition, getValueRequirement());
        setRunnableTaskState(new NextFunctionStep(getTask(), getFunctions()), builder);
        return;
      }
      final PumpingState state = new PumpingState(getTask(), getFunctions(), getResolvedOutput(), getFunction(), builder, worker);
      setTaskState(state);
      if (inputRequirements.isEmpty()) {
        state.inputsAvailable(Collections.<ValueSpecification>emptySet());
        state.finished();
      } else {
        for (ValueRequirement inputRequirement : inputRequirements) {
          final ResolvedValueProducer inputProducer = builder.resolveRequirement(inputRequirement, getTask());
          worker.addInput(inputRequirement, inputProducer);
        }
        worker.setPumpingState(state);
      }
    } else {
      // Another task is working on this, so delegate to it
      s_logger.debug("Delegating production of {} to worker {}", getResolvedOutput(), producer);
      final DelegateState state = new DelegateState(getTask(), getFunctions(), builder);
      setTaskState(state);
      producer.addCallback(state);
    }
  }

  @Override
  public String toString() {
    return "APPLY_FUNCTION";
  }

}
