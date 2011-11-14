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
 * The data model represents the sum total of analytic functions applied to positions
 * in a particular view. It is the primary data repository for a particular
 * {@link ViewProcess}.
 *
 * @author kirk
 */
@PublicAPI
public interface ViewComputationResultModel extends ViewResultModel {

  /**
   * Gets all market data used to calculate this result
   *
   * @return all market data used to calculate this result
   */
  Set<ComputedValue> getAllMarketData();

  /**
   * Returns mapping of requirements to specifications resolved for this result.
   *
   * @return mapping of requirements to specifications
   */
  Map<ValueSpecification, Set<ValueRequirement>> getRequirementToSpecificationMapping();

}
