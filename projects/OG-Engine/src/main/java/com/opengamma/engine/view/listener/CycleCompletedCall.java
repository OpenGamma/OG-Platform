/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;

/**
 * Represents a call to {@link ViewResultListener#cycleCompleted(com.opengamma.engine.view.ViewComputationResultModel, com.opengamma.engine.view.ViewDeltaResultModel)}
 */
public class CycleCompletedCall extends AbstractCompletedResultsCall {

  private static final Logger s_logger = LoggerFactory.getLogger(CycleCompletedCall.class);

  public CycleCompletedCall(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    super(fullResult, deltaResult);
  }

  public ViewComputationResultModel getFullResult() {
    return getViewComputationResultModel();
  }

  public ViewDeltaResultModel getDeltaResult() {
    return getViewDeltaResultModel();
  }

  @Override
  public Object apply(ViewResultListener listener) {
    listener.cycleCompleted(getFullResult(), getDeltaResult());
    return null;
  }

  @Override
  protected void newResult(final ViewComputationResultModel full) {
    // New full result replaces the old one
    s_logger.debug("New full result replaces previous one");
    setViewComputationResultModel(full);
  }

  @Override
  protected void ambiguousResult(final ViewComputationResultModel full) {
    // Two results calculated so close together they appear "at the same time". Better merge them, but the result might be wrong.
    s_logger.warn("Merging two results both calculated at the same time");
    getViewComputationResultModelCopy().update(full);
  }

  @Override
  protected void oldResult(final ViewComputationResultModel full) {
    // The previous full result is newer than the new one - discard the new one
    s_logger.info("Ignoring full result that is older that the previously received one");
  }

}
