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
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.tuple.Pair;

/**
 * A container for the results of calculations performed when executing one cycle of a {@link ViewCalculationConfiguration} within a {@link ViewDefinition}.
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
   * Returns the computed values for a given target. The values are returned as a map from the value name and properties to the {@link ComputedValue}.
   * 
   * @param target the target to search for, not null
   * @return the computed values for this configuration, or null if the target does not exist in the view results
   */

  // TODO: note that getValues should be querying on the target's unique identifier (and any context identifiers) and not the target type as the caller
  // may be passing in a more (or less) descriptive type component that will not be an exact match

  Map<Pair<String, ValueProperties>, ComputedValueResult> getValues(ComputationTargetSpecification target);
  
  /**
   * Returns all computed values for a given target.
   * 
   * @param target the target to search for, not null
   * @return the computed values for this configuration, or null if the target does not exist in the view results
   */
  Collection<ComputedValueResult> getAllValues(ComputationTargetSpecification target);

}
