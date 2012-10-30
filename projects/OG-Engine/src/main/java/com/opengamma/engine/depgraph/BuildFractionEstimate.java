/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;

/**
 * Estimates the percentage completion of the graph build.
 */
/* package */final class BuildFractionEstimate implements Supplier<Double> {

  private static final Logger s_logger = LoggerFactory.getLogger(BuildFractionEstimate.class);

  private final DependencyGraphBuilder _builder;
  private long _maxRemaining;

  public BuildFractionEstimate(final DependencyGraphBuilder builder) {
    _builder = builder;
  }

  private DependencyGraphBuilder getBuilder() {
    return _builder;
  }

  @Override
  public Double get() {
    if (getBuilder().isCancelled()) {
      return 1d;
    }
    // Note that this will break for big jobs that are > 2^63 steps. Is this a limit that can be reasonably hit?
    // Loose synchronization okay; this is only a guesstimate
    final long completed = getBuilder().getCompletedSteps();
    long scheduled = getBuilder().getScheduledSteps();
    if ((scheduled <= 0) || (completed <= 0)) {
      return 0d;
    }
    while (completed >= scheduled) {
      if (getBuilder().isGraphBuilt()) {
        return 1d;
      } else {
        // spin and have another go; scheduled steps will eventually increase
        scheduled = getBuilder().getScheduledSteps();
        if (scheduled <= 0) {
          // 2^63 overflow
          return 0d;
        }
      }
    }
    s_logger.info("Completed {} of {} scheduled steps", completed, scheduled);
    getBuilder().reportStateSize();
    // TODO: What can we do based on sampling the counters available and applying knowledge of typical graph shapes? Don't want anything too heavyweight.
    final long remaining = scheduled - completed;
    if (remaining > _maxRemaining) {
      _maxRemaining = remaining;
    }
    return (double) (_maxRemaining - remaining) / (double) _maxRemaining;
  }

  @Override
  public String toString() {
    return (get() * 100) + "%";
  }

}
