/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

/**
 * Provides an infinite sequence of view cycle execution options.
 */
public class InfiniteViewCycleExecutionSequence extends MergingViewCycleExecutionSequence {

  @Override
  public ViewCycleExecutionOptions poll(ViewCycleExecutionOptions defaultExecutionOptions) {
    return merge(new ViewCycleExecutionOptions(), defaultExecutionOptions);
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
    return (other instanceof InfiniteViewCycleExecutionSequence);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public int estimateRemaining() {
    return Integer.MAX_VALUE;
  }

  @Override
  public InfiniteViewCycleExecutionSequence copy() {
    return this;
  }

}
