/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.engine.view.cache.ViewComputationCache;

/**
 * 
 */
public interface ViewCycleInternal extends ViewCycle {

  void execute(ViewCycleInternal previousCycle) throws InterruptedException;
  
  ViewComputationCache getComputationCache(String calcConfigName);
  
}
