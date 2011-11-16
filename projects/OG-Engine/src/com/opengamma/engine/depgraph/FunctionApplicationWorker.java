/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.depgraph.DependencyGraphBuilder.GraphBuildingContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/* package */final class FunctionApplicationWorker extends AbstractResolvedValueProducer implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionApplicationWorker.class);

  private final Map<ValueRequirement, ValueSpecification> _inputs = new HashMap<ValueRequirement, ValueSpecification>();
  private final Map<ValueRequirement, Cancelable> _inputHandles = new HashMap<ValueRequirement, Cancelable>();
  private final Collection<ResolutionPump> _pumps = new ArrayList<ResolutionPump>();
  private int _pendingInputs;
  private int _validInputs;
  private boolean _invokingFunction;
  private boolean _deferredPump;
  private FunctionApplicationStep.PumpingState _taskState;
  private boolean _closed;

  public FunctionApplicationWorker(final ValueRequirement valueRequirement) {
    super(valueRequirement);
  }

  @Override
  protected void pumpImpl(final GraphBuildingContext context) {
    Collection<ResolutionPump> pumps = null;
    FunctionApplicationStep.PumpingState finished = null;
    synchronized (this) {
      if (_invokingFunction) {
        s_logger.debug("Deferring pump until after function invocation");
        _deferredPump = true;
        return;
      }
      if (_pendingInputs < 1) {
        if (_pumps.isEmpty()) {
          if (_validInputs == 0) {
            s_logger.debug("{} finished (state={})", this, _taskState);
            finished = _taskState;
            _taskState = null;
          } else {
            // Can this happen? Should we just assert validInputs == 0?
            s_logger.error("{} inputs valid and not in pumped state", _validInputs);
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
      s_logger.debug("Finished producing function applications from {}", this);
      finished(context);
      s_logger.debug("Calling finished on {} from {}", finished, this);
      finished.finished(context);
    }
  }

  // Caller must hold the monitor
  private Map<ValueSpecification, ValueRequirement> createResolvedValuesMap() {
    final Map<ValueSpecification, ValueRequirement> resolvedValues = Maps.<ValueSpecification, ValueRequirement>newHashMapWithExpectedSize(_inputs.size());
    for (Map.Entry<ValueRequirement, ValueSpecification> input : _inputs.entrySet()) {
      assert input.getValue() != null;
      resolvedValues.put(input.getValue(), input.getKey());
    }
    return resolvedValues;
  }

  private void inputsAvailable(final GraphBuildingContext context, final FunctionApplicationStep.PumpingState state, final Map<ValueSpecification, ValueRequirement> resolvedValues) {
    if (resolvedValues != null) {
      boolean inputsAccepted = state.inputsAvailable(context, resolvedValues);
      synchronized (this) {
        _invokingFunction = false;
        if (_deferredPump) {
          inputsAccepted = false;
          _deferredPump = false;
        }
      }
      if (!inputsAccepted) {
        pumpImpl(context);
      }
    }
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.debug("Resolution of {} failed at {}", value, this);
    FunctionApplicationStep.PumpingState state = null;
    ResolutionFailure requirementFailure = null;
    final Map<ValueSpecification, ValueRequirement> resolvedValues;
    do {
      final Collection<Cancelable> unsubscribes;
      synchronized (this) {
        _inputHandles.remove(value);
        _pendingInputs--;
        _validInputs--;
        if (_inputs.get(value) == null) {
          s_logger.info("Resolution of {} failed", value);
          if (_taskState != null) {
            requirementFailure = _taskState.functionApplication().requirement(value, failure);
            if (_taskState.canHandleMissingInputs()) {
              _inputs.remove(value);
              state = _taskState;
              if (_pendingInputs == 0) {
                // Got as full a set of inputs as we're going to get; notify the task state
                resolvedValues = createResolvedValuesMap();
                _invokingFunction = true;
                s_logger.debug("Partial input set available");
              } else {
                resolvedValues = null;
                s_logger.debug("Waiting for {} other inputs in {}", _pendingInputs, _inputs);
                // Fall through so that the failure is still logged
              }
              break;
            }
          }
        } else {
          if (_taskState != null) {
            // Might be okay - we've already had at least one value for this requirement
            if (_pendingInputs > 0) {
              // Ok; still waiting on other inputs
              s_logger.debug("{} other inputs still pending", _pendingInputs);
              return;
            }
            if (_validInputs > 0) {
              // Ok; we've got other new values to push up
              state = _taskState;
              resolvedValues = createResolvedValuesMap();
              _invokingFunction = true;
              s_logger.debug("Partial input set available on {} new inputs", _validInputs);
              break;
            }
          }
        }
        if (_inputHandles.isEmpty()) {
          unsubscribes = null;
        } else {
          unsubscribes = new ArrayList<Cancelable>(_inputHandles.values());
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
        for (Cancelable handle : unsubscribes) {
          if (handle != null) {
            handle.cancel(context);
          }
        }
      }
      // Propagate the failure message to anything subscribing to us
      if (state != null) {
        finished(context);
        s_logger.debug("Calling finished on {}", state);
        state.finished(context);
      }
      return;
    } while (false);
    // Ok; partial results may be available
    if (state != null) {
      state.storeFailure(requirementFailure);
    }
    storeFailure(requirementFailure);
    inputsAvailable(context, state, resolvedValues);
  }

  protected void abort(final GraphBuildingContext context) {
    final FunctionApplicationStep.PumpingState state;
    synchronized (this) {
      state = _taskState;
      _taskState = null;
    }
    if (state != null) {
      s_logger.debug("Aborting worker {}", this);
      finished(context);
    } else {
      s_logger.debug("Ignoring abort call {}", this);
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
          // We've got a full (or partial) set of inputs; notify the task state
          resolvedValues = createResolvedValuesMap();
          state = _taskState;
          _invokingFunction = true;
          s_logger.debug("Full input set available");
        } else {
          s_logger.debug("Waiting for {} other inputs in {}", _pendingInputs, _inputs);
        }
        _pumps.add(pump);
      } else {
        s_logger.debug("Already aborted resolution");
      }
    }
    inputsAvailable(context, state, resolvedValues);
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
    final Cancelable handle = inputProducer.addCallback(context, this);
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
    s_logger.debug("Starting {}", this);
    FunctionApplicationStep.PumpingState state = null;
    Map<ValueSpecification, ValueRequirement> resolvedValues = null;
    FunctionApplicationStep.PumpingState finished = null;
    synchronized (this) {
      if (_taskState != null) {
        if (--_pendingInputs == 0) {
          if (_validInputs > 0) {
            // We've got a full set of inputs; notify the task state
            resolvedValues = createResolvedValuesMap();
            state = _taskState;
            _invokingFunction = true;
            s_logger.debug("Full input set available at {}", this);
          } else {
            // We've got no valid inputs
            assert _pumps.isEmpty();
            if (_taskState.canHandleMissingInputs()) {
              // Empty input set; notify the task state
              resolvedValues = Collections.emptyMap();
              state = _taskState;
              _invokingFunction = true;
              s_logger.debug("Empty input set available at {}", this);
            } else {
              s_logger.debug("No input set available, finished at {}", this);
              finished = _taskState;
              _taskState = null;
            }
          }
        }
      } else {
        s_logger.debug("Already aborted resolution");
      }
    }
    if (resolvedValues != null) {
      boolean inputsAccepted = state.inputsAvailable(context, resolvedValues);
      synchronized (this) {
        _invokingFunction = false;
        if (_deferredPump) {
          inputsAccepted = false;
          _deferredPump = false;
        }
      }
      if (!inputsAccepted) {
        pumpImpl(context);
      }
    } else if (finished != null) {
      // TODO: should we call "finished" here ?
      s_logger.debug("Calling finished on {} from {}", finished, this);
      finished.finished(context);
    }
  }

  @Override
  public String toString() {
    return "Worker" + getObjectId() + "[" + getValueRequirement() + "]";
  }

  @Override
  public int release(final GraphBuildingContext context) {
    final int count = super.release(context);
    if (count == 1) {
      // Last reference left will be from the state object
      _closed = true;
    }
    return count;
  }

  /**
   * {@see ResolveTask.State#isActive}
   */
  protected boolean isActive() {
    return !_closed;
  }

}
