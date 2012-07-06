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
public class MainGridViewport extends AnalyticsViewport {

  private final MainGridStructure _gridStructure;
  private final ResultsFormatter _formatter;

  MainGridViewport(ViewportSpecification viewportSpec,
                   MainGridStructure gridStructure,
                   String dataId,
                   ViewComputationResultModel latestResults,
                   AnalyticsHistory history,
                   ResultsFormatter formatter) {
    super(dataId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notNull(formatter, "formatter");
    _formatter = formatter;
    _gridStructure = gridStructure;
    update(viewportSpec, latestResults, history);
  }

  // TODO this is specific to the main grids, need separate subclasses for depgraphs?
  /* package */ void updateResults(ViewComputationResultModel results, AnalyticsHistory history) {
    List<List<Object>> allResults = new ArrayList<List<Object>>();
    for (Integer rowIndex : _viewportSpec.getRows()) {
      MainGridStructure.Row row = _gridStructure.getRowAtIndex(rowIndex);
      ComputationTargetSpecification target = row.getTarget();
      Map<Integer, Object> rowResultsMap = new TreeMap<Integer, Object>();
      ViewTargetResultModel targetResult = results.getTargetResult(target);
      if (targetResult != null) {
        for (String calcConfigName : targetResult.getCalculationConfigurationNames()) {
          for (ComputedValue value : targetResult.getAllValues(calcConfigName)) {
            ValueSpecification valueSpec = value.getSpecification();
            Set<ValueRequirement> valueReqs =
                _gridStructure.getRequirementsForSpecification(calcConfigName, valueSpec);
            for (ValueRequirement req : valueReqs) {
              int colIndex = _gridStructure.getColumnIndexForRequirement(calcConfigName, req);
              if (_viewportSpec.getColumns().contains(colIndex)) {
                Object formattedValue = _formatter.formatValueForDisplay(value.getValue(), valueSpec);
                rowResultsMap.put(colIndex, formattedValue);
              }
            }
          }
        }
      }
      String rowName = row.getName();
      List<Object> rowResults = new ArrayList<Object>(_viewportSpec.getColumns().size() + 1);
      // row label always goes in the first column
      rowResults.add(rowName);
      for (int colIndex = 1; colIndex < _gridStructure.getColumnCount(); colIndex++) {
        if (_viewportSpec.getColumns().contains(colIndex)) {
          // this intentionally inserts null into the results if there is no value for a given column
          rowResults.add(rowResultsMap.get(colIndex));
        }
      }
      allResults.add(rowResults);
    }
    _latestResults = new ViewportResults(allResults);
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
    // TODO fire an update here so the client knows to get some updated results?
  }

}
