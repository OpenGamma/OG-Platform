/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.resource;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Default implementation of {@link EngineResourceManager}
 * 
 * @param <T>  the type of resource
 */
public class EngineResourceManagerImpl<T extends EngineResource> implements EngineResourceManagerInternal<T> {
  
  private final ConcurrentMap<UniqueId, ReferenceCountedResource<T>> _resourceMap = new ConcurrentHashMap<UniqueId, ReferenceCountedResource<T>>();
  
  @Override
  public EngineResourceReferenceImpl<T> manage(T resource) {
    ArgumentChecker.notNull(resource, "resource");
    ReferenceCountedResource<T> refCountedResource = new ReferenceCountedResource<T>(resource);
    if (_resourceMap.put(resource.getUniqueId(), refCountedResource) != null) {
      throw new IllegalArgumentException("A resource with ID " + resource.getUniqueId() + " is already being managed");
    }
    return new EngineResourceReferenceImpl<T>(this, resource);
  }

  @Override
  public EngineResourceReference<T> createReference(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    T resource = incrementCycleReferenceCountCore(uniqueId);
    return resource == null ? null : new EngineResourceReferenceImpl<T>(this, resource);
  }

  @Override
  public boolean incrementCycleReferenceCount(UniqueId uniqueId) {
    return incrementCycleReferenceCountCore(uniqueId) != null;
  }
  
  @Override
  public boolean decrementCycleReferenceCount(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ReferenceCountedResource<T> refCountedResource = _resourceMap.get(uniqueId);
    
    if (refCountedResource == null) {
      return false;
    }
    
    synchronized (refCountedResource) {
      if (refCountedResource.decrementReferenceCount() == 0) {
        _resourceMap.remove(uniqueId);
        refCountedResource.get().release();
      }
    }
    return true;
  }
  
  @Override
  public int getResourceCount() {
    return _resourceMap.size();
  }
  
  //-------------------------------------------------------------------------
  private T incrementCycleReferenceCountCore(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ReferenceCountedResource<T> refCountedResource = _resourceMap.get(uniqueId);
     
    if (refCountedResource == null) {
      return null;
    }
    
    synchronized (refCountedResource) {
      if (refCountedResource.getReferenceCount() > 0) {
        refCountedResource.incrementReferenceCount();
      } else {
        // Concurrently being released, so to the external user it's already been deleted
        throw new IllegalArgumentException("No resource with ID " + uniqueId + " could be found");
      }
    }
    
    return refCountedResource.get();
  }
  
  //-------------------------------------------------------------------------
  /**
   * Holds reference counting state for a resource. Intentionally not thread-safe, so requires external
   * synchronisation.
   */
  private static class ReferenceCountedResource<T> {
    
    private final T _resource;
    private long _refCount = 1;
    
    public ReferenceCountedResource(T resource) {
      _resource = resource;
    }
    
    public T get() {
      return _resource;
    }
    
    public long getReferenceCount() {
      return _refCount;
    }
    
    public long incrementReferenceCount() {
      return ++_refCount;
    }
    
    public long decrementReferenceCount() {
      return --_refCount;
    }
    
  }
  
}
