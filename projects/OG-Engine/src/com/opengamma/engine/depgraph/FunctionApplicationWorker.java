/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/* package */final class FunctionApplicationWorker extends AbstractResolvedValueProducer implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionApplicationWorker.class);

  private final Map<ValueRequirement, ValueSpecification> _inputs = new HashMap<ValueRequirement, ValueSpecification>();
  private final Collection<ResolutionPump> _pumps = new ArrayList<ResolutionPump>();
  private int _pendingInputs;
  private int _validInputs;
  private FunctionApplicationStep.PumpingState _taskState;

  public FunctionApplicationWorker(final ValueRequirement valueRequirement) {
    super(valueRequirement);
  }

  @Override
  protected void pumpImpl() {
    final Collection<ResolutionPump> pumps;
    synchronized (this) {
      if (_pumps.isEmpty()) {
        pumps = null;
      } else {
        pumps = new ArrayList<ResolutionPump>(_pumps);
        _pumps.clear();
        _pendingInputs = pumps.size();
      }
    }
    if (pumps != null) {
      for (ResolutionPump pump : pumps) {
        pump.pump();
      }
    }
  }

  @Override
  public void failed(final ValueRequirement value) {
    s_logger.info("Resolution of {} failed", value);
    FunctionApplicationStep.PumpingState state;
    synchronized (this) {
      _pendingInputs--;
      _validInputs--;
      if (((_pendingInputs > 0) || (_validInputs > 0)) && (_inputs.get(value) != null)) {
        // Ok; we've already had some values for this requirement and there are others pending or still valid
        s_logger.debug("PendingInputs={}, ValidInputs={}", _pendingInputs, _validInputs);
        return;
      }
      state = _taskState;
      _taskState = null;
    }
    // Not ok; we either couldn't satisfy anything or the pumped enumeration is complete
    s_logger.info("Worker complete");
    finished();
    state.finished();
  }

  @Override
  public void resolved(final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
    s_logger.info("Resolved {} to {}", valueRequirement, resolvedValue);
    FunctionApplicationStep.PumpingState state = null;
    Map<ValueSpecification, ValueRequirement> resolvedValues = null;
    synchronized (this) {
      _inputs.put(valueRequirement, resolvedValue.getValueSpecification());
      if (--_pendingInputs == 0) {
        // We've got a full set of inputs; notify the task state
        resolvedValues = Maps.<ValueSpecification, ValueRequirement>newHashMapWithExpectedSize(_inputs.size());
        for (Map.Entry<ValueRequirement, ValueSpecification> input : _inputs.entrySet()) {
          resolvedValues.put(input.getValue(), input.getKey());
        }
        state = _taskState;
      }
      _pumps.add(pump);
    }
    if (resolvedValue != null) {
      state.inputsAvailable(resolvedValues);
    }
  }

  public void addInput(final ValueRequirement valueRequirement, final ResolvedValueProducer inputProducer) {
    synchronized (this) {
      _inputs.put(valueRequirement, null);
      _pendingInputs++;
    }
    inputProducer.addCallback(this);
  }

  public void setPumpingState(final FunctionApplicationStep.PumpingState state, final int validInputs) {
    synchronized (this) {
      _taskState = state;
      _validInputs = validInputs;
    }
  }

}
