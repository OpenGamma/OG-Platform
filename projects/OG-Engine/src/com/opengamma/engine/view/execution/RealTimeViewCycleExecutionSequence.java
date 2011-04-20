/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import javax.time.Instant;

/**
 * Provides an infinite sequence of view cycle execution options based on the current time.
 */
public class RealTimeViewCycleExecutionSequence implements ViewCycleExecutionSequence {

  @Override
  public ViewCycleExecutionOptions getNext() {
    Instant now = Instant.now();
    return new ViewCycleExecutionOptions(now, now);
  }

  @Override
  public int hashCode() {
    return 1;
  }
  
  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    return (other instanceof RealTimeViewCycleExecutionSequence);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

}
