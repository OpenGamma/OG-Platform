/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Provides mutator methods for live data, allowing customisation of live data.
 */
public interface LiveDataInjector {
  
  /**
   * Injects a live data value.
   * 
   * @param valueRequirement  the requirement satisfied by the value
   * @param value  the value to add
   */
  void addValue(ValueRequirement valueRequirement, Object value);

  /**
   * Removes a previously-added live data value.
   * 
   * @param valueRequirement  the value to remove
   */
  void removeValue(final ValueRequirement valueRequirement);
  
}
