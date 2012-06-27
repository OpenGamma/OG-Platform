/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewTargetResultModel;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class AnalyticsViewport {

  private final AnalyticsGridStructure _gridStructure;
  private final String _dataId;

  private ViewportSpecification _viewportSpec;
  private ViewportResults _latestResults;

  /* package */ AnalyticsViewport(AnalyticsGridStructure gridStructure,
                                  ViewportSpecification viewportSpec,
                                  ViewComputationResultModel latestResults,
                                  AnalyticsHistory history,
                                  String dataId) {
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notNull(viewportSpec, "viewportSpec");
    ArgumentChecker.notNull(latestResults, "latestResults");
    ArgumentChecker.notNull(history, "history");
    ArgumentChecker.notNull(dataId, "dataId");
    _gridStructure = gridStructure;
    _dataId = dataId;
    update(viewportSpec, latestResults, history);
  }

  // TODO this is specific to the main grids, need separate subclasses for depgraphs?
  /* package */ void updateResults(ViewComputationResultModel results, AnalyticsHistory history) {
    /*
    get the target for each row in the viewport from the grid structure
    query the results for the results for the target
    for each value get the column index from the grid structure
    if the column is in the viewport update the results
    */
    // TODO should this logic go in ViewportResults?
    List<List<Object>> allResults = new ArrayList<List<Object>>();
    for (Integer rowIndex : _viewportSpec.getRows()) {
      AnalyticsGridStructure.Row row = _gridStructure.getRowAtIndex(rowIndex);
      ComputationTargetSpecification target = row.getTarget();
      Map<Integer, Object> rowResultsMap = new TreeMap<Integer, Object>();
      ViewTargetResultModel targetResult = results.getTargetResult(target);
      if (targetResult != null) {
        for (String calcConfigName : targetResult.getCalculationConfigurationNames()) {
          for (ComputedValue value : targetResult.getAllValues(calcConfigName)) {
            ValueSpecification valueSpec = value.getSpecification();
            Set<ValueRequirement> valueReqs =
                _gridStructure.getColumns().getRequirementsForSpecification(calcConfigName, valueSpec);
            for (ValueRequirement req : valueReqs) {
              int colIndex = _gridStructure.getColumnIndexForRequirement(calcConfigName, req);
              if (_viewportSpec.getColumns().contains(colIndex)) {
                rowResultsMap.put(colIndex, value.getValue());
              }
            }
          }
        }
      }
      String rowName = row.getName();
      List<Object> rowResults = new ArrayList<Object>(_viewportSpec.getColumns().size() + 1);
      // row label always goes in the first column
      rowResults.add(rowName);
      for (int colIndex = 1; colIndex < _gridStructure.getColumns().getColumnCount(); colIndex++) {
        if (_viewportSpec.getColumns().contains(colIndex)) {
          // this intentionally inserts null into the results if there is no value for a given column
          rowResults.add(rowResultsMap.get(colIndex));
        }
      }
      allResults.add(rowResults);
    }
    _latestResults = new ViewportResults(allResults);
  }

  /* package */ ViewportResults getData() {
    return _latestResults;
  }

  public void update(ViewportSpecification viewportSpec, ViewComputationResultModel results, AnalyticsHistory history) {
    ArgumentChecker.notNull(viewportSpec, "viewportSpec");
    ArgumentChecker.notNull(results, "results");
    ArgumentChecker.notNull(history, "history");
    if (!viewportSpec.isValidFor(_gridStructure)) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportSpec + ", grid: " + _gridStructure);
    }
    _viewportSpec = viewportSpec;
    updateResults(results, history);
  }

  public String getDataId() {
    return _dataId;
  }
}
