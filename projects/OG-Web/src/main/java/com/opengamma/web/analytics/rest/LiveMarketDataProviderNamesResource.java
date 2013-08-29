/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;

import com.opengamma.engine.marketdata.live.LiveMarketDataProviderFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * REST endpoint returning a JSON array containing the names of available live data provider factories.
 */
@Path("livedatasources")
public class LiveMarketDataProviderNamesResource {

  private final LiveMarketDataProviderFactory _liveMarketDataProviderFactory;
  
  public LiveMarketDataProviderNamesResource(LiveMarketDataProviderFactory liveMarketDataProviderFactory) {
    ArgumentChecker.notNull(liveMarketDataProviderFactory, "liveMarketDataProviderFactory");
    _liveMarketDataProviderFactory = liveMarketDataProviderFactory;
  }
  
  /**
   * @return JSON {@code [dataSourceName1, dataSourceName2, ...]}
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getLiveDataSourceNames() {
    return new JSONArray(_liveMarketDataProviderFactory.getProviderNames()).toString();
  }
  
}
