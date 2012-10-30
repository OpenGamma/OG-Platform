/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import com.opengamma.engine.view.calc.ComputationCacheQuery;
import com.opengamma.engine.view.calc.ComputationCacheResponse;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.util.ArgumentChecker;

/**
 * Viewport on a grid displaying the dependency graph showing how a value is calculated. This class isn't thread safe.
 */
public class DependencyGraphViewport extends AnalyticsViewport {

  /** The calculation configuration used when calculating the value and its ancestor values. */
  private final String _calcConfigName;
  /** The row and column structure of the underlying grid. */
  private final DependencyGraphGridStructure _gridStructure;

  /**
   * @param calcConfigName Calculation configuration used to calculate the dependency graph
   * @param gridStructure Row and column structure of the grid
   * @param cycle Calculation cycle that produced the most recent results
   * @param cache Cache of calculation results used to populate the viewport's data
   * @param callbackId ID that's passed to listeners when the viewport's data changes
   */
  /* package */ DependencyGraphViewport(ViewportDefinition viewportDefinition,
                                        String calcConfigName,
                                        DependencyGraphGridStructure gridStructure,
                                        ViewCycle cycle,
                                        ResultsCache cache,
                                        String callbackId) {
    super(callbackId);
    _calcConfigName = calcConfigName;
    _gridStructure = gridStructure;
    update(viewportDefinition, cycle, cache);
  }

  /**
   * Updates the viewport, e.g. in response to the user scrolling the grid.
   *
   * @param cycle The cycle used to calculate the latest set of results
   * @param cache Cache of results for the grid
   * @return Version number of the viewport, allows the client to confirm a set of results corresponds to the
   * current state of the viewport
   */
  /* package */ long update(ViewportDefinition viewportDefinition, ViewCycle cycle, ResultsCache cache) {
    ArgumentChecker.notNull(viewportDefinition, "viewportSpec");
    ArgumentChecker.notNull(cycle, "cycle");
    ArgumentChecker.notNull(cache, "cache");
    if (!viewportDefinition.isValidFor(_gridStructure)) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportDefinition + ", grid: " + _gridStructure);
    }
    _viewportDefinition = viewportDefinition;
    ++_version;
    updateResults(cycle, cache);
    return _version;
  }

  /**
   * Updates the data in the viewport when a new set of results arrives from the calculation engine.
   * @param cycle Calculation cycle that calculated the results
   * @param cache Cache of results
   * @return ID of the viewport's data which is passed to listeners to notify them the data has changed
   */
  /* package */ String updateResults(ViewCycle cycle, ResultsCache cache) {
    ComputationCacheQuery query = new ComputationCacheQuery();
    query.setCalculationConfigurationName(_calcConfigName);
    query.setValueSpecifications(_gridStructure.getValueSpecifications());
    ComputationCacheResponse cacheResponse = cycle.queryComputationCaches(query);
    cache.put(_calcConfigName, cacheResponse.getResults(), cycle.getDuration());
    List<ViewportResults.Cell> gridResults = _gridStructure.createResultsForViewport(_viewportDefinition, cache, _calcConfigName);
    ViewportResults newResults = new ViewportResults(gridResults,
                                                     _viewportDefinition,
                                                     _gridStructure.getColumnStructure(),
                                                     _version,
                                                     cache.getLastCalculationDuration());
    String callbackId;
    if (newResults.equals(_latestResults)) {
      // return null to signal the results haven't changed
      callbackId = null;
    } else {
      // results have changed, return the viewport's callback ID so the client is notified
      callbackId = _callbackId;
    }
    _latestResults = newResults;
    return callbackId;
  }
}
