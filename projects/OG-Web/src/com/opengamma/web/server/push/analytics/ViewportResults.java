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

  private final List<List<Cell>> _allResults;
  private final AnalyticsColumnGroups _columns;
  private final ViewportSpecification _viewportSpec;

  /* package */ ViewportResults(List<List<Cell>> allResults,
                                ViewportSpecification viewportSpec,
                                AnalyticsColumnGroups columns) {
    ArgumentChecker.notNull(allResults, "allResults");
    ArgumentChecker.notNull(columns, "columns");
    ArgumentChecker.notNull(viewportSpec, "viewportSpec");
    _allResults = allResults;
    _viewportSpec = viewportSpec;
    _columns = columns;
  }

  public List<List<Cell>> getResults() {
    return _allResults;
  }

  public boolean isExpanded() {
    return _viewportSpec.isExpanded();
  }

  public AnalyticsColumnGroups getColumns() {
    return _columns;
  }

  public ViewportSpecification getViewportSpec() {
    return _viewportSpec;
  }

  @Override
  public String toString() {
    return "ViewportResults [" +
        "_allResults=" + _allResults +
        ", _columns=" + _columns +
        ", _viewportSpec=" + _viewportSpec +
        "]";
  }

  public static Cell stringCell(String value) {
    ArgumentChecker.notNull(value, "value");
    return new Cell(value, null, null);
  }

  public static Cell valueCell(Object value, ValueSpecification valueSpecification, Collection<Object> history) {
    ArgumentChecker.notNull(value, "value");
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    return new Cell(value, valueSpecification, history);
  }

  public static Cell emptyCell() {
    return new Cell(null, null, null);
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
