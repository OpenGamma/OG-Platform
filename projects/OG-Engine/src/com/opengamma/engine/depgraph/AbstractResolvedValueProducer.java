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

/* package */abstract class AbstractResolvedValueProducer implements ResolvedValueProducer {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractResolvedValueProducer.class);

  // TODO: the locking here is not very efficient; rewrite if this causes a bottleneck 

  private final class Callback implements ResolutionPump {

    private final ResolvedValueCallback _callback;
    private ResolvedValue[] _results;
    private int _resultsPushed;

    public Callback(final ResolvedValueCallback callback) {
      _callback = callback;
    }

    @Override
    public void pump() {
      s_logger.debug("Pump called on {}", this);
      ResolvedValue nextValue = null;
      boolean finished = false;
      boolean needsOuterPump = false;
      synchronized (this) {
        if (_resultsPushed < _results.length) {
          nextValue = _results[_resultsPushed++];
        }
      }
      if (nextValue == null) {
        synchronized (AbstractResolvedValueProducer.this) {
          synchronized (this) {
            _results = AbstractResolvedValueProducer.this._results;
            if (_resultsPushed < _results.length) {
              nextValue = _results[_resultsPushed++];
            } else {
              if (_finished) {
                finished = true;
              } else {
                needsOuterPump = _pumped.isEmpty();
                _pumped.add(this);
              }
            }
          }
        }
      }
      if (nextValue != null) {
        s_logger.debug("Publishing value {}", nextValue);
        _callback.resolved(getValueRequirement(), nextValue, this);
      } else {
        if (needsOuterPump) {
          pumpImpl();
        } else {
          if (finished) {
            s_logger.debug("Finished {}", getValueRequirement());
            _callback.failed(getValueRequirement());
          }
        }
      }
    }

    @Override
    public String toString() {
      return "Callback[" + _callback + ", " + AbstractResolvedValueProducer.this.toString() + "]";
    }

  }

  private final ValueRequirement _valueRequirement;
  private final List<Callback> _pumped = new ArrayList<Callback>();
  private ResolvedValue[] _results;
  private boolean _finished;

  public AbstractResolvedValueProducer(final ValueRequirement valueRequirement) {
    _valueRequirement = valueRequirement;
    _results = new ResolvedValue[0];
    _finished = false;
  }

  @Override
  public void addCallback(final ResolvedValueCallback valueCallback) {
    final Callback callback = new Callback(valueCallback);
    ResolvedValue firstResult = null;
    boolean finished = false;
    synchronized (this) {
      s_logger.debug("Added callback {} to {}", valueCallback, this);
      callback._results = _results;
      if (_results.length > 0) {
        callback._resultsPushed = 1;
        firstResult = _results[0];
      } else {
        if (_finished) {
          finished = true;
        } else {
          _pumped.add(callback);
        }
      }
    }
    if (firstResult != null) {
      s_logger.debug("Pushing first callback result {}", firstResult);
      valueCallback.resolved(getValueRequirement(), firstResult, callback);
    } else if (finished) {
      s_logger.debug("Pushing failure");
      valueCallback.failed(getValueRequirement());
    }
  }

  protected void pushResult(final ResolvedValue value) {
    assert value != null;
    assert !_finished;
    assert getValueRequirement().isSatisfiedBy(value.getValueSpecification());
    Collection<Callback> pumped = null;
    synchronized (this) {
      final int l = _results.length;
      s_logger.debug("Result {} available from {}", value, this);
      ResolvedValue[] newResults = new ResolvedValue[l + 1];
      System.arraycopy(_results, 0, newResults, 0, l);
      newResults[l] = value;
      _results = newResults;
      if (!_pumped.isEmpty()) {
        pumped = new ArrayList<Callback>(_pumped);
        _pumped.clear();
      }
    }
    if (pumped != null) {
      for (Callback callback : pumped) {
        synchronized (callback) {
          callback._resultsPushed++;
        }
        s_logger.debug("Pushing result to {}", callback._callback);
        callback._callback.resolved(getValueRequirement(), value, callback);
      }
    }
  }

  protected abstract void pumpImpl();

  protected void finished() {
    assert !_finished;
    s_logger.debug("Finished producing results at {}", this);
    final Collection<Callback> pumped;
    synchronized (this) {
      _finished = true;
      if (_pumped.isEmpty()) {
        pumped = null;
      } else {
        pumped = new ArrayList<Callback>(_pumped);
        _pumped.clear();
      }
    }
    if (pumped != null) {
      for (Callback callback : pumped) {
        s_logger.debug("Pushing failure to {}", callback._callback);
        callback._callback.failed(getValueRequirement());
      }
    } else {
      s_logger.debug("No pumped callbacks to propogate failure to", this);
      // Should this ever happen? At least one must be pumped if we are in a receive state?
    }
  }

  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }

}
