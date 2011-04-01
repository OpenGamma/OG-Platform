/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 */
public class ViewCycleReferenceImpl implements ViewCycleReference {

  private final ViewCycleManagerImpl _manager;
  private final SingleComputationCycle _cycle;
  private AtomicBoolean _isReleased = new AtomicBoolean(false);
  
  public ViewCycleReferenceImpl(ViewCycleManagerImpl manager, SingleComputationCycle cycle) {
    _manager = manager;
    _cycle = cycle;
  }
  
  @Override
  public ViewCycleInternal getCycle() {
    assertNotReleased();
    return _cycle;
  }

  @Override
  public void release() {
    if (_isReleased.getAndSet(true)) {
      // Already released
      return;
    }
    _manager.decrementCycleReferenceCount(_cycle.getUniqueId());
  }
  
  //-------------------------------------------------------------------------
  private void assertNotReleased() {
    if (_isReleased.get()) {
      throw new IllegalStateException("The computation cycle reference has been been released");
    }
  }
  
}
