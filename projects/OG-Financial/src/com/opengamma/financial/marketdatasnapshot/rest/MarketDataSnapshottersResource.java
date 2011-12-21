/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.marketdatasnapshot.MarketDataSnapshotterImpl;
import com.opengamma.financial.view.rest.DataViewProcessorsResource;

/**
 * RESTful back-end to provide access to {@link MarketDataSnapshotter}s
 * TODO: this directly
 */
@Path("/data/marketDataSnapshotters")
public class MarketDataSnapshottersResource {
  private final DataViewProcessorsResource _processors;
  private final VolatilityCubeDefinitionSource _volatilityCubeDefinitionSource;
  
  public MarketDataSnapshottersResource(final DataViewProcessorsResource processors, VolatilityCubeDefinitionSource volatilityCubeDefinitionSource) {
    _processors = processors;
    _volatilityCubeDefinitionSource = volatilityCubeDefinitionSource;
  }
  
  @Path("{viewProcessorId}")
  public MarketDataSnapshotterResource createFromViewProcessor(@PathParam("viewProcessorId") String viewProcessorId) {
    ViewProcessor viewProcessor = _processors.findViewProcessor(viewProcessorId).getViewProcessor();
    return new MarketDataSnapshotterResource(viewProcessor, new MarketDataSnapshotterImpl(_volatilityCubeDefinitionSource));
  }
  
  @GET
  public Response get() {
    return Response.ok().build();
  }
}
