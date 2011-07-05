/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirement;

/* package */class AggregateResolvedValueProducer extends AbstractResolvedValueProducer implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(AggregateResolvedValueProducer.class);

  private int _pendingTasks = 1;
  private final List<ResolutionPump> _pumps = new ArrayList<ResolutionPump>();

  public AggregateResolvedValueProducer(final ValueRequirement valueRequirement) {
    super(valueRequirement);
  }

  protected int getPendingTasks() {
    return _pendingTasks;
  }

  protected void setPendingTasks(final int pendingTasks) {
    _pendingTasks = pendingTasks;
  }

  @Override
  public void failed(final ValueRequirement value) {
    boolean lastTask = false;
    synchronized (this) {
      if (--_pendingTasks < 1) {
        lastTask = true;
      }
    }
    if (lastTask) {
      finished();
    }
  }

  @Override
  public void resolved(final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
    s_logger.debug("Received {} for {}", value, valueRequirement);
    synchronized (this) {
      _pendingTasks--;
      _pumps.add(pump);
    }
    pushResult(value);
  }

  @Override
  protected void pumpImpl() {
    Collection<ResolutionPump> pumps = null;
    synchronized (this) {
      if (getPendingTasks() < 1) {
        pumps = new ArrayList<ResolutionPump>(_pumps);
        _pumps.clear();
      }
    }
    if (pumps != null) {
      if (pumps.isEmpty()) {
        // We have nothing to pump, so must have failed
        finished();
      } else {
        s_logger.debug("Pumping {} origin tasks", pumps.size());
        for (ResolutionPump pump : pumps) {
          pump.pump();
        }
      }
    }
  }

  public void addProducer(final ResolvedValueProducer producer) {
    synchronized (this) {
      _pendingTasks++;
    }
    producer.addCallback(this);
  }

  public void start() {
    boolean lastTask = false;
    synchronized (this) {
      if (--_pendingTasks == 0) {
        // Only finished if nothing outstanding, and nothing to pump
        lastTask = _pumps.isEmpty();
      }
    }
    if (lastTask) {
      finished();
    }
  }

}
