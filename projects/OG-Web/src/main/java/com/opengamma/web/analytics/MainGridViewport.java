/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Viewport on one of the main analytics grids displaying portfolio or primitives data.
 */
/* package */ class MainGridViewport implements Viewport {

  /** Row and column structure of the grid. */
  private final MainGridStructure _gridStructure;
  /** The ID that is sent to the client to notify it that the viewport's data has been updated. */
  private final String _callbackId;

  /** Defines the extent of the viewport. */
  private ViewportDefinition _viewportDefinition;
  /** The current viewport data. */
  private ViewportResults _latestResults;
  /** The current state. */
  private State _state = State.EMPTY;

  /**
   * @param gridStructure Row and column structure of the grid
   * @param callbackId ID that's passed to listeners when the grid structure changes
   * @param viewportDefinition The viewport definition
   * @param cycle The view cycle from the previous calculation cycle
   * @param cache The current results
   */
  /* package */ MainGridViewport(MainGridStructure gridStructure,
                                 String callbackId,
                                 ViewportDefinition viewportDefinition,
                                 ViewCycle cycle,
                                 ResultsCache cache) {
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notEmpty(callbackId, "callbackId");
    _callbackId = callbackId;
    _gridStructure = gridStructure;
    update(viewportDefinition, cycle, cache);
  }

  /**
   * Updates the data in the viewport using the results in the cache.
   * @param cache The latest results
   */
  /* package */ void updateResults(ResultsCache cache) {
    Pair<ViewportResults, State> resultsAndState = _gridStructure.createResults(_viewportDefinition, cache);
    _latestResults = resultsAndState.getFirst();
    _state = resultsAndState.getSecond();
  }

  /**
   * Updates the viewport definition (e.g. in reponse to the user scrolling the grid and changing the visible area).
   * @param viewportDefinition The new viewport definition
   * @param viewCycle The view cycle from the previous calculation cycle
   * @param cache The current results
   */
  @Override
  public void update(ViewportDefinition viewportDefinition, ViewCycle viewCycle, ResultsCache cache) {
    ArgumentChecker.notNull(viewportDefinition, "viewportDefinition");
    ArgumentChecker.notNull(cache, "cache");
    if (!viewportDefinition.isValidFor(_gridStructure)) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportDefinition + ", grid: " + _gridStructure);
    }
    _viewportDefinition = viewportDefinition;
    updateResults(cache);
  }

  @Override
  public ViewportResults getData() {
    return _latestResults;
  }

  @Override
  public ViewportDefinition getDefinition() {
    return _viewportDefinition;
  }

  public String getCallbackId() {
    return _callbackId;
  }

  @Override
  public State getState() {
    return _state;
  }
}
