/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.resource;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicAPI;


/**
 * Represents a reference to a resource, allowing safe, explicit resource management by client code.
 * <p>
 * To avoid resource leaks, always call {@link #release()} when this reference is no longer required.
 * <p>
 * A single reference may be used concurrently provided that {@link #release()} is not called prematurely.
 * 
 * @param <T>  the type of resource
 */
@PublicAPI
public interface EngineResourceReference<T extends UniqueIdentifiable> {

  T get();
  
  /**
   * Releases this reference to the computation cycle. A call to this method is mandatory to avoid resource leaks; the
   * computation cycle is discarded only when every reference to it has been released.
   * <p>
   * This method may be called multiple times; only the first call will have any effect.
   */
  void release();
  
}
