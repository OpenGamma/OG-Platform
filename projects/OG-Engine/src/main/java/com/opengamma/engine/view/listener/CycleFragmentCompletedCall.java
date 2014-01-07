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
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;

/**
 * Represents a call to {@link ViewResultListener#cycleFragmentCompleted(ViewResultModel, ViewDeltaResultModel)}
 */
public class CycleFragmentCompletedCall extends AbstractCompletedResultsCall {

  private static final Logger s_logger = LoggerFactory.getLogger(CycleFragmentCompletedCall.class);

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
  protected void newResult(final ViewComputationResultModel full) {
    // New full result updates the old one
    s_logger.debug("New full result updates previous one");
    getViewComputationResultModelCopy().update(full);
  }

  @Override
  protected void ambiguousResult(final ViewComputationResultModel full) {
    // This result predates the current value, so swap and merge to get ordering right. Fragments are commutative so ordering shouldn't
    // matter unless results get calculated multiple times with different values for each because of job failure/resubmission.
    s_logger.debug("Merging two results both calculated at the same time");
    getViewComputationResultModelCopy().update(full);
  }

  @Override
  protected void oldResult(final ViewComputationResultModel full) {
    // This result predates the current value, so swap and merge to get ordering right. Fragments are commutative so ordering shouldn't
    // matter unless results get calculated multiple times with different values for each because of job failure/resubmission.
    s_logger.debug("Applying old result to new baseline result fragment");
    final InMemoryViewComputationResultModel newResult = new InMemoryViewComputationResultModel(full);
    newResult.update(getViewComputationResultModel());
    setViewComputationResultModelCopy(newResult);
  }

}
