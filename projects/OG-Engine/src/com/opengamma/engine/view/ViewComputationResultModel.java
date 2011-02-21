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
 * The data model represents the sum total of analytic functions applied to positions
 * in a particular view. It is the primary data repository for a particular
 * {@link View}.
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
  Set<ComputedValue> getAllLiveData();

}
