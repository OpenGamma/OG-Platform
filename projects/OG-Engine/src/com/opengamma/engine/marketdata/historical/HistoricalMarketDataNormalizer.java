/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;

/**
 * A normalization hook for historical market data.
 */
public interface HistoricalMarketDataNormalizer {

  /**
   * Normalizes a value acquired for the given identifier from underlying historical data.
   * The value name is the requested value from the satisfied {@link ValueRequirement} on
   * the given target.
   * 
   * @param identifier target identifier
   * @param name name of the value (as specified in a requirement) - e.g. it could be
   *        a normalized name from {@link MarketValueRequirementNames}.
   * @param value either the raw value from the time series, not null
   * @return the normalized form, or null to reject the value
   */
  Object normalize(ExternalId identifier, String name, Object value);

  // TODO: bulk operations could be useful here as some the underlying utilities such as unique id resolution support bulk 

}
