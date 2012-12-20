/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.google.common.base.Function;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.merging.ViewDeltaResultModelMerger;

/**
 * Represents a call to {@link ViewResultListener#cycleCompleted(com.opengamma.engine.view.ViewComputationResultModel, com.opengamma.engine.view.ViewDeltaResultModel)}
 */
public class CycleCompletedCall implements Function<ViewResultListener, Object> {

  private ViewComputationResultModel _fullResult;
  private final ViewDeltaResultModelMerger _deltaMerger = new ViewDeltaResultModelMerger();
  
  public CycleCompletedCall(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    update(fullResult, deltaResult);
  }

  public void update(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _fullResult = fullResult;
    if (deltaResult != null) {
      _deltaMerger.merge(deltaResult);
    }
  }
    
  public ViewComputationResultModel getFullResult() {
    return _fullResult;
  }

  public ViewDeltaResultModel getDeltaResult() {
    return _deltaMerger.getLatestResult();
  }

  @Override
  public Object apply(ViewResultListener listener) {
    listener.cycleCompleted(getFullResult(), getDeltaResult());
    return null;
  }

}
