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
import com.opengamma.engine.depgraph.ResolveTask.State;
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
      s_logger.debug("Failed {} at {}", value, this);
      _pump = null;
      _builder.addToRunQueue(getTask());
    }

    @Override
    public void resolved(final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
      s_logger.debug("Resolved {} to {}", valueRequirement, value);
      pushResult(value);
      _pump = pump;
    }

    @Override
    protected void pump() {
      s_logger.debug("Pumping {} from {}", _pump, this);
      _pump.pump();
      _pump = null;
    }

    @Override
    public String toString() {
      return "Delegate";
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
      if (strictConstraints) {
        pushResult(getWorker(), inputs, resolvedOutput);
        return;
      }
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
      if (getOutputs().equals(newOutputValues)) {
        // Fetch any additional input requirements now needed as a result of input and output resolution
        getAdditionalRequirementsAndPushResults(getWorker(), inputs, resolvedOutput, false);
        return;
      }
      // Resolve output value is now different (probably more precise), so adjust ResolvedValueProducer
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
      if (resolvedOutput == null) {
        s_logger.info("Provisional specification {} no longer in output after late resolution of {}", getValueSpecification(), getValueRequirement());
        pump();
        return;
      }
      if (resolvedOutput.equals(getValueSpecification())) {
        // The resolved output has not changed
        getAdditionalRequirementsAndPushResults(getWorker(), inputs, resolvedOutput, false);
        return;
      }
      // Has the resolved output now reduced this to something already produced elsewhere
      final Map<ResolveTask, ResolvedValueProducer> reducingTasks = getBuilder().getTasksProducing(resolvedOutput);
      if (reducingTasks.isEmpty()) {
        produceSubstitute(inputs, resolvedOutput);
        return;
      }
      final AggregateResolvedValueProducer aggregate = new AggregateResolvedValueProducer(getValueRequirement());
      for (Map.Entry<ResolveTask, ResolvedValueProducer> reducingTask : reducingTasks.entrySet()) {
        if (!getTask().hasParent(reducingTask.getKey())) {
          // Task that's not our parent may produce the value for us
          aggregate.addProducer(reducingTask.getValue());
        }
      }
      final ValueSpecification resolvedOutputCopy = resolvedOutput;
      final ResolutionSubstituteDelegate delegate = new ResolutionSubstituteDelegate(getTask()) {
        @Override
        public void failedImpl() {
          produceSubstitute(inputs, resolvedOutputCopy);
        }
      };
      setTaskState(delegate);
      aggregate.addCallback(delegate);
      aggregate.start();
    }

    private void produceSubstitute(final Map<ValueSpecification, ValueRequirement> inputs, final ValueSpecification resolvedOutput) {
      final FunctionApplicationWorker newWorker = new FunctionApplicationWorker(getValueRequirement());
      final ResolvedValueProducer producer = getBuilder().declareTaskProducing(resolvedOutput, getTask(), newWorker);
      if (producer == newWorker) {
        getAdditionalRequirementsAndPushResults(newWorker, inputs, resolvedOutput, true);
      } else {
        // An equivalent task is producing the revised value specification
        final ResolutionSubstituteDelegate delegate = new ResolutionSubstituteDelegate(getTask()) {
          @Override
          protected void failedImpl() {
            getWorker().pumpImpl();
          }
        };
        setTaskState(delegate);
        producer.addCallback(delegate);
      }
    }

    private abstract class ResolutionSubstituteDelegate extends State implements ResolvedValueCallback {

      private ResolutionPump _pump;

      protected ResolutionSubstituteDelegate(final ResolveTask task) {
        super(task);
      }

      @Override
      public void failed(final ValueRequirement value) {
        // Go back to the original state
        setTaskState(PumpingState.this);
        // Do the required action
        failedImpl();
      }

      protected abstract void failedImpl();

      @Override
      public void resolved(final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
        _pump = pump;
        pushResult(resolvedValue);
      }

      @Override
      public void pump() {
        _pump.pump();
        _pump = null;
      }

      @Override
      public String toString() {
        return "ResolutionSubstituteDelegate[" + getFunction() + ", " + getValueSpecification() + "]";
      }

    }

    private void getAdditionalRequirementsAndPushResults(final FunctionApplicationWorker worker, final Map<ValueSpecification, ValueRequirement> inputs, final ValueSpecification resolvedOutput,
        final boolean killWorkerAfterwards) {
      Set<ValueRequirement> additionalRequirements = null;
      try {
        additionalRequirements = getFunction().getFunction().getAdditionalRequirements(getBuilder().getCompilationContext(), getComputationTarget(), inputs.keySet(), getOutputs());
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getAdditionalRequirements", t);
        getBuilder().postException(t);
      }
      if (additionalRequirements == null) {
        s_logger.info("Function {} returned NULL for getAdditionalRequirements on {}", getFunction(), inputs);
        if (killWorkerAfterwards) {
          worker.finished();
        }
        pump();
        return;
      }
      if (additionalRequirements.isEmpty()) {
        pushResult(worker, inputs, resolvedOutput);
        if (killWorkerAfterwards) {
          worker.finished();
        }
        return;
      }
      s_logger.debug("Resolving additional requirements for {} on {}", getFunction(), inputs);
      final AtomicInteger lock = new AtomicInteger(1);
      final FunctionApplicationWorker workerCopy = worker;
      final ResolvedValueCallback callback = new ResolvedValueCallback() {

        @Override
        public void failed(final ValueRequirement value) {
          s_logger.info("Couldn't resolve additional requirement {} for {}", value, getFunction());
          if (killWorkerAfterwards) {
            worker.finished();
          }
          pump();
        }

        @Override
        public void resolved(final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
          inputs.put(resolvedValue.getValueSpecification(), valueRequirement);
          if (lock.decrementAndGet() == 0) {
            s_logger.debug("Additional requirements complete");
            pushResult(workerCopy, inputs, resolvedOutput);
            if (killWorkerAfterwards) {
              worker.finished();
            }
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
        pushResult(worker, inputs, resolvedOutput);
        if (killWorkerAfterwards) {
          worker.finished();
        }
      }
    }

    private void pushResult(final FunctionApplicationWorker worker, final Map<ValueSpecification, ValueRequirement> inputs, final ValueSpecification resolvedOutput) {
      final ResolvedValue result = createResult(resolvedOutput, getFunction(), inputs.keySet(), getOutputs());
      s_logger.info("Result {} for {}", result, getValueRequirement());
      worker.pushResult(result);
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
        worker.finished();
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
        worker.finished();
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
        worker.start();
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
    return "ApplyFunction[" + getFunction() + ", " + getResolvedOutput() + "]";
  }

}
