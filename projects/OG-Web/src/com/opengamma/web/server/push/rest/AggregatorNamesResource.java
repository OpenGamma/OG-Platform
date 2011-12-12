/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.web.server.AggregatedViewDefinitionManager;
import org.json.JSONArray;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST interface that produces a JSON list of aggregator names for populating the web client.
 */
@Path("aggregatornames")
public class AggregatorNamesResource {

  private final AggregatedViewDefinitionManager _aggregatedViewDefinitionManager;

  public AggregatorNamesResource(AggregatedViewDefinitionManager aggregatedViewDefinitionManager) {
    _aggregatedViewDefinitionManager = aggregatedViewDefinitionManager;
  }

  /**
   * @return JSON {@code [aggregatorName1, aggregatorName2, ...]}
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getAggregatorNamesJson() {
    return new JSONArray(_aggregatedViewDefinitionManager.getAggregatorNames()).toString();
  }
}
