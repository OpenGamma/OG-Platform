/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
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
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.depgraph.ResolveTask.State;
import com.opengamma.engine.depgraph.ResolvedValueCallback.ResolvedValueCallbackChain;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/* package */class FunctionApplicationStep extends NextFunctionStep {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionApplicationStep.class);

  private final Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> _resolved;
  private final ValueSpecification _resolvedOutput;

  public FunctionApplicationStep(final ResolveTask task, final Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> functions,
      final Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> resolved, final ValueSpecification resolvedOutput) {
    super(task, functions);
    _resolved = resolved;
    _resolvedOutput = resolvedOutput;
  }

  protected Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> getResolved() {
    return _resolved;
  }

  protected ParameterizedFunction getFunction() {
    return getResolved().getFirst();
  }

  protected ValueSpecification getOriginalOutput() {
    return getResolved().getSecond();
  }

  protected Collection<ValueSpecification> getOriginalOutputs() {
    return getResolved().getThird();
  }

  protected ValueSpecification getResolvedOutput() {
    return _resolvedOutput;
  }

  private static class DelegateState extends NextFunctionStep implements ResolvedValueCallback {

    private ResolutionPump _pump;

    public DelegateState(final ResolveTask task, final Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> functions) {
      super(task, functions);
    }

    @Override
    public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
      s_logger.debug("Failed {} at {}", value, this);
      storeFailure(failure);
      synchronized (this) {
        _pump = null;
      }
      context.run(getTask());
    }

    @Override
    public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
      s_logger.debug("Resolved {} to {}", valueRequirement, value);
      if (pump != null) {
        synchronized (this) {
          _pump = pump;
        }
        if (!pushResult(context, value, false)) {
          synchronized (this) {
            assert _pump == pump;
            _pump = null;
          }
          context.pump(pump);
        }
      } else {
        if (!pushResult(context, value, true)) {
          context.failed(this, valueRequirement, null);
        }
      }
    }

    @Override
    protected void pump(final GraphBuildingContext context) {
      final ResolutionPump pump;
      synchronized (this) {
        pump = _pump;
        _pump = null;
      }
      if (pump == null) {
        // Either pump called twice for a resolve, called before the first resolve, or after failed
        throw new IllegalStateException();
      } else {
        s_logger.debug("Pumping underlying delegate");
        context.pump(pump);
      }
    }

    @Override
    protected void onDiscard(final GraphBuildingContext context) {
      final ResolutionPump pump;
      synchronized (this) {
        pump = _pump;
        _pump = null;
      }
      if (pump != null) {
        context.close(pump);
      }
    }

    @Override
    protected synchronized boolean isActive() {
      // Only active if pump has been called
      return _pump == null;
    }

    @Override
    public String toString() {
      return "Delegate" + getObjectId();
    }

  }

  protected static final class PumpingState extends NextFunctionStep {

    private final ParameterizedFunction _function;
    private final ValueSpecification _valueSpecification;
    private final Collection<ValueSpecification> _outputs;
    private final FunctionApplicationWorker _worker;

    private PumpingState(final ResolveTask task, final Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> functions,
        final ValueSpecification valueSpecification, final Collection<ValueSpecification> outputs, final ParameterizedFunction function, final FunctionApplicationWorker worker) {
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

    private Collection<ValueSpecification> getOutputs() {
      return _outputs;
    }

    private ParameterizedFunction getFunction() {
      return _function;
    }

    private FunctionApplicationWorker getWorker() {
      return _worker;
    }

    protected ResolutionFailure functionApplication(final GraphBuildingContext context) {
      return context.functionApplication(getValueRequirement(), getFunction(), getValueSpecification());
    }

    public boolean canHandleMissingInputs() {
      return getFunction().getFunction().canHandleMissingRequirements();
    }

    public boolean inputsAvailable(final GraphBuildingContext context, final Map<ValueSpecification, ValueRequirement> inputs, final boolean lastWorkerResult) {
      s_logger.info("Function inputs available {} for {}", inputs, getValueSpecification());
      // Late resolution of the output based on the actual inputs used (skip if everything was strict)
      ValueSpecification resolvedOutput = getValueSpecification();
      Set<ValueSpecification> newOutputValues = null;
      try {
        newOutputValues = getFunction().getFunction().getResults(context.getCompilationContext(), getComputationTarget(context), inputs);
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getResults", t);
        context.exception(t);
      }
      if (newOutputValues == null) {
        s_logger.info("Function {} returned NULL for getResults on {}", getFunction(), inputs);
        getWorker().storeFailure(functionApplication(context).requirements(inputs).getResultsFailed());
        return false;
      }
      if ((getOutputs().size() == newOutputValues.size()) && newOutputValues.containsAll(getOutputs())) {
        // Fetch any additional input requirements now needed as a result of input and output resolution
        return getAdditionalRequirementsAndPushResults(context, null, inputs, resolvedOutput, new HashSet<ValueSpecification>(getOutputs()), lastWorkerResult);
      }
      // Resolve output value is now different (probably more precise), so adjust ResolvedValueProducer
      final Set<ValueSpecification> resolvedOutputValues = Sets.newHashSetWithExpectedSize(newOutputValues.size());
      resolvedOutput = null;
      for (ValueSpecification outputValue : newOutputValues) {
        if ((resolvedOutput == null) && getValueRequirement().isSatisfiedBy(outputValue)) {
          resolvedOutput = outputValue.compose(getValueRequirement());
          if (resolvedOutput != outputValue) {
            resolvedOutput = MemoryUtils.instance(resolvedOutput);
          }
          s_logger.debug("Raw output {} resolves to {}", outputValue, resolvedOutput);
          resolvedOutputValues.add(resolvedOutput);
        } else {
          resolvedOutputValues.add(MemoryUtils.instance(outputValue));
        }
      }
      if (resolvedOutput == null) {
        s_logger.info("Provisional specification {} no longer in output after late resolution of {}", getValueSpecification(), getValueRequirement());
        getWorker().storeFailure(functionApplication(context).requirements(inputs).lateResolutionFailure());
        return false;
      }
      if (resolvedOutput.equals(getValueSpecification())) {
        // The resolved output has not changed
        s_logger.debug("Resolve output correct");
        return getAdditionalRequirementsAndPushResults(context, null, inputs, resolvedOutput, resolvedOutputValues, lastWorkerResult);
      }
      // Has the resolved output now reduced this to something already produced elsewhere
      final Pair<ResolveTask[], ResolvedValueProducer[]> reducing = context.getTasksProducing(resolvedOutput);
      if (reducing == null) {
        final ResolvedValue reduced = context.getProduction(resolvedOutput);
        if (reduced == null) {
          s_logger.debug("Resolved output not produced elsewhere");
          return produceSubstitute(context, inputs, resolvedOutput, resolvedOutputValues);
        }
        if (!pushResult(context, reduced, false)) {
          return produceSubstitute(context, inputs, resolvedOutput, resolvedOutputValues);
        } else {
          return true;
        }
      }
      // TODO: only use an aggregate object if there is more than one producer; otherwise use the producer directly
      final AggregateResolvedValueProducer aggregate = new AggregateResolvedValueProducer(getValueRequirement());
      final ResolveTask[] reducingTasks = reducing.getFirst();
      final ResolvedValueProducer[] reducingProducers = reducing.getSecond();
      for (int i = 0; i < reducingTasks.length; i++) {
        if (!getTask().hasParent(reducingTasks[i])) {
          // Task that's not our parent may produce the value for us
          aggregate.addProducer(context, reducingProducers[i]);
        }
        // Only the producers are ref-counted
        reducingProducers[i].release(context);
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
        boolean result = getAdditionalRequirementsAndPushResults(context, newWorker, inputs, resolvedOutput, resolvedOutputs, false);
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
        if (pump != null) {
          synchronized (this) {
            _pump = pump;
          }
          getWorker().pushResult(context, resolvedValue, false);
          if (!pushResult(context, resolvedValue, false)) {
            synchronized (this) {
              assert _pump == pump;
              _pump = null;
            }
            context.pump(pump);
          }
        } else {
          getWorker().pushResult(context, resolvedValue, true);
          if (!pushResult(context, resolvedValue, true)) {
            context.failed(this, valueRequirement, null);
          }
        }
      }

      @Override
      public final void pump(final GraphBuildingContext context) {
        final ResolutionPump pump;
        synchronized (this) {
          pump = _pump;
          _pump = null;
        }
        if (pump != null) {
          s_logger.debug("Pumping underlying delegate");
          context.pump(pump);
        }
      }

      @Override
      protected final synchronized boolean isActive() {
        // Only active if a resolve has been received, and there is no failure (i.e. it is waiting on the task to pump it)
        return _pump == null;
      }

      @Override
      protected final void onDiscard(final GraphBuildingContext context) {
        final ResolutionPump pump;
        synchronized (this) {
          pump = _pump;
          _pump = null;
        }
        if (pump != null) {
          context.close(pump);
        }
        getWorker().abort(context);
      }

      @Override
      public String toString() {
        return "ResolutionSubstituteDelegate" + getObjectId() + "[" + getFunction() + ", " + getValueSpecification() + "]";
      }

    }

    @Override
    protected void onDiscard(final GraphBuildingContext context) {
      getWorker().abort(context);
    }

    private boolean getAdditionalRequirementsAndPushResults(final GraphBuildingContext context, final FunctionApplicationWorker substituteWorker,
        final Map<ValueSpecification, ValueRequirement> inputs, final ValueSpecification resolvedOutput, final Set<ValueSpecification> resolvedOutputs, final boolean lastWorkerResult) {
      Set<ValueRequirement> additionalRequirements = null;
      try {
        additionalRequirements = getFunction().getFunction().getAdditionalRequirements(context.getCompilationContext(), getComputationTarget(context), inputs.keySet(), resolvedOutputs);
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getAdditionalRequirements", t);
        context.exception(t);
      }
      if (additionalRequirements == null) {
        s_logger.info("Function {} returned NULL for getAdditionalRequirements on {}", getFunction(), inputs);
        final ResolutionFailure failure = functionApplication(context).requirements(inputs).getAdditionalRequirementsFailed();
        if (substituteWorker != null) {
          substituteWorker.storeFailure(failure);
          substituteWorker.finished(context);
          context.discardTaskProducing(resolvedOutput, getTask());
        }
        getWorker().storeFailure(failure);
        return false;
      }
      if (additionalRequirements.isEmpty()) {
        return pushResult(context, substituteWorker, inputs, resolvedOutput, resolvedOutputs, lastWorkerResult);
      }
      s_logger.debug("Resolving additional requirements for {} on {}", getFunction(), inputs);
      final AtomicInteger lock = new AtomicInteger(1);
      final ResolvedValueCallback callback = new ResolvedValueCallbackChain() {

        private final AtomicBoolean _pumped = new AtomicBoolean(false);

        @Override
        public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
          s_logger.info("Couldn't resolve additional requirement {} for {}", value, getFunction());
          final ResolutionFailure additionalRequirement = functionApplication(context).requirements(inputs).additionalRequirement(value, failure);
          getWorker().storeFailure(additionalRequirement);
          if (substituteWorker != null) {
            substituteWorker.storeFailure(additionalRequirement);
            substituteWorker.finished(context);
            context.discardTaskProducing(resolvedOutput, getTask());
          }
          if (!_pumped.getAndSet(true)) {
            pump(context);
          }
        }

        @Override
        public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
          s_logger.debug("Resolved additional requirement {} to {}", valueRequirement, resolvedValue);
          inputs.put(resolvedValue.getValueSpecification(), valueRequirement);
          if (pump != null) {
            context.close(pump);
          }
          if (lock.decrementAndGet() == 0) {
            s_logger.debug("Additional requirements complete");
            if (!pushResult(context, substituteWorker, inputs, resolvedOutput, resolvedOutputs, lastWorkerResult)) {
              pump(context);
            }
          }
        }

        @Override
        public int cancelLoopMembers(final GraphBuildingContext context, final Set<Object> visited) {
          int result = getWorker().cancelLoopMembers(context, visited);
          if (substituteWorker != null) {
            result += substituteWorker.cancelLoopMembers(context, visited);
          }
          return result;
        }

        @Override
        public String toString() {
          return "AdditionalRequirements" + getObjectId() + "[" + getFunction() + ", " + inputs + "]";
        }

      };
      final Set<FunctionExclusionGroup> functionExclusion = getFunctionExclusion(context, getFunction().getFunction());
      for (ValueRequirement inputRequirement : additionalRequirements) {
        final ResolvedValueProducer inputProducer = context.resolveRequirement(inputRequirement, getTask(), functionExclusion);
        lock.incrementAndGet();
        inputProducer.addCallback(context, callback);
        inputProducer.release(context);
      }
      if (lock.decrementAndGet() == 0) {
        s_logger.debug("Additional requirements complete");
        return pushResult(context, substituteWorker, inputs, resolvedOutput, resolvedOutputs, lastWorkerResult);
      } else {
        return true;
      }
    }

    private boolean pushResult(final GraphBuildingContext context, final FunctionApplicationWorker substituteWorker, final Map<ValueSpecification, ValueRequirement> inputs,
        final ValueSpecification resolvedOutput, final Set<ValueSpecification> resolvedOutputs, final boolean lastWorkerResult) {
      if (context.getCompilationContext().getGraphBuildingBlacklist().isBlacklisted(getFunction(), resolvedOutput.getTargetSpecification(), inputs.keySet(), resolvedOutputs)) {
        s_logger.info("Result {} for {} suppressed by blacklist", resolvedOutput, getValueRequirement());
        final ResolutionFailure failure = functionApplication(context).requirements(inputs).suppressed();
        if (substituteWorker != null) {
          substituteWorker.storeFailure(failure);
          substituteWorker.finished(context);
          context.discardTaskProducing(resolvedOutput, getTask());
        }
        getWorker().storeFailure(failure);
        return false;
      }
      final ResolvedValue result = createResult(resolvedOutput, getFunction(), inputs.keySet(), resolvedOutputs);
      s_logger.info("Result {} for {}", result, getValueRequirement());
      context.declareProduction(result);
      if (substituteWorker != null) {
        if (!substituteWorker.pushResult(context, result, true)) {
          throw new IllegalStateException(result + " rejected by substitute worker");
        }
        context.discardTaskProducing(resolvedOutput, getTask());
      } else {
        getWorker().pushResult(context, result, lastWorkerResult);
      }
      return pushResult(context, result, false);
    }

    public void finished(final GraphBuildingContext context) {
      if (s_logger.isInfoEnabled()) {
        if (getWorker().getResults().length == 0) {
          s_logger.info("Application of {} to produce {} failed; rescheduling for next resolution", getFunction(), getValueSpecification());
        } else {
          s_logger.info("Application of {} to produce {} complete; rescheduling for next resolution", getFunction(), getValueSpecification());
        }
      }
      context.discardTaskProducing(getValueSpecification(), getTask());
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
  protected boolean run(final GraphBuildingContext context) {
    final FunctionApplicationWorker worker = new FunctionApplicationWorker(getValueRequirement());
    final ResolvedValueProducer producer = context.declareTaskProducing(getResolvedOutput(), getTask(), worker);
    if (producer == worker) {
      producer.release(context);
      // Populate the worker and position this task in the chain for pumping alternative resolutions to dependents
      s_logger.debug("Registered worker {} for {} production", worker, getResolvedOutput());
      final CompiledFunctionDefinition functionDefinition = getFunction().getFunction();
      Set<ValueRequirement> inputRequirements = null;
      try {
        inputRequirements = functionDefinition.getRequirements(context.getCompilationContext(), getComputationTarget(context), getValueRequirement());
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getRequirements", t);
        context.exception(t);
      }
      if (inputRequirements == null) {
        s_logger.info("Function {} returned NULL for getRequirements on {}", functionDefinition, getValueRequirement());
        final ResolutionFailure failure = context.functionApplication(getValueRequirement(), getFunction(), getResolvedOutput()).getRequirementsFailed();
        context.discardTaskProducing(getResolvedOutput(), getTask());
        worker.storeFailure(failure);
        worker.finished(context);
        storeFailure(failure);
        setRunnableTaskState(new NextFunctionStep(getTask(), getFunctions()), context);
        worker.release(context);
        return true;
      }
      final Collection<ValueSpecification> resolvedOutputValues;
      if (getOriginalOutput().equals(getResolvedOutput())) {
        resolvedOutputValues = getOriginalOutputs();
      } else {
        final Collection<ValueSpecification> originalOutputValues = getOriginalOutputs();
        resolvedOutputValues = new ArrayList<ValueSpecification>(originalOutputValues.size());
        for (ValueSpecification outputValue : originalOutputValues) {
          if (getOriginalOutput().equals(outputValue)) {
            s_logger.debug("Substituting {} with {}", outputValue, getResolvedOutput());
            resolvedOutputValues.add(getResolvedOutput());
          } else {
            resolvedOutputValues.add(outputValue);
          }
        }
      }
      final PumpingState state = new PumpingState(getTask(), getFunctions(), getResolvedOutput(), resolvedOutputValues, getFunction(), worker);
      setTaskState(state);
      if (inputRequirements.isEmpty()) {
        s_logger.debug("Function {} requires no inputs", functionDefinition);
        worker.setPumpingState(state, 0);
        if (!state.inputsAvailable(context, Collections.<ValueSpecification, ValueRequirement>emptyMap(), true)) {
          context.discardTaskProducing(getResolvedOutput(), getTask());
          setRunnableTaskState(new NextFunctionStep(getTask(), getFunctions()), context);
          worker.finished(context);
        }
      } else {
        s_logger.debug("Function {} requires {}", functionDefinition, inputRequirements);
        worker.setPumpingState(state, inputRequirements.size());
        final Set<FunctionExclusionGroup> functionExclusion = getFunctionExclusion(context, functionDefinition);
        for (ValueRequirement inputRequirement : inputRequirements) {
          final ResolvedValueProducer inputProducer = context.resolveRequirement(inputRequirement, getTask(), functionExclusion);
          worker.addInput(context, inputProducer);
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
    return true;
  }

  @Override
  public String toString() {
    return "ApplyFunction" + getObjectId() + "[" + getFunction() + ", " + getResolvedOutput() + "]";
  }

}
