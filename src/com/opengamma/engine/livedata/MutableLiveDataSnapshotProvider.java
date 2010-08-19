/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Provides mutator methods on top of the accessor methods in {@link LiveDataSnapshotProvider}. 
 */
public interface MutableLiveDataSnapshotProvider extends LiveDataSnapshotProvider {
  
  /**
   * Injects a live data value into the provider.
   * 
   * @param value  the value to add
   */
  void addValue(ComputedValue value);

  /**
   * Removes a previously-added value from the provider.
   * 
   * @param valueRequirement  the value to remove
   */
  void removeValue(final ValueRequirement valueRequirement);
  
}
