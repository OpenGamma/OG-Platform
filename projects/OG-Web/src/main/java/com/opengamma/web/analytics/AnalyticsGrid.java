/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Base class for grids that display analytics data calculated by the engine.
 * @param <V> The type of viewport created and used by this grid.
 */
/* package */ abstract class AnalyticsGrid<V extends AnalyticsViewport> {

  /** Viewports keyed by ID. */
  protected final Map<Integer, V> _viewports = Maps.newHashMap();

  private final ViewportListener _viewportListener;

  /** ID that's passed to listeners when this grid's row and column structure changes. */
  private final String _callbackId;

  /**
   * @param viewportListener
   * @param callbackId The ID that is passed to listeners when the grid structure changes. This can be any unique value,
   */
  protected AnalyticsGrid(ViewportListener viewportListener, String callbackId) {
    ArgumentChecker.notNull(viewportListener, "viewportListener");
    ArgumentChecker.notNull(callbackId, "callbackId");
    _viewportListener = viewportListener;
    _callbackId = callbackId;
  }

  /**
   * @return The row and column structure of the grid
   */
  public abstract GridStructure getGridStructure();

  protected abstract ViewCycle getViewCycle();

  protected abstract ResultsCache getResultsCache();

  /* package */ String updateViewport(int viewportId, ViewportDefinition viewportDefinition) {
    V viewport = getViewport(viewportId);
    ViewportDefinition currentViewportDefinition = viewport.getDefinition();
    String callbackId = viewport.update(viewportDefinition, getViewCycle(), getResultsCache());
    _viewportListener.viewportUpdated(currentViewportDefinition, viewportDefinition, getGridStructure());
    return callbackId;
  }

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
   *
   * @param viewportId ID of the viewport, must be unique
   * @param callbackId ID that will be passed to listeners when the grid's data changes, can be any unique value, the
   * grid makes no assumptions about its form
   * @param viewportDefinition Defines the extent and properties of the viewport
   * @return {@code true} if the viewport has data
   */
  /* package */ boolean createViewport(int viewportId, String callbackId, ViewportDefinition viewportDefinition) {
    if (_viewports.containsKey(viewportId)) {
      throw new IllegalArgumentException("Viewport ID " + viewportId + " is already in use");
    }
    Pair<V, Boolean> pair = createViewport(viewportDefinition, callbackId);
    V viewport = pair.getFirst();
    boolean hasData = pair.getSecond();
    _viewports.put(viewportId, viewport);
    return hasData;
  }

  /**
   * For subclasses to create implementation-specific viewport instances.
   *
   * @param viewportDefinition Defines the extent and properties of the viewport
   * @param callbackId ID that will be passed to listeners when the grid's data changes
   * @return The new viewport and a flag indicating whether there is data available for it
   */
  protected abstract Pair<V, Boolean> createViewport(ViewportDefinition viewportDefinition, String callbackId);

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
