/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.util.PublicAPI;

/**
 * The result of a single configuration of a view calculation. 
 */
@PublicAPI
public interface ViewCalculationResultModel {
  
  /**
   * Returns all terminal output target specifications present in the configuration.
   * 
   * @return the target specifications
   */
  Collection<ComputationTargetSpecification> getAllTargets();

  /**
   * Returns the computed values for a given target. The values are returned as a map of configuration names to {@link ComputedValue}s.
   * 
   * @param target the target to search for, not {@code null}
   * @return the computed values for each configuration, or {@code null} if the target does not exist in the view results
   */
  Map<String, ComputedValue> getValues(ComputationTargetSpecification target);
  
}
