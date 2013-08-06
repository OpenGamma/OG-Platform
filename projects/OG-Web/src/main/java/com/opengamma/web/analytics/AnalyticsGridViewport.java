/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

public class AnalyticsGridViewport extends MainGridViewport {

  /** The node structure. */
  private final ViewportNodeStructure _nodeStructure;
  /** The current expanded paths. */
  private Set<List<String>> _currentExpandedPaths;

  /**
   * @param gridStructure Row and column structure of the grid
   * @param callbackId ID that's passed to listeners when the grid structure changes
   * @param viewportDefinition The viewport definition
   * @param cycle The view cycle from the previous calculation cycle
   * @param cache The current results
   */
  AnalyticsGridViewport(MainGridStructure gridStructure,
                        String callbackId,
                        ViewportDefinition viewportDefinition,
                        ViewCycle cycle,
                        ResultsCache cache) {
    super(gridStructure, callbackId, viewportDefinition, cycle, cache);
    _nodeStructure = new ViewportNodeStructure(getGridStructure().getRootNode(), getGridStructure().getTargetLookup());
    _currentExpandedPaths = new HashSet<>(_nodeStructure.getPaths());
  }


  /**
   * Updates the structure of the tree nodes in the viewport.
   * called when the first set of results arrives after a view def recompilation
   * @param gridStructure The latest structure of the grid
   * @param cache Cache of calculation results
   */
  public void updateResultsAndStructure(MainGridStructure gridStructure, ResultsCache cache) {
    ViewportNodeStructure node = new ViewportNodeStructure(getGridStructure().getRootNode(),
                                                           getGridStructure().getTargetLookup(),
                                                           _currentExpandedPaths);
    //_gridStructure = _gridStructure.withNode(node);
    updateResults(cache);
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
    if (!viewportDefinition.isValidFor(getGridStructure())) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportDefinition + ", grid: " + getGridStructure());
    }
    if (getDefinition()  != null) {
      Pair<Integer, Boolean> changedNode = getDefinition() .getChangedNode(viewportDefinition);
      // if this is null then the user scrolled the viewport and didn't expand or collapse a node
      if (changedNode != null) {
        Integer rowIndex = changedNode.getFirst();
        // was it expanded or collapsed
        Boolean expanded = changedNode.getSecond();
        List<String> path = _nodeStructure.getPathForRow(rowIndex);
        System.out.println("Row: " + rowIndex.toString() + " Expanded: " + expanded.toString() + " Path: " + path);
        if (expanded) {
          _currentExpandedPaths.add(path);
        } else {
          _currentExpandedPaths.remove(path);
        }
        System.out.println("Current: " + _currentExpandedPaths);
      }
    }
    setViewportDefinition(viewportDefinition);
    updateResults(cache);
  }

}
