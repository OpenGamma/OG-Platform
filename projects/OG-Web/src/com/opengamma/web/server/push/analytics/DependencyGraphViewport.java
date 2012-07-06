/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calc.ComputationCacheQuery;
import com.opengamma.engine.view.calc.ComputationCacheResponse;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class DependencyGraphViewport extends AnalyticsViewport {

  private final String _calcConfigName;
  private final DependencyGraphGridStructure _gridStructure;

  /** {@link ValueSpecification}s for all rows visible in the viewport. */
  private List<ValueSpecification> _viewportValueSpecs = Collections.emptyList();

  DependencyGraphViewport(ViewportSpecification viewportSpec,
                          String calcConfigName,
                          DependencyGraphGridStructure gridStructure,
                          ViewCycle cycle,
                          AnalyticsHistory history,
                          String dataId) {
    super(dataId);
    _calcConfigName = calcConfigName;
    _gridStructure = gridStructure;
    update(viewportSpec, cycle, history);
  }

  /* package */ void update(ViewportSpecification viewportSpec, ViewCycle cycle, AnalyticsHistory history) {
    ArgumentChecker.notNull(viewportSpec, "viewportSpec");
    ArgumentChecker.notNull(cycle, "cycle");
    ArgumentChecker.notNull(history, "history");
    if (!viewportSpec.isValidFor(_gridStructure)) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportSpec + ", grid: " + _gridStructure);
    }
    _viewportSpec = viewportSpec;
    _viewportValueSpecs = _gridStructure.getValueSpecificationsForRows(_viewportSpec.getRows());
    updateResults(cycle, history);
  }

  /* package */ void updateResults(ViewCycle cycle, AnalyticsHistory history) {
    ComputationCacheQuery query = new ComputationCacheQuery();
    Map<ValueSpecification, Object> resultsMap = Maps.newHashMap();
    query.setCalculationConfigurationName(_calcConfigName);
    query.setValueSpecifications(_viewportValueSpecs);
    ComputationCacheResponse cacheResponse = cycle.queryComputationCaches(query);
    List<Pair<ValueSpecification, Object>> results = cacheResponse.getResults();
    for (Pair<ValueSpecification, Object> result : results) {
      ValueSpecification valueSpec = result.getFirst();
      Object value = result.getSecond();
      resultsMap.put(valueSpec, value);
    }
    List<List<Object>> gridResults = _gridStructure.createResultsForViewport(_viewportSpec, resultsMap);
    _latestResults = new ViewportResults(gridResults);
  }
}
