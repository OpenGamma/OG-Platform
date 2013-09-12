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

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

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
      ImmutableSet.of(Double.class, BigDecimal.class, CurrencyAmount.class, LocalDateLabelledMatrix1D.class);
  /** Empty result for types that don't have history, makes for cleaner code than using null. */
  private static final Result s_emptyResult = Result.empty();
  /** Empty result for types that have history, makes for cleaner code than using null. */
  private static final Result s_emptyResultWithHistory = Result.emptyWithHistory();

  /** The cached results. */
  private final Map<ResultKey, CacheItem> _results = Maps.newHashMap();

  /** Cache of portfolio entities, i.e. trades, positions, securities */
  private final Map<ObjectId, CacheItem> _entities = Maps.newHashMap();

  /** ID that's incremented each time results are received, used for keeping track of which items were updated. */
  private long _lastUpdateId;

  /** Duration of the last calculation cycle. */
  private Duration _lastCalculationDuration = Duration.ZERO;
  /** Last valuation time */
  private Instant _valuationTime = Instant.MIN;

  /**
   * Puts a set of main grid results into the cache.
   * @param results The results, not null
   */
  /* package */ void put(ViewResultModel results) {
    ArgumentChecker.notNull(results, "results");
    _lastUpdateId++;
    _lastCalculationDuration = results.getCalculationDuration();
    _valuationTime = results.getViewCycleExecutionOptions().getValuationTime();
    List<ViewResultEntry> allResults = results.getAllResults();
    Set<ResultKey> updatedKeys = Sets.newHashSet();
    for (ViewResultEntry result : allResults) {
      put(result.getCalculationConfiguration(), result.getComputedValue());
      updatedKeys.add(new ResultKey(result.getCalculationConfiguration(), result.getComputedValue().getSpecification()));
    }
    // duplicate the last history item for anything that hasn't changed this cycle
    for (Map.Entry<ResultKey, CacheItem> entry : _results.entrySet()) {
      if (entry.getValue().getHistory() != null && !updatedKeys.contains(entry.getKey())) {
        entry.getValue().valueUnchanged();
      }
    }
  }

  /**
   * Puts a set of dependency graph results into the cache.
   * @param calcConfigName The name of the calculation configuration used to calculate the results
   * @param results The results
   * @param duration Duration of the calculation cycle that produced the results
   */
  /* package */ void put(String calcConfigName, Map<ValueSpecification, ComputedValueResult> results, Duration duration) {
    _lastUpdateId++;
    _lastCalculationDuration = duration;
    for (ComputedValueResult result : results.values()) {
      put(calcConfigName, result);
    }
  }

  /**
   * Puts a single value into the cache.
   * @param calcConfigName The name of the calculation configuration used to calculate the results
   * @param result The result value and associated data
   */
  private void put(String calcConfigName, ComputedValueResult result) {
    ValueSpecification spec = result.getSpecification();
    Object value = result.getValue();
    ResultKey key = new ResultKey(calcConfigName, spec);
    CacheItem cacheResult = _results.get(key);
    if (cacheResult == null) {
      _results.put(key, new CacheItem(value, result.getAggregatedExecutionLog(), _lastUpdateId));
    } else {
      cacheResult.setLatestValue(value, result.getAggregatedExecutionLog(), _lastUpdateId);
    }
  }

  /* package */ void put(List<UniqueIdentifiable> entities) {
    ArgumentChecker.notNull(entities, "entities");
    _lastUpdateId++;
    for (UniqueIdentifiable entity : entities) {
      // TODO why is this failing sometimes?
      //ArgumentChecker.notNull(entity, "entity");
      if (entity != null) {
        putEntity(entity);
      }
    }
  }

  /* package */ void put(UniqueIdentifiable entity) {
    ArgumentChecker.notNull(entity, "entity");
    ++_lastUpdateId;
    putEntity(entity);
  }

  /* package */ void remove(ObjectId id) {
    ++_lastUpdateId;
    _entities.remove(id);
  }

  private void putEntity(UniqueIdentifiable entity) {
    ObjectId id = entity.getUniqueId().getObjectId();
    CacheItem cacheResult = _entities.get(id);
    if (cacheResult == null) {
      _entities.put(id, new CacheItem(entity, null, _lastUpdateId));
    } else {
      cacheResult.setLatestValue(entity, null, _lastUpdateId);
    }
  }

  /* package */ Result getEntity(ObjectId id) {
    CacheItem item = _entities.get(id);
    if (item != null) {
      // flag whether this result was updated by the last set of results that were put into the cache
      boolean updatedByLastResults = (item.getLastUpdateId() == _lastUpdateId);
      return Result.forValue(item.getValue(), null, null, updatedByLastResults);
    } else {
      return s_emptyResult;
    }
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
  /* package */ Result getResult(String calcConfigName, ValueSpecification valueSpec, Class<?> columnType) {
    CacheItem item = _results.get(new ResultKey(calcConfigName, valueSpec));
    if (item != null) {
      // flag whether this result was updated by the last set of results that were put into the cache
      boolean updatedByLastResults = (item.getLastUpdateId() == _lastUpdateId);
      return Result.forValue(item.getValue(), item.getHistory(), item.getAggregatedExecutionLog(), updatedByLastResults);
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
  /* package */ Duration getLastCalculationDuration() {
    return _lastCalculationDuration;
  }
  
  /**
   * Gets the lastCalculationTime.
   * @return the lastCalculationTime
   */
  /* package */ Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * Returns empty history appropriate for the type. For types that support history it will be an empty collection,
   * for types that don't it will be null.
   * @param type The type, possibly null
   * @return The history, possibly null
   */
  /* package */ Collection<Object> emptyHistory(Class<?> type) {
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
  /* package */ static final class Result {

    private final Object _value;
    private final Collection<Object> _history;
    private final boolean _updated;
    private final AggregatedExecutionLog _aggregatedExecutionLog;

    private Result(Object value, Collection<Object> history, AggregatedExecutionLog aggregatedExecutionLog, boolean updated) {
      _value = value;
      _history = history;
      _aggregatedExecutionLog = aggregatedExecutionLog;
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

    private static Result forValue(Object value,
                                   Collection<Object> history,
                                   AggregatedExecutionLog aggregatedExecutionLog,
                                   boolean updated) {
      ArgumentChecker.notNull(value, "value");
      return new Result(value, history, aggregatedExecutionLog, updated);
    }

    /**
     * @return A result with no value and no history, for value requirements that never have history
     */
    private static Result empty() {
      return new Result(null, null, null, false);
    }

    /**
     * @return A result with no value and empty history, for value requirements that can have history
     */
    private static Result emptyWithHistory() {
      return new Result(null, Collections.emptyList(), null, false);
    }

    /* package */ AggregatedExecutionLog getAggregatedExecutionLog() {
      return _aggregatedExecutionLog;
    }
  }

  /**
   * An item stored in the cache, this is an internal implementation detail.
   */
  private static final class CacheItem {

    private Collection<Object> _history;
    private Object _latestValue;
    private long _lastUpdateId = -1;
    private AggregatedExecutionLog _aggregatedExecutionLog;

    private CacheItem(Object value, AggregatedExecutionLog executionLog, long lastUpdateId) {
      setLatestValue(value, executionLog, lastUpdateId);
    }

    /**
     * Sets the latest value and the ID of the update that calculated it.
     * @param latestValue The value
     * @param executionLog The execution log associated generated when calculating the value
     * @param lastUpdateId ID of the set of results that calculated it
     */
    @SuppressWarnings("unchecked")
    private void setLatestValue(Object latestValue, AggregatedExecutionLog executionLog, long lastUpdateId) {
      ArgumentChecker.notNull(latestValue, "latestValue");
      _latestValue = latestValue;
      _lastUpdateId = lastUpdateId;
      _aggregatedExecutionLog = executionLog;
      // this can happen if the first value is an error and then real values arrive. this is possible if market
      // data subscriptions take time to set up. in that case the history will initially be null (because error
      // sentinel types aren't in s_historyTypes) and then when a valid value arrives the type can be checked and
      // history created if required
      if (_history == null && s_historyTypes.contains(latestValue.getClass())) {
        _history = new CircularFifoBuffer(MAX_HISTORY_SIZE);
      }
      if (_history != null) {
        _history.add(latestValue);
      }
    }

    private Object getValue() {
      return _latestValue;
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

    private AggregatedExecutionLog getAggregatedExecutionLog() {
      return _aggregatedExecutionLog;
    }

    /**
     * Invoked when a calculation cycle completes and doesn't update the value for an item. The latest value is
     * inserted into the history again to ensure the history is up to date.
     */
    private void valueUnchanged() {
      _history.add(_latestValue);
    }
  }

  /**
   * Immutable key for items in the cache, this is in implelemtation detail.
   */
  private static final class ResultKey {

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

    @Override
    public String toString() {
      return _valueSpec.toString() + "/" + _calcConfigName;
    }

  }
}
