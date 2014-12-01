/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Set;

import com.opengamma.util.result.FailureStatus;

/**
 * A source of market data.
 * <p>
 * This may be a source of live market data or backed by a database of historical data or snapshots.
 */
public interface MarketDataSource {

  /**
   * Returns a set of market data items.
   * <p>
   * The response contains a result for every request. If the market data is not present, the result will
   * be returned as a failure. Example reasons are {@link FailureStatus#MISSING_DATA}
   * and {@link FailureStatus#PERMISSION_DENIED}.
   *
   * @return a response containing success results containing the market data value,
   *  or failure results with a status explaining why data was not returned
   */
  MarketDataResponse get(Set<MarketDataRequest> requests);

}
