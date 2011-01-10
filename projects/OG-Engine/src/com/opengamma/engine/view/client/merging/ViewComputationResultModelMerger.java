/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ViewComputationResultModel;

/**
 * Provides the ability to merge {@link ViewComputationResultModel} instances.
 */
public class ViewComputationResultModelMerger implements IncrementalMerger<ViewComputationResultModel> {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewComputationResultModelMerger.class);
  
  private ViewComputationResultModel _latestResult;
  
  @Override
  public void merge(ViewComputationResultModel result) {
    // Simplest case of merging - new results supercede old ones.
    s_logger.debug("New result arrived");
    _latestResult = result;
  }

  @Override
  public ViewComputationResultModel consume() {
    s_logger.debug("Consumed result");
    return _latestResult;
  }

}
