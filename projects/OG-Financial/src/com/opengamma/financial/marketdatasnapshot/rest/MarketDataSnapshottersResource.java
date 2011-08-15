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

import com.opengamma.engine.marketdatasnapshot.MarketDataSnapshotter;
import com.opengamma.engine.marketdatasnapshot.MarketDataSnapshotterImpl;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.view.rest.DataViewProcessorsResource;

/**
 * RESTful back-end to provide access to {@link MarketDataSnapshotter}s
 * TODO: this directly
 */
@Path("/data/marketDataSnapshotters")
public class MarketDataSnapshottersResource {
  //private final Map<UniqueId, MarketDataSnapshotterResource> _resourceMap = new HashMap<UniqueId, MarketDataSnapshotterResource>();
  private final DataViewProcessorsResource _processors;

  public MarketDataSnapshottersResource(final DataViewProcessorsResource processors) {
    _processors = processors;
  }
  
  @Path("{viewProcessorId}")
  public MarketDataSnapshotterResource createFromViewProcessor(@PathParam("viewProcessorId") String viewProcessorId) {
    ViewProcessor viewProcessor = _processors.findViewProcessor(viewProcessorId).getViewProcessor();
    return new MarketDataSnapshotterResource(viewProcessor, new MarketDataSnapshotterImpl());
  }
  
  @GET
  public Response get() {
    return _processors.get();
  }
}
