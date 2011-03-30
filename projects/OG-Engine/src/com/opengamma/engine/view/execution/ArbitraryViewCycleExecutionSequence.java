/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import javax.time.Instant;

/**
 * Provides a finite sequence of view cycle execution details for known valuation times.
 */
public class ArbitraryViewCycleExecutionSequence implements ViewCycleExecutionSequence {

  private final Queue<Instant> _batchTimes;
  
  public ArbitraryViewCycleExecutionSequence(Collection<Instant> valuationTimes) {
    _batchTimes = new LinkedList<Instant>(valuationTimes);
  }
  
  public static ArbitraryViewCycleExecutionSequence of(long... valuationTimesEpochMillis) {
    Collection<Instant> valuationTimes = new ArrayList<Instant>(valuationTimesEpochMillis.length);
    for (long valuationTimeEpochMillis : valuationTimesEpochMillis) {
      valuationTimes.add(Instant.ofEpochMillis(valuationTimeEpochMillis));
    }
    return of(valuationTimes);
  }
  
  public static ArbitraryViewCycleExecutionSequence of(Instant... valuationTimes) {
    return of(Arrays.asList(valuationTimes));
  }
  
  public static ArbitraryViewCycleExecutionSequence of(Collection<Instant> valuationTimes) {
    return new ArbitraryViewCycleExecutionSequence(valuationTimes);
  }
  
  @Override
  public ViewCycleExecutionOptions getNext() {
    Instant nextValuationTime = _batchTimes.poll();
    return new ViewCycleExecutionOptions(nextValuationTime, nextValuationTime);
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
    if (!(obj instanceof ArbitraryViewCycleExecutionSequence)) {
      return false;
    }
    ArbitraryViewCycleExecutionSequence other = (ArbitraryViewCycleExecutionSequence) obj;
    return _batchTimes.equals(other._batchTimes);
  }

  @Override
  public boolean isEmpty() {
    return _batchTimes.isEmpty();
  }
 
}
