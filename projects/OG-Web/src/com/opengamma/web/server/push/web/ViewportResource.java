package com.opengamma.web.server.push.web;

import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.push.subscription.Viewport;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 *
 */
//@Path("viewports/{viewportId}") // TODO is this necessary on a sub-resource?
public class ViewportResource {

  private final Viewport _viewport;

  public ViewportResource(Viewport viewport) {
    _viewport = viewport;
  }

  // TODO may not be necessary, just have a field isRunning in the data
  /*@Path("status")
  @GET
  public Response getStatus() {
    _viewport.getStatus();
  }*/

  @Path("gridStructure")
  @GET
  public Response getGridStructure() {
    _viewport.getGridStructure();
    return null; // TODO
  }

  @Path("data")
  @GET
  public Response getLatestData() {
    _viewport.getLatestData();
    return null; // TODO
  }

  // TODO is a query param the right way to do this? easy to implement but is that adequate justification?
  @POST
  @Path("running")
  public Response setRunning(@QueryParam("run") boolean run) {
    _viewport.setRunning(run);
    return null; // TODO
  }

  // TODO is a query param the right way to do this? easy to implement but is that adequate justification?
  @POST
  @Path("mode")
  public Response setMode(@QueryParam("mode") ConversionMode mode) {
    _viewport.setConversionMode(mode);
    return null; // TODO
  }
}
