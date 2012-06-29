/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.value.ValueRequirement;

/**
 * An invalid production.
 */
/* package */final class NullResolvedValueProducer implements ResolvedValueProducer {

  private final ValueRequirement _valueRequirement;
  private final ResolutionFailure _failure;
  private int _refCount = 1;

  public NullResolvedValueProducer(final ValueRequirement valueRequirement, final ResolutionFailure failure) {
    _valueRequirement = valueRequirement;
    _failure = failure;
  }

  @Override
  public ValueRequirement getValueRequirement() {
    return _valueRequirement;
  }

  private ResolutionFailure getFailure() {
    return _failure;
  }

  @Override
  public Cancelable addCallback(final GraphBuildingContext context, final ResolvedValueCallback callback) {
    context.failed(callback, getValueRequirement(), getFailure());
    return null;
  }

  @Override
  public synchronized void addRef() {
    assert _refCount > 0;
    _refCount++;
  }

  @Override
  public synchronized int release(GraphBuildingContext context) {
    assert _refCount > 0;
    return --_refCount;
  }

  @Override
  public boolean hasActiveCallbacks() {
    return false;
  }

}
