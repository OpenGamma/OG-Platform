/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import com.opengamma.util.PublicAPI;

/**
 * Provides a sequence of execution options which define the individual view cycles to be executed. This could be an
 * infinite sequence, and the execution options might be a function of the current time.
 */
@PublicAPI
public interface ViewCycleExecutionSequence {

  /**
   * Gets the next execution options in the sequence. This is a destructive operation; it should be used only when
   * intending to begin a view cycle with the options returned.
   * 
   * @param defaultExecutionOptions  the default execution options, may be null
   * @return the execution options for the next cycle, null if there are no further cycles to execute
   */
  ViewCycleExecutionOptions getNext(ViewCycleExecutionOptions defaultExecutionOptions);

  /**
   * Gets whether there are no more cycles in the execution sequence.
   * 
   * @return true if there are no more cycles in the execution sequence
   */
  boolean isEmpty();

}
