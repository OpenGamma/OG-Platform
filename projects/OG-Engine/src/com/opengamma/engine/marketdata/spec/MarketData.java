/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.spec;

import javax.time.calendar.LocalDate;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Static helper for constructing instances of {@link MarketDataSpecification}.
 */
@PublicAPI
public class MarketData {

  /**
   * Avoid creating multiple instances since the live case is so common and most requests should come through this
   * helper. 
   */
  private static final LiveMarketDataSpecification LIVE_SPEC = new LiveMarketDataSpecification();
  
  /**
   * Gets a live market data specification.
   * 
   * @return the live market data specification
   */
  public static LiveMarketDataSpecification live() {
    return LIVE_SPEC;
  }
  
  /**
   * Gets a live market data specification for a specific data source.
   * 
   * @param dataSource  the name of the data source, not null
   * @return the live market data specification, not null
   */
  public static LiveMarketDataSpecification live(String dataSource) {
    ArgumentChecker.notNull(dataSource, "dataSource");
    return new LiveMarketDataSpecification(dataSource.intern());
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets a historical market data specification.
   * 
   * @param date  the date, not null
   * @param timeSeriesResolverKey time series resolver key, or null for the system default
   * @param timeSeriesFieldResolverKey time series field resolver key, or null for the system default
   * @return the historical market data specification, not null
   */
  public static HistoricalMarketDataSpecification historical(LocalDate date, String timeSeriesResolverKey, String timeSeriesFieldResolverKey) {
    return new FixedHistoricalMarketDataSpecification(timeSeriesResolverKey, timeSeriesFieldResolverKey, date);
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets a user market data specification.
   * 
   * @param snapshotId  the unique identifier of the snapshot, not null
   * @return the user market data specification, not null
   */
  public static UserMarketDataSpecification user(UniqueId snapshotId) {
    return new UserMarketDataSpecification(snapshotId);
  }
  
}
