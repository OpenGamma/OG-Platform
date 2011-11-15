/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.google.common.base.Function;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.client.merging.ViewDeltaResultModelMerger;

/**
 * Represents a call to {@link ViewResultListener#jobResultReceived(ViewResultModel, ViewDeltaResultModel)}
 */
public class JobResultReceivedCall implements Function<ViewResultListener, Object> {

  private ViewResultModel _fullResult;
  private final ViewDeltaResultModelMerger _deltaMerger = new ViewDeltaResultModelMerger();

  public JobResultReceivedCall(ViewResultModel fullResult, ViewDeltaResultModel deltaResult) {
    update(fullResult, deltaResult);
  }

  public void update(ViewResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _fullResult = fullResult;
    if (deltaResult != null) {
      _deltaMerger.merge(deltaResult);
    }
  }
    
  public ViewResultModel getFullResult() {
    return _fullResult;
  }

  public ViewDeltaResultModel getDeltaResult() {
    return _deltaMerger.getLatestResult();
  }

  @Override
  public Object apply(ViewResultListener listener) {
    listener.jobResultReceived(getFullResult(), getDeltaResult());
    return null;
  }

}
