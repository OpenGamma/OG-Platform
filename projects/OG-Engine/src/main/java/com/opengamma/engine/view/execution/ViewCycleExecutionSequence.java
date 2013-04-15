/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import com.opengamma.util.PublicAPI;

/**
 * Provides a sequence of execution options which define the individual view cycles to be executed. This could be an infinite sequence, and the execution options might be a function of the current
 * time.
 */
@PublicAPI
public interface ViewCycleExecutionSequence {

  /**
   * Gets the next execution options in the sequence. This is a destructive operation; it should be used only when intending to begin a view cycle with the options returned.
   * 
   * @param defaultExecutionOptions the default execution options, may be null
   * @return the execution options for the next cycle, null if there are no further cycles to execute
   */
  ViewCycleExecutionOptions poll(ViewCycleExecutionOptions defaultExecutionOptions);

  /**
   * Gets whether there are no more cycles in the execution sequence.
   * 
   * @return true if there are no more cycles in the execution sequence
   */
  boolean isEmpty();

  /**
   * Gets an estimate of the number of cycles remaining in the sequence. An infinite sequence should always return {@link Integer#MAX_VALUE}. A finite sequence should return the number of remaining
   * cycles. A minimum implementation should at least guarantee to return more than 0 if {@link #isEmpty} would return false.
   * 
   * @return an estimate of the number of cycles remaining
   */
  int estimateRemaining();

  /**
   * Produces a copy of the sequence in its current state. Calling {@link #poll} on the original will not affect the copy, and calling {@link #poll} on the copy will not affect this original.
   * 
   * @return the copy, not null
   */
  ViewCycleExecutionSequence copy();

}
