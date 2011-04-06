/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import com.google.common.base.Function;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessListener;

/**
 * Represents a call to {@link ViewProcessListener#result(com.opengamma.engine.view.ViewComputationResultModel, com.opengamma.engine.view.ViewDeltaResultModel)}
 */
public class ResultCall implements Function<ViewProcessListener, Object> {

  private ViewComputationResultModel _fullResult;
  private final ViewDeltaResultModelMerger _deltaMerger = new ViewDeltaResultModelMerger();
  
  public ResultCall(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    update(fullResult, deltaResult);
  }

  public void update(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    _fullResult = fullResult;
    if (deltaResult != null) {
      _deltaMerger.merge(deltaResult);
    }
  }
  
  @Override
  public Object apply(ViewProcessListener listener) {
    listener.result(_fullResult, _deltaMerger.consume());
    return null;
  }

}
