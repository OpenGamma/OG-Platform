/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import org.threeten.bp.Duration;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Set of calculation results for displaying in the viewport of a grid of analytics data.
 */
public class ViewportResults {

  /** The result values by row. */
  private final List<ResultsCell> _allResults;
  /** The grid columns. */
  private final GridColumnGroups _columns;
  /** Definition of the viewport. */
  private final ViewportDefinition _viewportDefinition;
  /** Duration of the last calculation cycle. */
  private final Duration _calculationDuration;

  /**
   * @param allResults Cells in the viewport containing the data, history and the value specification. The outer
   * list contains the data by rows and the inner lists contain the data for each row
   * @param viewportDefinition Definition of the rows and columns in the viewport
   * @param columns The columns in the viewport's grid
   */
  /* package */ ViewportResults(List<ResultsCell> allResults,
                                ViewportDefinition viewportDefinition,
                                GridColumnGroups columns,
                                Duration calculationDuration) {
    ArgumentChecker.notNull(allResults, "allResults");
    ArgumentChecker.notNull(columns, "columns");
    ArgumentChecker.notNull(viewportDefinition, "viewportDefinition");
    ArgumentChecker.notNull(calculationDuration, "calculationDuration");
    _allResults = allResults;
    _viewportDefinition = viewportDefinition;
    _columns = columns;
    _calculationDuration = calculationDuration;
  }

  /**
   * @return Cells in the viewport containing the data, history and the value specification. The outer
   * list contains the data by rows and the inner lists contain the data for each row
   */
  /* package */ List<ResultsCell> getResults() {
    return _allResults;
  }

  /**
   *
   * @return Whether the data is a summary or the full data. Summary data fits in a single grid cell whereas
   * the full data might need more space. e.g. displaying matrix data in a window that pops up over the main grid.
   */
  /* package */ TypeFormatter.Format getFormat() {
    return _viewportDefinition.getFormat();
  }

  /**
   * @return The version of the viewport used when creating the results, allows the client to that a set of results
   * correspond to the current viewport state.
   */
  /* package */ long getVersion() {
    return _viewportDefinition.getVersion();
  }

  /**
   * @return The duration of the last calculation cycle.
   */
  /* package */ Duration getCalculationDuration() {
    return _calculationDuration;
  }

  /* package */ ViewportDefinition getViewportDefinition() {
    return _viewportDefinition;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ViewportResults that = (ViewportResults) o;

    if (!_columns.equals(that._columns)) {
      return false;
    }
    if (!_viewportDefinition.equals(that._viewportDefinition)) {
      return false;
    }
    if (!_allResults.equals(that._allResults)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = _allResults.hashCode();
    result = 31 * result + _columns.hashCode();
    result = 31 * result + _viewportDefinition.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ViewportResults [" +
        "_allResults=" + _allResults +
        ", _columns=" + _columns +
        ", _viewportDefinition=" + _viewportDefinition +
        "]";
  }
}
