/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;

/**
 * Represents a call to {@link ViewResultListener#cycleFragmentCompleted(ViewResultModel, ViewDeltaResultModel)}
 */
public class CycleFragmentCompletedCall extends AbstractCompletedResultsCall {

  public CycleFragmentCompletedCall(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    super(fullResult, deltaResult);
  }

  public ViewComputationResultModel getFullFragment() {
    return getViewComputationResultModel();
  }

  public ViewDeltaResultModel getDeltaFragment() {
    return getViewDeltaResultModel();
  }

  @Override
  public Object apply(ViewResultListener listener) {
    listener.cycleFragmentCompleted(getFullFragment(), getDeltaFragment());
    return null;
  }

  @Override
  protected void newFull(final ViewComputationResultModel full) {
    updateFull(full);
  }

}
