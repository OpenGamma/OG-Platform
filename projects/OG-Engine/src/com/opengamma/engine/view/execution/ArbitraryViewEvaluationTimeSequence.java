/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import javax.time.Instant;

/**
 * Provides a finite sequence of fixed evaluation times.
 */
public class ArbitraryViewEvaluationTimeSequence implements ViewEvaluationTimeSequence {

  private final Queue<Instant> _batchTimes;
  
  public ArbitraryViewEvaluationTimeSequence(Collection<Instant> evaluationTimes) {
    _batchTimes = new LinkedList<Instant>(evaluationTimes);
  }
  
  @Override
  public Instant getNextEvaluationTime() {
    return _batchTimes.poll();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_batchTimes == null) ? 0 : _batchTimes.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ArbitraryViewEvaluationTimeSequence)) {
      return false;
    }
    ArbitraryViewEvaluationTimeSequence other = (ArbitraryViewEvaluationTimeSequence) obj;
    return _batchTimes.equals(other._batchTimes);
  }

  @Override
  public boolean isEmpty() {
    return _batchTimes.isEmpty();
  }
 
}
