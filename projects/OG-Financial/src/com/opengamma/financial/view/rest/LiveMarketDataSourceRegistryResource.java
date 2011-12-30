/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.Collection;

import javax.ws.rs.GET;

import com.opengamma.engine.marketdata.live.LiveMarketDataSourceRegistry;

/**
 * RESTful resource for {@link LiveMarketDataSourceRegistry}
 */
public class LiveMarketDataSourceRegistryResource {

  private final LiveMarketDataSourceRegistry _liveMarketDataSourceRegistry;

  public LiveMarketDataSourceRegistryResource(LiveMarketDataSourceRegistry liveMarketDataSourceRegistry) {
    _liveMarketDataSourceRegistry = liveMarketDataSourceRegistry;
  }

  @GET
  public Collection<String> getDataSources() {
    return _liveMarketDataSourceRegistry.getDataSources();
  }
}
