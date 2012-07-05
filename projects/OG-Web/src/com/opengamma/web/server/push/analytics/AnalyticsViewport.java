/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import com.opengamma.util.ArgumentChecker;

/**
 * TODO turn this into an interface? there's hardly anything left
 * TODO does there even need to be an interface?
 */
/* package */ abstract class AnalyticsViewport {

  private final String _dataId;

  protected ViewportSpecification _viewportSpec;
  protected ViewportResults _latestResults;

  /* package */ AnalyticsViewport(String dataId) {
    ArgumentChecker.notNull(dataId, "dataId");
    _dataId = dataId;
  }


  /* package */ ViewportResults getData() {
    return _latestResults;
  }

  /* package */ String getDataId() {
    return _dataId;
  }
}
