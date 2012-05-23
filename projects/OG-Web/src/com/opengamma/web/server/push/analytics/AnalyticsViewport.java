/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import com.opengamma.engine.view.ViewComputationResultModel;

/**
 *
 */
/* package */ class AnalyticsViewport {

  private final String _id;

  private AnalyticsViewport(String id) {
    _id = id;
  }

  /**
   * @return An empty viewport with no rows or columns
   */
  /* package */ static AnalyticsViewport empty() {
    // TODO implement AnalyticsViewport.empty()
    throw new UnsupportedOperationException("empty not implemented");
  }

  /* package */ AnalyticsViewport updateResults(ViewComputationResultModel fullResult, AnalyticsHistory history) {
    // TODO implement AnalyticsViewport.updateResults()
    throw new UnsupportedOperationException("updateResults not implemented");
  }

  /* package */ static AnalyticsViewport create(ViewportRequest request,
                                                AnalyticsHistory history,
                                                ViewComputationResultModel results) {
    // TODO implement AnalyticsViewport.create()
    throw new UnsupportedOperationException("create not implemented");
  }

  /* package */ String getId() {
    return _id;
  }
}
