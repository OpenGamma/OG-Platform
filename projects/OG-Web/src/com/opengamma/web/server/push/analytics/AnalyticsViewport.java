/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ abstract class AnalyticsViewport {

  protected final String _dataId;

  protected ViewportSpecification _viewportSpec;
  protected ViewportResults _latestResults;
  protected long _version;

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

  /* package */ long getVersion() {
    return _version;
  }
}
