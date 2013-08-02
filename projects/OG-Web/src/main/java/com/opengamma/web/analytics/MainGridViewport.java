/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.HashSet;
import java.util.Set;

import java.util.List;

import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Viewport on one of the main analytics grids displaying portfolio or primitives data.
 */
/* package */ class MainGridViewport implements Viewport {

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
  // TODO ViewportNodeStructure field
  private final ViewportNodeStructure _nodeStructure;
  /** The current expanded node paths visible in the viewport **/
  private Set<String> _currentNodePaths;

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
    _currentNodePaths = new HashSet<>();
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
   * Updates the viewport definition (e.g. in response to the user scrolling the grid and changing the visible area).
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
    Pair<Integer, Boolean> changedNode = _viewportDefinition.getChangedNode(viewportDefinition);
    // if this is null then the user scrolled the viewport and didn't expand or collapse a node
    if (changedNode != null) {
      Integer rowIndex = changedNode.getFirst();
      // was it expanded or collapsed
      Boolean expanded = changedNode.getSecond();
      List<String> path = _nodeStructure.getPathForRow(rowIndex);
      if (expanded) {
        // TODO add node path to the expanded set
      } else {
        // TODO remove node path from the expanded set
      }
    }
    _viewportDefinition = viewportDefinition;
    updateResults(cache);
  }

  // TODO this will need to go on the interface
  // called when the first set of results arrives after a view def recompilation
  void updateResultsAndStructure(/*grid structure, cache*/) {
    // TODO create a new grid structure using a node from ViewportNodeStructure, assign to _gridStructure
    // TODO call updateResults(cache)
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

  public GridStructure getGridStructure() {
    // TODO return the grid structure with a root node built using ViewportNodeBuilder
  }

  @Override
  public void updateGridStructure(MainGridStructure gridStructure) {
      _gridStructure = gridStructure;
  }
}
