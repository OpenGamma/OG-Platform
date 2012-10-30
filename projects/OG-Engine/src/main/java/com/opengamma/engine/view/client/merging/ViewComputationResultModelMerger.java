/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewResultModel;

/**
 * Provides the ability to merge {@link ViewResultModel} instances.
 */
public class ViewComputationResultModelMerger {

  private InMemoryViewComputationResultModel _currentMergedResult;
  
  /**
   * Adds a new result.
   * 
   * @param newResult  the new result to merge
   */
  public void merge(ViewComputationResultModel newResult) {
    if (_currentMergedResult == null) {
      // Start of a new result
      _currentMergedResult = new InMemoryViewComputationResultModel();
    }
    for (ComputedValue marketData : newResult.getAllMarketData()) {
      _currentMergedResult.addMarketData(marketData);
    }
    ViewResultModelMergeUtils.merge(newResult, _currentMergedResult);
  }

  /**
   * Retrieves the latest merged result.
   * 
   * @return  the latest merged result
   */
  public ViewComputationResultModel getLatestResult() {
    return _currentMergedResult;
  }
  
}
