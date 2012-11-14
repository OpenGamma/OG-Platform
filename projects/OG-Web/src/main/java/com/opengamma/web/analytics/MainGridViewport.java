/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Viewport on one of the main analytics grids displaying portfolio or primitives data.
 */
/* package */ abstract class MainGridViewport extends AnalyticsViewport {

  static final int LABEL_COLUMN = 0;
  /** Row and column structure of the grid. */
  private final MainGridStructure _gridStructure;

  /**
   * @param gridStructure Row and column structure of the grid
   * @param callbackId ID that's passed to listeners when the grid structure changes
   */
  /* package */ MainGridViewport(MainGridStructure gridStructure, String callbackId) {
    super(callbackId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    _gridStructure = gridStructure;
  }

  /**
   * Updates the data in the viewport using the results in the cache.
   * @param cache The latest results
   * @return If any of the data in this viewport was updated in the last calculation cycyel the callback ID of the
   * viewport is returned. If none of the data in the viewport was updated then null is returned.
   */
  /* package */ String updateResults(ResultsCache cache) {
    boolean updated = false;
    List<ViewportResults.Cell> results = Lists.newArrayList();
    for (GridCell cell : _viewportDefinition) {
      int rowIndex = cell.getRow();
      int colIndex = cell.getColumn();
      MainGridStructure.Row row = _gridStructure.getRowAtIndex(rowIndex);
      // TODO this needs to be refactored so these aren't hard-coded. subclasses for portfolio and primitives?
      // TODO would need MainAnalyticsGrid subclasses too. that's probably the right solution
      if (_gridStructure.isColumnFixed(colIndex)) {
        results.add(getFixedColumnResult(rowIndex, colIndex, row));
      } else {
        Pair<String, ValueSpecification> cellTarget = _gridStructure.getTargetForCell(rowIndex, colIndex);
        Class<?> columnType = _gridStructure.getColumnType(colIndex);
        if (cellTarget != null) {
          String calcConfigName = cellTarget.getFirst();
          ValueSpecification valueSpec = cellTarget.getSecond();
          ResultsCache.Result cacheResult = cache.getResult(calcConfigName, valueSpec, columnType);
          updated = updated || cacheResult.isUpdated();
          results.add(ViewportResults.valueCell(cacheResult.getValue(), valueSpec, cacheResult.getHistory(), colIndex));
        } else {
          Collection<Object> emptyHistory = cache.getEmptyHistory(columnType);
          results.add(ViewportResults.emptyCell(emptyHistory, colIndex));
        }
      }
    }
    _latestResults = new ViewportResults(results,
                                         _viewportDefinition,
                                         _gridStructure.getColumnStructure(),
                                         cache.getLastCalculationDuration());
    if (updated) {
      return _callbackId;
    } else {
      return null;
    }
  }

  /**
   * Returns a result for the specified row and column.
   * @param rowIndex Index of the row
   * @param colIndex Index of the column
   * @param row Contains the row's target and quantity (if applicable)
   * @return The result value
   */
  protected abstract ViewportResults.Cell getFixedColumnResult(int rowIndex, int colIndex, MainGridStructure.Row row);

  /**
   * Updates the viewport definition (e.g. in reponse to the user scrolling the grid and changing the visible area).
   * @param viewportDefinition The new viewport definition
   * @param cache The current results
   * @return The viewport's callback ID or {@code null} if it wasn't updated
   */
  public String update(ViewportDefinition viewportDefinition, ResultsCache cache) {
    ArgumentChecker.notNull(viewportDefinition, "viewportDefinition");
    ArgumentChecker.notNull(cache, "cache");
    if (!viewportDefinition.isValidFor(_gridStructure)) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportDefinition + ", grid: " + _gridStructure);
    }
    _viewportDefinition = viewportDefinition;
    return updateResults(cache);
  }
}
