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
public class MarketDataResponse {

  private final Map<MarketDataRequest, Result<?>> _data;
  private final Map<MarketDataRequest, Result<?>> _missing;

  private MarketDataResponse(Map<MarketDataRequest, Result<?>> data,
                             Map<MarketDataRequest, Result<?>> missing) {
    _data = ImmutableMap.copyOf(data);
    _missing = ImmutableMap.copyOf(missing);
  }

  public Map<MarketDataRequest, Result<?>> getData() {
    return _data;
  }

  public Map<MarketDataRequest, Result<?>> getMissingResults() {
    return _missing;
  }

  public Set<MarketDataRequest> getMissingRequests() {
    return _missing.keySet();
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return "BulkMarketDataResponse [_data=" + _data + ", _missing=" + _missing + "]";
  }

  public static final class Builder {

    private final Map<MarketDataRequest, Result<?>> _data = new HashMap<>();
    private final Map<MarketDataRequest, Result<?>> _missing = new HashMap<>();

    public Builder add(MarketDataRequest request, Result<?> result) {
      ArgumentChecker.notNull(request, "request");
      ArgumentChecker.notNull(result, "result");

      if (result.isSuccess()) {
        _data.put(request, result);
        // in case the value is overwriting a previously missing value
        _missing.remove(request);
      } else if (!_data.containsKey(request)) { // don't add a missing value if there's already a valid value
        _missing.put(request, result);
      }
      return this;
    }

    public Builder add(MarketDataRequest request, Object value) {
      ArgumentChecker.notNull(request, "request");
      ArgumentChecker.notNull(value, "value");

      _data.put(request, Result.success(value));
      _missing.remove(request);
      return this;
    }

    public Builder missing(MarketDataRequest request) {
      ArgumentChecker.notNull(request, "request");

      // don't add a missing value if there's already a valid value
      if (!_data.containsKey(request)) {
        _missing.put(request, Result.failure(FailureStatus.MISSING_DATA, "No data available for {}", request));
      }
      return this;
    }

    public Builder addAll(MarketDataResponse response) {
      ArgumentChecker.notNull(response, "response");

      _data.putAll(response._data);
      _missing.keySet().removeAll(response._data.keySet());
      return this;
    }

    public MarketDataResponse build() {
      return new MarketDataResponse(_data, _missing);
    }
  }
}
