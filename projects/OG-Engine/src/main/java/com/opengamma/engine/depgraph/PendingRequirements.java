/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Debugging utility; can periodically report on the outstanding terminal requirements that are in the run queue.
 */
/* package */final class PendingRequirements implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(PendingRequirements.class);
  private static final int REPORT_PERIOD = 10; // Only report every Nth housekeeping tick (1s per tick)

  private static final Housekeeper.Callback<PendingRequirements> s_report = new Housekeeper.Callback<PendingRequirements>() {

    @Override
    public boolean tick(final DependencyGraphBuilder builder, final PendingRequirements data) {
      data.tick();
      return true;
    }

    @Override
    public boolean cancelled(final DependencyGraphBuilder builder, final PendingRequirements data) {
      return false;
    }

    @Override
    public boolean completed(final DependencyGraphBuilder builder, final PendingRequirements data) {
      return false;
    }

  };

  private final Housekeeper _monitor;
  private final ConcurrentMap<ValueRequirement, ValueRequirement> _valueRequirements = new ConcurrentHashMap<ValueRequirement, ValueRequirement>();
  private int _tick;

  public PendingRequirements(final DependencyGraphBuilder builder) {
    _monitor = Housekeeper.of(builder, s_report, this);
  }

  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
    _valueRequirements.remove(valueRequirement);
    if (pump != null) {
      context.close(pump);
    }
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    _valueRequirements.remove(value);
  }

  @Override
  public void recursionDetected() {
    // No-op
  }

  private void tick() {
    if ((++_tick % REPORT_PERIOD) == 0) {
      if (s_logger.isDebugEnabled()) {
        s_logger.debug("{} pending in run queue", _valueRequirements);
      } else {
        s_logger.info("{} requirements pending in run queue", _valueRequirements.size());
      }
    }
  }

  public void add(final GraphBuildingContext context, final ResolvedValueProducer producer) {
    _valueRequirements.put(producer.getValueRequirement(), producer.getValueRequirement());
    producer.addCallback(context, this);
    if (s_logger.isDebugEnabled() || s_logger.isInfoEnabled()) {
      _monitor.start();
    }
  }

  public Collection<ValueRequirement> getValueRequirements() {
    return _valueRequirements.keySet();
  }

}
