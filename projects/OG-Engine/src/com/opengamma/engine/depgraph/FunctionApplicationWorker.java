/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/* package */final class FunctionApplicationWorker extends AbstractResolvedValueProducer implements ResolvedValueCallback {

  private final Map<ValueRequirement, ValueSpecification> _inputs = new HashMap<ValueRequirement, ValueSpecification>();
  private final Collection<ResolutionPump> _pumps = new ArrayList<ResolutionPump>();
  private int _pendingInputs;
  private int _validInputs = 1;
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
    synchronized (this) {
      _pendingInputs--;
      _validInputs--;
      if (((_pendingInputs > 0) || (_validInputs > 0)) && (_inputs.get(value) != null)) {
        // Ok; we've already had some values for this requirement and there are others pending or still valid
        return;
      }
    }
    // Not ok; we either couldn't satisfy anything or the pumped enumeration is complete
    finished();
    _taskState.finished();
    // Help out the GC; nothing needs the state any more
    _taskState = null;
  }

  @Override
  public void resolved(final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
    Set<ValueSpecification> resolvedValues = null;
    synchronized (this) {
      _inputs.put(valueRequirement, resolvedValue.getValueSpecification());
      if (--_pendingInputs == 0) {
        // We've got a full set of inputs; notify the task state
        resolvedValues = new HashSet<ValueSpecification>(_inputs.values());
      }
      _pumps.add(pump);
    }
    if (resolvedValue != null) {
      _taskState.inputsAvailable(resolvedValues);
    }
  }

  public void addInput(final ValueRequirement valueRequirement, final ResolvedValueProducer inputProducer) {
    synchronized (this) {
      _inputs.put(valueRequirement, null);
      _pendingInputs++;
      _validInputs++;
    }
    inputProducer.addCallback(this);
  }

  public void setPumpingState(final FunctionApplicationStep.PumpingState state) {
    synchronized (this) {
      _taskState = state;
      if (--_validInputs > 0) {
        // At least one valid input so carry on
        return;
      }
    }
    // No valid inputs, so we've finished the enumeration
    finished();
    _taskState.finished();
    // Help out the GC; nothing needs the state any more
    _taskState = null;
  }

}
