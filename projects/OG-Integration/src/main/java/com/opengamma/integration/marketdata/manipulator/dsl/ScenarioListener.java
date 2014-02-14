/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.livedata.UserPrincipal;

/**
 *
 */
/* package */ class ScenarioListener extends AbstractViewResultListener {

  private static final Logger s_logger = LoggerFactory.getLogger(ScenarioListener.class);

  @Override
  public UserPrincipal getUser() {
    return UserPrincipal.getLocalUser();
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    s_logger.info("cycle completed");
  }

  @Override
  public void processCompleted() {
    s_logger.info("process completed");
  }

  // TODO this needs to block until the results are available
  /* package */ ScenarioResults getResults() {
    throw new UnsupportedOperationException();
  }
}
