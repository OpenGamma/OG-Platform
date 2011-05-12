/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.Identifier;
import com.opengamma.util.PublicSPI;

/**
 * Provides mutator methods for live data, allowing customisation of live data.
 */
@PublicSPI
public interface LiveDataInjector {
  
  /**
   * Injects a live data value by {@link ValueRequirement}.
   * 
   * @param valueRequirement  the value requirement, not {@code null}
   * @param value  the value to add
   */
  void addValue(ValueRequirement valueRequirement, Object value);

  /**
   * Injects a live data value by {@link Identifier}. This identifier is resolved automatically into the
   * {@link UniqueIdentifier} to use in a {@link ValueRequirement}.
   * 
   * @param identifier  an identifier of the target, not {@code null}
   * @param valueName  the name of the value being added, not {@code null}
   * @param value  the value to add
   */
  void addValue(Identifier identifier, String valueName, Object value);
  
  /**
   * Removes a previously-added live data value by {@link ValueRequirement}.
   * 
   * @param valueRequirement  the value requirement, not {@code null}
   */
  void removeValue(ValueRequirement valueRequirement);
  
  /**
   * Removes a previously-added live data value by {@link Identifier}. This identifier is resolved automatically into
   * a {@link ValueRequirement} so could be different from the one used when the value was added, as long as it
   * resolves to the same target.
   * 
   * @param identifier  an identifier of the target, not {@code null}
   * @param valueName  the name of the value being removed, not {@code null}
   */
  void removeValue(Identifier identifier, String valueName);
  
}
