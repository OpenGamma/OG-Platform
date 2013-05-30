/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/* package */abstract class AbstractResolvedValueProducer implements ResolvedValueProducer, ResolvedValueProducer.Chain {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractResolvedValueProducer.class);
  private static final AtomicInteger s_nextObjectId = new AtomicInteger();

  private final class Callback implements ResolutionPump, Cancelable {

    private final int _objectId = s_nextObjectId.getAndIncrement();
    private final ResolvedValueCallback _callback;
    private ResolvedValue[] _results;
    private int _resultsPushed;

    public Callback(final ResolvedValueCallback callback) {
      _callback = callback;
      // Don't reference count the callback; the interface doesn't support it and if it is something like an
      // AggregateResolvedValueProducer then counting references in this direction will introduce a loop and
      // prevent dead tasks from being identified
    }

    @Override
    public void pump(final GraphBuildingContext context) {
      s_logger.debug("Pump called on {}", this);
      ResolvedValue nextValue = null;
      boolean finished = false;
      ResolutionFailure failure = null;
      boolean needsOuterPump = false;
      synchronized (this) {
        if (_resultsPushed < _results.length - 1) {
          nextValue = _results[_resultsPushed++];
        }
      }
      if (nextValue == null) {
        synchronized (AbstractResolvedValueProducer.this) {
          synchronized (this) {
            _results = AbstractResolvedValueProducer.this._results;
            if (_resultsPushed < _results.length) {
              nextValue = _results[_resultsPushed++];
              if (_finished && (_resultsPushed == _results.length)) {
                finished = true;
              }
            } else {
              if (_finished) {
                finished = true;
                failure = _failure;
              } else {
                if (_pumped != null) {
                  needsOuterPump = _pumped.isEmpty();
                  if (s_logger.isDebugEnabled()) {
                    if (needsOuterPump) {
                      s_logger.debug("Pumping outer object");
                    } else {
                      s_logger.debug("Adding to pump set");
                    }
                  }
                  if (needsOuterPump) {
                    _pumping = 1;
                  }
                  _pumped.add(this);
                } else {
                  finished = true;
                  failure = _failure;
                }
              }
            }
          }
        }
      }
      if (nextValue != null) {
        if (finished) {
          s_logger.debug("Publishing final value {}", nextValue);
          release(context); // the reference added by addCallback
          context.resolved(_callback, getValueRequirement(), nextValue, null);
        } else {
          s_logger.debug("Publishing value {}", nextValue);
          context.resolved(_callback, getValueRequirement(), nextValue, this);
        }
      } else {
        if (needsOuterPump) {
          pumpImpl(context);
          boolean lastResult = false;
          Callback[] pumped = null;
          int numPumped;
          synchronized (AbstractResolvedValueProducer.this) {
            switch (_pumping) {
              case 1:
                // No deferred results
                break;
              case 3:
                // Next result ready; need to notify subscribers
                numPumped = _pumped.size();
                if (numPumped > 0) {
                  pumped = _pumped.toArray(new Callback[numPumped]);
                  _pumped.clear();
                }
                break;
              case 7:
                // Last result ready; need to notify subscribers
                numPumped = _pumped.size();
                if (numPumped > 0) {
                  pumped = _pumped.toArray(new Callback[numPumped]);
                  _pumped.clear();
                }
                _pumped = null;
                lastResult = true;
                break;
              default:
                throw new IllegalStateException("pumping = " + _pumping);
            }
            _pumping = 0;
          }
          pumpCallbacks(context, pumped, lastResult);
          if (lastResult) {
            finished(context);
          }
        } else {
          if (finished) {
            s_logger.debug("Finished {}", getValueRequirement());
            release(context); // the reference added by addCallback
            context.failed(_callback, getValueRequirement(), failure);
          }
        }
      }
    }

    private boolean notPumpedState() {
      synchronized (AbstractResolvedValueProducer.this) {
        // Shouldn't be in the pumped state - a caller can't call close after calling pump
        return _pumped == null || !_pumped.remove(this);
      }
    }

    @Override
    public void close(final GraphBuildingContext context) {
      s_logger.debug("Closing callback {}", this);
      assert notPumpedState();
      release(context); // the reference added by addCallback
    }

    @Override
    public boolean cancel(final GraphBuildingContext context) {
      s_logger.debug("Cancelling callback {}", this);
      synchronized (AbstractResolvedValueProducer.this) {
        if ((_pumped != null) && _pumped.remove(this)) {
          // Was in a pumped state; close and release the parent resolver
        } else {
          // Not in a pumped state - perhaps cancel was already called
          return false;
        }
      }
      release(context); // the reference added by addCallback
      return true;
    }

    @Override
    public String toString() {
      return "Callback" + _objectId + "[" + _callback + ", " + AbstractResolvedValueProducer.this.toString() + "]";
    }

    @Override
    public int hashCode() {
      return _objectId;
    }

  }

  private static final ResolvedValue[] NO_RESULTS = new ResolvedValue[0];

  private final ValueRequirement _valueRequirement;
  private final int _objectId = s_nextObjectId.getAndIncrement();
  //private final InstanceCount _instanceCount = new InstanceCount(this);
  private Set<ValueSpecification> _resolvedValues;
  private Set<Callback> _pumped = new HashSet<Callback>();
  private int _pumping; // 0 = not, 1 = pumpImpl about to be called, 3 = next result ready, 7 = last result ready
  private int _refCount;
  private ResolvedValue[] _results;
  private volatile boolean _finished;
  private ResolutionFailure _failure;
  private boolean _failureCopied;

  public AbstractResolvedValueProducer(final ValueRequirement valueRequirement) {
    _valueRequirement = valueRequirement;
    _results = NO_RESULTS;
    _refCount = 1;
  }

  @Override
  public Cancelable addCallback(final GraphBuildingContext context, final ResolvedValueCallback valueCallback) {
    addRef(); // Caller already has open reference, this is for the reference held by the callback object
    final Callback callback = new Callback(valueCallback);
    ResolvedValue firstResult = null;
    boolean finished = false;
    ResolutionFailure failure = null;
    synchronized (this) {
      s_logger.debug("Added callback {} to {}", valueCallback, this);
      callback._results = _results;
      if (_results.length > 0) {
        callback._resultsPushed = 1;
        firstResult = _results[0];
        if (_finished && _results.length == 1) {
          finished = true;
        }
      } else {
        if (_finished) {
          finished = true;
          failure = _failure;
        } else {
          _pumped.add(callback);
        }
      }
    }
    if (firstResult != null) {
      if (finished) {
        s_logger.debug("Pushing single callback result {}", firstResult);
        release(context); // reference held by callback object
        context.resolved(valueCallback, getValueRequirement(), firstResult, null);
        return null;
      } else {
        s_logger.debug("Pushing first callback result {}", firstResult);
        context.resolved(valueCallback, getValueRequirement(), firstResult, callback);
        return callback;
      }
    } else if (finished) {
      s_logger.debug("Pushing failure");
      release(context); // reference held by callback object
      context.failed(valueCallback, getValueRequirement(), failure);
      return null;
    } else {
      return callback;
    }
  }

  private void pumpCallbacks(final GraphBuildingContext context, final Callback[] pumped, final boolean lastResult) {
    if (pumped != null) {
      final boolean finished;
      final ResolvedValue[] results;
      final ResolutionFailure failure;
      synchronized (this) {
        finished = _finished;
        results = _results;
        failure = _failure;
      }
      for (Callback callback : pumped) {
        ResolvedValue pumpValue = null;
        boolean lastCallbackResult = false;
        synchronized (callback) {
          if (callback._resultsPushed < results.length) {
            pumpValue = results[callback._resultsPushed++];
            lastCallbackResult = lastResult && (callback._resultsPushed == results.length);
          } else {
            assert finished;
          }
        }
        if (pumpValue != null) {
          if (lastCallbackResult) {
            s_logger.debug("Pushing last result to {}", callback._callback);
            release(context); // reference held by the callback object
            context.resolved(callback._callback, getValueRequirement(), pumpValue, null);
          } else {
            s_logger.debug("Pushing result to {}", callback._callback);
            context.resolved(callback._callback, getValueRequirement(), pumpValue, callback);
          }
        } else {
          s_logger.debug("Pushing failure to {}", callback._callback);
          release(context); // the reference added by addCallback
          context.failed(callback._callback, getValueRequirement(), failure);
        }
      }
    }
  }

  private static boolean equals(final ValueSpecification a, final ValueSpecification b) {
    // The ValueSpecifications put into ResolvedValue objects are normalized
    assert (a == b) == a.equals(b);
    return a == b;
  }

  /**
   * Stores the result in the producer, publishing to any active subscribers that are currently waiting for a value.
   * 
   * @param context the graph building context
   * @param value the value to store
   * @param lastResult last result indicator - if this is definitely the last result, the subscribers won't receive a "pump" handle allowing possible garbage collection of this producer
   * @return true if a value was pushed to the subscribers, false if no value was generated and pushResult or finished must still be called
   */
  protected boolean pushResult(final GraphBuildingContext context, final ResolvedValue value, final boolean lastResult) {
    assert value != null;
    assert !_finished;
    if (!getValueRequirement().getConstraints().isSatisfiedBy(value.getValueSpecification().getProperties())) {
      // Happens when a task was selected early on for satisfying the requirement as part of
      // an aggregation feed. Late resolution then produces different specifications which
      // would have caused in-line failures, but the delegating subscriber will receive them
      // as-is. This would be bad.
      s_logger.debug("Rejecting {} not satisfying {}", value, this);
      return false;
    }
    Callback[] pumped = null;
    synchronized (this) {
      if (_results == null) {
        // Already discarded -- accept the value silently
        return true;
      }
      if (_resolvedValues != null) {
        if (!_resolvedValues.add(value.getValueSpecification())) {
          s_logger.debug("Rejecting {} already available from {}", value, this);
          return false;
        }
      } else {
        final ValueSpecification valueSpec;
        switch (_results.length) {
          case 0:
            // No results yet
            break;
          case 1:
            valueSpec = value.getValueSpecification();
            if (equals(valueSpec, _results[0].getValueSpecification())) {
              s_logger.debug("Rejecting {} already available from {}", value, this);
              return false;
            }
            break;
          case 2:
            valueSpec = value.getValueSpecification();
            if (equals(valueSpec, _results[0].getValueSpecification())
                || equals(valueSpec, _results[1].getValueSpecification())) {
              s_logger.debug("Rejecting {} already available from {}", value, this);
              return false;
            }
            break;
          case 3:
            valueSpec = value.getValueSpecification();
            if (equals(valueSpec, _results[0].getValueSpecification())
                || equals(valueSpec, _results[1].getValueSpecification())
                || equals(valueSpec, _results[2].getValueSpecification())) {
              s_logger.debug("Rejecting {} already available from {}", value, this);
              return false;
            }
            _resolvedValues = new HashSet<ValueSpecification>(8);
            _resolvedValues.add(_results[0].getValueSpecification());
            _resolvedValues.add(_results[1].getValueSpecification());
            _resolvedValues.add(_results[2].getValueSpecification());
            _resolvedValues.add(valueSpec);
            break;
          default:
            throw new IllegalStateException();
        }
      }
      final int l = _results.length;
      s_logger.debug("Result {} available from {}", value, this);
      if (l > 0) {
        final ResolvedValue[] newResults = new ResolvedValue[l + 1];
        System.arraycopy(_results, 0, newResults, 0, l);
        newResults[l] = value;
        _results = newResults;
      } else {
        _results = new ResolvedValue[] {value };
      }
      if (_failure != null) {
        // Don't hold onto any failure state if there is a result
        _failure = null;
        _failureCopied = false;
      }
      if (_pumping > 0) {
        if (lastResult) {
          _pumping |= 6;
        } else {
          _pumping |= 2;
        }
        return true;
      }
      final int s = _pumped.size();
      if (s > 0) {
        pumped = _pumped.toArray(new Callback[s]);
        _pumped.clear();
      }
      if (lastResult) {
        // Clear the _pumped reference so that if a subscriber calls pump we won't get an invocation of pumpImpl (which may call
        // "finished") in addition to our call to "finished" below.
        _pumped = null;
      }
    }
    pumpCallbacks(context, pumped, lastResult);
    if (lastResult) {
      finished(context);
    }
    return true;
  }

  /**
   * Triggers either a future (or immediate) call to either one or more {@link #pushResult} or a single {@link #finished}.
   * 
   * @param context building context
   */
  protected abstract void pumpImpl(final GraphBuildingContext context);

  /**
   * Call when there are no more values that can be pushed. Any callbacks that have had pump called on them will receive a failure notification. This must only be called once.
   */
  protected void finished(final GraphBuildingContext context) {
    assert !_finished;
    s_logger.debug("Finished producing results at {}", this);
    Callback[] pumped = null;
    synchronized (this) {
      _finished = true;
      if (_pumping > 0) {
        _pumping |= 2;
        return;
      }
      if (_pumped != null) {
        final int s = _pumped.size();
        if (s > 0) {
          pumped = _pumped.toArray(new Callback[s]);
        }
        _pumped = null;
      }
    }
    pumpCallbacks(context, pumped, true);
  }

  protected boolean isFinished() {
    return _finished;
  }

  protected void storeFailure(final ResolutionFailure failure) {
    if (failure != null) {
      synchronized (this) {
        if ((_results != null) && (_results.length == 0)) {
          // Only store failure info if there are no results to push
          if (_failure == null) {
            _failure = failure;
            return;
          }
          if (!_failureCopied) {
            _failure = (ResolutionFailure) _failure.clone();
            _failureCopied = true;
          }
          _failure.merge(failure);
        }
      }
    }
  }

  /**
   * Returns the current results in the order they were produced. If the producer is not in a "finished" state, the results are the current intermediate values. The caller must not modify the content
   * of the array.
   * 
   * @return the current results
   */
  protected synchronized ResolvedValue[] getResults() {
    return _results;
  }

  @Override
  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }

  protected int getObjectId() {
    return _objectId;
  }

  @Override
  public synchronized boolean addRef() {
    if (_refCount > 0) {
      _refCount++;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public synchronized int release(final GraphBuildingContext context) {
    assert _refCount > 0;
    final int result = --_refCount;
    if (result == 0) {
      // help out the garbage collector
      _resolvedValues = null;
      _pumped = null;
      _results = null;
      _failure = null;
    }
    return result;
  }

  /**
   * Returns the current reference count for the object.
   * 
   * @return the reference count
   */
  @Override
  public synchronized int getRefCount() {
    return _refCount;
  }

  protected void setRecursionDetected() {
    final List<Callback> pumped;
    synchronized (this) {
      if ((_pumped == null) || _pumped.isEmpty()) {
        return;
      }
      pumped = new ArrayList<Callback>(_pumped);
    }
    for (Callback callback : pumped) {
      callback._callback.recursionDetected();
    }
  }

  @Override
  public int cancelLoopMembers(final GraphBuildingContext context, final Map<Chain, LoopState> visited) {
    final List<Callback> pumped;
    synchronized (this) {
      if ((_pumped == null) || _pumped.isEmpty()) {
        s_logger.debug("Callback {} has no pumped callbacks", this);
        // No callbacks pumped (or has already finished), so can't be in a loop
        return 0;
      }
      pumped = new ArrayList<Callback>(_pumped);
    }
    int result = 0;
    for (Callback callback : pumped) {
      if (callback._callback instanceof Chain) {
        final Chain chained = (Chain) callback._callback;
        final LoopState state = visited.put(chained, LoopState.CHECKING);
        if (state == null) {
          // Not visited yet
          final int looped = chained.cancelLoopMembers(context, visited);
          if (looped > 0) {
            result += looped;
            // The callback is at least in a chain that leads to a loop, but might not be in a loop itself
            if (visited.get(chained) == LoopState.CHECKING) {
              visited.remove(chained);
            }
          } else {
            // Known not to be in a loop
            visited.put(chained, LoopState.NOT_IN_LOOP);
          }
        } else if (state == LoopState.NOT_IN_LOOP) {
          // Callback is not in a loop - reset the flag
          visited.put(chained, LoopState.NOT_IN_LOOP);
        } else {
          // The callback is in a loop - reset the flag and cancel it
          visited.put(chained, LoopState.IN_LOOP);
          s_logger.info("Detected loop at {}, callback {}", this, callback);
          if (callback.cancel(context)) {
            s_logger.debug("Canceled callback; signalling failure");
            callback._callback.failed(context, getValueRequirement(), context.recursiveRequirement(getValueRequirement()));
            result++;
          } else {
            s_logger.debug("Already canceled");
          }
        }
      }
    }
    s_logger.info("Processed {} pumped callbacks, canceled {}", pumped.size(), result);
    return result;
  }

}
