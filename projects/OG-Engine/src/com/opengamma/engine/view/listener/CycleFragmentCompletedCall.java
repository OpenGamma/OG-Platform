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
import com.opengamma.engine.view.client.merging.ViewResultModelMerger;

/**
 * Represents a call to {@link ViewResultListener#cycleFragmentCompleted(ViewResultModel, ViewDeltaResultModel)}
 */
public class CycleFragmentCompletedCall implements Function<ViewResultListener, Object> {

  private final ViewResultModelMerger _fullFragmentMerger = new ViewResultModelMerger();
  private final ViewDeltaResultModelMerger _deltaFragmentMerger = new ViewDeltaResultModelMerger();

  public CycleFragmentCompletedCall(ViewResultModel fullResult, ViewDeltaResultModel deltaResult) {
    update(fullResult, deltaResult);
  }

  public void update(ViewResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
    if (fullFragment != null) {
      _fullFragmentMerger.merge(fullFragment);
    }
    if (deltaFragment != null) {
      _deltaFragmentMerger.merge(deltaFragment);
    }
  }
    
  public ViewResultModel getFullFragment() {
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
