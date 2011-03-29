/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.util.PublicAPI;

/**
 * Provides a callback for receiving full computation results. Full results contain every requested value on every 
 * computation cycle.
 */
@PublicAPI
public interface ComputationResultListener extends ComputationListener {
  
  /**
   * Called to indicate that a new result is available.
   * 
   * @param resultModel  the new result
   */
  void computationResultAvailable(ViewComputationResultModel resultModel);
  
}
