/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.util.ArgumentChecker;

/**
 * Base class for viewports on grids displaying analytics data. A viewport represents the visible part of a grid.
 * A viewport is defined by collections of row and column indices of the visible cells. These are non-contiguous
 * ordered sets. Row indices can be non-contiguous if the grid rows have a tree structure and parts of the
 * structure are collapsed and therefore not visible. Column indices can be non-contiguous if there is a fixed
 * set of columns and the non-fixed columns have been scrolled. This class isn't thread safe.
 */
/* package */ abstract class AnalyticsViewport {

  /** The ID that is sent to the client to notify it that the viewport's data has been updated. */
  protected final String _callbackId;
  /** Defines the extent of the viewport. */
  protected ViewportDefinition _viewportDefinition;
  /** The current viewport data. */
  protected ViewportResults _latestResults;

  /**
   * @param callbackId The ID that is sent to the client to notify it that the viewport's data has been updated. This
   * can have any unique value, the viewport makes no use of it and make no assumptions about its form.
   */
  /* package */ AnalyticsViewport(String callbackId) {
    ArgumentChecker.notNull(callbackId, "callbackId");
    _callbackId = callbackId;
  }

  /**
   * @return The current viewport data.
   */
  /* package */ ViewportResults getData() {
    return _latestResults;
  }
}
