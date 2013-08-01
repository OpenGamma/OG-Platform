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
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/* package */final class FunctionApplicationWorker extends DirectResolvedValueProducer implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(FunctionApplicationWorker.class);

  private Map<ValueRequirement, ValueSpecification> _inputs = new HashMap<ValueRequirement, ValueSpecification>();
  private Map<ValueRequirement, Cancelable> _inputHandles = new HashMap<ValueRequirement, Cancelable>();
  private Collection<ResolutionPump> _pumps = new ArrayList<ResolutionPump>();
  private int _pendingInputs;
  private int _validInputs;
  private boolean _invokingFunction;
  private boolean _deferredPump;
  private FunctionApplicationStep.PumpingState _taskState;

  public FunctionApplicationWorker(final ValueRequirement valueRequirement) {
    super(valueRequirement);
  }

  @Override
  protected void pumpImpl(final GraphBuildingContext context) {
    ResolutionPump[] pumps = null;
    FunctionApplicationStep.PumpingState finished = null;
    int refCount = 0;
    synchronized (this) {
      if (_invokingFunction) {
        s_logger.debug("Deferring pump until after function invocation");
        _deferredPump = true;
        return;
      }
      if (_pendingInputs < 1) {
        if ((_pumps == null) || _pumps.isEmpty()) {
          s_logger.debug("{} finished (state={})", this, _taskState);
          finished = _taskState;
          _taskState = null;
          refCount = getRefCount();
        } else {
          final int s = _pumps.size();
          pumps = _pumps.toArray(new ResolutionPump[s]);
          _pumps.clear();
          _pendingInputs = s;
        }
      } else {
        // If the state performs a direct pump as well as the trigger from AbstractResolvedValueProducer
        // on behalf of a subscriber, no action is needed.
        return;
      }
    }
    if (pumps != null) {
      for (ResolutionPump pump : pumps) {
        s_logger.debug("Pumping {} from {}", pump, this);
        context.pump(pump);
      }
    } else if (finished != null) {
      s_logger.debug("Finished producing function applications from {}", this);
      // If the last result was already pushed then finished will have already been called
      if (!isFinished()) {
        finished(context);
      }
      s_logger.debug("Calling finished on {} from {}", finished, this);
      finished.finished(context, refCount);
    }
  }

  @Override
  protected void finished(final GraphBuildingContext context) {
    super.finished(context);
    Collection<Cancelable> unsubscribes = null;
    Collection<ResolutionPump> pumps = null;
    synchronized (this) {
      _inputs = null;
      if (_inputHandles != null) {
        if (!_inputHandles.isEmpty()) {
          unsubscribes = _inputHandles.values();
        }
        _inputHandles = null;
      }
      if (_pumps != null) {
        if (!_pumps.isEmpty()) {
          pumps = _pumps;
        }
        _pumps = null;
      }
    }
    if (unsubscribes != null) {
      for (Cancelable unsubscribe : unsubscribes) {
        if (unsubscribe != null) {
          unsubscribe.cancel(context);
        }
      }
    }
    if (pumps != null) {
      for (ResolutionPump pump : pumps) {
        context.close(pump);
      }
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
      final boolean lastResult;
      synchronized (this) {
        if (_pumps == null) {
          // Already aborted/finished
          return;
        }
        lastResult = _pumps.isEmpty();
      }
      boolean inputsAccepted = state.inputsAvailable(context, resolvedValues, lastResult);
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
    int refCount = 0;
    ResolutionFailure requirementFailure = null;
    final Map<ValueSpecification, ValueRequirement> resolvedValues;
    do {
      final Collection<Cancelable> unsubscribes;
      synchronized (this) {
        _pendingInputs--;
        _validInputs--;
        if (_inputHandles != null) {
          _inputHandles.remove(value);
          if (_inputs.get(value) == null) {
            s_logger.info("Resolution of {} failed", value);
            if (_taskState != null) {
              if (_taskState.canHandleMissingInputs()) {
                state = _taskState;
                requirementFailure = state.functionApplication(context).requirement(value, null);
                _inputs.remove(value);
                if (_pendingInputs == 0) {
                  // Got as full a set of inputs as we're going to get; notify the task state
                  resolvedValues = createResolvedValuesMap();
                  _invokingFunction = true;
                  s_logger.debug("Partial input set available");
                } else {
                  resolvedValues = null;
                  if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Waiting for {} other inputs in {}", _pendingInputs, _inputs);
                  }
                  // Fall through so that failure is logged
                }
                break;
              } else {
                requirementFailure = _taskState.functionApplication(context).requirement(value, failure);
              }
            }
          } else {
            if (_taskState != null) {
              // Might be okay - we've already had at least one value for this requirement
              if (_pendingInputs > 0) {
                // Ok; still waiting on other inputs
                if (s_logger.isDebugEnabled()) {
                  s_logger.debug("{} other inputs still pending", _pendingInputs);
                }
                return;
              }
              if (_validInputs > 0) {
                // Ok; we've got other new values to push up
                state = _taskState;
                resolvedValues = createResolvedValuesMap();
                _invokingFunction = true;
                if (s_logger.isDebugEnabled()) {
                  s_logger.debug("Partial input set available on {} new inputs", _validInputs);
                }
                break;
              }
            }
          }
          if (_inputHandles.isEmpty()) {
            unsubscribes = null;
          } else {
            unsubscribes = _inputHandles.values();
          }
          _inputHandles = null;
        } else {
          unsubscribes = null;
        }
        state = _taskState;
        if (state != null) {
          _taskState = null;
          refCount = getRefCount();
        }
      }
      // Not ok; we either couldn't satisfy anything or the pumped enumeration is complete
      s_logger.info("{} complete", this);
      if (state != null) {
        state.storeFailure(requirementFailure);
      }
      storeFailure(requirementFailure);
      // Unsubscribe from any inputs that are still valid
      if (unsubscribes != null) {
        if (s_logger.isDebugEnabled()) {
          s_logger.debug("Unsubscribing from {} handles", unsubscribes.size());
        }
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
        state.finished(context, refCount);
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

  protected void discard(final GraphBuildingContext context) {
    Collection<Cancelable> unsubscribes = null;
    final Collection<ResolutionPump> pumps;
    synchronized (this) {
      _taskState = null;
      if (_inputs == null) {
        // Already discarded/finished
        return;
      }
      _inputs = null;
      if (_inputHandles != null) {
        if (!_inputHandles.isEmpty()) {
          unsubscribes = _inputHandles.values();
        }
        _inputHandles = null;
      }
      if (_pumps.isEmpty()) {
        pumps = null;
      } else {
        pumps = _pumps;
      }
      _pumps = null;
    }
    if (unsubscribes != null) {
      for (Cancelable unsubscribe : unsubscribes) {
        if (unsubscribe != null) {
          unsubscribe.cancel(context);
        }
      }
    }
    if (pumps != null) {
      for (ResolutionPump pump : pumps) {
        context.close(pump);
      }
    }
  }

  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, ResolutionPump pump) {
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
          if (s_logger.isDebugEnabled()) {
            s_logger.debug("Waiting for {} other inputs in {}", _pendingInputs, _inputs);
          }
        }
        if (pump != null) {
          _pumps.add(pump);
          pump = null;
        }
      } else {
        s_logger.debug("Already aborted resolution");
      }
    }
    if (pump != null) {
      context.close(pump);
    }
    inputsAvailable(context, state, resolvedValues);
  }

  @Override
  protected void storeFailure(final ResolutionFailure failure) {
    final FunctionApplicationStep.PumpingState state;
    synchronized (this) {
      if (_pumps == null) {
        // Already discarded; don't bother storing the failure information
        return;
      }
      state = _taskState;
    }
    if (state != null) {
      state.storeFailure(failure);
    }
    super.storeFailure(failure);
  }

  public void addInput(final GraphBuildingContext context, final ResolvedValueProducer inputProducer) {
    final ValueRequirement valueRequirement = inputProducer.getValueRequirement();
    s_logger.debug("Adding input {} to {}", valueRequirement, this);
    synchronized (this) {
      if ((_inputs == null) || (_inputHandles == null)) {
        // Already aborted or something has already failed
        _validInputs--;
        return;
      }
      _inputs.put(valueRequirement, null);
      _inputHandles.put(valueRequirement, null);
      _pendingInputs++;
    }
    final Cancelable handle = inputProducer.addCallback(context, this);
    synchronized (this) {
      if (_inputHandles != null) {
        if (handle != null) {
          // Only store the handle if the producer hasn't already failed
          if (_inputHandles.containsKey(valueRequirement)) {
            _inputHandles.put(valueRequirement, handle);
          }
        } else {
          // Remove the handle placeholder if the producer didn't give us a handle 
          _inputHandles.remove(valueRequirement);
        }
        return;
      }
    }
    if (handle != null) {
      handle.cancel(context);
    }
  }

  @Override
  public void recursionDetected() {
    // No-op
  }

  @Override
  protected void setRecursionDetected() {
    super.setRecursionDetected();
    synchronized (this) {
      _validInputs--;
    }
  }

  public void setPumpingState(final FunctionApplicationStep.PumpingState state, final int validInputs) {
    synchronized (this) {
      _taskState = state;
      _validInputs = validInputs;
      _pendingInputs = (validInputs > 0) ? 1 : 0;
      final int rc = getRefCount();
      for (int i = 0; i < rc; i++) {
        state.getTask().addRef();
      }
    }
  }

  public void start(final GraphBuildingContext context) {
    s_logger.debug("Starting {}", this);
    FunctionApplicationStep.PumpingState state = null;
    Map<ValueSpecification, ValueRequirement> resolvedValues = null;
    FunctionApplicationStep.PumpingState finished = null;
    int refCount = 0;
    boolean lastResult = false;
    synchronized (this) {
      if (_taskState != null) {
        if (--_pendingInputs == 0) {
          if (_validInputs > 0) {
            // We've got a full set of inputs; notify the task state
            resolvedValues = createResolvedValuesMap();
            lastResult = _pumps.isEmpty();
            state = _taskState;
            _invokingFunction = true;
            s_logger.debug("Full input set available at {}", this);
          } else {
            // We've got no valid inputs
            assert _pumps.isEmpty();
            if (_taskState.canHandleMissingInputs()) {
              // Empty input set; notify the task state
              resolvedValues = Collections.emptyMap();
              lastResult = true;
              state = _taskState;
              _invokingFunction = true;
              s_logger.debug("Empty input set available at {}", this);
            } else {
              s_logger.debug("No input set available, finished at {}", this);
              finished = _taskState;
              _taskState = null;
              refCount = getRefCount();
            }
          }
        }
      } else {
        s_logger.debug("Already aborted resolution");
      }
    }
    if (resolvedValues != null) {
      boolean inputsAccepted = state.inputsAvailable(context, resolvedValues, lastResult);
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
      s_logger.debug("Calling finished on {} from {}", finished, this);
      finished(context);
      finished.finished(context, refCount);
    }
  }

  @Override
  public String toString() {
    return "Worker" + getObjectId() + "[" + getValueRequirement() + "]";
  }

  @Override
  public synchronized boolean addRef() {
    if (super.addRef()) {
      if (_taskState != null) {
        _taskState.getTask().addRef();
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int release(final GraphBuildingContext context) {
    final int count;
    FunctionApplicationStep.PumpingState state;
    // If setPumpingState is scheduled here, the task will include the caller's open-reference in its refCount. We'll see the
    // non-null state and issue a net release.
    synchronized (this) {
      state = _taskState;
      if (state != null) {
        // Hold an open reference 
        state.getTask().addRef();
      }
      count = super.release(context);
    }
    // If setPumpingState is scheduled here, the task will not include the caller's open-reference in its refCount. We'll see
    // the null state however and take no action.
    if (state != null) {
      state.getTask().release(context);
      synchronized (this) {
        state = _taskState;
      }
      // If the finished state was entered (state == null), the task will have seen a full set of releases from there plus the
      // explicit one above. The explicit one will balance against the reference we added before starting. If the finished state
      // was not entered then we need to clear the reference we added above.
      if (state != null) {
        state.getTask().release(context);
      }
    }
    return count;
  }

  @Override
  public int cancelLoopMembers(final GraphBuildingContext context, final Map<Chain, Chain.LoopState> visited) {
    FunctionApplicationStep.PumpingState state;
    synchronized (this) {
      state = _taskState;
    }
    int result = super.cancelLoopMembers(context, visited);
    if (state != null) {
      result += state.cancelLoopMembersImpl(context, visited);
    }
    return result;
  }

}
