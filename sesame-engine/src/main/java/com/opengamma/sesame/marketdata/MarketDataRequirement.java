/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;

/**
 * Contains information about a piece of market data that is required for a calculation.
 */
public abstract class MarketDataRequirement {

  MarketDataRequirement() {
  }

  /**
   * @return the ID of the data
   */
  public abstract MarketDataId getMarketDataId();

  /**
   * @return the time for which the data is valid
   */
  public abstract MarketDataTime getMarketDataTime();

  /**
   * Queries the builder to get the requirements this requirement depends on.
   *
   * @param builder a builder to build this market data
   * @param valuationTime the valuation time of the calculations
   * @param suppliedData the data supplied by the user. This can be safely ignored. If this method returns
   *   requirements for data that is in the supplied data it is not a problem. It is included to allow
   *   market data builders to examine the data in case it contains requirements that are equivalent but
   *   not necessarily equal to the builders requirements. For example if an FX rate is in the supplied data
   *   that is the inverse of the rate required by the builder, the builder can use it.
   * @return requirements for the data needed to build this data
   */
  public abstract Set<MarketDataRequirement> getRequirements(MarketDataBuilder builder,
                                                             ZonedDateTime valuationTime,
                                                             MarketDataEnvironment suppliedData);
}
