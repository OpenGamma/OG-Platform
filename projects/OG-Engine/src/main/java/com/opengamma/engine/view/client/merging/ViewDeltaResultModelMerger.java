/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import com.opengamma.engine.view.InMemoryViewDeltaResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;

/**
 * Provides the ability to merge {@link ViewResultModel} instances.
 */
public class ViewDeltaResultModelMerger {

  private InMemoryViewDeltaResultModel _currentMergedResult;
  
  public void merge(ViewDeltaResultModel newResult) {
    if (_currentMergedResult == null) {
      // Start of a new result
      _currentMergedResult = new InMemoryViewDeltaResultModel();
      _currentMergedResult.setPreviousCalculationTime(newResult.getPreviousResultTimestamp());
    }
    ViewResultModelMergeUtils.merge(newResult, _currentMergedResult);
  }
  
  public ViewDeltaResultModel getLatestResult() {
    return _currentMergedResult;
  }
  
}
