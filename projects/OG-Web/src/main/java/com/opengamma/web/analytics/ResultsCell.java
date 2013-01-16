/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collection;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.calcnode.MissingInput;

/**
 * A single grid cell in a set of results, including the cell's value, value specification and history.
 */
/* package */ final class ResultsCell {

  private final Object _value;
  private final ValueSpecification _valueSpecification;
  private final Collection<Object> _history;
  private final int _column;
  private final AggregatedExecutionLog _executionLog;
  private final boolean _updated;

  /* package */ ResultsCell(Object value,
                            ValueSpecification valueSpecification,
                            Collection<Object> history,
                            int column,
                            AggregatedExecutionLog executionLog,
                            boolean updated) {
    _value = value;
    _valueSpecification = valueSpecification;
    _history = history;
    _column = column;
    _executionLog = executionLog;
    _updated = updated;
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

  /* package */ AggregatedExecutionLog getExecutionLog() {
    return _executionLog;
  }

  /* package */ boolean isUpdated() {
    return _updated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultsCell that = (ResultsCell) o;

    if (_column != that._column) {
      return false;
    }
    if (_updated != that._updated) {
      return false;
    }
    if (_executionLog != null) {
      if (!_executionLog.equals(that._executionLog)) {
        return false;
      }
    } else {
      if (that._executionLog != null) {
        return false;
      }
    }
    if (_history != null) {
      if (!_history.equals(that._history)) {
        return false;
      }
    } else {
      if (that._history != null) {
        return false;
      }
    }
    if (_value != null) {
      if (!_value.equals(that._value)) {
        return false;
      }
    } else {
      if (that._value != null) {
        return false;
      }
    }
    if (_valueSpecification != null) {
      if (!_valueSpecification.equals(that._valueSpecification)) {
        return false;
      }
    } else {
      if (that._valueSpecification != null) {
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
    result = 31 * result + (_executionLog != null ? _executionLog.hashCode() : 0);
    result = 31 * result + (_updated ? 1 : 0);
    return result;
  }
}
