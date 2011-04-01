/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.id.UniqueIdentifier;

/**
 * Manages the sliding retention of at most one view cycle.
 */
public class ViewCycleRetainer {

  private final ViewCycleManagerImpl _cycleManager;
  
  private UniqueIdentifier _retainedCycleId;
  
  public ViewCycleRetainer(ViewCycleManagerImpl cycleManager) {
    _cycleManager = cycleManager;
  }
  
  /**
   * Replaces any existing retained cycle with a new cycle.
   * 
   * @param cycleId  the unique identifier of the new cycle to retain, or {@code null} if there is nothing new to retain 
   */
  public void replaceRetainedCycle(UniqueIdentifier cycleId) {
    if (_retainedCycleId != null) {
      getCycleManager().decrementCycleReferenceCount(_retainedCycleId);
      _retainedCycleId = null;
    }
    if (cycleId != null) {
      getCycleManager().incrementCycleReferenceCount(cycleId);
      _retainedCycleId = cycleId;
    }
  }
  
  private ViewCycleManagerImpl getCycleManager() {
    return _cycleManager;
  }
    
}
