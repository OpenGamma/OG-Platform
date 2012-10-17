/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
    private boolean _closed;

    public Callback(final ResolvedValueCallback callback) {
      _callback = callback;
      // Don't reference count the callback; the interface doesn't support it and if it is something like an
      // AggregateResolvedValueProducer then counting references in this direction will introduce a loop and
      // prevent dead tasks from being identified
    }

    @Override
    public void pump(final GraphBuildingContext context) {
      s_logger.debug("Pump called on {}", this);
      assert !_closed;
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
                _callbacks--;
              }
            } else {
              if (_finished) {
                finished = true;
                failure = _failure;
                _callbacks--;
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
                  _callbacks--;
                }
              }
            }
          }
        }
      }
      if (nextValue != null) {
        if (finished) {
          s_logger.debug("Publishing final value {}", nextValue);
          release(context);
          context.resolved(_callback, getValueRequirement(), nextValue, null);
        } else {
          s_logger.debug("Publishing value {}", nextValue);
          context.resolved(_callback, getValueRequirement(), nextValue, this);
        }
      } else {
        if (needsOuterPump) {
          pumpImpl(context);
          boolean lastResult = false;
          Collection<Callback> pumped = null;
          synchronized (this) {
            switch (_pumping) {
              case 1:
                // No deferred results
                break;
              case 3:
                // Next result ready; need to notify subscribers
                if (!_pumped.isEmpty()) {
                  pumped = new ArrayList<Callback>(_pumped);
                  _pumped.clear();
                }
                break;
              case 7:
                // Last result ready; need to notify subscribers
                if (!_pumped.isEmpty()) {
                  pumped = new ArrayList<Callback>(_pumped);
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
            context.failed(_callback, getValueRequirement(), failure);
            release(context);
          }
        }
      }
    }

    @Override
    public void close(final GraphBuildingContext context) {
      s_logger.debug("Closing callback {}", this);
      synchronized (AbstractResolvedValueProducer.this) {
        assert !_closed;
        _closed = true;
        // Shouldn't be in the pumped state - a caller can't call close after calling pump
        assert _pumped == null || !_pumped.remove(this);
        _callbacks--;
      }
      release(context);
    }

    @Override
    public boolean cancel(final GraphBuildingContext context) {
      s_logger.debug("Cancelling callback {}", this);
      synchronized (AbstractResolvedValueProducer.this) {
        if ((_pumped != null) && _pumped.remove(this)) {
          // Was in a pumped state; close and release the parent resolver
          assert !_closed;
          _closed = true;
        } else {
          return false;
        }
        _callbacks--;
      }
      release(context);
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
  private final Set<ValueSpecification> _resolvedValues = new HashSet<ValueSpecification>();
  private Set<Callback> _pumped = new HashSet<Callback>();
  private int _pumping; // 0 = not, 1 = pumpImpl about to be called, 3 = next result ready, 7 = last result ready
  private int _refCount;
  private int _callbacks;
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
    addRef(); // reference held by the callback object
    final Callback callback = new Callback(valueCallback);
    ResolvedValue firstResult = null;
    boolean finished = false;
    ResolutionFailure failure = null;
    synchronized (this) {
      s_logger.debug("Added callback {} to {}", valueCallback, this);
      _callbacks++;
      callback._results = _results;
      if (_results.length > 0) {
        callback._resultsPushed = 1;
        firstResult = _results[0];
        if (_finished && _results.length == 1) {
          finished = true;
          _callbacks--;
        }
      } else {
        if (_finished) {
          finished = true;
          failure = _failure;
          _callbacks--;
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
      } else {
        s_logger.debug("Pushing first callback result {}", firstResult);
        context.resolved(valueCallback, getValueRequirement(), firstResult, callback);
      }
    } else if (finished) {
      s_logger.debug("Pushing failure");
      context.failed(valueCallback, getValueRequirement(), failure);
      release(context); // reference held by callback object
    } else {
      return callback;
    }
    return null;
  }

  private void pumpCallbacks(final GraphBuildingContext context, final Collection<Callback> pumped, final boolean lastResult) {
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
            synchronized (this) {
              _callbacks--;
            }
            release(context); // reference held by the callback object
            context.resolved(callback._callback, getValueRequirement(), pumpValue, null);
          } else {
            s_logger.debug("Pushing result to {}", callback._callback);
            context.resolved(callback._callback, getValueRequirement(), pumpValue, callback);
          }
        } else {
          s_logger.debug("Pushing failure to {}", callback._callback);
          synchronized (this) {
            _callbacks--;
          }
          context.failed(callback._callback, getValueRequirement(), failure);
        }
      }
    }
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
    Collection<Callback> pumped = null;
    synchronized (this) {
      if (!_resolvedValues.add(value.getValueSpecification())) {
        s_logger.debug("Rejecting {} already available from {}", value, this);
        return false;
      }
      final int l = _results.length;
      s_logger.debug("Result {} available from {}", value, this);
      final ResolvedValue[] newResults = new ResolvedValue[l + 1];
      System.arraycopy(_results, 0, newResults, 0, l);
      newResults[l] = value;
      _results = newResults;
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
      if (!_pumped.isEmpty()) {
        pumped = new ArrayList<Callback>(_pumped);
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
    Collection<Callback> pumped = null;
    synchronized (this) {
      _finished = true;
      if (_pumping > 0) {
        _pumping |= 2;
        return;
      }
      if (_pumped != null) {
        if (!_pumped.isEmpty()) {
          pumped = new ArrayList<Callback>(_pumped);
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
        if (_results.length == 0) {
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
    if (s_logger.isDebugEnabled()) {
      s_logger.debug("Release called on {}, refCount={}", this, _refCount);
    }
    final int result = --_refCount;
    if (result == 0) {
      // help out the garbage collector
      _failure = null;
    }
    return result;
  }

  @Override
  public synchronized boolean hasActiveCallbacks() {
    return _callbacks > 0;
  }

  @Override
  public int cancelLoopMembers(final GraphBuildingContext context, final Set<Object> visited) {
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
        if (visited.add(callback)) {
          result += ((Chain) callback._callback).cancelLoopMembers(context, visited);
          visited.remove(callback);
        } else {
          // The callback is in a loop; cancel it
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
