/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;

/**
 * Represents a call to {@link ViewResultListener#cycleCompleted(com.opengamma.engine.view.ViewComputationResultModel, com.opengamma.engine.view.ViewDeltaResultModel)}
 */
public class CycleCompletedCall extends AbstractCompletedResultsCall {

  public CycleCompletedCall(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    super(fullResult, deltaResult);
  }

  public ViewComputationResultModel getFullResult() {
    return getViewComputationResultModel();
  }

  public ViewDeltaResultModel getDeltaResult() {
    return getViewDeltaResultModel();
  }

  @Override
  public Object apply(ViewResultListener listener) {
    listener.cycleCompleted(getFullResult(), getDeltaResult());
    return null;
  }

}
