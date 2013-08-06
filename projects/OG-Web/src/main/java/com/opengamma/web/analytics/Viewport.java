/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.view.cycle.ViewCycle;

/**
 * Base class for viewports on grids displaying analytics data. A viewport represents the visible part of a grid.
 * A viewport is defined by collections of row and column indices of the visible cells. These are non-contiguous
 * ordered sets. Row indices can be non-contiguous if the grid rows have a tree structure and parts of the
 * structure are collapsed and therefore not visible. Column indices can be non-contiguous if there is a fixed
 * set of columns and the non-fixed columns have been scrolled. This class isn't thread safe.
 */
/* package */ interface Viewport {

  /**
   * The state of a viewport's data
   */
  enum State {
    /** The viewport contains no data. */
    EMPTY,
    /** The viewport contains data that wasn't updated in the previous calculation cycle. */
    STALE_DATA,
    /** The viewport contains data that was updated in the previous calculation cycle. */
    FRESH_DATA
  }

  /**
   * @return The viewport's data.
   */
  ViewportResults getData();

  /**
   * Updates a viewport, e.g. in response to the user scrolling the grid.
   * @param viewportDefinition Details of the updated viewport
   * @param viewCycle From the previous calculation cycle
   * @param cache Cache of calculation results
   */
  void update(ViewportDefinition viewportDefinition, ViewCycle viewCycle, ResultsCache cache);

  /**
   * @return The viewport's definition
   */
  ViewportDefinition getDefinition();

  /**
   * @return The value sent to listeners to notify them the viewport's data has changed
   */
  String getCallbackId();

  /**
   * @return The viewport's state
   */
  State getState();

  /**
   * Updates the grid structure in the viewport.
   * @param gridStructure The latest structure of the grid
   */
  void updateGridStructure(MainGridStructure gridStructure);

  /**
   * Updates the structure of the tree nodes in the viewport.
   * called when the first set of results arrives after a view def recompilation
   * @param gridStructure The latest structure of the grid
   * @param cache Cache of calculation results
   */
  void updateResultsAndStructure(MainGridStructure gridStructure, ResultsCache cache);

}


