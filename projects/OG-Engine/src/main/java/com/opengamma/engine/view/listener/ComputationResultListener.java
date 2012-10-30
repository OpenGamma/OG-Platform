/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.opengamma.engine.view.ViewComputationResultModel;

/**
 * A listener to computation results.
 */
public interface ComputationResultListener {

  /**
   * Called when a computation result is available.
   *
   * @param result  the result, not null
   */
  void resultAvailable(ViewComputationResultModel result);
  
}
