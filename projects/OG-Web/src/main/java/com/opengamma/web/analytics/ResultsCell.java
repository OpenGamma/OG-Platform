/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collection;

import com.opengamma.engine.calcnode.MissingValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * A single grid cell in a set of results, including the cell's value, value specification and history.
 */
/* package */ final class ResultsCell {

  private final Object _value;
  private final ValueSpecification _valueSpecification;
  private final Collection<Object> _history;
  private final AggregatedExecutionLog _executionLog;
  private final boolean _updated;
  private final Class<?> _type;
  private final Object _inlineKey;
  private final TypeFormatter.Format _format;

  private ResultsCell(Object value,
                      ValueSpecification valueSpecification,
                      Collection<Object> history,
                      AggregatedExecutionLog executionLog,
                      boolean updated,
                      Class<?> type,
                      Object inlineKey,
                      TypeFormatter.Format format) {
    _value = value;
    _valueSpecification = valueSpecification;
    _history = history;
    _executionLog = executionLog;
    _updated = updated;
    _type = type;
    _inlineKey = inlineKey;
    _format = format;
  }

  /**
   * Factory method that creates a grid cell for displaying a static value.
   * @param value The cell's value
   * @param updated true if the value was updated in the last calculation cycle
   * @param type TODO remove
   * @return A cell for displaying the value
   */
  /* package */ static ResultsCell forStaticValue(Object value, Class<?> type, TypeFormatter.Format format, boolean updated) {
    return new ResultsCell(value, null, null, null, updated, type, null, format);
  }

  // TODO is this version still required? or should all callers be specifying whether the value was updated?
  /**
   * Factory method that creates a grid cell for displaying a static value.
   * @param value The cell's value
   * @param type TODO remove
   * @return A cell for displaying the value
   */
  /* package */ static ResultsCell forStaticValue(Object value, Class<?> type, TypeFormatter.Format format) {
    return forStaticValue(value, type, format, false);
  }

  /**
   * Factory method that creates a grid cell for displaying a calculated value.
   * @param value The value
   * @param valueSpecification The value's specification
   * @param history The value's history
   * @param updated true if the value was updated in the last calculation cycle
   * @param type TODO remove this parameter
   * @return A cell for displaying the value
   */
  /* package */ static ResultsCell forCalculatedValue(Object value,
                                                      ValueSpecification valueSpecification,
                                                      Collection<Object> history,
                                                      AggregatedExecutionLog executionLog,
                                                      boolean updated,
                                                      Class<?> type,
                                                      TypeFormatter.Format format) {
    return new ResultsCell(value, valueSpecification, history, executionLog, updated, type, null, format);
  }

  /* package */ static ResultsCell forCalculatedValue(Object value,
                                                      ValueSpecification valueSpecification,
                                                      Collection<Object> history,
                                                      AggregatedExecutionLog executionLog,
                                                      boolean updated,
                                                      Class<?> type,
                                                      Object inlineKey,
                                                      TypeFormatter.Format format) {
    return new ResultsCell(value, valueSpecification, history, executionLog, updated, type, inlineKey, format);
  }

  /**
   * Factory method that returns a grid cell with no value.
   * @return An empty cell
   * @param emptyHistory Empty history appropriate for the cell's type. For types that support history it should
   * be an empty collection, for types that don't it should be null.
   * @param type TODO remove
   */
  /* package */ static ResultsCell empty(Collection<Object> emptyHistory, Class<?> type) {
    return new ResultsCell(null, null, emptyHistory, null, false, type, null, TypeFormatter.Format.CELL);
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
    return _value instanceof MissingValue;
  }

  /* package */ AggregatedExecutionLog getExecutionLog() {
    return _executionLog;
  }

  /* package */ boolean isUpdated() {
    return _updated;
  }

  /* package */ Class<?> getType() {
    return _type;
  }

  /* package */ Object getInlineKey() {
    return _inlineKey;
  }

  /* package */ TypeFormatter.Format getFormat() {
    return _format;
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
    result = 31 * result + (_executionLog != null ? _executionLog.hashCode() : 0);
    result = 31 * result + (_updated ? 1 : 0);
    return result;
  }
}
