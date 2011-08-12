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

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.debug("Failed for {}", value);
    storeFailure(failure);
    Collection<ResolutionPump> pumps = null;
    synchronized (this) {
      assert _pendingTasks > 0;
      _pendingTasks--;
      s_logger.debug("{} pending tasks", _pendingTasks);
      if (getPendingTasks() < 1) {
        _deferredPump = false;
        pumps = pumpImpl();
      }
    }
    pumpImpl(context, pumps);
  }

  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue value, final ResolutionPump pump) {
    s_logger.debug("Received {} for {}", value, valueRequirement);
    if (pushResult(context, value)) {
      Collection<ResolutionPump> pumps = null;
      synchronized (this) {
        assert _pendingTasks > 0;
        _pendingTasks--;
        _pumps.add(pump);
        if (_deferredPump) {
          if (getPendingTasks() < 1) {
            _deferredPump = false;
            pumps = pumpImpl();
          }
        }
      }
      pumpImpl(context, pumps);
    } else {
      context.pump(pump);
    }
  }

  @Override
  protected void pumpImpl(final GraphBuildingContext context) {
    Collection<ResolutionPump> pumps = null;
    synchronized (this) {
      assert _pendingTasks >= 0;
      if (_pendingTasks < 1) {
        pumps = pumpImpl();
      } else {
        s_logger.debug("Deferring pump while {} task(s) pending", getPendingTasks());
        _deferredPump = true;
      }
    }
    pumpImpl(context, pumps);
  }

  // Caller must hold the monitor
  private Collection<ResolutionPump> pumpImpl() {
    if (_pumps.isEmpty()) {
      return Collections.emptyList();
    } else {
      final List<ResolutionPump> pumps = new ArrayList<ResolutionPump>(_pumps);
      _pumps.clear();
      _pendingTasks = pumps.size();
      return pumps;
    }
  }

  private void pumpImpl(final GraphBuildingContext context, final Collection<ResolutionPump> pumps) {
    if (pumps != null) {
      if (pumps.isEmpty()) {
        // We have nothing to pump, so must have finished (failed)
        s_logger.debug("Finished {}", this);
        finished(context);
      } else {
        s_logger.debug("Pumping {} origin tasks", pumps.size());
        for (ResolutionPump pump : pumps) {
          context.pump(pump);
        }
      }
    }
  }

  public void addProducer(final GraphBuildingContext context, final ResolvedValueProducer producer) {
    synchronized (this) {
      assert _pendingTasks >= 0;
      _pendingTasks++;
      s_logger.debug("{} pending tasks for {}", _pendingTasks, this);
    }
    producer.addCallback(context, this);
  }

  public void start(final GraphBuildingContext context) {
    Collection<ResolutionPump> pumps = null;
    synchronized (this) {
      assert _pendingTasks > 0;
      if (--_pendingTasks == 0) {
        if (_deferredPump) {
          _deferredPump = false;
          pumps = pumpImpl();
        } else if (_pumps.isEmpty()) {
          pumps = Collections.emptyList();
        }
      }
    }
    pumpImpl(context, pumps);
  }

  @Override
  protected boolean finished(final GraphBuildingContext context) {
    synchronized (this) {
      if (_pendingTasks > 0) {
        s_logger.debug("Another thread has attached another producer - {} pending task(s)", _pendingTasks);
        return false;
      }
    }
    return super.finished(context);
  }

  @Override
  public int release(final GraphBuildingContext context) {
    final int count = super.release(context);
    if (count == 0) {
      List<ResolutionPump> pumps;
      synchronized (this) {
        s_logger.debug("Releasing {} - with {} pumped inputs", this, _pumps.size());
        if (_pumps.isEmpty()) {
          return count;
        }
        pumps = new ArrayList<ResolutionPump>(_pumps);
        _pumps.clear();
      }
      for (ResolutionPump pump : pumps) {
        context.close(pump);
      }
    }
    return count;
  }

  @Override
  public String toString() {
    return "AGGREGATE" + getObjectId();
  }

}
