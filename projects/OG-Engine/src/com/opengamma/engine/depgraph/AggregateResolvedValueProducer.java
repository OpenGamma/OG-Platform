/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
import com.opengamma.engine.value.ValueRequirement;

/* package */class AggregateResolvedValueProducer extends AbstractResolvedValueProducer implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(AggregateResolvedValueProducer.class);

  private int _pendingTasks = 1;
  private final List<ResolutionPump> _pumps = new ArrayList<ResolutionPump>();
  private boolean _deferredPump;

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
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.debug("Failed for {}", value);
    storeFailure(failure);
    boolean deferredPump;
    synchronized (this) {
      _pendingTasks--;
      deferredPump = _deferredPump;
      if (deferredPump) {
        _deferredPump = false;
      } else {
        // If finished; do a deferred pump to complete
        if (_pendingTasks < 1) {
          deferredPump = true;
        }
      }
    }
    if (deferredPump) {
      s_logger.debug("Running deferred pump");
      pumpImpl(context);
    }
  }

  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
    s_logger.debug("Received {} for {}", value, valueRequirement);
    if (pushResult(context, value)) {
      boolean deferredPump;
      synchronized (this) {
        _pendingTasks--;
        _pumps.add(pump);
        deferredPump = _deferredPump;
        if (deferredPump) {
          _deferredPump = false;
        }
      }
      if (deferredPump) {
        s_logger.debug("Running deferred pump");
        pumpImpl(context);
      }
    } else {
      context.pump(pump);
    }
  }

  @Override
  protected void pumpImpl(final GraphBuildingContext context) {
    Collection<ResolutionPump> pumps = null;
    synchronized (this) {
      if (getPendingTasks() < 1) {
        pumps = new ArrayList<ResolutionPump>(_pumps);
        s_logger.debug("Pumping {}", pumps);
        _pumps.clear();
      } else {
        s_logger.debug("Deferring pump while {} task(s) pending", getPendingTasks());
        _deferredPump = true;
      }
    }
    if (pumps != null) {
      if (pumps.isEmpty()) {
        // We have nothing to pump, so must have failed
        finished(context);
      } else {
        synchronized (this) {
          s_logger.debug("Pumping {} origin tasks", pumps.size());
          setPendingTasks(pumps.size());
        }
        for (ResolutionPump pump : pumps) {
          context.pump(pump);
        }
      }
    }
  }

  public void addProducer(final GraphBuildingContext context, final ResolvedValueProducer producer) {
    synchronized (this) {
      _pendingTasks++;
      s_logger.debug("pendingTasks={}", _pendingTasks);
    }
    producer.addCallback(context, this);
  }

  public void start(final GraphBuildingContext context) {
    Collection<ResolutionPump> pumps = null;
    synchronized (this) {
      if (--_pendingTasks == 0) {
        if (_pumps.isEmpty()) {
          pumps = Collections.emptySet();
        } else {
          _deferredPump = false;
          pumps = new ArrayList<ResolutionPump>(_pumps);
          _pumps.clear();
        }
      }
    }
    if (pumps != null) {
      if (pumps.isEmpty()) {
        finished(context);
      } else {
        synchronized (this) {
          s_logger.debug("Pumping {} origin tasks", pumps.size());
          setPendingTasks(pumps.size());
        }
        for (ResolutionPump pump : pumps) {
          context.pump(pump);
        }
      }
    }

  }

  @Override
  public String toString() {
    return "AGGREGATE" + getObjectId();
  }

}
