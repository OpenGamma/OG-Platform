/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.PublicSPI;

/**
 * Provides mutator methods for live data, allowing customisation of live data.
 */
@PublicSPI
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
