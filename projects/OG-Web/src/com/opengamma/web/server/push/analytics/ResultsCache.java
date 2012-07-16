/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
/* package */ class ResultsCache {

  private static final int MAX_HISTORY_SIZE = 20;

  // is this likely to change? will it ever by dynamic? i.e. client specifies what types it wants history for?
  private static final Set<Class<?>> s_historyTypes =
      ImmutableSet.<Class<?>>of(Double.class, BigDecimal.class, CurrencyAmount.class);
  private static final Result s_emptyResult = Result.empty();
  private static final Result s_emptyResultWithHistory = Result.emptyWithHistory();

  private final Map<ResultKey, Result> _results = Maps.newHashMap();

  /* package */ void put(ViewResultModel results) {
    List<ViewResultEntry> allResults = results.getAllResults();
    for (ViewResultEntry result : allResults) {
      ComputedValue computedValue = result.getComputedValue();
      Object value = computedValue.getValue();
      ResultKey key = new ResultKey(result.getCalculationConfiguration(), computedValue.getSpecification());
      Result cacheResult = _results.get(key);
      if (cacheResult == null) {
        Result newResult = Result.forValue(value);
        _results.put(key, newResult);
      } else {
        cacheResult.setLatestValue(value);
      }
    }
  }

    /* package */ Collection<Object> getHistory(String calcConfigName, ValueSpecification valueSpec) {
      return getHistory(calcConfigName, valueSpec, null);
    }

    /* package */ Collection<Object> getHistory(String calcConfigName, ValueSpecification valueSpec, Class<?> columnType) {
    Result result = getResult(calcConfigName, valueSpec, columnType);
    return result.getHistory();
  }

  /* package */ Result getResult(String calcConfigName, ValueSpecification valueSpec, Class<?> columnType) {
    Result result = _results.get(new ResultKey(calcConfigName, valueSpec));
    if (result != null) {
      return result;
    } else {
      if (s_historyTypes.contains(columnType)) {
        return s_emptyResultWithHistory;
      } else {
        return s_emptyResult;
      }
    }
  }

  /* package */ /*boolean isHistoryType(Class<?> columnType) {
    return s_historyTypes.contains(columnType);
  }*/

  /* package */ static class Result {

    private final Collection<Object> _history;

    private Object _latestValue;

    @SuppressWarnings("unchecked")
    private Result(Collection<Object> history) {
      _history = history;
    }

    @SuppressWarnings("unchecked")
    private static Result forValue(Object value) {
      ArgumentChecker.notNull(value, "latestValue");
      CircularFifoBuffer history;
      if (s_historyTypes.contains(value.getClass())) {
        history = new CircularFifoBuffer(MAX_HISTORY_SIZE);
      } else {
        history = null;
      }
      Result result = new Result(history);
      result.setLatestValue(value);
      return result;
    }

    private static Result empty() {
      return new Result(null);
    }

    private static Result emptyWithHistory() {
      return new Result(Collections.emptyList());
    }

    /* package */ Object getLatestValue() {
      return _latestValue;
    }

    private void setLatestValue(Object latestValue) {
      _latestValue = latestValue;
      if (_history != null) {
        _history.add(latestValue);
      }
    }

    /* package */
    @SuppressWarnings("unchecked")
    Collection<Object> getHistory() {
      if (_history != null) {
        return Collections.unmodifiableCollection(_history);
      } else {
        return null;
      }
    }
  }

  private static class ResultKey {

    private final String _calcConfigName;
    private final ValueSpecification _valueSpec;

    private ResultKey(String calcConfigName, ValueSpecification valueSpec) {
      _calcConfigName = calcConfigName;
      _valueSpec = valueSpec;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ResultKey resultKey = (ResultKey) o;
      if (!_calcConfigName.equals(resultKey._calcConfigName)) {
        return false;
      }
      return _valueSpec.equals(resultKey._valueSpec);
    }

    @Override
    public int hashCode() {
      int result = _calcConfigName.hashCode();
      result = 31 * result + _valueSpec.hashCode();
      return result;
    }
  }
}
