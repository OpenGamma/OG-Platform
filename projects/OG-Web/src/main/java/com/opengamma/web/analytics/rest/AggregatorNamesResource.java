/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;

import com.opengamma.util.ArgumentChecker;

/**
 * REST interface that produces a JSON list of aggregator names for populating the web client.
 */
@Path("aggregators")
public class AggregatorNamesResource {

  private final Set<String> _aggregatorNames;

  public AggregatorNamesResource(Set<String> aggregatorNames) {
    ArgumentChecker.notNull(aggregatorNames, "aggregatorNames");
    _aggregatorNames = aggregatorNames;
  }

  /**
   * @return JSON {@code [aggregatorName1, aggregatorName2, ...]}
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getAggregatorNamesJson() {
    return new JSONArray(_aggregatorNames).toString();
  }
}
