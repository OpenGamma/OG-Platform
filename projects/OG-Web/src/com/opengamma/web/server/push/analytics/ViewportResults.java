/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collection;
import java.util.List;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.util.ArgumentChecker;

/**
 * Set of calculation results for displaying in the viewport of a grid of analytics data.
 */
public class ViewportResults {

  /** An empty grid cell. */
  private static final Cell s_emptyCell = new Cell(null, null, null);

  /** The result values by row. */
  private final List<List<Cell>> _allResults;
  /** The grid columns. */
  private final AnalyticsColumnGroups _columns;
  /** Definition of the viewport. */
  private final ViewportSpecification _viewportSpec;
  /** Version of the viewport used when building these results. */
  private final long _version;

  /**
   * @param allResults Cells in the viewport containing the data, history and the value specification. The outer
   * list contains the data by rows and the inner lists contain the data for each row
   * @param viewportSpec Definition of the rows and columns in the viewport
   * @param columns The columns in the viewport's grid
   * @param version The version of the viewport used when creating the results
   */
  /* package */ ViewportResults(List<List<Cell>> allResults,
                                ViewportSpecification viewportSpec,
                                AnalyticsColumnGroups columns,
                                long version) {
    ArgumentChecker.notNull(allResults, "allResults");
    ArgumentChecker.notNull(columns, "columns");
    ArgumentChecker.notNull(viewportSpec, "viewportSpec");
    _allResults = allResults;
    _viewportSpec = viewportSpec;
    _columns = columns;
    _version = version;
  }

  /**
   * @return Cells in the viewport containing the data, history and the value specification. The outer
   * list contains the data by rows and the inner lists contain the data for each row
   */
  /* package */ List<List<Cell>> getResults() {
    return _allResults;
  }

  /**
   *
   * @return Whether the data is a summary or the full data. Summary data fits in a single grid cell whereas
   * the full data might need more space. e.g. displaying matrix data in a window that pops up over the main grid.
   */
  /* package */ boolean isExpanded() {
    return _viewportSpec.isExpanded();
  }

  /**
   * @return The version of the viewport used when creating the results, allows the client to that a set of results
   * correspond to the current viewport state.
   */
  /* package */ long getVersion() {
    return _version;
  }

  /**
   * @param viewportColIndex The column index <em>in terms of the viewport columns</em>. For exmaple, if a viewport
   * contains columns 3 and 5 a call to {@code getColumnType(0)} will return the type of column 3 and
   * {@code getColumnType(1)} will return the type of column 5
   * @return The type of the specified column
   */
  /* package */ Class<?> getColumnType(int viewportColIndex) {
    Integer gridColIndex = _viewportSpec.getGridColumnIndex(viewportColIndex);
    return _columns.getColumn(gridColIndex).getType();
  }

  /**
   * Factory method that creates a grid cell for displaying a string value.
   * @param value The cell's value
   * @return A cell for displaying the value
   */
  /* package */ static Cell stringCell(String value) {
    ArgumentChecker.notNull(value, "value");
    return new Cell(value, null, null);
  }

  /**
   * Factory method that creates a grid cell for displaying a calculated value.
   * @param value The value
   * @param valueSpecification The value's specification
   * @param history The value's history
   * @return A cell for displaying the value
   */
  /* package */ static Cell valueCell(Object value, ValueSpecification valueSpecification, Collection<Object> history) {
    return new Cell(value, valueSpecification, history);
  }

  /**
   * Factory method that returns a grid cell with no value.
   * @return An empty cell
   */
  /* package */ static Cell emptyCell() {
    return s_emptyCell;
  }

  @Override
  public String toString() {
    return "ViewportResults [" +
        "_allResults=" + _allResults +
        ", _columns=" + _columns +
        ", _viewportSpec=" + _viewportSpec +
        ", _version=" + _version +
        "]";
  }

  /**
   * A single grid cell in a set of results, including the cell's value, value specification and history.
   */
  /* package */ static class Cell {

    private final Object _value;
    private final ValueSpecification _valueSpecification;
    private final Collection<Object> _history;

    private Cell(Object value, ValueSpecification valueSpecification, Collection<Object> history) {
      _value = value;
      _valueSpecification = valueSpecification;
      _history = history;
    }

    /**
     * @return The cell's value, can be null
     */
    /* package */ Object getValue() {
      return _value;
    }

    /**
     * @return The cell's value specification, can be null
     */
    /* package */ ValueSpecification getValueSpecification() {
      return _valueSpecification;
    }

    /**
     * @return The cell's value history, can be null or empty
     */
    /* package */ Collection<Object> getHistory() {
      return _history;
    }

    /**
     * @return true if the cell's value couldn't be calculated because of an error
     */
    /* package */ boolean isError() {
      return _value instanceof MissingInput;
    }
  }
}
