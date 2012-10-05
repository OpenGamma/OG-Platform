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
 * Viewport on one of the main analytics grids displaying portfolio or primitives data.
 */
public class MainGridViewport extends AnalyticsViewport {

  /** Row and column structure of the grid. */
  private final MainGridStructure _gridStructure;

  /**
   * @param gridStructure Row and column structure of the grid
   * @param callbackId ID that's passed to listeners when the grid structure changes
   * @param cache Cache of calculation results used to populate the viewport's data
   */
  /* package */ MainGridViewport(ViewportDefinition viewportDefinition,
                                 MainGridStructure gridStructure,
                                 String callbackId,
                                 ResultsCache cache) {
    super(callbackId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    _gridStructure = gridStructure;
    update(viewportDefinition, cache);
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
      if (colIndex == MainGridStructure.LABEL_COLUMN) {
        results.add(ViewportResults.stringCell(row.getName(), colIndex));
      } else if (colIndex == MainGridStructure.QUANTITY_COLUMN) {
        results.add(ViewportResults.valueCell(row.getQuantity(), null, Collections.emptyList(), colIndex));
      } else {
        Pair<String, ValueSpecification> cellTarget = _gridStructure.getTargetForCell(rowIndex, colIndex);
        if (cellTarget != null) {
          Class<?> columnType = _gridStructure.getColumnType(colIndex);
          String calcConfigName = cellTarget.getFirst();
          ValueSpecification valueSpec = cellTarget.getSecond();
          ResultsCache.Result cacheResult = cache.getResult(calcConfigName, valueSpec, columnType);
          updated = updated || cacheResult.isUpdated();
          results.add(ViewportResults.valueCell(cacheResult.getValue(), valueSpec, cacheResult.getHistory(), colIndex));
        } else {
          results.add(ViewportResults.emptyCell());
        }
      }
    }
    _latestResults = new ViewportResults(results, _viewportDefinition, _gridStructure.getColumnStructure(), _version);
    if (updated) {
      return _callbackId;
    } else {
      return null;
    }
  }

  /**
   * Updates the viewport definition (e.g. in reponse to the user scrolling the grid and changing the visible area).
   * @param viewportDefinition The new viewport definition
   * @param cache The current results
   * @return The version number of the viewport, this allows clients to ensure the data they receive for a viewport
   * was built for the current version of the viewport
   */
  public long update(ViewportDefinition viewportDefinition, ResultsCache cache) {
    ArgumentChecker.notNull(viewportDefinition, "viewportDefinition");
    ArgumentChecker.notNull(cache, "cache");
    if (!viewportDefinition.isValidFor(_gridStructure)) {
      throw new IllegalArgumentException("Viewport contains cells outside the bounds of the grid. Viewport: " +
                                             viewportDefinition + ", grid: " + _gridStructure);
    }
    _viewportDefinition = viewportDefinition;
    _version++;
    updateResults(cache);
    return _version;
  }
}
