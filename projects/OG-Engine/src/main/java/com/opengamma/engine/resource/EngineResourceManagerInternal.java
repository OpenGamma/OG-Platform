/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.resource;

import com.opengamma.id.UniqueId;

/**
 * An internal interface to an engine resource manager.
 * 
 * @param <T>  the type of resource
 */
public interface EngineResourceManagerInternal<T extends EngineResource> extends EngineResourceManager<T> {

  /**
   * Puts a resource under management, initially with a single reference to it which is returned.
   * 
   * @param resource  the resource to be managed, not null
   * @return a reference to the resource, not null
   * @throws IllegalArgumentException  if a cycle with the same ID is already under management
   */
  EngineResourceReference<T> manage(T resource);
  
  /**
   * Increments the reference count for a resource which ensures it is retained. A call to this method must be paired
   * with a subsequent call to {@link #decrementCycleReferenceCount(UniqueId)} to avoid resource leaks. 
   * 
   * @param uniqueId  the unique identifier of the resource to retain, not null
   * @return true if the operation was successful, false if the cycle was not found
   */
  boolean incrementCycleReferenceCount(UniqueId uniqueId);
  
  /**
   * Decrements the reference count for a resource which may allow it to be released. A call to this method must only
   * follow a previous call to {@link #incrementCycleReferenceCount(UniqueId)}.
   * 
   * @param uniqueId  the unique identifier of the resource to release, not null
   * @return true if the operation was successful, false if the resource was not found
   */
  boolean decrementCycleReferenceCount(UniqueId uniqueId);
  
  /**
   * Gets the number of resources under management.
   * 
   * @return the number of resources under management
   */
  int getResourceCount();
  
}
