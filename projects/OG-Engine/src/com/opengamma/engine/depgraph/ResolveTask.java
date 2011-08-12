/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Unit of task resolution. A resolve task executes to convert a {@link ValueRequirement} into a dependency node.
 */
/* package */final class ResolveTask extends AbstractResolvedValueProducer implements ContextRunnable {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolveTask.class);
  private static final AtomicInteger s_nextObjectId = new AtomicInteger();

  /**
   * State within a task. As the task executes, the execution is delegated to the
   * current state object.
   */
  protected abstract static class State {

    private final int _objectId = s_nextObjectId.getAndIncrement();
    private final ResolveTask _task;

    protected State(final ResolveTask task) {
      assert task != null;
      _task = task;
    }

    protected int getObjectId() {
      return _objectId;
    }

    protected ResolveTask getTask() {
      return _task;
    }

    protected void setTaskStateFinished(final GraphBuildingContext context) {
      getTask().finished(context);
    }

    protected void setTaskState(final State nextState) {
      getTask().setState(nextState);
    }

    protected void setRunnableTaskState(final State nextState, final GraphBuildingContext context) {
      final ResolveTask task = getTask();
      task.setState(nextState);
      context.run(task);
    }

    protected boolean pushResult(final GraphBuildingContext context, final ResolvedValue resolvedValue) {
      return getTask().pushResult(context, resolvedValue);
    }

    protected ResolvedValue createResult(final ValueSpecification valueSpecification, final ParameterizedFunction parameterizedFunction, final Set<ValueSpecification> functionInputs,
        final Set<ValueSpecification> functionOutputs) {
      return new ResolvedValue(valueSpecification, parameterizedFunction, getComputationTarget(), functionInputs, functionOutputs);
    }

    protected boolean pushResult(final GraphBuildingContext context, final ValueSpecification valueSpecification, final ParameterizedFunction parameterizedFunction,
        final Set<ValueSpecification> functionInputs,
        final Set<ValueSpecification> functionOutputs) {
      return pushResult(context, createResult(valueSpecification, parameterizedFunction, functionInputs, functionOutputs));
    }

    protected void storeFailure(final ResolutionFailure failure) {
      getTask().storeFailure(failure);
    }

    protected ValueRequirement getValueRequirement() {
      return getTask().getValueRequirement();
    }

    protected ComputationTarget getComputationTarget() {
      return getTask().getComputationTarget();
    }

    protected void run(final GraphBuildingContext context) {
      throw new UnsupportedOperationException("Not runnable state (" + toString() + ")");
    }

    protected void pump(final GraphBuildingContext context) {
      throw new UnsupportedOperationException("Not pumpable state (" + toString() + ")");
    }

    /**
     * Tests if the state is somehow active and may reschedule the task to run (i.e. it's blocked on something) given that the
     * parent task is going to neither call {@link #run} nor {@link #pump}
     * 
     * @return {@code true} if the state is active, {@code false} otherwise
     */
    protected abstract boolean isActive();

  }

  /**
   * Parent resolve task (i.e. chain of previous value requirements) so that loops can be detected and avoided. This is not
   * ref-counted.
   */
  private final ResolveTask _parent;

  /**
   * Pre-calculated hashcode.
   */
  private final int _hashCode;

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
    _hashCode = (parent != null) ? valueRequirement.hashCode() * 31 + parent.hashCode() : valueRequirement.hashCode();
    setState(new ResolveTargetStep(this));
  }

  private State getState() {
    return _state;
  }

  private void setState(final State state) {
    assert state != null;
    s_logger.debug("State transition {} to {}", _state, state);
    if (_state == null) {
      // Increase the ref-count as the state holds a reference to us 
      addRef();
    }
    _state = state;
  }

  protected boolean isFinished() {
    return _state == null;
  }

  @Override
  protected boolean finished(final GraphBuildingContext context) {
    assert _state != null;
    _state = null;
    // Decrease the ref-count as the state no longer holds a reference to us
    release(context);
    return super.finished(context);
  }

  protected void setComputationTarget(final ComputationTarget target) {
    assert target != null;
    _target = target;
  }

  protected ComputationTarget getComputationTarget() {
    return _target;
  }

  @Override
  public void run(final GraphBuildingContext context) {
    getState().run(context);
    // Release the lock that the context added before we got queued
    release(context);
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

  public void printParentTree(final String indent) {
    System.out.println(indent + toString());
    if (getParent() != null) {
      getParent().printParentTree(indent + "  ");
    }
  }

  // HashCode and Equality are to allow tasks to be considered equal iff they are for the same value requirement and
  // correspond to the same resolution depth (i.e. the sets of parents are equal)

  @Override
  public int hashCode() {
    return _hashCode;
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
    final Set<ValueRequirement> set = new HashSet<ValueRequirement>();
    ResolveTask parent = getParent();
    while (parent != null) {
      set.add(parent.getValueRequirement());
      parent = parent.getParent();
    }
    parent = other.getParent();
    while (parent != null) {
      if (!set.remove(parent.getValueRequirement())) {
        // Other has a parent we don't have
        return false;
      }
      parent = parent.getParent();
    }
    // Set is empty if all parents matched
    return set.isEmpty();
  }

  @Override
  protected void pumpImpl(final GraphBuildingContext context) {
    s_logger.debug("Pump called on {}", this);
    getState().pump(context);
  }

  @Override
  public String toString() {
    return "ResolveTask" + getObjectId() + "[" + getValueRequirement() + ", " + getState() + "]";
  }

  @Override
  public int release(final GraphBuildingContext context) {
    final int count = super.release(context);
    if (getState() != null) {
      if (count == 2) {
        // References held from the cache and the simulated one from our state
        if (!getState().isActive()) {
          s_logger.debug("Remove unfinished {} from the cache", this);
          context.discardUnfinishedTask(this);
        }
      }
    } else {
      s_logger.debug("Leave finished {} in the cache", this);
    }
    return count;
  }

}
