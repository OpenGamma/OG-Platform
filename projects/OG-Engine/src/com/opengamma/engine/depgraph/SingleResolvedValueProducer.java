/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.concurrent.atomic.AtomicReference;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Source of a single resolved value.
 */
/* package */final class SingleResolvedValueProducer implements ResolvedValueProducer {

  private final ValueRequirement _valueRequirement;
  private final ResolvedValue _resolvedValue;
  private int _refCount = 1;
  private int _activeCallbacks;

  public SingleResolvedValueProducer(final ValueRequirement valueRequirement, final ResolvedValue resolvedValue) {
    _valueRequirement = valueRequirement;
    _resolvedValue = resolvedValue;
  }

  @Override
  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }

  private ResolvedValue getResolvedValue() {
    return _resolvedValue;
  }

  @Override
  public Cancelable addCallback(final GraphBuildingContext context, final ResolvedValueCallback callback) {
    final AtomicReference<ResolvedValueCallback> callbackRef = new AtomicReference<ResolvedValueCallback>(callback);
    synchronized (this) {
      _activeCallbacks++;
    }
    context.resolved(callback, getValueRequirement(), getResolvedValue(), new ResolutionPump() {

      @Override
      public void pump(final GraphBuildingContext context) {
        final ResolvedValueCallback callback = callbackRef.getAndSet(null);
        if (callback != null) {
          synchronized (this) {
            _activeCallbacks--;
          }
          // No error information to push; just that there are no additional value requirements
          context.failed(callback, getValueRequirement(), null);
        }
      }

      @Override
      public void close(final GraphBuildingContext context) {
        if (callbackRef.getAndSet(null) != null) {
          synchronized (this) {
            _activeCallbacks--;
          }
        }
      }

    });
    return new Cancelable() {
      @Override
      public boolean cancel(final GraphBuildingContext context) {
        if (callbackRef.getAndSet(null) != null) {
          synchronized (this) {
            _activeCallbacks--;
          }
          return true;
        } else {
          return false;
        }
      }
    };
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
  public synchronized int release(GraphBuildingContext context) {
    assert _refCount > 0;
    return --_refCount;
  }

  @Override
  public synchronized boolean hasActiveCallbacks() {
    return _activeCallbacks > 0;
  }

}
