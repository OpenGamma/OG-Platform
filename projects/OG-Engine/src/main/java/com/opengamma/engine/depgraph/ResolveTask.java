/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.target.lazy.LazyComputationTargetResolver;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Unit of task resolution. A resolve task executes to convert a {@link ValueRequirement} into a dependency node.
 */
/* package */final class ResolveTask extends DirectResolvedValueProducer implements ContextRunnable {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolveTask.class);
  private static final AtomicInteger s_nextObjectId = new AtomicInteger();

  /**
   * State within a task. As the task executes, the execution is delegated to the current state object.
   */
  protected abstract static class State implements ResolvedValueProducer.Chain {

    private final int _objectId = s_nextObjectId.getAndIncrement();
    private final ResolveTask _task;

    //private final InstanceCount _instanceCount = new InstanceCount(this);

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

    protected boolean setTaskState(final State nextState) {
      return getTask().setState(this, nextState);
    }

    protected boolean setRunnableTaskState(final State nextState, final GraphBuildingContext context) {
      final ResolveTask task = getTask();
      if (task.setState(this, nextState)) {
        context.run(task);
        return true;
      } else {
        return false;
      }
    }

    protected boolean pushResult(final GraphBuildingContext context, final ResolvedValue resolvedValue, final boolean lastResult) {
      return getTask().pushResult(context, resolvedValue, lastResult);
    }

    /**
     * Creates a result instance.
     * <p>
     * The {@code valueSpecification} specification must be a normalized/canonical form.
     * 
     * @param valueSpecification the resolved value specification, as it will appear in the dependency graph, not null
     * @param parameterizedFunction the function identifier and parameters, not null
     * @param functionInputs the resolved input specifications, as they will appear in the dependency graph, not null
     * @param functionOutputs the resolved output specifications, as they will appear in the dependency graph, not null
     */
    protected ResolvedValue createResult(final ValueSpecification valueSpecification, final ParameterizedFunction parameterizedFunction, final Set<ValueSpecification> functionInputs,
        final Set<ValueSpecification> functionOutputs) {
      return new ResolvedValue(valueSpecification, parameterizedFunction, functionInputs, functionOutputs);
    }

    protected void storeFailure(final ResolutionFailure failure) {
      getTask().storeFailure(failure);
    }

    protected ValueRequirement getValueRequirement() {
      return getTask().getValueRequirement();
    }

    // TODO: Profile how much time is spent calling getTargetSpecification. If it is a costly op, would holding the resolved specification in the task object be justified?

    protected ComputationTargetSpecification getTargetSpecification(final GraphBuildingContext context) {
      return context.resolveTargetReference(getValueRequirement().getTargetReference());
    }

    protected ComputationTarget getComputationTarget(final GraphBuildingContext context) {
      final ComputationTargetSpecification specification = getTargetSpecification(context);
      if (specification == null) {
        return null;
      }
      final ComputationTarget target = LazyComputationTargetResolver.resolve(context.getCompilationContext().getComputationTargetResolver(), specification);
      if (target == null) {
        s_logger.warn("Computation target {} not found", specification);
      }
      return target;
    }

    protected boolean run(final GraphBuildingContext context) {
      throw new UnsupportedOperationException("Not runnable state (" + toString() + ")");
    }

    protected abstract void pump(final GraphBuildingContext context);

    @Override
    public int cancelLoopMembers(final GraphBuildingContext context, final Map<Chain, Chain.LoopState> visited) {
      return cancelLoopMembersImpl(context, visited);
    }

    protected int cancelLoopMembersImpl(final GraphBuildingContext context, final Map<Chain, Chain.LoopState> visited) {
      return getTask().cancelLoopMembers(context, visited);
    }

    /**
     * Called when the parent task is discarded.
     * 
     * @param context the graph building context, not null
     */
    protected void discard(final GraphBuildingContext context) {
      // No-op; only implement if there is data to discard (e.g. cancel things to free resources) for the state
    }

  }

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
   * Function mutual exclusion group hints. Functions shouldn't be considered for the same value requirement name if their group hint is already present.
   */
  private final Collection<FunctionExclusionGroup> _functionExclusion;

  public ResolveTask(final ValueRequirement valueRequirement, final ResolveTask parent, final Collection<FunctionExclusionGroup> functionExclusion) {
    super(valueRequirement);
    final int hc;
    if (parent != null) {
      if (parent.getParentValueRequirements() != null) {
        _parentRequirements = new HashSet<ValueRequirement>(parent.getParentValueRequirements());
        _parentRequirements.add(parent.getValueRequirement());
      } else {
        _parentRequirements = Collections.singleton(parent.getValueRequirement());
      }
      hc = valueRequirement.hashCode() * 31 + _parentRequirements.hashCode();
    } else {
      _parentRequirements = null;
      hc = valueRequirement.hashCode();
    }
    if (functionExclusion != null) {
      _functionExclusion = functionExclusion;
      _hashCode = hc * 31 + functionExclusion.hashCode();
    } else {
      _functionExclusion = null;
      _hashCode = hc;
    }
    _state = new GetFunctionsStep(this);
  }

  private State getState() {
    return _state;
  }

  private synchronized boolean setState(final State previousState, final State nextState) {
    assert nextState != null;
    if (_state == previousState) {
      s_logger.debug("State transition {} to {}", previousState, nextState);
      _state = nextState;
      return true;
    } else {
      System.err.println("Invalid state transition - was " + _state + ", not " + previousState + " - not advancing to " + nextState);
      return false;
    }
  }

  @Override
  public boolean isFinished() {
    return _state == null;
  }

  @Override
  protected void finished(final GraphBuildingContext context) {
    assert _state != null;
    _state = null;
    super.finished(context);
  }

  @Override
  public boolean tryRun(final GraphBuildingContext context) {
    final State state = getState();
    assert state != null;
    if (state.run(context)) {
      // Release the lock that the context added before we got queued (or run in-line)
      release(context);
      return true;
    } else {
      return false;
    }
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

  /**
   * Tests if the parent value requirements of this task are the same as a task would have if it used the given task as its parent.
   * <p>
   * This is part of a cheaper test for an existing task than creating a new instance and using the {@link #equals} method.
   * 
   * @param parent the candidate parent to test, not null
   * @return true if the parent value requirements would match
   */
  public boolean hasParentValueRequirements(final ResolveTask parent) {
    if (getParentValueRequirements() != null) {
      if (parent != null) {
        if (parent.getParentValueRequirements() != null) {
          if (getParentValueRequirements().size() == parent.getParentValueRequirements().size() + 1) {
            return getParentValueRequirements().contains(parent.getValueRequirement()) && getParentValueRequirements().containsAll(parent.getParentValueRequirements());
          } else {
            return false;
          }
        } else {
          if (getParentValueRequirements().size() == 1) {
            return getParentValueRequirements().contains(parent.getValueRequirement());
          } else {
            return false;
          }
        }
      } else {
        return false;
      }
    } else {
      return parent == null;
    }
  }

  public Collection<FunctionExclusionGroup> getFunctionExclusion() {
    return _functionExclusion;
  }

  // TODO: could use a ResolveTaskKey instead of the unusual behavior of hash/equal here

  // HashCode and Equality are to allow tasks to be considered equal iff they
  // are for the same value requirement, correspond to the same resolution
  // depth (i.e. the sets of parents are equal), and have the same function
  // exclusion set

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
    final ResolveTask other = (ResolveTask) o;
    if (!getValueRequirement().equals(other.getValueRequirement())) {
      return false;
    }
    return ObjectUtils.equals(getParentValueRequirements(), other.getParentValueRequirements()) && ObjectUtils.equals(getFunctionExclusion(), other.getFunctionExclusion());
  }

  @Override
  protected void pumpImpl(final GraphBuildingContext context) {
    s_logger.debug("Pump called on {}", this);
    final State state = getState();
    if (state != null) {
      state.pump(context);
    }
  }

  @Override
  public String toString() {
    return "ResolveTask" + getObjectId() + "[" + getValueRequirement() + ", " + getState() + "]";
  }

  @Override
  public int release(final GraphBuildingContext context) {
    int count = super.release(context);
    if (count == 1) {
      // It's possible that only the _requirements collection from the graph builder now holds a reference to us that we care about
      if (!isFinished()) {
        // Only discard unfinished tasks; others might be useful and worth keeping if we can afford the memory
        context.discardTask(this);
      }
    } else if (count == 0) {
      // Nothing holds a reference to us; discard any state remnants
      final State state = getState();
      if (state != null) {
        state.discard(context);
      }
    }
    return count;
  }

}
