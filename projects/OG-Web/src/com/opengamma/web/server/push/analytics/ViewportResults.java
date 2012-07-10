/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ViewportResults {

  private final List<List<Cell>> _allResults;
  private final boolean _expanded;

  // TODO does this need to contain the viewport spec?

  /* package */ ViewportResults(List<List<Cell>> allResults, boolean expanded) {
    ArgumentChecker.notNull(allResults, "allResults");
    _allResults = allResults;
    _expanded = expanded;
  }

  public List<List<Cell>> getResults() {
    return _allResults;
  }

  public boolean isExpanded() {
    return _expanded;
  }

  @Override
  public String toString() {
    return "ViewportResults [_allResults=" + _allResults + ", _expanded=" + _expanded + "]";
  }

  public static Cell stringCell(String value) {
    return new Cell(value, null, null);
  }

  public static Cell valueCell(Object value, ValueSpecification valueSpecification, List<Object> history) {
    return new Cell(value, valueSpecification, history);
  }

  public static class Cell {

    private final Object _value;
    private final ValueSpecification _valueSpecification;
    private final List<Object> _history; // TODO what type should this be?

    private Cell(Object value, ValueSpecification valueSpecification, List<Object> history) {
      ArgumentChecker.notNull(value, "value");
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

    public List<Object> getHistory() {
      return _history;
    }
  }
}
