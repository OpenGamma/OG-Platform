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

import javax.time.InstantProvider;

import com.opengamma.util.ArgumentChecker;

/**
 * Provides a finite sequence of view cycles.
 */
public class ArbitraryViewCycleExecutionSequence extends MergingViewCycleExecutionSequence {

  private final LinkedList<ViewCycleExecutionOptions> _executionSequence;
  
  public ArbitraryViewCycleExecutionSequence(Collection<ViewCycleExecutionOptions> executionSequence) {
    ArgumentChecker.notNull(executionSequence, "executionSequence");
    _executionSequence = new LinkedList<ViewCycleExecutionOptions>(executionSequence);
  }
  
  /**
   * Gets a sequence for a single cycle which relies on default cycle execution options.
   * 
   * @return the sequence, not null
   */
  public static ArbitraryViewCycleExecutionSequence single() {
    return new ArbitraryViewCycleExecutionSequence(Collections.singletonList(new ViewCycleExecutionOptions()));
  }
  
  /**
   * Gets a sequence for a single cycle.
   * 
   * @param executionOptions  the execution options for the single cycle, not null
   * @return the sequence, not null
   */
  public static ArbitraryViewCycleExecutionSequence single(ViewCycleExecutionOptions executionOptions) {
    return new ArbitraryViewCycleExecutionSequence(Collections.singletonList(executionOptions));
  }
  
  /**
   * Gets a sequence for a collection of valuation times.
   * 
   * @param valuationTimeProviders  the valuation times, not null
   * @return the sequence, not null
   */
  public static ArbitraryViewCycleExecutionSequence of(InstantProvider... valuationTimeProviders) {
    return of(Arrays.asList(valuationTimeProviders));
  }

  /**
   * Gets a sequence for a collection of valuation times.
   * 
   * @param valuationTimeProviders  the valuation times, not  null
   * @return the sequence, not null
   */
  public static ArbitraryViewCycleExecutionSequence of(Collection<InstantProvider> valuationTimeProviders) {
    ArgumentChecker.notNull(valuationTimeProviders, "valuationTimeProviders");
    List<ViewCycleExecutionOptions> executionSequence = new ArrayList<ViewCycleExecutionOptions>(valuationTimeProviders.size());
    for (InstantProvider valuationTimeProvider : valuationTimeProviders) {
      ViewCycleExecutionOptions options = new ViewCycleExecutionOptions(valuationTimeProvider);
      executionSequence.add(options);
    }
    return new ArbitraryViewCycleExecutionSequence(executionSequence);
  }
  
  /**
   * Gets a sequence for a collection of cycles.
   * 
   * @param executionSequence the sequence, not null
   * @return the sequence, not null
   */
  public static ArbitraryViewCycleExecutionSequence of(ViewCycleExecutionOptions... executionSequence) {
    return new ArbitraryViewCycleExecutionSequence(Arrays.asList(executionSequence));
  }
  
  public List<ViewCycleExecutionOptions> getRemainingSequence() {
    return Collections.unmodifiableList(_executionSequence);
  }
  
  @Override
  public ViewCycleExecutionOptions getNext(ViewCycleExecutionOptions defaultExecutionOptions) {
    return merge(_executionSequence.poll(), defaultExecutionOptions);
  }

  @Override
  public boolean isEmpty() {
    return _executionSequence.isEmpty();
  }
 
}
