/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.Map;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.tuple.Pair;

/**
 * A normalization hook for historical market data.
 */
public interface HistoricalMarketDataNormalizer {

  /**
   * Normalizes a value acquired for the given identifier from underlying historical data.
   * The value name is the requested value from the satisfied {@link ValueRequirement} on
   * the given target.
   * 
   * @param identifiers target identifier(s)
   * @param name name of the value (as specified in a requirement) - e.g. it could be
   *        a normalized name from {@link MarketValueRequirementNames}.
   * @param value either the raw value from the time series, not null
   * @return the normalized form, or null to reject the value
   */
  Object normalize(ExternalIdBundle identifiers, String name, Object value);

  /**
   * Bulk normalization operation.
   * 
   * @param values the values to normalize as a map of ExternalId/Value name pairs to
   *        the original values, not null
   * @return the normalized form, as a map, not null but with values omitted if they
   *         were rejected
   */
  Map<Pair<ExternalIdBundle, String>, Object> normalize(Map<Pair<ExternalIdBundle, String>, Object> values);

}
