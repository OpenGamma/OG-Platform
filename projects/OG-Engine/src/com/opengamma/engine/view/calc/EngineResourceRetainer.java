/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.id.UniqueIdentifier;

/**
 * Manages the sliding retention of at most one resource from a manager.
 */
public class EngineResourceRetainer {

  private final EngineResourceManagerInternal<?> _manager;
  
  private UniqueIdentifier _retainedResourceId;
  
  public EngineResourceRetainer(EngineResourceManagerInternal<?> manager) {
    _manager = manager;
  }
  
  /**
   * Replaces any existing retained resource with a new resource.
   * 
   * @param resourceId  the unique identifier of the new resource to retain, or {@code null} if there is nothing new to retain 
   */
  public void replaceRetainedCycle(UniqueIdentifier resourceId) {
    if (_retainedResourceId != null) {
      getManager().decrementCycleReferenceCount(_retainedResourceId);
      _retainedResourceId = null;
    }
    if (resourceId != null) {
      getManager().incrementCycleReferenceCount(resourceId);
      _retainedResourceId = resourceId;
    }
  }
  
  private EngineResourceManagerInternal<?> getManager() {
    return _manager;
  }
    
}
