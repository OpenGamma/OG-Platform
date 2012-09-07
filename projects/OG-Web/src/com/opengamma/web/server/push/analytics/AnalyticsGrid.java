/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.util.ArgumentChecker;

/**
 * Base class for grids that display analytics data calculated by the engine.
 * @param <V> The type of viewport created and used by this grid.
 */
/* package */ abstract class AnalyticsGrid<V extends AnalyticsViewport> {

  /** Viewports keyed by ID. */
  protected final Map<Integer, V> _viewports = Maps.newHashMap();

  /** ID that's passed to listeners when this grid's row and column structure changes. */
  private final String _callbackId;

  /**
   * @param callbackId The ID that is passed to listeners when the grid structure changes. This can be any unique value,
   * the grid doesn't use it and makes no assumptions about its form.
   */
  protected AnalyticsGrid(String callbackId) {
    ArgumentChecker.notNull(callbackId, "callbackId");
    _callbackId = callbackId;
  }

  /**
   * @return The row and column structure of the grid
   */
  public abstract GridStructure getGridStructure();

  /**
   * Returns a viewport that represents part of the grid that a user is viewing.
   * @param viewportId ID of the viewport
   * @return The viewort
   * @throws DataNotFoundException If no viewport exists with the specified ID
   */
  protected V getViewport(int viewportId) {
    V viewport = _viewports.get(viewportId);
    if (viewport == null) {
      throw new DataNotFoundException("No viewport found with ID " + viewportId);
    }
    return viewport;
  }

  /**
   * Creates a viewport for viewing this grid's data.
   * @param viewportId ID of the viewport, must be unique
   * @param callbackId ID that will be passed to listeners when the grid's data changes, can be any unique value, the
   * grid makes no assumptions about its form
   * @param viewportSpecification Defines the extent and properties of the viewport
   * @return The version number of the new viewport
   */
  /* package */ long createViewport(int viewportId, String callbackId, ViewportSpecification viewportSpecification) {
    if (_viewports.containsKey(viewportId)) {
      throw new IllegalArgumentException("Viewport ID " + viewportId + " is already in use");
    }
    V viewport = createViewport(viewportSpecification, callbackId);
    _viewports.put(viewportId, viewport);
    return viewport.getVersion();
  }

  /**
   * For subclasses to create implementation-specific viewport instances.
   * @param viewportSpecification Defines the extent and properties of the viewport
   * @param callbackId ID that will be passed to listeners when the grid's data changes
   * @return The new viewport
   */
  protected abstract V createViewport(ViewportSpecification viewportSpecification, String callbackId);

  /**
   * Deletes a viewport.
   * @param viewportId ID of the viewport
   * @throws DataNotFoundException If no viewport exists with the specified ID
   */
  /* package */ void deleteViewport(int viewportId) {
    AnalyticsViewport viewport = _viewports.remove(viewportId);
    if (viewport == null) {
      throw new DataNotFoundException("No viewport found with ID " + viewportId);
    }
  }

  /**
   * Returns the current data displayed in the viewport.
   * @param viewportId ID of the viewport
   * @return The current data displayed in the viewport
   */
  /* package */ ViewportResults getData(int viewportId) {
    return getViewport(viewportId).getData();
  }

  /**
   * @return ID that's sent to listeners when the row and column structure of the grid is updated
   */
  /* package */ String getCallbackId() {
    return _callbackId;
  }
}
