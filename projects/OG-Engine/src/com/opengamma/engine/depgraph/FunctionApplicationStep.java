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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
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

    private ResolutionPump _pump;

    public DelegateState(final ResolveTask task, final Iterator<Pair<ParameterizedFunction, ValueSpecification>> functions) {
      super(task, functions);
    }

    @Override
    public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
      s_logger.debug("Failed {} at {}", value, this);
      storeFailure(failure);
      _pump = null;
      context.run(getTask());
    }

    @Override
    public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
      s_logger.debug("Resolved {} to {}", valueRequirement, value);
      _pump = pump;
      if (!pushResult(context, value)) {
        assert _pump == pump;
        _pump = null;
        context.pump(pump);
      }
    }

    @Override
    protected void pump(final GraphBuildingContext context) {
      if (_pump == null) {
        // Either pump called twice for a resolve, called before the first resolve, or after failed
        throw new IllegalStateException();
      } else {
        s_logger.debug("Pumping underlying delegate");
        ResolutionPump pump = _pump;
        _pump = null;
        context.pump(pump);
      }
    }

    @Override
    protected boolean isActive() {
      // Only active if pump has been called
      return _pump == null;
    }

    @Override
    public String toString() {
      return "Delegate" + getObjectId();
    }

  }

  protected static final class PumpingState extends NextFunctionStep {

    private final ValueSpecification _valueSpecification;
    private final Set<ValueSpecification> _outputs;
    private final ParameterizedFunction _function;
    private final FunctionApplicationWorker _worker;

    private PumpingState(final ResolveTask task, final Iterator<Pair<ParameterizedFunction, ValueSpecification>> functions, final ValueSpecification valueSpecification,
        final Set<ValueSpecification> outputs, final ParameterizedFunction function, final FunctionApplicationWorker worker) {
      super(task, functions);
      assert outputs.contains(valueSpecification);
      _valueSpecification = valueSpecification;
      _outputs = outputs;
      _function = function;
      worker.addRef();
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

    private FunctionApplicationWorker getWorker() {
      return _worker;
    }

    protected ResolutionFailure functionApplication() {
      return ResolutionFailure.functionApplication(getValueRequirement(), getFunction(), getValueSpecification());
    }

    public boolean canHandleMissingInputs() {
      return getFunction().getFunction().canHandleMissingRequirements();
    }

    public boolean inputsAvailable(final GraphBuildingContext context, final Map<ValueSpecification, ValueRequirement> inputs) {
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
        return pushResult(context, getWorker(), null, inputs, resolvedOutput, getOutputs());
      }
      Set<ValueSpecification> newOutputValues = null;
      try {
        newOutputValues = getFunction().getFunction().getResults(context.getCompilationContext(), getComputationTarget(), inputs);
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getResults", t);
        context.exception(t);
      }
      if (newOutputValues == null) {
        s_logger.info("Function {} returned NULL for getResults on {}", getFunction(), inputs);
        getWorker().storeFailure(functionApplication().requirements(inputs).getResultsFailed());
        return false;
      }
      if (getOutputs().equals(newOutputValues)) {
        // Fetch any additional input requirements now needed as a result of input and output resolution
        return getAdditionalRequirementsAndPushResults(context, null, inputs, resolvedOutput, getOutputs());
      }
      // Resolve output value is now different (probably more precise), so adjust ResolvedValueProducer
      final Set<ValueSpecification> resolvedOutputValues = Sets.newHashSetWithExpectedSize(newOutputValues.size());
      resolvedOutput = null;
      for (ValueSpecification outputValue : newOutputValues) {
        if ((resolvedOutput == null) && getValueRequirement().isSatisfiedBy(outputValue)) {
          resolvedOutput = outputValue.compose(getValueRequirement());
          s_logger.debug("Raw output {} resolves to {}", outputValue, resolvedOutput);
          resolvedOutputValues.add(resolvedOutput);
        } else {
          resolvedOutputValues.add(outputValue);
        }
      }
      if (resolvedOutput == null) {
        s_logger.info("Provisional specification {} no longer in output after late resolution of {}", getValueSpecification(), getValueRequirement());
        getWorker().storeFailure(functionApplication().requirements(inputs).lateResolutionFailure());
        return false;
      }
      if (resolvedOutput.equals(getValueSpecification())) {
        // The resolved output has not changed
        s_logger.debug("Resolve output correct");
        return getAdditionalRequirementsAndPushResults(context, null, inputs, resolvedOutput, resolvedOutputValues);
      }
      // Has the resolved output now reduced this to something already produced elsewhere
      final Map<ResolveTask, ResolvedValueProducer> reducingTasks = context.getTasksProducing(resolvedOutput);
      if (reducingTasks.isEmpty()) {
        s_logger.debug("Resolved output not produced elsewhere");
        return produceSubstitute(context, inputs, resolvedOutput, resolvedOutputValues);
      }
      // TODO: only use an aggregate object if there is more than one producer; otherwise use the producer directly
      final AggregateResolvedValueProducer aggregate = new AggregateResolvedValueProducer(getValueRequirement());
      for (Map.Entry<ResolveTask, ResolvedValueProducer> reducingTask : reducingTasks.entrySet()) {
        if (!getTask().hasParent(reducingTask.getKey())) {
          // Task that's not our parent may produce the value for us
          aggregate.addProducer(context, reducingTask.getValue());
        }
        // Only the values are ref-counted
        reducingTask.getValue().release(context);
      }
      final ValueSpecification resolvedOutputCopy = resolvedOutput;
      final ResolutionSubstituteDelegate delegate = new ResolutionSubstituteDelegate(getTask()) {
        @Override
        public void failedImpl(final GraphBuildingContext context) {
          if (!produceSubstitute(context, inputs, resolvedOutputCopy, resolvedOutputValues)) {
            getWorker().pumpImpl(context);
          }
        }
      };
      setTaskState(delegate);
      aggregate.addCallback(context, delegate);
      aggregate.start(context);
      aggregate.release(context);
      return true;
    }

    private boolean produceSubstitute(final GraphBuildingContext context, final Map<ValueSpecification, ValueRequirement> inputs, final ValueSpecification resolvedOutput,
        final Set<ValueSpecification> resolvedOutputs) {
      if (inputs.containsKey(resolvedOutput)) {
        s_logger.debug("Backtracking on identity reduction");
        return false;
      }
      final FunctionApplicationWorker newWorker = new FunctionApplicationWorker(getValueRequirement());
      final ResolvedValueProducer producer = context.declareTaskProducing(resolvedOutput, getTask(), newWorker);
      if (producer == newWorker) {
        producer.release(context);
        // The new worker is going to produce the value specification
        boolean result = getAdditionalRequirementsAndPushResults(context, newWorker, inputs, resolvedOutput, resolvedOutputs);
        newWorker.release(context);
        return result;
      } else {
        newWorker.release(context);
        // An equivalent task is producing the revised value specification
        final ResolutionSubstituteDelegate delegate = new ResolutionSubstituteDelegate(getTask()) {
          @Override
          protected void failedImpl(final GraphBuildingContext context) {
            getWorker().pumpImpl(context);
          }
        };
        setTaskState(delegate);
        producer.addCallback(context, delegate);
        producer.release(context);
        return true;
      }
    }

    private abstract class ResolutionSubstituteDelegate extends State implements ResolvedValueCallback {

      private ResolutionPump _pump;

      protected ResolutionSubstituteDelegate(final ResolveTask task) {
        super(task);
      }

      @Override
      public final void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
        // Record details of the failure
        getWorker().storeFailure(failure);
        // Go back to the original state
        setTaskState(PumpingState.this);
        // Do the required action
        failedImpl(context);
      }

      protected abstract void failedImpl(final GraphBuildingContext context);

      @Override
      public final void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
        _pump = pump;
        getWorker().pushResult(context, resolvedValue);
        if (!pushResult(context, resolvedValue)) {
          assert _pump == pump;
          _pump = null;
          context.pump(pump);
        }
      }

      @Override
      public final void pump(final GraphBuildingContext context) {
        if (_pump != null) {
          s_logger.debug("Pumping underlying delegate");
          ResolutionPump pump = _pump;
          _pump = null;
          context.pump(pump);
        }
      }

      @Override
      protected final boolean isActive() {
        // Only active if a resolve has been received, and there is no failure (i.e. it is waiting on the task to pump it)
        return _pump == null;
      }

      @Override
      protected final void onDiscard(final GraphBuildingContext context) {
        getWorker().abort(context);
      }

      @Override
      public String toString() {
        return "ResolutionSubstituteDelegate" + getObjectId() + "[" + getFunction() + ", " + getValueSpecification() + "]";
      }

    }

    private boolean getAdditionalRequirementsAndPushResults(final GraphBuildingContext context, final FunctionApplicationWorker substituteWorker,
        final Map<ValueSpecification, ValueRequirement> inputs,
        final ValueSpecification resolvedOutput, final Set<ValueSpecification> resolvedOutputs) {
      Set<ValueRequirement> additionalRequirements = null;
      try {
        additionalRequirements = getFunction().getFunction().getAdditionalRequirements(context.getCompilationContext(), getComputationTarget(), inputs.keySet(), resolvedOutputs);
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getAdditionalRequirements", t);
        context.exception(t);
      }
      if (additionalRequirements == null) {
        s_logger.info("Function {} returned NULL for getAdditionalRequirements on {}", getFunction(), inputs);
        final ResolutionFailure failure = functionApplication().requirements(inputs).getAdditionalRequirementsFailed();
        if (substituteWorker != null) {
          substituteWorker.storeFailure(failure);
          substituteWorker.finished(context);
        }
        getWorker().storeFailure(failure);
        return false;
      }
      if (additionalRequirements.isEmpty()) {
        return pushResult(context, getWorker(), substituteWorker, inputs, resolvedOutput, resolvedOutputs);
      }
      s_logger.debug("Resolving additional requirements for {} on {}", getFunction(), inputs);
      final AtomicInteger lock = new AtomicInteger(1);
      final ResolvedValueCallback callback = new ResolvedValueCallback() {

        private final AtomicBoolean _pumped = new AtomicBoolean(false);

        @Override
        public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
          s_logger.info("Couldn't resolve additional requirement {} for {}", value, getFunction());
          final ResolutionFailure additionalRequirement = functionApplication().requirements(inputs).additionalRequirement(value, failure);
          getWorker().storeFailure(additionalRequirement);
          if (substituteWorker != null) {
            substituteWorker.storeFailure(additionalRequirement);
            substituteWorker.finished(context);
          }
          if (!_pumped.getAndSet(true)) {
            pump(context);
          }
        }

        @Override
        public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
          s_logger.debug("Resolved additional requirement {} to {}", valueRequirement, resolvedValue);
          inputs.put(resolvedValue.getValueSpecification(), valueRequirement);
          if (lock.decrementAndGet() == 0) {
            s_logger.debug("Additional requirements complete");
            if (!pushResult(context, getWorker(), substituteWorker, inputs, resolvedOutput, resolvedOutputs)) {
              pump(context);
            }
          }
        }

        @Override
        public String toString() {
          return "AdditionalRequirements" + getObjectId() + "[" + getFunction() + ", " + inputs + "]";
        }

      };
      for (ValueRequirement inputRequirement : additionalRequirements) {
        final ResolvedValueProducer inputProducer = context.resolveRequirement(inputRequirement, getTask());
        lock.incrementAndGet();
        inputProducer.addCallback(context, callback);
        inputProducer.release(context);
      }
      if (lock.decrementAndGet() == 0) {
        s_logger.debug("Additional requirements complete");
        return pushResult(context, getWorker(), substituteWorker, inputs, resolvedOutput, resolvedOutputs);
      } else {
        return true;
      }
    }

    private boolean pushResult(final GraphBuildingContext context, final FunctionApplicationWorker mainWorker, final FunctionApplicationWorker substituteWorker,
        final Map<ValueSpecification, ValueRequirement> inputs, final ValueSpecification resolvedOutput, final Set<ValueSpecification> resolvedOutputs) {
      final ResolvedValue result = createResult(resolvedOutput, getFunction(), inputs.keySet(), resolvedOutputs);
      s_logger.info("Result {} for {}", result, getValueRequirement());
      mainWorker.pushResult(context, result);
      if (substituteWorker != null) {
        if (!substituteWorker.pushResult(context, result)) {
          throw new IllegalStateException(result + " rejected by substitute worker");
        }
        substituteWorker.finished(context);
      }
      return pushResult(context, result);
    }

    public void finished(final GraphBuildingContext context) {
      if (getWorker().getResults().length == 0) {
        s_logger.info("Application of {} to produce {} failed; rescheduling for next resolution", getFunction(), getValueSpecification());
        context.discardTaskProducing(getValueSpecification(), getTask());
      } else {
        s_logger.info("Application of {} to produce {} complete; rescheduling for next resolution", getFunction(), getValueSpecification());
      }
      // Become runnable again; the next function will then be considered
      context.run(getTask());
    }

    @Override
    protected void pump(final GraphBuildingContext context) {
      s_logger.debug("Pumping worker {} from {}", getWorker(), this);
      getWorker().pumpImpl(context);
    }

    @Override
    protected boolean isActive() {
      return getWorker().isActive();
    }

    @Override
    public String toString() {
      return "WaitForInputs" + getObjectId() + "[" + getFunction() + ", " + getValueSpecification() + "]";
    }

  }

  @Override
  protected void run(final GraphBuildingContext context) {
    final FunctionApplicationWorker worker = new FunctionApplicationWorker(getValueRequirement());
    final ResolvedValueProducer producer = context.declareTaskProducing(getResolvedOutput(), getTask(), worker);
    if (producer == worker) {
      producer.release(context);
      // Populate the worker and position this task in the chain for pumping alternative resolutions to dependents
      s_logger.debug("Registered worker {} for {} production", worker, getResolvedOutput());
      final CompiledFunctionDefinition functionDefinition = getFunction().getFunction();
      Set<ValueSpecification> originalOutputValues = null;
      try {
        originalOutputValues = functionDefinition.getResults(context.getCompilationContext(), getComputationTarget());
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getResults", t);
        context.exception(t);
      }
      if (originalOutputValues == null) {
        s_logger.info("Function {} returned NULL for getResults on {}", functionDefinition, getComputationTarget());
        final ResolutionFailure failure = ResolutionFailure.functionApplication(getValueRequirement(), getFunction(), getResolvedOutput()).getResultsFailed();
        context.discardTaskProducing(getResolvedOutput(), getTask());
        worker.storeFailure(failure);
        worker.finished(context);
        storeFailure(failure);
        setRunnableTaskState(new NextFunctionStep(getTask(), getFunctions()), context);
        worker.release(context);
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
        inputRequirements = functionDefinition.getRequirements(context.getCompilationContext(), getComputationTarget(), getValueRequirement());
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getRequirements", t);
        context.exception(t);
      }
      if (inputRequirements == null) {
        s_logger.info("Function {} returned NULL for getRequirements on {}", functionDefinition, getValueRequirement());
        final ResolutionFailure failure = ResolutionFailure.functionApplication(getValueRequirement(), getFunction(), getResolvedOutput()).getRequirementsFailed();
        context.discardTaskProducing(getResolvedOutput(), getTask());
        worker.storeFailure(failure);
        worker.finished(context);
        storeFailure(failure);
        setRunnableTaskState(new NextFunctionStep(getTask(), getFunctions()), context);
        worker.release(context);
        return;
      }
      final PumpingState state = new PumpingState(getTask(), getFunctions(), getResolvedOutput(), resolvedOutputValues, getFunction(), worker);
      setTaskState(state);
      if (inputRequirements.isEmpty()) {
        s_logger.debug("Function {} requires no inputs", functionDefinition);
        worker.setPumpingState(state, 0);
        if (!state.inputsAvailable(context, Collections.<ValueSpecification, ValueRequirement>emptyMap())) {
          context.discardTaskProducing(getResolvedOutput(), getTask());
          setRunnableTaskState(new NextFunctionStep(getTask(), getFunctions()), context);
          worker.finished(context);
        }
      } else {
        s_logger.debug("Function {} requires {}", functionDefinition, inputRequirements);
        worker.setPumpingState(state, inputRequirements.size());
        for (ValueRequirement inputRequirement : inputRequirements) {
          final ResolvedValueProducer inputProducer = context.resolveRequirement(inputRequirement, getTask());
          worker.addInput(context, inputRequirement, inputProducer);
          inputProducer.release(context);
        }
        worker.start(context);
      }
      worker.release(context);
    } else {
      worker.release(context);
      // Another task is working on this, so delegate to it
      s_logger.debug("Delegating production of {} to worker {}", getResolvedOutput(), producer);
      final DelegateState state = new DelegateState(getTask(), getFunctions());
      setTaskState(state);
      producer.addCallback(context, state);
      producer.release(context);
    }
  }

  @Override
  public String toString() {
    return "ApplyFunction" + getObjectId() + "[" + getFunction() + ", " + getResolvedOutput() + "]";
  }

}
