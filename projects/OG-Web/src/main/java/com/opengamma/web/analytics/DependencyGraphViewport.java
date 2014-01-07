/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.engine.view.cycle.ComputationCycleQuery;
import com.opengamma.engine.view.cycle.ComputationResultsResponse;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Viewport on a grid displaying the dependency graph showing how a value is calculated. This class isn't thread safe.
 */
public class DependencyGraphViewport implements Viewport {

  /** The calculation configuration used when calculating the value and its ancestor values. */
  private final String _calcConfigName;
  /** The row and column structure of the underlying grid. */
  private final DependencyGraphGridStructure _gridStructure;
  /** The ID that is sent to the client to notify it that the viewport's data has been updated. */
  private final String _callbackId;
  /** The ID that is sent to the client to notify it that the viewport's structure has been updated. */
  private final String _structureCallbackId;

  /** Defines the extent of the viewport. */
  private ViewportDefinition _viewportDefinition;
  /** The current viewport data. */
  private ViewportResults _latestResults;
  /** The current state. Dep graph viewports are never empty, they will always have structure and metadata. */
  private State _state = State.STALE_DATA;

  /**
   * Creates an instance.
   * 
   * @param calcConfigName  the calculation configuration used to calculate the dependency graph
   * @param gridStructure  the row and column structure of the grid
   * @param callbackId  the ID that's passed to listeners when the viewport's data changes
   * @param structureCallbackId  the ID that's passed to listeners when the viewport's structure changes
   *  TODO not used currently
   * @param viewportDefinition  the viewport definition
   * @param cycle  the view cycle from the previous calculation cycle
   * @param cache  the current results TODO should this be a new cache?
   *  if all depgraphs share the main cache it won't get cleaned up when they close. is there a good reason to share
   *  the cache? could this just be a new instance?
   */
  /* package */ DependencyGraphViewport(String calcConfigName,
                                        DependencyGraphGridStructure gridStructure,
                                        String callbackId,
                                        String structureCallbackId,
                                        ViewportDefinition viewportDefinition,
                                        ViewCycle cycle,
                                        ResultsCache cache) {
    _structureCallbackId = structureCallbackId;
    ArgumentChecker.notEmpty(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notEmpty(callbackId, "callbackId");
    _calcConfigName = calcConfigName;
    _gridStructure = gridStructure;
    _callbackId = callbackId;
    update(viewportDefinition, cycle, cache);
  }

  /**
   * Updates the viewport, e.g. in response to the user scrolling the grid.
   * 
   * @param viewportDefinition  the definition of the viewport, not null
   * @param cycle  the cycle used to calculate the latest set of results, not null
   * @param cache  the cache of results for the grid, not null
   */
  @Override
  public void update(ViewportDefinition viewportDefinition, ViewCycle cycle, ResultsCache cache) {
    ArgumentChecker.notNull(viewportDefinition, "viewportSpec");
    ArgumentChecker.notNull(cycle, "cycle");
    ArgumentChecker.notNull(cache, "cache");
    if (!viewportDefinition.isValidFor(_gridStructure)) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportDefinition + ", grid: " + _gridStructure);
    }
    _viewportDefinition = viewportDefinition;
    updateResults(cycle, cache);
  }

  /**
   * Updates the data in the viewport when a new set of results arrives from the calculation engine.
   * 
   * @param cycle  the view cycle, not null
   * @param cache  the cache of results, not null
   */
  /* package */void updateResults(ViewCycle cycle, ResultsCache cache) {
    ComputationCycleQuery query = new ComputationCycleQuery();
    query.setCalculationConfigurationName(_calcConfigName);
    query.setValueSpecifications(_gridStructure.getValueSpecifications());
    ComputationResultsResponse resultsResponse = cycle.queryResults(query);
    cache.put(_calcConfigName, resultsResponse.getResults(), cycle.getDuration());
    Pair<ViewportResults, State> resultsAndState = _gridStructure.createResults(_viewportDefinition, cache, _latestResults);
    _latestResults = resultsAndState.getFirst();
    _state = resultsAndState.getSecond();
  }

  @Override
  public GridStructure getGridStructure() {
    return _gridStructure;
  }

  @Override
  public ViewportResults getData() {
    return _latestResults;
  }

  @Override
  public ViewportDefinition getDefinition() {
    return _viewportDefinition;
  }

  @Override
  public String getCallbackId() {
    return _callbackId;
  }

  @Override
  public State getState() {
    return _state;
  }

  @Override
  public String getStructureCallbackId() {
    return _structureCallbackId;
  }
}
