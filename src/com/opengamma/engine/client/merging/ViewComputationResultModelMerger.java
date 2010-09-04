/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.client.merging;

import com.opengamma.engine.view.ViewComputationResultModel;

/**
 * Provides the ability to merge {@link ViewComputationResultModel} instances.
 */
public class ViewComputationResultModelMerger implements IncrementalMerger<ViewComputationResultModel> {

  private ViewComputationResultModel _latestResult;
  
  @Override
  public void merge(ViewComputationResultModel result) {
    // Simplest case of merging - new results supercede old ones.
    _latestResult = result;
  }

  @Override
  public ViewComputationResultModel consume() {
    return _latestResult;
  }

}
