/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import javax.time.Instant;

/**
 * Provides a sequence of evaluation times representing the times at which a view should be evaluated. This could be
 * an infinite sequence, and the values might be a function of the current time.
 */
public interface ViewEvaluationTimeSequence {

  /**
   * Gets the next evaluation time in the sequence. This is a destructive operation; it should be used only when
   * intending to begin a computation cycle with the evaluation time returned.
   * <p>
   * Every evaluation time in a sequence must be unique.
   * 
   * @return the next evaluation time, or {@code null} if there are no further evaluation times
   */
  Instant getNextEvaluationTime();
  
  /**
   * Gets whether the view evaluation time sequence is empty, meaning that there are no further evaluation times in the
   * sequence.
   * 
   * @return {@code true} if there are no further evaluation times, {@code false} otherwise.
   */
  boolean isEmpty();
  
}
