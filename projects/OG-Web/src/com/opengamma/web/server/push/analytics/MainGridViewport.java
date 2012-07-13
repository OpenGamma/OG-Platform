/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.ViewTargetResultModel;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class MainGridViewport extends AnalyticsViewport {

  private final MainGridStructure _gridStructure;

  MainGridViewport(ViewportSpecification viewportSpec,
                   MainGridStructure gridStructure,
                   String dataId,
                   ViewResultModel latestResults,
                   AnalyticsHistory history) {
    super(dataId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    _gridStructure = gridStructure;
    update(viewportSpec, latestResults, history);
  }

  // TODO this method makes my head hurt. REFACTOR
  // TODO these need to be delta results
  // TODO results cache built from successive deltas. so new viewports have data without waiting for the cycle to finish
  // TODO cache should be at the view level so it can be shared with the primitives grid
  /* package */ String updateResults(ViewResultModel results, AnalyticsHistory history) {
    boolean updated = false;
    List<List<ViewportResults.Cell>> allResults = new ArrayList<List<ViewportResults.Cell>>();
    // iterate over each row in the viewport
    for (Integer rowIndex : _viewportSpec.getRows()) {
      MainGridStructure.Row row = _gridStructure.getRowAtIndex(rowIndex);
      ComputationTargetSpecification target = row.getTarget();
      // results for one row in the viewport, keyed by column index
      Map<Integer, ViewportResults.Cell> rowResultsMap = new TreeMap<Integer, ViewportResults.Cell>();
      ViewTargetResultModel targetResult = results.getTargetResult(target);
      if (targetResult != null) {
        for (String calcConfigName : targetResult.getCalculationConfigurationNames()) {
          for (ComputedValue result : targetResult.getAllValues(calcConfigName)) {
            ValueSpecification valueSpec = result.getSpecification();
            Set<ValueRequirement> valueReqs =
                _gridStructure.getRequirementsForSpecification(calcConfigName, valueSpec);
            for (ValueRequirement req : valueReqs) {
              int colIndex = _gridStructure.getColumnIndexForRequirement(calcConfigName, req);
              Object resultValue = result.getValue();
              if (_viewportSpec.getColumns().contains(colIndex)) {
                Collection<Object> valueHistory = history.getHistory(calcConfigName, valueSpec, resultValue);
                ViewportResults.Cell cell = ViewportResults.valueCell(resultValue, valueSpec, valueHistory);
                updated = true;
                rowResultsMap.put(colIndex, cell);
              }
            }
          }
        }
      }
      // create a list for each row of results, reusing the previous results if there was no new value for a cell
      String rowName = row.getName();
      List<ViewportResults.Cell> rowResults = Lists.newArrayListWithCapacity(_viewportSpec.getColumns().size() + 1);
      // row label always goes in the first column
      rowResults.add(ViewportResults.stringCell(rowName));
      // iterate over all columns in the viewport and populate the results for the current row
      for (int colIndex = 1; colIndex < _gridStructure.getColumnCount(); colIndex++) {
        if (_viewportSpec.getColumns().contains(colIndex)) {
          // this intentionally inserts null into the results if there is no value for a given column
          ViewportResults.Cell cell = rowResultsMap.get(colIndex);
          if (cell != null) {
            rowResults.add(cell);
          } else {
            // TODO look up the previous value in the cache
            rowResults.add(ViewportResults.emptyCell());
          }
        }
      }
      allResults.add(rowResults);
    }
    _latestResults = new ViewportResults(allResults, _viewportSpec, _gridStructure.getColumnStructure());
    if (updated) {
      return _dataId;
    } else {
      return null;
    }
  }

  public void update(ViewportSpecification viewportSpec, ViewResultModel results, AnalyticsHistory history) {
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
}
