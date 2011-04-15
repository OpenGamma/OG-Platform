/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

/**
 * Provides a sequence of execution options which define the individual view cycles to be executed. This could be an
 * infinite sequence, and the execution options might be a function of the current time.
 */
public interface ViewCycleExecutionSequence {

  /**
   * Gets the next execution options in the sequence. This is a destructive operation; it should be used only when
   * intending to begin a view cycle with the options returned.
   * 
   * @return the next time details, or {@code null} if there are no further times for execution
   */
  ViewCycleExecutionOptions getNext();
  
  /**
   * Gets whether there are no more cycles in the execution sequence.
   * 
   * @return {@code true} if there are no more cycles in the execution sequence, {@code false} otherwise.
   */
  boolean isEmpty();
  
}
