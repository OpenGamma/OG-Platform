/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

/**
 * A callback for code that only wants to receive delta updates
 * on view recomputation.
 *
 * @author kirk
 */
public interface DeltaComputationResultListener {

  void deltaResultAvailable(ViewDeltaResultModel deltaModel);
}
