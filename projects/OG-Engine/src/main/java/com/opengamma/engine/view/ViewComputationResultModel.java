/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.util.PublicAPI;

/**
 * The top-level container for the results of calculations performed when executing one cycle of a {@link ViewDefinition}.
 */
@PublicAPI
public interface ViewComputationResultModel extends ViewResultModel {

  /**
   * Gets all market data used to calculate this result.
   * 
   * @return all market data used to calculate this result, not null
   */
  Set<ComputedValue> getAllMarketData();

}
