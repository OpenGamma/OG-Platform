/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.Map;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.tuple.Pair;

/**
 * Instance of {@link HistoricalMarketDataNormalizer} that passes all inputs through
 * unchanged.
 */
public class IdentityDataNormalizer implements HistoricalMarketDataNormalizer {

  @Override
  public Object normalize(final ExternalIdBundle identifiers, final String name, final Object value) {
    return value;
  }

  @Override
  public Map<Pair<ExternalIdBundle, String>, Object> normalize(final Map<Pair<ExternalIdBundle, String>, Object> values) {
    return values;
  }

}
