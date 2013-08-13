/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Viewport on one of the main analytics grids displaying portfolio or primitives data.
 */
/* package */ abstract class MainGridViewport implements Viewport {

  /** The ID that is sent to the client to notify it that the viewport's data has been updated. */
  private final String _callbackId;
  /** Defines the extent of the viewport. */
  private ViewportDefinition _viewportDefinition;
  /** The current viewport data. */
  private ViewportResults _latestResults;
  /** The current state. */
  private State _state = State.EMPTY;

  /**
   * @param callbackId ID that's passed to listeners when the grid structure changes
   * @param viewportDefinition The viewport definition
   */
  /* package */ MainGridViewport(String callbackId,
                                 ViewportDefinition viewportDefinition) {
    ArgumentChecker.notEmpty(callbackId, "callbackId");
    _callbackId = callbackId;
    _viewportDefinition = viewportDefinition;
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

  public abstract MainGridStructure getGridStructure();

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

}
