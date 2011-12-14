/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;

/**
 * Instance of {@link HistoricalMarketDataNormalizer} that passes the input to another
 * normalizer based on the scheme.
 */
public class SchemeMappedDataNormalizer implements HistoricalMarketDataNormalizer {

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

}
