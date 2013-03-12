/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.resource;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation of {@link EngineResourceReference}
 * 
 * @param <T>  the type of resource
 */
public class EngineResourceReferenceImpl<T extends EngineResource> implements EngineResourceReference<T> {

  private final EngineResourceManagerImpl<T> _manager;
  private final T _resource;
  private AtomicBoolean _isReleased = new AtomicBoolean(false);
  
  public EngineResourceReferenceImpl(EngineResourceManagerImpl<T> manager, T resource) {
    _manager = manager;
    _resource = resource;
  }
  
  @Override
  public T get() {
    assertNotReleased();
    return _resource;
  }

  @Override
  public void release() {
    if (_isReleased.getAndSet(true)) {
      // Already released
      return;
    }
    _manager.decrementCycleReferenceCount(_resource.getUniqueId());
  }
  
  //-------------------------------------------------------------------------
  private void assertNotReleased() {
    if (_isReleased.get()) {
      throw new IllegalStateException("The computation cycle reference has been been released");
    }
  }
  
}
