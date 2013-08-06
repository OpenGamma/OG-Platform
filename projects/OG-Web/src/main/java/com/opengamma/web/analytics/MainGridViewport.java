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
/* package */ abstract class MainGridViewport implements Viewport {

  /** Row and column structure of the grid. */
  private MainGridStructure _gridStructure;
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
    _viewportDefinition = viewportDefinition;
    update(viewportDefinition, cycle, cache);


  }

  /**
   * Updates the data in the viewport using the results in the cache.
   * @param cache The latest results
   */
  /* package */ void updateResults(ResultsCache cache) {
    Pair<ViewportResults, State> resultsAndState = getGridStructure().createResults(getDefinition() , cache);
    _latestResults = resultsAndState.getFirst();
    _state = resultsAndState.getSecond();
  }


  MainGridStructure getGridStructure() {
    return _gridStructure;
  }

  @Override
  public ViewportResults getData() {
    return _latestResults;
  }

  void setViewportDefinition(ViewportDefinition viewportDefinition) {
    _viewportDefinition = viewportDefinition;
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

  @Override
  public void updateGridStructure(MainGridStructure gridStructure) {
      _gridStructure = gridStructure;
  }

}
