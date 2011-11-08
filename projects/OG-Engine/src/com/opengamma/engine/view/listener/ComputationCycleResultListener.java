/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;

import javax.time.Instant;

/**
 * A listener to the output of a view cycle. Calls to the listener are always made in the sequence in which they
 * occur; it may be assumed that the listener will not be used concurrently.
 */
@PublicAPI
public interface ComputationCycleResultListener {


  //-------------------------------------------------------------------------
  /**
   * Called following single calculation cycle completion.
   *
   * @param result  the result of single calculation job, not null
   */
  void jobResultReceived(ViewResultModel result);
  
}
