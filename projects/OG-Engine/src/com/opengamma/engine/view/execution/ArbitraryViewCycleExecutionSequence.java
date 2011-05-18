/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.time.Instant;

import com.opengamma.util.ArgumentChecker;

/**
 * Provides a finite sequence of view cycle execution details for known valuation times.
 */
public class ArbitraryViewCycleExecutionSequence implements ViewCycleExecutionSequence {

  private final LinkedList<ViewCycleExecutionOptions> _executionSequence;
  
  public ArbitraryViewCycleExecutionSequence(Collection<ViewCycleExecutionOptions> executionSequence) {
    ArgumentChecker.notNull(executionSequence, "executionSequence");
    _executionSequence = new LinkedList<ViewCycleExecutionOptions>(executionSequence);
  }
  
  public static ArbitraryViewCycleExecutionSequence of(Instant valuationTime, Instant snapshotTime) {
    return new ArbitraryViewCycleExecutionSequence(Collections.singletonList(new ViewCycleExecutionOptions(valuationTime, snapshotTime)));
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
    ArgumentChecker.notNull(valuationTimes, "valuationTimes");
    List<ViewCycleExecutionOptions> executionSequence = new ArrayList<ViewCycleExecutionOptions>(valuationTimes.size());
    for (Instant valuationTime : valuationTimes) {
      executionSequence.add(new ViewCycleExecutionOptions(valuationTime, valuationTime));
    }
    return new ArbitraryViewCycleExecutionSequence(executionSequence);
  }
  
  public List<ViewCycleExecutionOptions> getRemainingSequence() {
    return Collections.unmodifiableList(_executionSequence);
  }
  
  @Override
  public ViewCycleExecutionOptions getNext() {
    return _executionSequence.poll();
  }

  @Override
  public boolean isEmpty() {
    return _executionSequence.isEmpty();
  }
 
}
