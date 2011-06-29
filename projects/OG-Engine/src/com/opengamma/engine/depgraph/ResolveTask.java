/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * Unit of task resolution. A resolve task executes to convert a {@link ValueRequirement} into a dependency node.
 */
/* package */class ResolveTask {

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
  private State _state;

  public ResolveTask(final ValueRequirement valueRequirement) {
    ArgumentChecker.notNull(valueRequirement, "valueRequirement");
    _valueRequirement = valueRequirement;
    setState(State.PENDING);
  }

  private State getState() {
    return _state;
  }

  private void setState(final State state) {
    _state = state;
  }

  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getValueRequirement() + " " + getState() + "]";
  }

}
