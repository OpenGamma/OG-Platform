/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Unit of task resolution. A resolve task executes to convert a {@link ValueRequirement} into a dependency node.
 */
/* package */final class ResolveTask {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolveTask.class);

  public static enum State {
    /**
     * Task has just been created, is runnable and will resolve the computation target ready to enter the RESOLVED state.
     */
    PENDING,
    /**
     * Task has been resolved, is runnable and will either query the function resolver for functions to enter the FUNCTIONS
     * state (or FAILED), or be satisfied by the output of a node already in the graph and enter the COMPLETE state.
     */
    RESOLVED,
    /**
     * Task has a sequence of functions to attempt, is runnable and will enter the BLOCKED, FAILED or COMPLETE state depending
     * on whether there are functions left to try or completion has occurred.
     */
    FUNCTIONS,
    /**
     * Task is not runnable and waiting for completion or failure of another.
     */
    BLOCKED,
    /**
     * The value requirement cannot be satisfied. This task is complete and not runnable.
     */
    FAILED,
    /**
     * The value requirement can be satisfied and a {@link DependencyNode} is available. The task is complete and not runnable.
     */
    COMPLETE
  };

  /**
   * Value requirement to resolve.
   */
  private final ValueRequirement _valueRequirement;

  /**
   * Current state.
   */
  private volatile State _state;

  /**
   * Dependency node in COMPLETE, target in RESOLVED, FUNCTIONS, BLOCKED.
   */
  private volatile Object _targetOrDependencyNode;

  /**
   * Output value, only valid if state is COMPLETE.
   */
  private volatile ValueSpecification _valueSpecification;

  /**
   * Functions that can produce something satisfying the requirement.
   */
  private Iterator<Pair<ParameterizedFunction, ValueSpecification>> _functions;

  /**
   * Tasks that are blocked on this one.
   */
  private final Queue<TerminationCallback> _blocked = new ConcurrentLinkedQueue<TerminationCallback>();

  /**
   * Tasks that feed into this one; held so that the requirements used and their corresponding specifications will
   * be available instead of just the dependency nodes. Only set if state is COMPLETE
   */
  private Collection<ResolveTask> _inputTasks;

  public ResolveTask(final ValueRequirement valueRequirement) {
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    _valueRequirement = valueRequirement;
    setState(State.PENDING);
  }

  public State getState() {
    return _state;
  }

  private void setState(final State state) {
    _state = state;
  }

  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }

  private void setTarget(final ComputationTarget target) {
    _targetOrDependencyNode = target;
  }

  private ComputationTarget getTarget() {
    return (ComputationTarget) _targetOrDependencyNode;
  }

  public DependencyNode getDependencyNode() {
    return (DependencyNode) _targetOrDependencyNode;
  }

  private void setFunctions(final Iterator<Pair<ParameterizedFunction, ValueSpecification>> functions) {
    _functions = functions;
  }

  private Iterator<Pair<ParameterizedFunction, ValueSpecification>> getFunctions() {
    return _functions;
  }

  private void notifyComplete() {
    TerminationCallback callback = _blocked.poll();
    while (callback != null) {
      callback.complete(this);
      callback = _blocked.poll();
    }
  }

  public void setDependencyNode(final DependencyNode dependencyNode) {
    _targetOrDependencyNode = dependencyNode;
  }

  private void setStateComplete(final DependencyNode dependencyNode, final ValueSpecification valueSpecification, final Collection<ResolveTask> inputTasks) {
    setDependencyNode(dependencyNode);
    _valueSpecification = valueSpecification;
    _inputTasks = inputTasks;
    setState(State.COMPLETE);
    notifyComplete();
  }

  private void notifyFailed() {
    TerminationCallback callback = _blocked.poll();
    while (callback != null) {
      callback.failed(this);
      callback = _blocked.poll();
    }
  }

  private void setStateFailed() {
    setState(State.FAILED);
    notifyFailed();
  }

  public ValueSpecification getValueSpecification() {
    return _valueSpecification;
  }

  public Collection<ResolveTask> getInputTasks() {
    return _inputTasks;
  }

  protected void run(final DependencyGraphBuilder builder) {
    switch (getState()) {
      case PENDING: {
        final ComputationTargetResolver targetResolver = builder.getTargetResolver();
        final ComputationTarget target = targetResolver.resolve(getValueRequirement().getTargetSpecification());
        if (target == null) {
          s_logger.warn("Couldn't resolve target for {}", getValueRequirement());
          builder.postException(new UnsatisfiableDependencyGraphException(getValueRequirement(), "No ComputationTarget").addState("targetResolver ComputationTargetResolver", targetResolver));
          setStateFailed();
        } else {
          s_logger.debug("Resolved target {}", getValueRequirement().getTargetSpecification());
          setTarget(target);
          setState(State.RESOLVED);
          builder.addToRunQueue(this);
        }
        break;
      }
      case RESOLVED: {
        if (builder.getLiveDataAvailabilityProvider().isAvailable(getValueRequirement())) {
          s_logger.info("Found live data for {}", getValueRequirement());
          final DependencyNode node = new DependencyNode(getTarget());
          final LiveDataSourcingFunction function = new LiveDataSourcingFunction(getValueRequirement());
          node.setFunction(function);
          node.addOutputValue(function.getResult());
          setStateComplete(node, function.getResult(), null);
        } else {
          final Iterator<Pair<ParameterizedFunction, ValueSpecification>> itr = builder.getFunctionResolver().resolveFunction(getValueRequirement(), getTarget());
          if (itr.hasNext()) {
            s_logger.debug("Found functions for {}", getValueRequirement());
            setFunctions(itr);
            setState(State.FUNCTIONS);
            builder.addToRunQueue(this);
          } else {
            s_logger.info("No functions for {}", getValueRequirement());
            setStateFailed();
          }
        }
        break;
      }
      case FUNCTIONS: {
        final Pair<ParameterizedFunction, ValueSpecification> resolvedFunction = getFunctions().next();
        s_logger.debug("Considering {} for {}", resolvedFunction, getValueRequirement());
        final ValueSpecification originalOutput = resolvedFunction.getSecond();
        final ValueSpecification resolvedOutput = originalOutput.compose(getValueRequirement());
        final Set<ResolveTask> existingTasks = builder.getOtherTasksProducing(resolvedOutput, this);
        setState(State.BLOCKED);
        if (existingTasks != null) {
          s_logger.debug("Waiting for {} existing tasks to complete", existingTasks.size());
          final AtomicInteger blocked = new AtomicInteger(1);
          final TerminationCallback callback = new TerminationCallback() {

            @Override
            public void complete(final ResolveTask task) {
              if (getState() == State.BLOCKED) {
                // Cope with concurrent notifications
                synchronized (this) {
                  if (getState() == State.BLOCKED) {
                    // Another task has completed and got a dependency node, so use that
                    s_logger.info("Another task completed for {}", resolvedOutput);
                    setStateComplete(task.getDependencyNode(), task.getValueSpecification(), task.getInputTasks());
                  } else {
                    s_logger.debug("Duplicate task completion for {}", resolvedOutput);
                  }
                }
              } else {
                s_logger.debug("Duplicate task completion for {}", resolvedOutput);
              }
            }

            @Override
            public void failed(final ResolveTask task) {
              if (blocked.decrementAndGet() == 0) {
                if (getState() == State.BLOCKED) {
                  functionApply(builder, resolvedFunction.getFirst(), originalOutput, resolvedOutput);
                }
              }
            }

          };
          for (ResolveTask existingTask : existingTasks) {
            blocked.incrementAndGet();
            existingTask.notifyOnTermination(callback);
          }
          callback.failed(null);
        } else {
          functionApply(builder, resolvedFunction.getFirst(), originalOutput, resolvedOutput);
        }
        break;
      }
      default:
        throw new IllegalStateException("State=" + getState());
    }
  }

  private abstract class InputRequirementCallback implements TerminationCallback {

    private final AtomicInteger _inputs = new AtomicInteger(1);
    private final DependencyGraphBuilder _builder;
    private final ValueSpecification _outputValue;

    public InputRequirementCallback(final DependencyGraphBuilder builder, final ValueSpecification outputValue) {
      _builder = builder;
      _outputValue = outputValue;
    }

    protected DependencyGraphBuilder getBuilder() {
      return _builder;
    }

    protected ValueSpecification getOutputValue() {
      return _outputValue;
    }

    protected abstract void completeImpl(final ResolveTask task);

    protected abstract void completeImpl();

    @Override
    public void complete(final ResolveTask task) {
      completeImpl(task);
      decrementAndCheck();
    }

    private void decrementAndCheck() {
      if (_inputs.decrementAndGet() == 0) {
        completeImpl();
      }
    }

    @Override
    public void failed(final ResolveTask task) {
      s_logger.debug("Input {} needed by {} ", task, getOutputValue());
      if (getState() == State.BLOCKED) {
        synchronized (this) {
          if (getState() == State.BLOCKED) {
            // One of the inputs has failed so we'll move onto the next state
            functionNextState(getBuilder());
          }
        }
      }
    }

    public void waitOn(final ResolveTask task) {
      _inputs.incrementAndGet();
      task.notifyOnTermination(this);
    }

    public void run() {
      decrementAndCheck();
    }

  }

  private void functionApply(final DependencyGraphBuilder builder, final ParameterizedFunction function, final ValueSpecification originalOutput,
      final ValueSpecification resolvedOutput) {
    final ResolveTask task = builder.declareTaskProducing(resolvedOutput, this);
    if (task == this) {
      // We're going to work on producing this value
      final CompiledFunctionDefinition functionDefinition = function.getFunction();
      Set<ValueSpecification> originalOutputValues = null;
      try {
        originalOutputValues = functionDefinition.getResults(builder.getCompilationContext(), getTarget());
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getResults", t);
        builder.postException(t);
      }
      if (originalOutputValues == null) {
        s_logger.debug("Function {} returned NULL for getResults on {}", functionDefinition, getTarget());
        functionNextState(builder);
        return;
      }
      final Set<ValueSpecification> resolvedOutputValues;
      if (originalOutput.equals(resolvedOutput)) {
        resolvedOutputValues = originalOutputValues;
        // Stake our claim on other output values
        for (ValueSpecification outputValue : originalOutputValues) {
          builder.declareTaskProducing(outputValue, this);
        }
      } else {
        resolvedOutputValues = Sets.newHashSetWithExpectedSize(originalOutputValues.size());
        for (ValueSpecification outputValue : originalOutputValues) {
          if (originalOutput.equals(outputValue)) {
            s_logger.debug("Substituting {} with {}", outputValue, resolvedOutput);
            resolvedOutputValues.add(resolvedOutput);
          } else {
            resolvedOutputValues.add(outputValue);
            // Stake our claim on other output values
            builder.declareTaskProducing(outputValue, this);
          }
        }
      }
      Set<ValueRequirement> inputRequirements = null;
      try {
        inputRequirements = functionDefinition.getRequirements(builder.getCompilationContext(), getTarget(), getValueRequirement());
      } catch (Throwable t) {
        s_logger.warn("Exception thrown by getRequirements", t);
        builder.postException(t);
      }
      if (inputRequirements == null) {
        s_logger.debug("Function {} returned NULL for getResults on {}", functionDefinition, getValueRequirement());
        functionNextState(builder);
        return;
      }
      final int inputCount = inputRequirements.size();
      final InputRequirementCallback callback = new InputRequirementCallback(builder, resolvedOutput) {

        private final List<ResolveTask> _inputTasks = new ArrayList<ResolveTask>(inputCount);

        @Override
        protected void completeImpl(final ResolveTask task) {
          synchronized (_inputTasks) {
            _inputTasks.add(task);
          }
        }

        @Override
        protected void completeImpl() {
          // Late resolution of output based on inputs found
          final Set<ValueSpecification> inputValues;
          final CompiledFunctionDefinition functionDefinition = function.getFunction();
          if (getOutputValue().getProperties().isStrict()) {
            inputValues = Sets.newHashSetWithExpectedSize(_inputTasks.size());
            for (ResolveTask inputTask : _inputTasks) {
              inputValues.add(inputTask.getValueSpecification());
            }
          } else {
            final Map<ValueSpecification, ValueRequirement> inputs = Maps.newHashMapWithExpectedSize(_inputTasks.size());
            for (ResolveTask inputTask : _inputTasks) {
              inputs.put(inputTask.getValueSpecification(), inputTask.getValueRequirement());
            }
            Set<ValueSpecification> newOutputValues = null;
            try {
              newOutputValues = functionDefinition.getResults(getBuilder().getCompilationContext(), getTarget(), inputs);
            } catch (Throwable t) {
              s_logger.warn("Exception thrown by getResults", t);
              getBuilder().postException(t);
            }
            if (newOutputValues == null) {
              s_logger.debug("Function {} returned NULL for getResults on {}", functionDefinition, inputs);
              functionNextState(getBuilder());
              return;
            }
            // TODO: are the original output values different from the new output values?
            inputValues = inputs.keySet();
          }
          // Fetch any additional input requirements now needed as a result of input and output resolution
          Set<ValueRequirement> additionalRequirements = null;
          try {
            additionalRequirements = functionDefinition.getAdditionalRequirements(getBuilder().getCompilationContext(), getTarget(), inputValues, resolvedOutputValues);
          } catch (Throwable t) {
            s_logger.warn("Exception thrown by getAdditionalRequirements", t);
            getBuilder().postException(t);
          }
          if (additionalRequirements == null) {
            s_logger.debug("Function {} returned NULL for getAdditionalRequirements on {}", functionDefinition, inputValues);
            functionNextState(getBuilder());
            return;
          }
          if (additionalRequirements.isEmpty()) {
            onComplete();
          } else {
            final InputRequirementCallback callback = new InputRequirementCallback(getBuilder(), getOutputValue()) {

              @Override
              protected void completeImpl(final ResolveTask task) {
                synchronized (_inputTasks) {
                  _inputTasks.add(task);
                }
              }

              @Override
              protected void completeImpl() {
                onComplete();
              }

            };
            for (ValueRequirement inputRequirement : additionalRequirements) {
              final ResolveTask subTask = builder.resolveRequirement(inputRequirement, ResolveTask.this);
              callback.waitOn(subTask);
            }
            callback.run();
          }
        }

        private void onComplete() {
          s_logger.info("Inputs resolved for {}", getOutputValue());
          final DependencyNode node = new DependencyNode(getTarget());
          node.setFunction(function);
          node.addOutputValues(resolvedOutputValues);
          setStateComplete(node, getOutputValue(), _inputTasks);
        }

      };
      for (ValueRequirement inputRequirement : inputRequirements) {
        final ResolveTask subTask = builder.resolveRequirement(inputRequirement, this);
        callback.waitOn(subTask);
      }
      callback.run();
    } else {
      // Another task at equivalent depth is working on this
      s_logger.debug("Waiting for equivalent task to complete");
      task.notifyOnTermination(new TerminationCallback() {

        @Override
        public void complete(final ResolveTask task) {
          // The other completed and got a dependency node, so use that
          s_logger.info("Equivalent task completed for {}", resolvedOutput);
          setStateComplete(task.getDependencyNode(), task.getValueSpecification(), task.getInputTasks());
        }

        @Override
        public void failed(final ResolveTask task) {
          // The other failed so we'll move onto the next state
          functionNextState(builder);
        }

      });
    }
  }

  // TODO: update javadoc on CompiledFunctionDefinition about nulls; nothing should return null, but doing so is better than an exception for halting graph construction in a controlled manner

  private void functionNextState(final DependencyGraphBuilder builder) {
    if (getFunctions().hasNext()) {
      setState(State.FUNCTIONS);
      builder.addToRunQueue(ResolveTask.this);
    } else {
      s_logger.info("Exhausted function list for {}", getValueRequirement());
      setStateFailed();
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getValueRequirement() + " " + getState() + "]";
  }

  // HashCode and Equality are to allow tasks to be considered equal iff they are for the same value requirement and
  // correspond to the same resolution depth (i.e. the chain of functions that will be applied to their outputs)

  @Override
  public int hashCode() {
    return getValueRequirement().hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ResolveTask)) {
      return false;
    }
    final ResolveTask other = (ResolveTask) o;
    if (!getValueRequirement().equals(other.getValueRequirement())) {
      return false;
    }
    // TODO: check depth chain for functions
    return true;
  }

  public static interface TerminationCallback {

    void complete(ResolveTask task);

    void failed(ResolveTask task);

  }

  public void notifyOnTermination(final TerminationCallback callback) {
    // Check if state has already changed
    switch (getState()) {
      case COMPLETE:
        callback.complete(this);
        return;
      case FAILED:
        callback.failed(this);
        return;
    }
    // Add to list for later callback
    _blocked.add(callback);
    // If state changed during the call, the queue may not have been processed
    switch (getState()) {
      case COMPLETE:
        notifyComplete();
        return;
      case FAILED:
        notifyFailed();
        return;
    }
  }

}
