/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.util.PublicAPI;

/**
 * Allows code to register callbacks for when new computation results
 * are available.
 *
 * @author kirk
 */
@PublicAPI
public interface ComputationResultListener extends ComputationListener {
  
  void computationResultAvailable(ViewComputationResultModel resultModel);
  
}
