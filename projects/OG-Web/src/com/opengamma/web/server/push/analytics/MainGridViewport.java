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
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class MainGridViewport extends AnalyticsViewport {

  private final MainGridStructure _gridStructure;

  MainGridViewport(ViewportSpecification viewportSpec,
                   MainGridStructure gridStructure,
                   String dataId,
                   ViewResultModel latestResults,
                   ResultsCache cache) {
    super(dataId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    _gridStructure = gridStructure;
    update(viewportSpec, latestResults, cache);
  }

  // TODO this method makes my head hurt. REFACTOR
  /* package */ String updateResults(ViewResultModel results, ResultsCache cache) {
    boolean updated = false;
    List<List<ViewportResults.Cell>> allResults = new ArrayList<List<ViewportResults.Cell>>();
    // iterate over each row in the viewport
    // TODO could move this to the ResultsCache. add flag to each item if it was updated last time
    for (int rowIndex : _viewportSpec.getRows()) {
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
                Class<?> columnType = _gridStructure.getColumnType(colIndex);
                Collection<Object> valueHistory = cache.getHistory(calcConfigName, valueSpec, columnType);
                ViewportResults.Cell cell = ViewportResults.valueCell(resultValue, valueSpec, valueHistory);
                updated = true;
                rowResultsMap.put(colIndex, cell);
              }
            }
          }
        }
      }
      // TODO split into 2 loops over the rows. can exit early if nothing was updated and keep the previous results
      // create a list for each row of results, reusing the previous results if there was no new value for a cell
      String rowName = row.getName();
      List<ViewportResults.Cell> rowResults = Lists.newArrayListWithCapacity(_viewportSpec.getColumns().size() + 1);
      // row label always goes in the first column
      rowResults.add(ViewportResults.stringCell(rowName));
      // iterate over all columns in the viewport and populate the results for the current row
      for (int colIndex : _viewportSpec.getColumns()) {
        // TODO is there a better way that just hard-coding for the label column?
        if (colIndex != 0) { // label column
          ViewportResults.Cell cell = rowResultsMap.get(colIndex);
          if (cell != null) {
            rowResults.add(cell);
          } else {
            Pair<String, ValueSpecification> cellTarget = _gridStructure.getTargetForCell(rowIndex, colIndex);
            if (cellTarget != null) {
              Class<?> columnType = _gridStructure.getColumnType(colIndex);
              String calcConfigName = cellTarget.getFirst();
              ValueSpecification valueSpec = cellTarget.getSecond();
              ResultsCache.Result cacheResult = cache.getResult(calcConfigName, valueSpec, columnType);
              rowResults.add(ViewportResults.valueCell(cacheResult.getLatestValue(), valueSpec, cacheResult.getHistory()));
            } else {
              rowResults.add(ViewportResults.emptyCell());
            }
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

  // this will be needed when delta results are supported and we can reuse old results if nothing has been updated
  /*private ViewportResults emtpyResults(ResultsCache cache) {
    List<List<ViewportResults.Cell>> rows = Lists.newArrayListWithCapacity(_viewportSpec.getRows().size());
    List<ViewportResults.Cell> rowCells = Lists.newArrayListWithCapacity(_viewportSpec.getColumns().size());

    for (int i = 0; i < _viewportSpec.getRows().size(); i++) {
      for (Integer colIndex : _viewportSpec.getColumns()) {
        Class<?> columnType = _gridStructure.getColumnType(colIndex);
        if (cache.isHistoryType(columnType)) {
          rowCells.add(ViewportResults.emptyCellWithHistory());
        } else {
          rowCells.add(ViewportResults.emptyCell());
        }
      }
      rows.add(rowCells);
    }
    return new ViewportResults(rows, _viewportSpec, _gridStructure.getColumnStructure());
  }*/

  public void update(ViewportSpecification viewportSpec, ViewResultModel results, ResultsCache cache) {
    ArgumentChecker.notNull(viewportSpec, "viewportSpec");
    ArgumentChecker.notNull(results, "results");
    ArgumentChecker.notNull(cache, "cache");
    if (!viewportSpec.isValidFor(_gridStructure)) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportSpec + ", grid: " + _gridStructure);
    }
    _viewportSpec = viewportSpec;
    updateResults(results, cache);
  }
}
