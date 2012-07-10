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

import com.google.common.collect.Lists;
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
public class MainGridViewport extends AnalyticsViewport {

  private final MainGridStructure _gridStructure;

  MainGridViewport(ViewportSpecification viewportSpec,
                   MainGridStructure gridStructure,
                   String dataId,
                   ViewComputationResultModel latestResults,
                   AnalyticsHistory history) {
    super(dataId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    _gridStructure = gridStructure;
    update(viewportSpec, latestResults, history);
  }

  /* package */ boolean updateResults(ViewComputationResultModel results, AnalyticsHistory history) {
    List<List<ViewportResults.Cell>> allResults = new ArrayList<List<ViewportResults.Cell>>();
    boolean columnsUpdated = false;
    for (Integer rowIndex : _viewportSpec.getRows()) {
      MainGridStructure.Row row = _gridStructure.getRowAtIndex(rowIndex);
      ComputationTargetSpecification target = row.getTarget();
      Map<Integer, ViewportResults.Cell> rowResultsMap = new TreeMap<Integer, ViewportResults.Cell>();
      ViewTargetResultModel targetResult = results.getTargetResult(target);
      if (targetResult != null) {
        for (String calcConfigName : targetResult.getCalculationConfigurationNames()) {
          for (ComputedValue value : targetResult.getAllValues(calcConfigName)) {
            ValueSpecification valueSpec = value.getSpecification();
            Set<ValueRequirement> valueReqs =
                _gridStructure.getRequirementsForSpecification(calcConfigName, valueSpec);
            for (ValueRequirement req : valueReqs) {
              int colIndex = _gridStructure.getColumnIndexForRequirement(calcConfigName, req);
              boolean columnUpdated = _gridStructure.setTypeForColumn(colIndex, value.getClass());
              columnsUpdated = columnsUpdated || columnUpdated;
              if (_viewportSpec.getColumns().contains(colIndex)) {
                List<Object> valueHistory = history.getHistory(valueSpec, value.getValue());
                ViewportResults.Cell cell = ViewportResults.valueCell(value.getValue(), valueSpec, valueHistory);
                rowResultsMap.put(colIndex, cell);
              }
            }
          }
        }
      }
      String rowName = row.getName();
      List<ViewportResults.Cell> rowResults = Lists.newArrayListWithCapacity(_viewportSpec.getColumns().size() + 1);
      // row label always goes in the first column
      rowResults.add(ViewportResults.stringCell(rowName));
      for (int colIndex = 1; colIndex < _gridStructure.getColumnCount(); colIndex++) {
        if (_viewportSpec.getColumns().contains(colIndex)) {
          // this intentionally inserts null into the results if there is no value for a given column
          rowResults.add(rowResultsMap.get(colIndex));
        }
      }
      allResults.add(rowResults);
    }
    _latestResults = new ViewportResults(allResults, _viewportSpec.isExpanded());
    return columnsUpdated;
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
}
