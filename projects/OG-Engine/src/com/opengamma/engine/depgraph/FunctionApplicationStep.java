/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
      _builder.addToRunQueue(getTask());
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
        s_logger.debug("Pumping {} from {}", _pump, this);
        _pump.pump();
        _pump = null;
      }
    }

    @Override
    public String toString() {
      return "DELEGATE";
    }

  }

  protected static final class PumpingState extends NextFunctionStep {

    private final ValueSpecification _valueSpecification;
    private final Set<ValueSpecification> _outputs;
    private final ParameterizedFunction _function;
    private final DependencyGraphBuilder _builder;
    private final FunctionApplicationWorker _worker;

    private PumpingState(final ResolveTask task, final Iterator<Pair<ParameterizedFunction, ValueSpecification>> functions, final ValueSpecification valueSpecification,
        final Set<ValueSpecification> outputs, final ParameterizedFunction function, final DependencyGraphBuilder builder, final FunctionApplicationWorker worker) {
      super(task, functions);
      _valueSpecification = valueSpecification;
      _outputs = outputs;
      _function = function;
      _builder = builder;
      _worker = worker;
    }

    private ValueSpecification getValueSpecification() {
      return _valueSpecification;
    }

    private Set<ValueSpecification> getOutputs() {
      return _outputs;
    }

    private ParameterizedFunction getFunction() {
      return _function;
    }

    private DependencyGraphBuilder getBuilder() {
      return _builder;
    }

    private FunctionApplicationWorker getWorker() {
      return _worker;
    }

    public void inputsAvailable(final Map<ValueSpecification, ValueRequirement> inputs) {
      s_logger.info("Function inputs available {} for {}", inputs, getValueSpecification());
      // Late resolution of the output based on the actual inputs used (skip if everything was strict)
      ValueSpecification resolvedOutput = getValueSpecification();
      boolean strictConstraints = resolvedOutput.getProperties().isStrict();
      if (strictConstraints) {
        for (ValueRequirement input : inputs.values()) {
          if (!input.getConstraints().isStrict()) {
            strictConstraints = false;
            break;
          }
        }
      }
      if (!strictConstraints) {
        Set<ValueSpecification> newOutputValues = null;
        try {
          newOutputValues = getFunction().getFunction().getResults(getBuilder().getCompilationContext(), getComputationTarget(), inputs);
        } catch (Throwable t) {
          s_logger.warn("Exception thrown by getResults", t);
          getBuilder().postException(t);
        }
        if (newOutputValues == null) {
          s_logger.info("Function {} returned NULL for getResults on {}", getFunction(), inputs);
          pump();
          return;
        }
        if (!getOutputs().equals(newOutputValues)) {
          getOutputs().clear();
          resolvedOutput = null;
          for (ValueSpecification outputValue : newOutputValues) {
            if ((resolvedOutput == null) && getValueRequirement().isSatisfiedBy(outputValue)) {
              resolvedOutput = outputValue.compose(getValueRequirement());
              s_logger.debug("Raw output {} resolves to {}", outputValue, resolvedOutput);
              getOutputs().add(resolvedOutput);
            } else {
              getOutputs().add(outputValue);
            }
          }
          if (resolvedOutput != null) {
            // TODO: has the resolved output now reduced this to something already produced in the graph?
            s_logger.error("Don't know how to check whether a node reduction has taken place");
          } else {
            s_logger.info("Provisional specification {} no longer in output after late resolution of {}", getValueSpecification(), getValueRequirement());
            pump();
            return;
          }
        }
        // Fetch any additional input requirements now needed as a result of input and output resolution
        Set<ValueRequirement> additionalRequirements = null;
        try {
          additionalRequirements = getFunction().getFunction().getAdditionalRequirements(getBuilder().getCompilationContext(), getComputationTarget(), inputs.keySet(), getOutputs());
        } catch (Throwable t) {
          s_logger.warn("Exception thrown by getAdditionalRequirements", t);
          getBuilder().postException(t);
        }
        if (additionalRequirements == null) {
          s_logger.info("Function {} returned NULL for getAdditionalRequirements on {}", getFunction(), inputs);
          pump();
          return;
        }
        if (!additionalRequirements.isEmpty()) {
          s_logger.debug("Resolving additional requirements for {} on {}", getFunction(), inputs);
          final AtomicInteger lock = new AtomicInteger(1);
          final ResolvedValueCallback callback = new ResolvedValueCallback() {

            @Override
            public void failed(final ValueRequirement value) {
              s_logger.info("Couldn't resolve additional requirement {} for {}", value, getFunction());
              pump();
            }

            @Override
            public void resolved(final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
              inputs.put(resolvedValue.getValueSpecification(), valueRequirement);
              if (lock.decrementAndGet() == 0) {
                s_logger.debug("Additional requirements complete");
                pushResult(inputs);
              }
            }

            @Override
            public String toString() {
              return "AdditionalRequirements[" + getFunction() + ", " + inputs + "]";
            }

          };
          for (ValueRequirement inputRequirement : additionalRequirements) {
            final ResolvedValueProducer inputProducer = getBuilder().resolveRequirement(inputRequirement, getTask());
            lock.incrementAndGet();
            inputProducer.addCallback(callback);
          }
          if (lock.decrementAndGet() == 0) {
            s_logger.debug("Additional requirements complete");
            pushResult(inputs);
          }
          return;
        }
      }
      pushResult(inputs);
    }

    private void pushResult(final Map<ValueSpecification, ValueRequirement> inputs) {
      // TODO: we could be pushing a DependencyNode object to the builder that has all the second stage results values
      final ResolvedValue result = createResult(getValueSpecification(), getFunction(), inputs.keySet());
      s_logger.info("Result {} for {}", result, getValueRequirement());
      getWorker().pushResult(result);
      pushResult(result);
    }

    public void finished() {
      s_logger.info("Application of {} to produce {} complete; rescheduling for next resolution", getFunction(), getValueSpecification());
      // Become runnable again; the next function will then be considered
      getBuilder().addToRunQueue(getTask());
    }

    @Override
    protected void pump() {
      s_logger.debug("Pumping worker {} from {}", getWorker(), this);
      getWorker().pumpImpl();
    }

    @Override
    public String toString() {
      return "WaitForInputs[" + getFunction() + ", " + getValueSpecification() + "]";
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
        s_logger.info("Function {} returned NULL for getResults on {}", functionDefinition, getComputationTarget());
        setRunnableTaskState(new NextFunctionStep(getTask(), getFunctions()), builder);
        return;
      }
      final Set<ValueSpecification> resolvedOutputValues;
      if (getOriginalOutput().equals(getResolvedOutput())) {
        resolvedOutputValues = new HashSet<ValueSpecification>(originalOutputValues);
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
        s_logger.info("Function {} returned NULL for getResults on {}", functionDefinition, getValueRequirement());
        setRunnableTaskState(new NextFunctionStep(getTask(), getFunctions()), builder);
        return;
      }
      final PumpingState state = new PumpingState(getTask(), getFunctions(), getResolvedOutput(), resolvedOutputValues, getFunction(), builder, worker);
      setTaskState(state);
      if (inputRequirements.isEmpty()) {
        s_logger.debug("Function {} required no inputs", functionDefinition);
        worker.setPumpingState(state, 0);
        state.inputsAvailable(Collections.<ValueSpecification, ValueRequirement>emptyMap());
      } else {
        s_logger.debug("Function {} requires {}", functionDefinition, inputRequirements);
        worker.setPumpingState(state, inputRequirements.size());
        for (ValueRequirement inputRequirement : inputRequirements) {
          final ResolvedValueProducer inputProducer = builder.resolveRequirement(inputRequirement, getTask());
          worker.addInput(inputRequirement, inputProducer);
        }
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
  protected void pump() {
    s_logger.debug("Ignoring pump - applying {} to produce {}", getFunction(), getResolvedOutput());
  }

  @Override
  public String toString() {
    return "ApplyFunction[" + getFunction() + ", " + getResolvedOutput() + "]";
  }

}
