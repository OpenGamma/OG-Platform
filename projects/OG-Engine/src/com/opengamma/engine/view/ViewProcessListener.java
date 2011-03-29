/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.view.compilation.ViewEvaluationModel;

/**
 * 
 */
public interface ViewProcessListener {

  boolean isDeltaResultRequired();
  
  void compiled(ViewEvaluationModel viewEvaluationModel);
  
  void result(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult);
  
  void processCompleted();
  
  void shutdown();
  
}
