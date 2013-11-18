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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.ResolveTask.State;
import com.opengamma.engine.depgraph.ResolvedValueCallback.ResolvedValueCallbackChain;
import com.opengamma.engine.depgraph.ResolvedValueProducer.Chain;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/* package */class FunctionApplicationStep extends FunctionIterationStep {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionApplicationStep.class);

  private final Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> _resolved;
  private final ValueSpecification _resolvedOutput;

  public FunctionApplicationStep(final ResolveTask task, final FunctionIterationStep.IterationBaseStep base,
      final Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> resolved, final ValueSpecification resolvedOutput) {
    super(task, base);
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

  @Override
  protected ComputationTargetSpecification getTargetSpecification(final GraphBuildingContext context) {
    return getResolvedOutput().getTargetSpecification();
  }

  private static class DelegateState extends FunctionIterationStep implements ResolvedValueCallback {

    private ResolutionPump _pump;

    public DelegateState(final ResolveTask task, final FunctionIterationStep.IterationBaseStep base) {
      super(task, base);
    }

    @Override
    public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
      s_logger.debug("Failed {} at {}", value, this);
      storeFailure(failure);
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
            if (_pump != pump) {
              // A rogue pump occurred
              return;
            }
            _pump = null;
          }
          context.pump(pump);
        }
      } else {
        // The last result from the producer, but we can always reschedule ourselves
        synchronized (this) {
          _pump = ResolutionPump.Dummy.INSTANCE;
        }
        if (!pushResult(context, value, false)) {
          synchronized (this) {
            // Clear the pump so a rogue call can't also cause us to reschedule
            _pump = null;
          }
          context.failed(this, valueRequirement, null);
        }
      }
    }

    @Override
    public void recursionDetected() {
      s_logger.debug("Recursion detected in {}", this);
      getTask().setRecursionDetected();
    }

    @Override
    protected void pump(final GraphBuildingContext context) {
      final ResolutionPump pump;
      synchronized (this) {
        pump = _pump;
        _pump = null;
      }
      if (pump == null) {
        // Rogue pump -- see PumpingState.finished for an explanation
        return;
      }
      if (pump != ResolutionPump.Dummy.INSTANCE) {
        s_logger.debug("Pumping underlying delegate");
        context.pump(pump);
      } else {
        // Reschedule ourself
        context.run(getTask());
      }
    }

    @Override
    protected void discard(final GraphBuildingContext context) {
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
    public String toString() {
      return "Delegate" + getObjectId();
    }

  }

  protected static final class PumpingState extends FunctionIterationStep {

    private final ParameterizedFunction _function;
    private final ValueSpecification _valueSpecification;
    private final Collection<ValueSpecification> _outputs;
    // The worker reference is not ref-counted
    private final FunctionApplicationWorker _worker;

    private PumpingState(final ResolveTask task, final FunctionIterationStep.IterationBaseStep base, final ValueSpecification valueSpecification,
        final Collection<ValueSpecification> outputs, final ParameterizedFunction function, final FunctionApplicationWorker worker) {
      super(task, base);
      assert outputs.contains(valueSpecification);
      _valueSpecification = valueSpecification;
      _outputs = outputs;
      _function = function;
      // _worker is not ref-counted
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

    @Override
    protected ComputationTargetSpecification getTargetSpecification(final GraphBuildingContext context) {
      return getValueSpecification().getTargetSpecification();
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
        //DebugUtils.getResults2_enter();
        newOutputValues = getFunction().getFunction().getResults(context.getCompilationContext(), getComputationTarget(context), inputs);
        //DebugUtils.getResults2_leave();
      } catch (Throwable t) {
        //DebugUtils.getResults2_leave();
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
      resolvedOutput = getIterationBase().getResolvedOutputs(context, newOutputValues, resolvedOutputValues);
      if (resolvedOutput == null) {
        if (s_logger.isDebugEnabled() && getIterationBase() instanceof TargetDigestStep) {
          s_logger.debug("Digest {} failed for {}", getIterationBase().getDesiredValue(), getValueRequirement());
        }
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
      if (setTaskState(delegate)) {
        aggregate.addCallback(context, delegate);
        aggregate.start(context);
      }
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
        if (setTaskState(delegate)) {
          producer.addCallback(context, delegate);
        }
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
          // This is the last value from the producer, but we have a state to go to afterwards
          synchronized (this) {
            _pump = ResolutionPump.Dummy.INSTANCE;
          }
          getWorker().pushResult(context, resolvedValue, false);
          if (!pushResult(context, resolvedValue, false)) {
            context.failed(this, valueRequirement, null);
          }
        }
      }

      @Override
      public final void recursionDetected() {
        s_logger.debug("Recursion detected in {}", this);
        getTask().setRecursionDetected();
      }

      @Override
      public final void pump(final GraphBuildingContext context) {
        final ResolutionPump pump;
        synchronized (this) {
          pump = _pump;
          _pump = null;
        }
        if (pump == null) {
          // Rogue pump -- see PumpingState.finished for an explanation
          return;
        }
        if (pump != ResolutionPump.Dummy.INSTANCE) {
          s_logger.debug("Pumping underlying delegate");
          context.pump(pump);
        } else {
          // Go back to the original state
          if (setTaskState(PumpingState.this)) {
            // Do the required action
            failedImpl(context);
          }
        }
      }

      @Override
      public int cancelLoopMembers(final GraphBuildingContext context, final Map<Chain, Chain.LoopState> visited) {
        return PumpingState.this.cancelLoopMembers(context, visited);
      }

      @Override
      protected final void discard(final GraphBuildingContext context) {
        final ResolutionPump pump;
        synchronized (this) {
          pump = _pump;
          _pump = null;
        }
        if (pump != null) {
          context.close(pump);
        }
        getWorker().discard(context);
      }

      @Override
      public String toString() {
        return "ResolutionSubstituteDelegate" + getObjectId() + "[" + getFunction() + ", " + getValueSpecification() + "]";
      }

    }

    @Override
    protected void discard(final GraphBuildingContext context) {
      getWorker().discard(context);
    }

    private boolean getAdditionalRequirementsAndPushResults(final GraphBuildingContext context, final FunctionApplicationWorker substituteWorker,
        final Map<ValueSpecification, ValueRequirement> inputs, final ValueSpecification resolvedOutput, final Set<ValueSpecification> resolvedOutputs, final boolean lastWorkerResult) {
      // the substituteWorker is not ref-counted from here
      final ComputationTarget target = getComputationTarget(context);
      Set<ValueRequirement> additionalRequirements = null;
      try {
        //DebugUtils.getAdditionalRequirements_enter();
        additionalRequirements = getFunction().getFunction().getAdditionalRequirements(context.getCompilationContext(), target, inputs.keySet(), resolvedOutputs);
        //DebugUtils.getAdditionalRequirements_leave();
      } catch (Throwable t) {
        //DebugUtils.getAdditionalRequirements_leave();
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
        public void recursionDetected() {
          // No-op
        }

        @Override
        public int cancelLoopMembers(final GraphBuildingContext context, final Map<Chain, Chain.LoopState> visited) {
          int result = PumpingState.this.cancelLoopMembers(context, visited);
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
      String functionExclusionValueName = getValueRequirement().getValueName();
      Collection<FunctionExclusionGroup> functionExclusion = null;
      for (ValueRequirement inputRequirement : additionalRequirements) {
        final ResolvedValueProducer inputProducer;
        if ((inputRequirement.getValueName() == functionExclusionValueName) && inputRequirement.getTargetReference().equals(target.toSpecification())) {
          if (functionExclusion == null) {
            functionExclusion = getFunctionExclusion(context, getFunction().getFunction());
            if (functionExclusion == null) {
              functionExclusionValueName = null;
            }
          }
          inputProducer = context.resolveRequirement(inputRequirement, getTask(), functionExclusion);
        } else {
          inputProducer = context.resolveRequirement(inputRequirement, getTask(), null);
        }
        lock.incrementAndGet();
        if (inputProducer != null) {
          inputProducer.addCallback(context, callback);
          inputProducer.release(context);
        } else {
          getTask().setRecursionDetected();
          callback.failed(context, inputRequirement, context.recursiveRequirement(inputRequirement));
        }
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
      // the substituteWorker is not ref-counted from here
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
      // TODO: Control this with a flag? 
      getIterationBase().reportResult();
      context.declareProduction(result);
      if (substituteWorker != null) {
        if (!substituteWorker.pushResult(context, result, true)) {
          // The substitute was created specifically for this result; it won't have already had a value pushed and the value must satisfy the requirement
          throw new IllegalStateException();
        }
        context.discardTaskProducing(resolvedOutput, getTask());
      } else {
        getWorker().pushResult(context, result, lastWorkerResult);
      }
      return pushResult(context, result, false);
    }

    public void finished(final GraphBuildingContext context, final int refCounts) {
      context.discardTaskProducing(getValueSpecification(), getTask());
      // Release any ref-counts held by the worker on the task
      for (int i = 0; i < refCounts; i++) {
        getTask().release(context);
      }
      // Become runnable again; the next function will then be considered.
      context.run(getTask());
      // Note: We're effectively pumped ourselves, a consumer of the ResolveTask may also pump us and so we'll see a rogue call to
      // pump here, or to one of the next states that this task adopts. The call to pump from there may even already be scheduled.
    }

    @Override
    protected void pump(final GraphBuildingContext context) {
      s_logger.debug("Pumping worker {} from {}", getWorker(), this);
      getWorker().pumpImpl(context);
    }

    @Override
    public int cancelLoopMembers(final GraphBuildingContext context, final Map<Chain, Chain.LoopState> visited) {
      return getWorker().cancelLoopMembers(context, visited);
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
      final ComputationTarget target = getComputationTarget(context);
      Set<ValueRequirement> inputRequirements = null;
      try {
        //DebugUtils.getRequirements_enter();
        inputRequirements = functionDefinition.getRequirements(context.getCompilationContext(), target, getIterationBase().getDesiredValue());
        //DebugUtils.getRequirements_leave();
      } catch (Throwable t) {
        //DebugUtils.getRequirements_leave();
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
        worker.release(context);
        setRunnableTaskState(getIterationBase(), context);
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
      final PumpingState state = new PumpingState(getTask(), getIterationBase(), getResolvedOutput(), resolvedOutputValues, getFunction(), worker);
      if (setTaskState(state)) {
        if (inputRequirements.isEmpty()) {
          s_logger.debug("Function {} requires no inputs", functionDefinition);
          worker.setPumpingState(state, 0);
          if (!state.inputsAvailable(context, Collections.<ValueSpecification, ValueRequirement>emptyMap(), true)) {
            context.discardTaskProducing(getResolvedOutput(), getTask());
            state.setRunnableTaskState(getIterationBase(), context);
            worker.finished(context);
          }
        } else {
          s_logger.debug("Function {} requires {}", functionDefinition, inputRequirements);
          worker.setPumpingState(state, inputRequirements.size());

          String functionExclusionValueName = getValueRequirement().getValueName();
          Collection<FunctionExclusionGroup> functionExclusion = null;
          for (ValueRequirement inputRequirement : inputRequirements) {
            final ResolvedValueProducer inputProducer;
            if ((inputRequirement.getValueName() == functionExclusionValueName) && inputRequirement.getTargetReference().equals(target.toSpecification())) {
              if (functionExclusion == null) {
                functionExclusion = getFunctionExclusion(context, functionDefinition);
                if (functionExclusion == null) {
                  functionExclusionValueName = null;
                }
              }
              inputProducer = context.resolveRequirement(inputRequirement, getTask(), functionExclusion);
            } else {
              inputProducer = context.resolveRequirement(inputRequirement, getTask(), null);
            }
            if (inputProducer != null) {
              worker.addInput(context, inputProducer);
              inputProducer.release(context);
            } else {
              worker.setRecursionDetected();
              getTask().setRecursionDetected();
            }
          }
          worker.start(context);
        }
      }
      worker.release(context);
    } else {
      worker.release(context);
      // Another task is working on this, so delegate to it
      s_logger.debug("Delegating production of {} to worker {}", getResolvedOutput(), producer);
      final DelegateState state = new DelegateState(getTask(), getIterationBase());
      if (setTaskState(state)) {
        producer.addCallback(context, state);
      }
      producer.release(context);
    }
    return true;
  }

  @Override
  protected void pump(final GraphBuildingContext context) {
    // No-op; happens if a worker "finishes" a function application PumpingState and it progresses to the next natural
    // state in advance of the pump from the abstract value producer. See PumpingState.finished for an explanation
  }

  @Override
  public String toString() {
    return "ApplyFunction" + getObjectId() + "[" + getFunction() + ", " + getResolvedOutput() + "]";
  }

}
