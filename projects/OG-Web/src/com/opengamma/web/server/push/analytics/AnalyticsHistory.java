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
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
/* package */ class AnalyticsHistory {

  private static final int MAX_HISTORY_SIZE = 20;

  // is this likely to change?
  private static final Set<Class<?>> s_historyTypes = ImmutableSet.<Class<?>>of(Double.class,
                                                                                BigDecimal.class,
                                                                                CurrencyAmount.class);

  private final Map<ResultKey, CircularFifoBuffer> _history = Maps.newHashMap();

  /* package */ void addResults(ViewComputationResultModel results) {
    List<ViewResultEntry> allResults = results.getAllResults();
    for (ViewResultEntry result : allResults) {
      ComputedValue computedValue = result.getComputedValue();
      Object value = computedValue.getValue();
      if (s_historyTypes.contains(value.getClass())) {
        ResultKey key = new ResultKey(result.getCalculationConfiguration(), computedValue.getSpecification());
        CircularFifoBuffer itemHistory = _history.get(key);
        if (itemHistory == null) {
          itemHistory = new CircularFifoBuffer(MAX_HISTORY_SIZE);
          _history.put(key, itemHistory);
        }
        itemHistory.add(value);
      }
    }
  }

  @SuppressWarnings("unchecked")
    /* package */ Collection<Object> getHistory(String calcConfigName, ValueSpecification valueSpec, Object currentValue) {
    CircularFifoBuffer valueHistory = _history.get(new ResultKey(calcConfigName, valueSpec));
    if (valueHistory != null) {
      return Collections.unmodifiableCollection(valueHistory);
    }
    if (s_historyTypes.contains(currentValue.getClass())) {
      return Collections.emptyList();
    }
    return null;
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
