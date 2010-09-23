/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

/**
 * Allows code to register callbacks for when new computation results
 * are available.
 *
 * @author kirk
 */
public interface ComputationResultListener extends ComputationListener {
  
  void computationResultAvailable(ViewComputationResultModel resultModel);
  
}
