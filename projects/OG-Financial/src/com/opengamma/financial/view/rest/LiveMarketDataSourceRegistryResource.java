/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

import com.opengamma.engine.marketdata.live.LiveMarketDataSourceRegistry;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for {@link LiveMarketDataSourceRegistry}
 */
public class LiveMarketDataSourceRegistryResource extends AbstractDataResource {

  private final LiveMarketDataSourceRegistry _liveMarketDataSourceRegistry;

  public LiveMarketDataSourceRegistryResource(LiveMarketDataSourceRegistry liveMarketDataSourceRegistry) {
    _liveMarketDataSourceRegistry = liveMarketDataSourceRegistry;
  }

  @GET
  public Response getDataSources() {
    return responseOkFudge(_liveMarketDataSourceRegistry.getDataSources());
  }

}
