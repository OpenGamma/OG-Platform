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

import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * REST endpoint returning a JSON array containing the names of available live market data specifications.
 */
@Path("livedatasources")
@SuppressWarnings("deprecation")
public class LiveMarketDataSpecificationNamesResource {

  private final NamedMarketDataSpecificationRepository _marketDataSpecRepo;

  public LiveMarketDataSpecificationNamesResource(NamedMarketDataSpecificationRepository marketDataSpecRepo) {
    ArgumentChecker.notNull(marketDataSpecRepo, "marketDataSpecRepo");
    _marketDataSpecRepo = marketDataSpecRepo;
  }

  /**
   * @return JSON {@code [dataSourceName1, dataSourceName2, ...]}
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getLiveDataSourceNames() {
    return new JSONArray(_marketDataSpecRepo.getNames()).toString();
  }
  
}
