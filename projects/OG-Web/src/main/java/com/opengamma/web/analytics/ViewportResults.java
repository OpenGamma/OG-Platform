/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collection;
import java.util.List;

import javax.time.Duration;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Set of calculation results for displaying in the viewport of a grid of analytics data.
 */
public class ViewportResults {

  /** The result values by row. */
  private final List<Cell> _allResults;
  /** The grid columns. */
  private final AnalyticsColumnGroups _columns;
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
  /* package */ ViewportResults(List<Cell> allResults,
                                ViewportDefinition viewportDefinition,
                                AnalyticsColumnGroups columns,
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
  /* package */ List<Cell> getResults() {
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
   * @param colIndex The column index in the grid (zero based)
   * @return The type of the specified column
   */
  /* package */ Class<?> getColumnType(int colIndex) {
    return _columns.getColumn(colIndex).getType();
  }

  /**
   * @return The duration of the last calculation cycle.
   */
  /* package */ Duration getCalculationDuration() {
    return _calculationDuration;
  }

  /**
   * Factory method that creates a grid cell for displaying a string value.
   * @param value The cell's value
   * @return A cell for displaying the value
   */
  /* package */ static Cell stringCell(String value, int column) {
    ArgumentChecker.notNull(value, "value");
    return new Cell(value, null, null, column, null, null, null);
  }

  /**
   * Factory method that creates a grid cell for displaying a calculated value.
   * @param value The value
   * @param valueSpecification The value's specification
   * @param history The value's history
   * @return A cell for displaying the value
   */
  /* package */ static Cell valueCell(Object value,
                                      ValueSpecification valueSpecification,
                                      Collection<Object> history,
                                      AggregatedExecutionLog executionLog,
                                      int column) {
    return new Cell(value, valueSpecification, history, column, null, null, executionLog);
  }

  /**
   * Factory method that returns a grid cell with no value.
   * @return An empty cell
   * @param emptyHistory Empty history appropriate for the cell's type. For types that support history it should
   * be an empty collection, for types that don't it should be null.
   * @param colIndex Index of the cell's grid column
   */
  /* package */ static Cell emptyCell(Collection<Object> emptyHistory, int colIndex) {
    return new Cell(null, null, emptyHistory, colIndex, null, null, null);
  }

  /* package */ static Cell positionCell(String name, int colIndex, UniqueId positionId) {
    return new Cell(name, null, null, colIndex, positionId, null, null);
  }

  /* package */ static Cell nodeCell(String name, int colIndex, UniqueId nodeId) {
    return new Cell(name, null, null, colIndex, null, nodeId, null);
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

  /**
   * A single grid cell in a set of results, including the cell's value, value specification and history.
   */
  /* package */ static final class Cell {

    private final Object _value;
    private final ValueSpecification _valueSpecification;
    private final Collection<Object> _history;
    private final int _column;
    private final UniqueId _positionId;
    private final UniqueId _nodeId;
    private final AggregatedExecutionLog _executionLog;

    private Cell(Object value,
                 ValueSpecification valueSpecification,
                 Collection<Object> history,
                 int column,
                 UniqueId positionId,
                 UniqueId nodeId,
                 AggregatedExecutionLog executionLog) {
      _value = value;
      _valueSpecification = valueSpecification;
      _history = history;
      _column = column;
      _positionId = positionId;
      _nodeId = nodeId;
      _executionLog = executionLog;
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

    /* package */ int getColumn() {
      return _column;
    }

    /* package */ UniqueId getPositionId() {
      return _positionId;
    }

    /* package */ UniqueId getNodeId() {
      return _nodeId;
    }

    /* package */ AggregatedExecutionLog getExecutionLog() {
      return _executionLog;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Cell cell = (Cell) o;

      if (_column != cell._column) {
        return false;
      }
      if (_history != null) {
        if (!_history.equals(cell._history)) {
          return false;
        }
      } else {
        if (cell._history != null) {
          return false;
        }
      }
      if (_value != null) {
        if (!_value.equals(cell._value)) {
          return false;
        }
      } else {
        if (cell._value != null) {
          return false;
        }
      }
      if (_valueSpecification != null) {
        if (!_valueSpecification.equals(cell._valueSpecification)) {
          return false;
        }
      } else {
        if (cell._valueSpecification != null) {
          return false;
        }
      }
      return true;
    }

    @Override
    public int hashCode() {
      int result = _value != null ? _value.hashCode() : 0;
      result = 31 * result + (_valueSpecification != null ? _valueSpecification.hashCode() : 0);
      result = 31 * result + (_history != null ? _history.hashCode() : 0);
      result = 31 * result + _column;
      return result;
    }
  }
}
