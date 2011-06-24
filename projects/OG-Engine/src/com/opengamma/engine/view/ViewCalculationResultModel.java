/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Pair;

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
   * Returns the computed values for a given target. The values are returned as a map from the value name and
   * properties to the {@link ComputedValue}.
   * 
   * @param target the target to search for, not {@code null}
   * @return the computed values for this configuration, or {@code null} if the target does not exist in the view results
   */
  Map<Pair<String, ValueProperties>, ComputedValue> getValues(ComputationTargetSpecification target);
  
  /**
   * Returns all computed values for a given target.
   * 
   * @param target the target to search for, not {@code null}
   * @return the computed values for this configuration, or {@code null} if the target does not exist in the view results
   */
  Collection<ComputedValue> getAllValues(ComputationTargetSpecification target);

}
