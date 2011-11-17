/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.ObjectUtils;
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
      // No-op; happens if a worker "finishes" a function application PumpingState and it progresses to the next natural
      // state in advance of the pump from the abstract value producer
    }

    /**
     * Tests if the state is somehow active and may reschedule the task to run (i.e. it's blocked on something) given that the
     * parent task is going to neither call {@link #run} nor {@link #pump}
     * 
     * @return true if the state is active
     */
    protected abstract boolean isActive();

    /**
     * Called when the parent task is discarded.
     * 
     * @param context the graph building context, not null
     */
    protected void onDiscard(final GraphBuildingContext context) {
      // No-op
    }

  }

  /**
   * Parent resolve task (i.e. chain of previous value requirements) so that loops can be detected and avoided. This is not
   * ref-counted.
   */
  private final ResolveTask _parent;

  /**
   * Parent value requirements.
   */
  private final Set<ValueRequirement> _parentRequirements;

  /**
   * Pre-calculated hashcode.
   */
  private final int _hashCode;

  /**
   * Current state.
   */
  private volatile State _state;

  /**
   * Flag to mark whether any child production was rejected because of a value requirement loop.
   */
  private volatile boolean _recursion;

  /**
   * Resolved target for the value requirement.
   */
  private ComputationTarget _target;

  public ResolveTask(final ValueRequirement valueRequirement, final ResolveTask parent) {
    super(valueRequirement);
    _parent = parent;
    if (parent != null) {
      if (parent.getParentValueRequirements() != null) {
        _parentRequirements = new HashSet<ValueRequirement>(parent.getParentValueRequirements());
        _parentRequirements.add(parent.getValueRequirement());
      } else {
        _parentRequirements = Collections.singleton(parent.getValueRequirement());
      }
      _hashCode = valueRequirement.hashCode() * 31 + _parentRequirements.hashCode();
    } else {
      _parentRequirements = null;
      _hashCode = valueRequirement.hashCode();
    }
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

  public boolean isFinished() {
    return _state == null;
  }

  @Override
  protected void finished(final GraphBuildingContext context) {
    assert _state != null;
    _state = null;
    // Decrease the ref-count as the state no longer holds a reference to us
    release(context);
    super.finished(context);
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

  private Set<ValueRequirement> getParentValueRequirements() {
    return _parentRequirements;
  }

  public boolean hasParent(final ResolveTask task) {
    if (task == this) {
      return true;
    } else {
      return hasParent(task.getValueRequirement());
    }
  }

  public boolean hasParent(final ValueRequirement valueRequirement) {
    if (valueRequirement.equals(getValueRequirement())) {
      return true;
    } else if (getParentValueRequirements() == null) {
      return false;
    } else {
      return getParentValueRequirements().contains(valueRequirement);
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
    return ObjectUtils.equals(getParentValueRequirements(), other.getParentValueRequirements());
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

  private void toLongString(final StringBuilder sb) {
    sb.append(getValueRequirement());
    if (getParent() != null) {
      sb.append("->");
      getParent().toLongString(sb);
    }
  }

  public String toLongString() {
    final StringBuilder sb = new StringBuilder();
    toLongString(sb);
    return sb.toString();
  }

  @Override
  public int release(final GraphBuildingContext context) {
    final int count = super.release(context);
    if (getState() != null) {
      if (count == 2) {
        // References held from the cache and the simulated one from our state
        final State state = getState();
        if (!state.isActive()) {
          s_logger.debug("Remove unfinished {} from the cache", this);
          context.discardTask(this);
        }
      } else if (count == 1) {
        // Simulated reference held from our state only
        final State state = getState();
        if (!state.isActive()) {
          s_logger.debug("Discarding state for unfinished {}", this);
          state.onDiscard(context);
          _state = null;
        }
      }
    }
    return count;
  }

  // TODO: The recursion logic isn't entirely correct. A resolve task may end up working from
  // a substitute/delegate (see FunctionApplicationStep) that encountered recursion causing a
  // failure. This will not be flagged using the current mechanism preventing complete state
  // exploration.

  public void setRecursionDetected() {
    _recursion = true;
  }

  public boolean wasRecursionDetected() {
    return _recursion;
  }

}
