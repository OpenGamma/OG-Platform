/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.util.PublicAPI;

/**
 * Provides a callback for receiving delta computation results. Delta results contain only the values which have
 * changed since the previous computation cycle.
 */
@PublicAPI
public interface DeltaComputationResultListener extends ComputationListener {

  /**
   * Called to indicate that a new delta result is available.
   * 
   * @param deltaModel  the new delta result
   */
  void deltaResultAvailable(ViewDeltaResultModel deltaModel);
  
}
