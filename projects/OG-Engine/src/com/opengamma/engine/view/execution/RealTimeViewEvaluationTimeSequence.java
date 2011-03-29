/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import javax.time.Instant;

/**
 * Provides an infinite sequence of evaluation times based on the current time.
 */
public class RealTimeViewEvaluationTimeSequence implements ViewEvaluationTimeSequence {

  @Override
  public Instant getNextEvaluationTime() {
    return Instant.now();
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
    return (other instanceof RealTimeViewEvaluationTimeSequence);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

}
