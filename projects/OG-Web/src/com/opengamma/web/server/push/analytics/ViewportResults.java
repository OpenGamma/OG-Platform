/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collection;
import java.util.List;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ViewportResults {

  private static final Cell s_emptyCell = new Cell(null, null, null);

  private final List<List<Cell>> _allResults;
  private final AnalyticsColumnGroups _columns;
  private final ViewportSpecification _viewportSpec;
  private final long _version;

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

  public List<List<Cell>> getResults() {
    return _allResults;
  }

  public boolean isExpanded() {
    return _viewportSpec.isExpanded();
  }

  public long getVersion() {
    return _version;
  }

  /**
   * @param viewportColIndex The column index <em>in terms of the viewport columns</em>. For exmaple, if a viewport
   * contains columns 3 and 5 a call to {@code getColumnType(0)} will return the type of column 3 and
   * {@code getColumnType(1)} will return the type of column 5
   * @return The type of the specified column
   */
  public Class<?> getColumnType(int viewportColIndex) {
    Integer gridColIndex = _viewportSpec.getGridColumnIndex(viewportColIndex);
    return _columns.getColumn(gridColIndex).getType();
  }

  public static Cell stringCell(String value) {
    ArgumentChecker.notNull(value, "value");
    return new Cell(value, null, null);
  }

  public static Cell valueCell(Object value, ValueSpecification valueSpecification, Collection<Object> history) {
    return new Cell(value, valueSpecification, history);
  }

  public static Cell emptyCell() {
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

  public static class Cell {

    private final Object _value;
    private final ValueSpecification _valueSpecification;
    private final Collection<Object> _history;

    private Cell(Object value, ValueSpecification valueSpecification, Collection<Object> history) {
      _value = value;
      _valueSpecification = valueSpecification;
      _history = history;
    }

    public Object getValue() {
      return _value;
    }

    public ValueSpecification getValueSpecification() {
      return _valueSpecification;
    }

    public Collection<Object> getHistory() {
      return _history;
    }
  }
}
