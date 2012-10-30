/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicAPI;

/**
 * The top-level container for the results of calculations performed when executing one cycle of a
 * {@link ViewDefinition}.
 */
@PublicAPI
public interface ViewComputationResultModel extends ViewResultModel {

  /**
   * Gets all market data used to calculate this result.
   *
   * @return all market data used to calculate this result, not null
   */
  Set<ComputedValue> getAllMarketData();

  /**
   * Gets a mapping from each value specification in the results to the set of original requirements that it satisfies.
   *
   * @return the mapping of each value specification to the set of original requirements that it satisfies, not null
   */
  Map<ValueSpecification, Set<ValueRequirement>> getRequirementToSpecificationMapping();

}
