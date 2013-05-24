/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Source of a single resolved value.
 */
/* package */final class SingleResolvedValueProducer implements ResolvedValueProducer {

  private final ValueRequirement _valueRequirement;
  private final ResolvedValue _resolvedValue;
  private int _refCount = 1;

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
    context.resolved(callback, getValueRequirement(), getResolvedValue(), null);
    // Single result was posted; can't be cancelled
    return null;
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
  public synchronized int getRefCount() {
    return _refCount;
  }

}
