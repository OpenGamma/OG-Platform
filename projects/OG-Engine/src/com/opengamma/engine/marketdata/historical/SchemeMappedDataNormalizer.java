/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.tuple.Pair;

/**
 * Instance of {@link HistoricalMarketDataNormalizer} that passes the input to another
 * normalizer based on the scheme.
 */
public class SchemeMappedDataNormalizer implements HistoricalMarketDataNormalizer {

  private static final Logger s_logger = LoggerFactory.getLogger(SchemeMappedDataNormalizer.class);

  private final Map<ExternalScheme, HistoricalMarketDataNormalizer> _normalizers;

  private static ExternalScheme toExternalScheme(final Object o) {
    if (o instanceof ExternalScheme) {
      return (ExternalScheme) o;
    } else if (o instanceof String) {
      return ExternalScheme.of((String) o);
    } else {
      throw new IllegalArgumentException("Bad key - " + o);
    }
  }

  /**
   * Creates a new normalizer instance. The map may contain keys that are any of:
   * <li>
   *   <ul>Strings
   *   <ul>ExternalSchemes
   *   <ul>Collections of the above
   * </li>
   * 
   * @param normalizers the map of schemes to normalizers 
   */
  @SuppressWarnings("unchecked")
  public SchemeMappedDataNormalizer(final Map<?, ? extends HistoricalMarketDataNormalizer> normalizers) {
    _normalizers = new HashMap<ExternalScheme, HistoricalMarketDataNormalizer>();
    for (Map.Entry<?, ? extends HistoricalMarketDataNormalizer> normalizer : normalizers.entrySet()) {
      if (normalizer.getKey() instanceof Collection) {
        for (Object x : (Collection<Object>) normalizer.getKey()) {
          _normalizers.put(toExternalScheme(x), normalizer.getValue());
        }
      } else {
        _normalizers.put(toExternalScheme(normalizer.getKey()), normalizer.getValue());
      }
    }
  }

  private Map<ExternalScheme, ? extends HistoricalMarketDataNormalizer> getNormalizers() {
    return _normalizers;
  }

  @Override
  public Object normalize(final ExternalIdBundle identifiers, final String name, final Object value) {
    for (ExternalId identifier : identifiers.getExternalIds()) {
      final HistoricalMarketDataNormalizer normalizer = getNormalizers().get(identifier.getScheme());
      if (normalizer != null) {
        final Object result = normalizer.normalize(identifiers, name, value);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  @Override
  public Map<Pair<ExternalIdBundle, String>, Object> normalize(final Map<Pair<ExternalIdBundle, String>, Object> values) {
    final Map<HistoricalMarketDataNormalizer, Map<Pair<ExternalIdBundle, String>, Object>> delegates = new HashMap<HistoricalMarketDataNormalizer, Map<Pair<ExternalIdBundle, String>, Object>>();
    for (Map.Entry<Pair<ExternalIdBundle, String>, Object> value : values.entrySet()) {
      for (ExternalId identifier : value.getKey().getFirst().getExternalIds()) {
        final HistoricalMarketDataNormalizer normalizer = getNormalizers().get(identifier.getScheme());
        if (normalizer != null) {
          Map<Pair<ExternalIdBundle, String>, Object> delegate = delegates.get(normalizer);
          if (delegate == null) {
            delegate = new HashMap<Pair<ExternalIdBundle, String>, Object>();
            delegates.put(normalizer, delegate);
          }
          delegate.put(value.getKey(), value.getValue());
          break;
        }
      }
    }
    final Map<Pair<ExternalIdBundle, String>, Object> results = Maps.newHashMapWithExpectedSize(values.size());
    for (Map.Entry<HistoricalMarketDataNormalizer, Map<Pair<ExternalIdBundle, String>, Object>> delegate : delegates.entrySet()) {
      s_logger.debug("Delegating {} to {}", delegate.getValue(), delegate.getKey());
      final Map<Pair<ExternalIdBundle, String>, Object> normalized = delegate.getKey().normalize(delegate.getValue());
      if (normalized != null) {
        s_logger.debug("Normalized results = {}", normalized);
        results.putAll(normalized);
      }
    }
    return results;
  }

}
