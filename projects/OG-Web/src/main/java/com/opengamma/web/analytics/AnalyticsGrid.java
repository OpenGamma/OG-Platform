/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;

/**
 * Base class for grids that display analytics data calculated by the engine.
 * @param <V> The type of viewport created and used by this grid.
 */
/* package */ abstract class AnalyticsGrid<V extends Viewport> {

  private static final Logger s_logger = LoggerFactory.getLogger(AnalyticsGrid.class);

  /** Viewports keyed by ID. */
  private final Map<Integer, V> _viewports;

  private final ViewportListener _viewportListener;

  /** ID that's passed to listeners when this grid's row and column structure changes. */
  private final String _callbackId;

  /**
   * Creates an instance.
   * 
   * @param viewportListener  the listener for changes to this grid's viewports, not null
   * @param callbackId  the ID that is passed to listeners when the grid structure changes, any unique value, not null
   */
  /* package */ AnalyticsGrid(ViewportListener viewportListener, String callbackId) {
    ArgumentChecker.notNull(viewportListener, "viewportListener");
    ArgumentChecker.notNull(callbackId, "callbackId");
    _viewportListener = viewportListener;
    _callbackId = callbackId;
    _viewports = Maps.newHashMap();
  }

  /**
   * Creates an instance.
   *
   * @param viewportListener  the listener for changes to this grid's viewports, not null
   * @param callbackId  the ID that is passed to listeners when the grid structure changes, any unique value, not null
   * @param viewports a map of the viewports to be associated with the grid
   */
  /* package */ AnalyticsGrid(ViewportListener viewportListener,
                              String callbackId,
                              Map<Integer, V> viewports) {
    ArgumentChecker.notNull(viewportListener, "viewportListener");
    ArgumentChecker.notNull(callbackId, "callbackId");
    ArgumentChecker.notNull(viewports, "viewports");
    _viewportListener = viewportListener;
    _callbackId = callbackId;
    _viewports = viewports;
  }

  /**
   * @return the row and column structure of the grid
   */
  /* package */ abstract GridStructure getGridStructure();

  /* package */ abstract ViewCycle getViewCycle();

  /* package */ String updateViewport(int viewportId, ViewportDefinition viewportDefinition, ResultsCache cache) {
    V viewport = getViewport(viewportId);
    ViewportDefinition currentViewportDefinition = viewport.getDefinition();
    viewport.update(viewportDefinition, getViewCycle(), cache);
    _viewportListener.viewportUpdated(currentViewportDefinition, viewportDefinition, getGridStructure());
    String callbackId;
    if (viewport.getState() != Viewport.State.EMPTY) {
      callbackId = viewport.getCallbackId();
    } else {
      callbackId = null;
    }
    return callbackId;
  }

  /**
   * Returns a viewport that represents part of the grid that a user is viewing.
   * 
   * @param viewportId  the ID of the viewport
   * @return the viewport, not null
   * @throws DataNotFoundException If no viewport exists with the specified ID
   */
  /* package */ V getViewport(int viewportId) {
    V viewport = _viewports.get(viewportId);
    if (viewport == null) {
      s_logger.debug("Received request for non-existent viewport ID {}", viewportId);
      throw new DataNotFoundException("No viewport found with ID " + viewportId);
    }
    return viewport;
  }

  /**
   * Creates a viewport for viewing this grid's data.
   *
   * @param viewportId  the ID of the viewport, must be unique
   * @param callbackId  the ID that will be passed to listeners when the grid's data changes,
   *  can be any unique value, the grid makes no assumptions about its form
   * @param structureCallbackId  the ID that will be passed to listeners when the grid's structure changes,
   *  can be any unique value, the grid makes no assumptions about its form
   * @param viewportDefinition  defines the extent and properties of the viewport
   * @param cache  the result cache
   * @return true if the viewport has data
   */
  /* package */ boolean createViewport(int viewportId,
                                       String callbackId,
                                       String structureCallbackId,
                                       ViewportDefinition viewportDefinition,
                                       ResultsCache cache) {
    if (_viewports.containsKey(viewportId)) {
      throw new IllegalArgumentException("Viewport ID " + viewportId + " is already in use");
    }
    V viewport = createViewport(viewportDefinition, callbackId, structureCallbackId, cache);
    _viewportListener.viewportCreated(viewportDefinition, getGridStructure());
    boolean hasData = (viewport.getState() != Viewport.State.EMPTY);
    _viewports.put(viewportId, viewport);
    return hasData;
  }

  /**
   * For subclasses to create implementation-specific viewport instances.
   *
   * @param viewportDefinition  defines the extent and properties of the viewport
   * @param callbackId  the ID that will be passed to listeners when the grid's data changes
   * @param structureCallbackId  the ID that will be passed to listeners when the grid's structure changes
   * @param cache  the result cache
   * @return the new viewport and a flag indicating whether there is data available for it
   */
  /* package */ abstract V createViewport(ViewportDefinition viewportDefinition,
                                          String callbackId,
                                          String structureCallbackId,
                                          ResultsCache cache);
  /**
   * Deletes a viewport.
   * 
   * @param viewportId  the ID of the viewport
   * @throws DataNotFoundException if no viewport exists with the specified ID
   */
  /* package */ void deleteViewport(int viewportId) {
    Viewport viewport = _viewports.remove(viewportId);
    if (viewport == null) {
      s_logger.debug("Received request to delete non-existent viewport ID {}", viewportId);
      throw new DataNotFoundException("No viewport found with ID " + viewportId);
    }
    _viewportListener.viewportDeleted(viewport.getDefinition(), getGridStructure());
  }

  /**
   * Returns the current data displayed in the viewport.
   * 
   * @param viewportId  the ID of the viewport
   * @return the current data displayed in the viewport
   */
  /* package */ ViewportResults getData(int viewportId) {
    return getViewport(viewportId).getData();
  }

  /**
   * Gets the callback ID.
   * 
   * @return the ID that's sent to listeners when the row and column structure of the grid is updated
   */
  /* package */ String getCallbackId() {
    return _callbackId;
  }

  /**
   * Gets the viewport listener.
   * 
   * @return the viewport listener
   */
  ViewportListener getViewportListener() {
    return _viewportListener;
  }

  /**
   * Viewports keyed by ID.
   * 
   * @return the map of viewports, not null
   */
  /* package */ Map<Integer, V> getViewports() {
    return _viewports;
  }

}
