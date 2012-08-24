/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import com.opengamma.util.ArgumentChecker;

/**
 * Base class for viewports on grids displaying analytics data. A viewport represents the visible part of a grid.
 * A viewport is defined by collections of row and column indices of the visible cells. These are non-contiguous
 * ordered sets. Row indices can be non-contiguous if the grid rows have a tree structure and parts of the
 * structure are collapsed and therefore not visible. Column indices can be non-contiguous if there is a fixed
 * set of columns and the non-fixed columns have been scrolled.
 */
/* package */ abstract class AnalyticsViewport {

  protected final String _dataId;

  /** Defines the extent of the viewport. */
  protected ViewportSpecification _viewportSpec;
  /** The current viewport data. */
  protected ViewportResults _latestResults;
  /**
   * The version of the viewport. When a viewport is updated (by the user scrolling the grid for example) the
   * version is incremented. This version number is returned to the client when the viewport is updated and it is also
   * included in the viewport data. This allows clients to ensure the data they receive matches the viewport.
   * Without this there would be a race condition if the client updates the viewport when it has a pending request
   * for data. It is possible that the data it receives will apply to the previous version of the viewport.
   */
  protected long _version;

  /**
   * @param dataId The ID that is sent to the client to notify it that the viewport's data has been updated. This
   * can have any unique value, the viewport makes no use of it and make no assumptions about its form.
   */
  /* package */ AnalyticsViewport(String dataId) {
    ArgumentChecker.notNull(dataId, "dataId");
    _dataId = dataId;
  }

  /**
   * @return The current viewport data.
   */
  /* package */ ViewportResults getData() {
    return _latestResults;
  }


  /**
   * @return The ID that is sent to the client to notify it that the viewport's data has been updated.
   */
  /* package */ String getDataId() {
    return _dataId;
  }

  /**
   * @return The current version of the viewport.
   */
  /* package */ long getVersion() {
    return _version;
  }
}
