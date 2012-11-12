/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.Duration;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calcnode.MissingInput;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 * <p>Cache of results from a running view process. This is intended for use with view clients where the first
 * set of results is a full set and subsequent results are deltas. This cache maintains a full set of results
 * which includes every target that has ever had a value calculated. It also keeps track of which values were
 * updated in the previous calculation cycle.</p>
 * <p>This class isn't thread safe.</p>
 */
/* package */ class ResultsCache {

  /** Maximum number of history values stored for each item. */
  private static final int MAX_HISTORY_SIZE = 20;

  // is this likely to change? will it ever by dynamic? i.e. client specifies what types it wants history for?
  /** Types of result values for which history is stored. */
  private static final Set<Class<?>> s_historyTypes =
      ImmutableSet.<Class<?>>of(Double.class, BigDecimal.class, CurrencyAmount.class);
  /** Empty result for types that don't have history, makes for cleaner code than using null. */
  private static final Result s_emptyResult = Result.empty();
  /** Empty result for types that have history, makes for cleaner code than using null. */
  private static final Result s_emptyResultWithHistory = Result.emptyWithHistory();

  /** The cached results. */
  private final Map<ResultKey, CacheItem> _results = Maps.newHashMap();

  /** ID that's incremented each time results are received, used for keeping track of which items were updated. */
  private long _lastUpdateId = 0;

  /** Duration of the last calculation cycle. */
  private Duration _lastCalculationDuration = Duration.ZERO;

  /**
   * Puts a set of results into the cache.
   * @param results The results, not null
   */
  /* package */ void put(final ViewResultModel results) {
    ArgumentChecker.notNull(results, "results");
    _lastUpdateId++;
    _lastCalculationDuration = results.getCalculationDuration();
    final List<ViewResultEntry> allResults = results.getAllResults();
    for (final ViewResultEntry result : allResults) {
      final ComputedValue computedValue = result.getComputedValue();
      put(result.getCalculationConfiguration(), computedValue.getSpecification(), computedValue.getValue());
    }
  }

  /**
   * Puts a set of results into the cache.
   * @param calcConfigName The name of the calculation configuration used to calculate the results
   * @param results The results
   * @param duration Duration of the calculation cycle that produced the results
   */
  /* package */ void put(final String calcConfigName, final List<Pair<ValueSpecification, Object>> results, final Duration duration) {
    _lastUpdateId++;
    _lastCalculationDuration = duration;
    for (final Pair<ValueSpecification, Object> result : results) {
      final ValueSpecification spec = result.getFirst();
      final Object value = result.getSecond();
      put(calcConfigName, spec, value);
    }
  }

  /**
   * Puts a single value into the cache.
   * @param calcConfigName The name of the calculation configuration used to calculate the results
   * @param spec The value's specification
   * @param value The value
   */
  private void put(final String calcConfigName, final ValueSpecification spec, final Object value) {
    final ResultKey key = new ResultKey(calcConfigName, spec);
    final CacheItem cacheResult = _results.get(key);
    if (cacheResult == null) {
      // don't create an item for an error value
      if (value instanceof MissingInput) {
        return;
      }
      final CacheItem newResult = CacheItem.forValue(value, _lastUpdateId);
      _results.put(key, newResult);
    } else {
      cacheResult.setLatestValue(value, _lastUpdateId);
    }
  }

  /**
   * Returns the history for a value and calculation configuration.
   * @param calcConfigName The calculation configuration name
   * @param valueSpec The value specification
   * @return The item's history or null if no history is stored for the item's type or no value has ever been received
   * for the item
   */
  /* package */ Collection<Object> getHistory(final String calcConfigName, final ValueSpecification valueSpec) {
    return getResult(calcConfigName, valueSpec, null).getHistory();
  }

  /**
   * Returns a cache result for a value specification and calculation configuration.
   * @param calcConfigName The calculation configuration name
   * @param valueSpec The value specification
   * @param columnType The expected type of the value, used to decide whether empty history should be provided for
   * a value that isn't in the cache but would have history if it were. Can be null in which case no history is
   * provided for missing values.
   * @return A cache result, not null
   */
  /* package */ Result getResult(final String calcConfigName, final ValueSpecification valueSpec, final Class<?> columnType) {
    final CacheItem item = _results.get(new ResultKey(calcConfigName, valueSpec));
    if (item != null) {
      // flag whether this result was updated by the last set of results that were put into the cache
      final boolean updatedByLastResults = (item.getLastUpdateId() == _lastUpdateId);
      return new Result(item.getValue(), item.getHistory(), updatedByLastResults);
    } else {
      if (s_historyTypes.contains(columnType)) {
        return s_emptyResultWithHistory;
      } else {
        return s_emptyResult;
      }
    }
  }

  /**
   * @return Duration of the last calculation cycle
   */
  public Duration getLastCalculationDuration() {
    return _lastCalculationDuration;
  }

  /**
   * Returns empty history appropriate for the type. For types that support history it will be an empty collection,
   * for types that don't it will be null.
   * @param type The type, possibly null
   * @return The history, possibly null
   */
  public Collection<Object> getEmptyHistory(final Class<?> type) {
    if (s_historyTypes.contains(type)) {
      return Collections.emptyList();
    } else {
      return null;
    }
  }

  /**
   * An item from the cache including its history and a flag indicating whether it was updated by the most recent
   * calculation cycle. Instances of this class are intended for users of the cache.
   */
  /* package */ static class Result {

    private final Object _value;
    private final Collection<Object> _history;
    private final boolean _updated;

    private Result(final Object value, final Collection<Object> history, final boolean updated) {
      _value = value;
      _history = history;
      _updated = updated;
    }

    /**
     * @return The most recent value, null if no value has ever been calculated for the requirement
     */
    /* package */ Object getValue() {
      return _value;
    }

    /**
     * @return The history for the value, empty if no value has been calculated, null if history isn't stored for the
     * requirement
     */
    /* package */ Collection<Object> getHistory() {
      return _history;
    }

    /**
     * @return true if the value was updated by the most recent calculation cycle
     */
    /* package */ boolean isUpdated() {
      return _updated;
    }

    /**
     * @return A result with no value and no history, for value requirements that never have history
     */
    private static Result empty() {
      return new Result(null, null, false);
    }

    /**
     * @return A result with no value and empty history, for value requirements that can have history
     */
    private static Result emptyWithHistory() {
      return new Result(null, Collections.emptyList(), false);
    }
  }

  /**
   * An item stored in the cache, this is an internal implementation detail.
   */
  private static final class CacheItem {

    private final Collection<Object> _history;

    private Object _latestValue;
    private long _lastUpdateId = -1;

    private CacheItem(final Collection<Object> history) {
      _history = history;
    }

    @SuppressWarnings("unchecked")
    private static CacheItem forValue(final Object value, final long lastUpdateId) {
      ArgumentChecker.notNull(value, "value");
      CircularFifoBuffer history;
      if (s_historyTypes.contains(value.getClass())) {
        history = new CircularFifoBuffer(MAX_HISTORY_SIZE);
      } else {
        history = null;
      }
      final CacheItem result = new CacheItem(history);
      result.setLatestValue(value, lastUpdateId);
      return result;
    }

    private Object getValue() {
      return _latestValue;
    }

    /**
     * Sets the latest value and the ID of the update that calculated it.
     * @param latestValue The value
     * @param lastUpdateId ID of the set of results that calculated it
     */
    private void setLatestValue(final Object latestValue, final long lastUpdateId) {
      _latestValue = latestValue;
      _lastUpdateId = lastUpdateId;
      if (_history != null) {
        _history.add(latestValue);
      }
    }


    /* package */ Collection<Object> getHistory() {
      if (_history != null) {
        return Collections.unmodifiableCollection(_history);
      } else {
        return null;
      }
    }

    /**
     * @return ID of the set of results that updated this item, used to decide whether the item was updated by
     * the most recent calculation cycle.
     */
    private long getLastUpdateId() {
      return _lastUpdateId;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("Result(");
      sb.append(getLastUpdateId());
      sb.append(", ");
      if (getValue() != null) {
        sb.append(getValue().toString());
      } else {
        sb.append("NULL");
      }
      return sb.append(")").toString();
    }

  }

  /**
   * Immutable key for items in the cache, this is in implelemtation detail.
   */
  private static class ResultKey {

    private final String _calcConfigName;
    private final ValueSpecification _valueSpec;

    private ResultKey(final String calcConfigName, final ValueSpecification valueSpec) {
      _calcConfigName = calcConfigName;
      _valueSpec = valueSpec;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final ResultKey resultKey = (ResultKey) o;
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

    @Override
    public String toString() {
      return _valueSpec.toString() + "/" + _calcConfigName;
    }

  }
}
