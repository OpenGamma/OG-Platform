/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.tuple.Pair;

/**
 * Instance of {@link HistoricalMarketDataNormalizer} that passes the input to other
 * normalizers until one accepts the value.
 */
public class SequentialDataNormalizer implements HistoricalMarketDataNormalizer {

  private final HistoricalMarketDataNormalizer[] _normalizers;

  public SequentialDataNormalizer(final List<? extends HistoricalMarketDataNormalizer> normalizers) {
    _normalizers = normalizers.toArray(new HistoricalMarketDataNormalizer[normalizers.size()]);
  }

  private HistoricalMarketDataNormalizer[] getNormalizers() {
    return _normalizers;
  }
  
  @Override
  public Object normalize(final ExternalIdBundle identifiers, final String name, final Object value) {
    for (HistoricalMarketDataNormalizer normalizer : getNormalizers()) {
      final Object normalized = normalizer.normalize(identifiers, name, value);
      if (normalized != null) {
        return normalized;
      }
    }
    // None accepted, so reject completely
    return null;
  }

  @Override
  public Map<Pair<ExternalIdBundle, String>, Object> normalize(final Map<Pair<ExternalIdBundle, String>, Object> values) {
    final Map<Pair<ExternalIdBundle, String>, Object> results = Maps.newHashMapWithExpectedSize(values.size());
    final Map<Pair<ExternalIdBundle, String>, Object> inputs = new HashMap<Pair<ExternalIdBundle, String>, Object>(values);
    for (HistoricalMarketDataNormalizer normalizer : getNormalizers()) {
      final Map<Pair<ExternalIdBundle, String>, Object> normalized = normalizer.normalize(inputs);
      results.putAll(normalized);
      inputs.keySet().removeAll(normalized.keySet());
      if (inputs.isEmpty()) {
        break;
      }
    }
    return results;
  }

}
