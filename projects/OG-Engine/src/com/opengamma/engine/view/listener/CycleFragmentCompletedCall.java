/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.google.common.base.Function;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.client.merging.ViewDeltaResultModelMerger;
import com.opengamma.engine.view.client.merging.ViewComputationResultModelMerger;

/**
 * Represents a call to {@link ViewResultListener#cycleFragmentCompleted(ViewResultModel, ViewDeltaResultModel)}
 */
public class CycleFragmentCompletedCall implements Function<ViewResultListener, Object> {

  private final ViewComputationResultModelMerger _fullFragmentMerger = new ViewComputationResultModelMerger();
  private final ViewDeltaResultModelMerger _deltaFragmentMerger = new ViewDeltaResultModelMerger();

  public CycleFragmentCompletedCall(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    update(fullResult, deltaResult);
  }

  public void update(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
    if (fullFragment != null) {
      _fullFragmentMerger.merge(fullFragment);
    }
    if (deltaFragment != null) {
      _deltaFragmentMerger.merge(deltaFragment);
    }
  }
    
  public ViewComputationResultModel getFullFragment() {
    return _fullFragmentMerger.getLatestResult();
  }

  public ViewDeltaResultModel getDeltaFragment() {
    return _deltaFragmentMerger.getLatestResult();
  }

  @Override
  public Object apply(ViewResultListener listener) {
    listener.cycleFragmentCompleted(getFullFragment(), getDeltaFragment());
    return null;
  }

}
