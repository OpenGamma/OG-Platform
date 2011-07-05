/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Unit of task resolution. A resolve task executes to convert a {@link ValueRequirement} into a dependency node.
 */
/* package */final class ResolveTask extends AbstractResolvedValueProducer {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolveTask.class);

  /**
   * State within a task. As the task executes, the execution is delegated to the
   * current state object.
   */
  protected abstract static class State {

    private final ResolveTask _task;

    protected State(final ResolveTask task) {
      assert task != null;
      _task = task;
    }

    protected ResolveTask getTask() {
      return _task;
    }

    protected void setTaskStateFinished() {
      getTask().finished();
    }

    protected void setTaskState(final State nextState) {
      getTask().setState(nextState);
    }

    protected void setRunnableTaskState(final State nextState, final DependencyGraphBuilder builder) {
      final ResolveTask task = getTask();
      task.setState(nextState);
      builder.addToRunQueue(task);
    }

    protected void pushResult(final ResolvedValue resolvedValue) {
      getTask().pushResult(resolvedValue);
    }

    protected ResolvedValue createResult(final ValueSpecification valueSpecification, final ParameterizedFunction parameterizedFunction, final Set<ValueSpecification> functionInputs,
        final Set<ValueSpecification> functionOutputs) {
      return new ResolvedValue(valueSpecification, parameterizedFunction, getComputationTarget(), functionInputs, functionOutputs);
    }

    protected void pushResult(final ValueSpecification valueSpecification, final ParameterizedFunction parameterizedFunction, final Set<ValueSpecification> functionInputs,
        final Set<ValueSpecification> functionOutputs) {
      pushResult(createResult(valueSpecification, parameterizedFunction, functionInputs, functionOutputs));
    }

    protected ValueRequirement getValueRequirement() {
      return getTask().getValueRequirement();
    }

    protected ComputationTarget getComputationTarget() {
      return getTask().getComputationTarget();
    }

    protected void run(final DependencyGraphBuilder builder) {
      throw new UnsupportedOperationException("Not runnable state (" + toString() + ")");
    }

    protected void pump() {
      throw new UnsupportedOperationException("Not pumpable state (" + toString() + ")");
    }

  }

  /**
   * Parent resolve task (i.e. chain of previous value requirements) so that loops can be detected and avoided.
   */
  private final ResolveTask _parent;

  /**
   * Current state.
   */
  private volatile State _state;

  /**
   * Resolved target for the value requirement.
   */
  private ComputationTarget _target;

  public ResolveTask(final ValueRequirement valueRequirement, final ResolveTask parent) {
    super(valueRequirement);
    _parent = parent;
    setState(new ResolveTargetStep(this));
  }

  private State getState() {
    return _state;
  }

  private void setState(final State state) {
    assert state != null;
    s_logger.debug("State transition {} to {}", _state, state);
    _state = state;
  }

  @Override
  protected void finished() {
    _state = null;
    super.finished();
  }

  protected void setComputationTarget(final ComputationTarget target) {
    assert target != null;
    _target = target;
  }

  protected ComputationTarget getComputationTarget() {
    return _target;
  }

  protected void run(final DependencyGraphBuilder builder) {
    getState().run(builder);
  }

  private ResolveTask getParent() {
    return _parent;
  }

  public boolean hasParent(final ResolveTask task) {
    if (task == this) {
      return true;
    } else if (getParent() == null) {
      return false;
    } else {
      return getParent().hasParent(task);
    }
  }

  public boolean hasParent(final ValueRequirement valueRequirement) {
    if (valueRequirement.equals(getValueRequirement())) {
      return true;
    } else if (getParent() == null) {
      return false;
    } else {
      return getParent().hasParent(valueRequirement);
    }
  }

  // HashCode and Equality are to allow tasks to be considered equal iff they are for the same value requirement and
  // correspond to the same resolution depth (i.e. the sets of parents are equal)

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
    ResolveTask other = (ResolveTask) o;
    if (!getValueRequirement().equals(other.getValueRequirement())) {
      return false;
    }
    if (other.getParent() == null) {
      return getParent() == null;
    }
    final Set<ResolveTask> set = new HashSet<ResolveTask>();
    ResolveTask parent = getParent();
    while (parent != null) {
      set.add(parent);
      parent = parent.getParent();
    }
    parent = other.getParent();
    while (parent != null) {
      if (!set.remove(parent)) {
        // Other has a parent we don't have
        return false;
      }
      parent = parent.getParent();
    }
    // Set is empty if all parents matched
    return set.isEmpty();
  }

  @Override
  protected void pumpImpl() {
    s_logger.debug("Pump called on {}", this);
    getState().pump();
  }

  @Override
  public String toString() {
    return "ResolveTask[" + getValueRequirement() + ", " + getState() + "]";
  }

  // TODO: update javadoc on CompiledFunctionDefinition about nulls; nothing should return null, but doing so is better than an exception for halting graph construction in a controlled manner

}
