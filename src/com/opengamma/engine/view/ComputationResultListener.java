/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.livedata.msg.UserPrincipal;

/**
 * Allows code to register callbacks for when new computation results
 * are available.
 *
 * @author kirk
 */
public interface ComputationResultListener extends ComputationListener {
  
  void computationResultAvailable(ViewComputationResultModel resultModel);
  
}
