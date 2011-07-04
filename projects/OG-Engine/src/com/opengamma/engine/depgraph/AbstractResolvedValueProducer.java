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
                s_logger.debug("Callback {} pumped", this);
                needsOuterPump = _pumped.isEmpty();
                _pumped.add(this);
              }
            }
          }
        }
      }
      if (nextValue != null) {
        s_logger.debug("Callback {} pumping value {}", this, nextValue);
        _callback.resolved(getValueRequirement(), nextValue, this);
      } else {
        if (needsOuterPump) {
          pumpImpl();
        } else {
          if (finished) {
            s_logger.debug("Callback {} failed", this);
            _callback.failed(getValueRequirement());
          }
        }
      }
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
    synchronized (this) {
      s_logger.debug("Added callback {}", valueCallback);
      callback._results = _results;
      if (_results.length > 0) {
        callback._resultsPushed = 1;
        firstResult = _results[0];
      } else {
        _pumped.add(callback);
      }
    }
    if (firstResult != null) {
      s_logger.debug("Pushing first callback result {}", firstResult);
      valueCallback.resolved(getValueRequirement(), firstResult, callback);
    }
  }

  protected void pushResult(final ResolvedValue value) {
    assert value != null;
    assert !_finished;
    assert getValueRequirement().isSatisfiedBy(value.getValueSpecification());
    Collection<Callback> pumped = null;
    synchronized (this) {
      final int l = _results.length;
      s_logger.debug("Adding result {} to {} previous values", value, l);
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
      s_logger.debug("Pushing result to pumped callbacks");
      for (Callback callback : pumped) {
        synchronized (callback) {
          callback._resultsPushed++;
        }
        callback._callback.resolved(getValueRequirement(), value, callback);
      }
    }
  }

  protected abstract void pumpImpl();

  protected void finished() {
    assert !_finished;
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
      s_logger.debug("Pushing failure to pumped callbacks");
      for (Callback callback : pumped) {
        callback._callback.failed(getValueRequirement());
      }
    } else {
      s_logger.debug("No pumped callbacks to propogate failure to");
      // Should this ever happen? At least one must be pumped if we are in a receive state?
    }
  }

  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }

}
