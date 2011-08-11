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
import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.Cancellable;

/* package */final class FunctionApplicationWorker extends AbstractResolvedValueProducer implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionApplicationWorker.class);

  private final Map<ValueRequirement, ValueSpecification> _inputs = new HashMap<ValueRequirement, ValueSpecification>();
  private final Map<ValueRequirement, Cancellable> _inputHandles = new HashMap<ValueRequirement, Cancellable>();
  private final Collection<ResolutionPump> _pumps = new ArrayList<ResolutionPump>();
  private int _pendingInputs;
  private int _validInputs;
  private FunctionApplicationStep.PumpingState _taskState;

  public FunctionApplicationWorker(final ValueRequirement valueRequirement) {
    super(valueRequirement);
  }

  @Override
  protected void pumpImpl(final GraphBuildingContext context) {
    Collection<ResolutionPump> pumps = null;
    FunctionApplicationStep.PumpingState finished = null;
    synchronized (this) {
      if (_pendingInputs < 1) {
        if (_pumps.isEmpty()) {
          if (_validInputs == 0) {
            s_logger.debug("{} finished (state={})", this, _taskState);
            finished = _taskState;
            _taskState = null;
          } else {
            s_logger.debug("{} inputs valid and not in pumped state", _validInputs);
          }
        } else {
          pumps = new ArrayList<ResolutionPump>(_pumps);
          _pumps.clear();
          _pendingInputs = pumps.size();
        }
      } else {
        s_logger.debug("Ignoring pump while {} input(s) pending", _pendingInputs);
      }
    }
    if (pumps != null) {
      for (ResolutionPump pump : pumps) {
        s_logger.debug("Pumping {} from {}", pump, this);
        context.pump(pump);
      }
    } else if (finished != null) {
      if (finished(context)) {
        s_logger.debug("Calling finished on {} from {}", finished, this);
        finished.finished(context);
      }
    }
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.debug("Resolution of {} failed at {}", value, this);
    final FunctionApplicationStep.PumpingState state;
    final Collection<Cancellable> unsubscribes;
    ResolutionFailure requirementFailure = null;
    synchronized (this) {
      _inputHandles.remove(value);
      _pendingInputs--;
      _validInputs--;
      if (_inputs.get(value) == null) {
        s_logger.info("Resolution of {} failed", value);
        if (_taskState != null) {
          requirementFailure = _taskState.functionApplication().requirement(value, failure);
        }
      } else {
        if ((_pendingInputs > 0) || (_validInputs > 0)) {
          // Ok; we've already had some values for this requirement and there are others pending or still valid
          s_logger.debug("PendingInputs={}, ValidInputs={}", _pendingInputs, _validInputs);
          return;
        }
      }
      if (_inputHandles.isEmpty()) {
        unsubscribes = null;
      } else {
        unsubscribes = new ArrayList<Cancellable>(_inputHandles.values());
      }
      state = _taskState;
      _taskState = null;
    }
    // Not ok; we either couldn't satisfy anything or the pumped enumeration is complete
    s_logger.info("{} complete", this);
    if (state != null) {
      state.storeFailure(requirementFailure);
    }
    storeFailure(requirementFailure);
    // Unsubscribe from any inputs that are still valid
    if (unsubscribes != null) {
      s_logger.debug("Unsubscribing from {} handles", unsubscribes.size());
      for (Cancellable handle : unsubscribes) {
        if (handle != null) {
          handle.cancel(false);
        }
      }
    }
    // Propagate the failure message to anything subscribing to us
    if (state != null) {
      if (finished(context)) {
        s_logger.debug("Calling finished on {}", state);
        state.finished(context);
      }
    }
  }

  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
    s_logger.debug("Resolution complete at {}", this);
    s_logger.info("Resolved {} to {}", valueRequirement, resolvedValue);
    FunctionApplicationStep.PumpingState state = null;
    Map<ValueSpecification, ValueRequirement> resolvedValues = null;
    synchronized (this) {
      if (_taskState != null) {
        _inputs.put(valueRequirement, resolvedValue.getValueSpecification());
        if (--_pendingInputs == 0) {
          // We've got a full set of inputs; notify the task state
          resolvedValues = Maps.<ValueSpecification, ValueRequirement>newHashMapWithExpectedSize(_inputs.size());
          for (Map.Entry<ValueRequirement, ValueSpecification> input : _inputs.entrySet()) {
            resolvedValues.put(input.getValue(), input.getKey());
          }
          state = _taskState;
          s_logger.debug("Full input set available");
        } else {
          s_logger.debug("Waiting for {} other inputs in {}", _pendingInputs, _inputs);
        }
        _pumps.add(pump);
      } else {
        s_logger.debug("Already aborted resolution");
      }
    }
    if (resolvedValues != null) {
      if (!state.inputsAvailable(context, resolvedValues)) {
        pumpImpl(context);
      }
    }
  }

  @Override
  protected void storeFailure(final ResolutionFailure failure) {
    final FunctionApplicationStep.PumpingState state;
    synchronized (this) {
      state = _taskState;
    }
    if (state != null) {
      state.storeFailure(failure);
    }
    super.storeFailure(failure);
  }

  public void addInput(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValueProducer inputProducer) {
    s_logger.debug("Adding input {} to {}", valueRequirement, this);
    synchronized (this) {
      _inputs.put(valueRequirement, null);
      _inputHandles.put(valueRequirement, null);
      _pendingInputs++;
    }
    final Cancellable handle = inputProducer.addCallback(context, this);
    synchronized (this) {
      if (handle != null) {
        // Only store the handle if the producer hasn't already failed
        if (_inputHandles.containsKey(valueRequirement)) {
          _inputHandles.put(valueRequirement, handle);
        }
      } else {
        // Remove the handle placeholder if the producer didn't give us a handle 
        _inputHandles.remove(valueRequirement);
      }
    }
  }

  public void setPumpingState(final FunctionApplicationStep.PumpingState state, final int validInputs) {
    synchronized (this) {
      _taskState = state;
      _validInputs = validInputs;
      _pendingInputs = (validInputs > 0) ? 1 : 0;
    }
  }

  public void start(final GraphBuildingContext context) {
    FunctionApplicationStep.PumpingState state = null;
    Map<ValueSpecification, ValueRequirement> resolvedValues = null;
    Collection<ResolutionPump> pumps = null;
    FunctionApplicationStep.PumpingState finished = null;
    synchronized (this) {
      if (_taskState != null) {
        if (--_pendingInputs == 0) {
          if (_validInputs > 0) {
            // We've got a full set of inputs; notify the task state
            resolvedValues = Maps.<ValueSpecification, ValueRequirement>newHashMapWithExpectedSize(_inputs.size());
            for (Map.Entry<ValueRequirement, ValueSpecification> input : _inputs.entrySet()) {
              resolvedValues.put(input.getValue(), input.getKey());
            }
            state = _taskState;
            s_logger.debug("Full input set available at {}", this);
          } else {
            // We've got no valid inputs
            if (_pumps.isEmpty()) {
              s_logger.debug("No input set available, finished at {}", this);
              pumps = null;
              finished = _taskState;
              _taskState = null;
            } else {
              s_logger.debug("No input set available, pumping at {}", this);
              pumps = new ArrayList<ResolutionPump>(_pumps);
              _pumps.clear();
              _pendingInputs = pumps.size();
            }
          }
        }
      } else {
        s_logger.debug("Already aborted resolution");
      }
    }
    if (resolvedValues != null) {
      if (!state.inputsAvailable(context, resolvedValues)) {
        pumpImpl(context);
      }
    } else if (pumps != null) {
      for (ResolutionPump pump : pumps) {
        s_logger.debug("Pumping {} from {}", pump, this);
        context.pump(pump);
      }
    } else if (finished != null) {
      s_logger.debug("Calling finished on {} from {}", finished, this);
      finished.finished(context);
    }
  }

  @Override
  public String toString() {
    return "Worker" + getObjectId() + "[" + getValueRequirement() + "]";
  }

}
