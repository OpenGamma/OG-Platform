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
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueId;

/**
 * RESTful resource for a {@link MarketDataSnapshotter}.
 */
@Path("/data/marketDataSnapshotters/{viewProcessorId}")
public class MarketDataSnapshotterResource {

  //CSOFF: just constants
  public static final String PATH_CREATE_SNAPSHOT = "create";
  public static final String PATH_YIELD_CURVE_SPECS = "yieldCurveSpecs";
  //CSON: just constants

  
  private final ViewProcessor _viewProcessor;
  private final MarketDataSnapshotter _snapshotter;
  
  public MarketDataSnapshotterResource(ViewProcessor viewProcessor, MarketDataSnapshotter snapshotter) {
    _viewProcessor = viewProcessor;
    _snapshotter = snapshotter;
  }

  
  @GET
  @Path(PATH_CREATE_SNAPSHOT + "/{viewClientId}" + "/{viewCycleId}")
  public Response createSnapshot(@PathParam("viewClientId") String viewClientIdString, @PathParam("viewCycleId") String viewCycleIdString) {
    UniqueId viewClientId = UniqueId.parse(viewClientIdString);
    UniqueId viewCycleId = UniqueId.parse(viewCycleIdString);
    ViewClient client = _viewProcessor.getViewClient(viewClientId);
    EngineResourceReference<? extends ViewCycle> cycleReference = client.createCycleReference(viewCycleId);
    if (cycleReference == null) {
      throw new IllegalArgumentException("Cycle is not available");
    }
    try {
      return Response.ok(_snapshotter.createSnapshot(client, cycleReference.get())).build();
    } finally {
      cycleReference.release();
    }
  }

  
  @GET
  @Path(PATH_YIELD_CURVE_SPECS + "/{viewClientId}" + "/{viewCycleId}")
  public Response getYieldCurveSpecs(@PathParam("viewClientId") String viewClientIdString, @PathParam("viewCycleId") String viewCycleIdString) {
    UniqueId viewClientId = UniqueId.parse(viewClientIdString);
    UniqueId viewCycleId = UniqueId.parse(viewCycleIdString);
    ViewClient client = _viewProcessor.getViewClient(viewClientId);
    EngineResourceReference<? extends ViewCycle> cycleReference = client.createCycleReference(viewCycleId);
    
    if (cycleReference == null) {
      throw new IllegalArgumentException("Cycle is not available");
    }
    try {
      return Response.ok(_snapshotter.getYieldCurveSpecifications(client, cycleReference.get())).build();
    } finally {
      cycleReference.release();
    }
  }
  
  @GET
  public Response get() {
    return Response.ok("Snapshotter").build();
  }
}
