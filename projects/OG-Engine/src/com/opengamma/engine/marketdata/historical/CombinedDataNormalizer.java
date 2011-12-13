/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.List;

import com.opengamma.id.ExternalId;

/**
 * Instance of {@link HistoricalMarketDataNormalizer} that passes the input to other
 * normalizers until one accepts the value.
 */
public class CombinedDataNormalizer implements HistoricalMarketDataNormalizer {

  private final HistoricalMarketDataNormalizer[] _normalizers;

  public CombinedDataNormalizer(final List<HistoricalMarketDataNormalizer> normalizers) {
    _normalizers = normalizers.toArray(new HistoricalMarketDataNormalizer[normalizers.size()]);
  }

  private HistoricalMarketDataNormalizer[] getNormalizers() {
    return _normalizers;
  }
  
  @Override
  public Object normalize(final ExternalId identifier, final String name, final Object value) {
    for (HistoricalMarketDataNormalizer normalizer : getNormalizers()) {
      final Object normalized = normalizer.normalize(identifier, name, value);
      if (normalized != null) {
        return normalized;
      }
    }
    // None accepted, so reject completely
    return null;
  }

}
