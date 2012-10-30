/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Manages engine resources for which reference counting is required.
 * 
 * @param <T>  the type of resource
 */
public interface EngineResourceManager<T extends UniqueIdentifiable> {
  
  /**
   * Constructs a reference wrapper to a resource, incrementing the reference count for that resource. The reference
   * wrapper facilitates decrementing the reference count safely through the method
   * {@link EngineResourceReference#release()}.
   * 
   * @param uniqueId  the unique identifier of the resource for which a reference is required, not null
   * @return a reference wrapper to the resource, or null if the resource was not found
   */
  EngineResourceReference<T> createReference(UniqueId uniqueId);
  
}
