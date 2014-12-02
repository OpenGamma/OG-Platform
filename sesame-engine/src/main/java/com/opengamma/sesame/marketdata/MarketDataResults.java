/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * The response to a request for a set of market data values.
 */
public class MarketDataResults {

  private final Map<MarketDataRequest, Result<?>> _data;
  private final Map<MarketDataRequest, Result<?>> _unavailable;

  private MarketDataResults(Map<MarketDataRequest, Result<?>> data,
                            Map<MarketDataRequest, Result<?>> unavailable) {
    _data = ImmutableMap.copyOf(data);
    _unavailable = ImmutableMap.copyOf(unavailable);
  }

  /**
   * @return the data from the requests that was available
   */
  public Map<MarketDataRequest, Result<?>> getData() {
    return _data;
  }

  /**
   * @return the data from the requests that wasn't available. The {@code Result} contains details of the reason,
   *   for example the ID was unknown or the user has insufficient permissions to view the data
   */
  public Map<MarketDataRequest, Result<?>> getUnavailableResults() {
    return _unavailable;
  }

  /**
   * @return the requests whose data wasn't available. This corresponds to the keys in the map returned
   *   by {@link #getUnavailableResults()}
   */
  public Set<MarketDataRequest> getUnavailableRequests() {
    return _unavailable.keySet();
  }

  /**
   * @return a builder for building a results instance
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return "MarketDataResults [_data=" + _data + ", _missing=" + _unavailable + "]";
  }

  /**
   * A builder for building instance of {@link MarketDataResults}.
   */
  public static final class Builder {

    private final Map<MarketDataRequest, Result<?>> _data = new HashMap<>();
    private final Map<MarketDataRequest, Result<?>> _unavailable = new HashMap<>();

    /**
     * Adds a result to the builder. The result can be a success or a failure.
     *
     * @param request a request for market data
     * @param result the result of looking up data for the request
     * @return this builder
     */
    public Builder add(MarketDataRequest request, Result<?> result) {
      ArgumentChecker.notNull(request, "request");
      ArgumentChecker.notNull(result, "result");

      if (result.isSuccess()) {
        _data.put(request, result);
        // in case the value is overwriting a previously missing value
        _unavailable.remove(request);
      } else if (!_data.containsKey(request)) { // don't add an unavailable value if there's already a valid value
        _unavailable.put(request, result);
      }
      return this;
    }

    /**
     * Adds a successful result to the builder.
     *
     * @param request a request for market data
     * @param value the market data value for the request
     * @return this builder
     */
    public Builder add(MarketDataRequest request, Object value) {
      ArgumentChecker.notNull(request, "request");
      ArgumentChecker.notNull(value, "value");

      _data.put(request, Result.success(value));
      _unavailable.remove(request);
      return this;
    }

    /**
     * Adds a missing result to the builder.
     *
     * @param request the request for which no data was available
     * @return this builder
     */
    public Builder missing(MarketDataRequest request) {
      ArgumentChecker.notNull(request, "request");

      // don't add a missing value if there's already a valid value
      if (!_data.containsKey(request)) {
        _unavailable.put(request, Result.failure(FailureStatus.MISSING_DATA, "No data available for {}", request));
      }
      return this;
    }

    /**
     * Adds all the data from some existing results to the builder.
     *
     * @param results existing market data results
     * @return this builder
     */
    public Builder addAll(MarketDataResults results) {
      ArgumentChecker.notNull(results, "results");

      _data.putAll(results._data);
      _unavailable.keySet().removeAll(results._data.keySet());
      return this;
    }

    /**
     * Builds a results instance from the data in this builder.
     *
     * @return a results instance containing the data from this builder
     */
    public MarketDataResults build() {
      return new MarketDataResults(_data, _unavailable);
    }
  }
}
