/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * TODO label and quantity columns are hard-coded here and in {@link MainGridStructure}. there has to be a better way
 */
public class MainGridViewport extends AnalyticsViewport {

  private final MainGridStructure _gridStructure;

  MainGridViewport(ViewportSpecification viewportSpec, MainGridStructure gridStructure, String dataId, ResultsCache cache) {
    super(dataId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    _gridStructure = gridStructure;
    update(viewportSpec, cache);
  }

  /* package */ String updateResults(ResultsCache cache) {
    boolean updated = false;
    List<List<ViewportResults.Cell>> allResults = Lists.newArrayList();
    // iterate over each row in the viewport
    for (int rowIndex : _viewportSpec.getRows()) {
      MainGridStructure.Row row = _gridStructure.getRowAtIndex(rowIndex);
      // create a list for each row of results
      List<ViewportResults.Cell> rowResults = Lists.newArrayListWithCapacity(_viewportSpec.getColumns().size() + 1);
      // iterate over all columns in the viewport and populate the results for the current row
      for (int colIndex : _viewportSpec.getColumns()) {
        if (colIndex == MainGridStructure.LABEL_COLUMN) {
          rowResults.add(ViewportResults.stringCell(row.getName()));
        } else if (colIndex == MainGridStructure.QUANTITY_COLUMN) {
          rowResults.add(ViewportResults.valueCell(row.getQuantity(), null, Collections.emptyList()));
        } else {
          Pair<String, ValueSpecification> cellTarget = _gridStructure.getTargetForCell(rowIndex, colIndex);
          if (cellTarget != null) {
            Class<?> columnType = _gridStructure.getColumnType(colIndex);
            String calcConfigName = cellTarget.getFirst();
            ValueSpecification valueSpec = cellTarget.getSecond();
            ResultsCache.Result cacheResult = cache.getResult(calcConfigName, valueSpec, columnType);
            updated = updated || cacheResult.isUpdated();
            rowResults.add(ViewportResults.valueCell(cacheResult.getValue(), valueSpec, cacheResult.getHistory()));
          } else {
            rowResults.add(ViewportResults.emptyCell());
          }
        }
      }
      allResults.add(rowResults);
    }
    _latestResults = new ViewportResults(allResults, _viewportSpec, _gridStructure.getColumnStructure(), _version);
    if (updated) {
      return _dataId;
    } else {
      return null;
    }
  }

  public long update(ViewportSpecification viewportSpec, ResultsCache cache) {
    ArgumentChecker.notNull(viewportSpec, "viewportSpec");
    ArgumentChecker.notNull(cache, "cache");
    if (!viewportSpec.isValidFor(_gridStructure)) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportSpec + ", grid: " + _gridStructure);
    }
    _viewportSpec = viewportSpec;
    _version++;
    updateResults(cache);
    return _version;
  }
}
