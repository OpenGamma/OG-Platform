package com.opengamma.web.server.push.rest;

import com.opengamma.web.server.push.Viewport;
import com.opengamma.web.server.push.reports.ReportFactory;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST resource that wraps a {@link Viewport}.
 */
public class ViewportResource {

  private final Viewport _viewport;
  private final ReportFactory _reportGeneratorFactory;

  public ViewportResource(Viewport viewport, ReportFactory reportGeneratorFactory) {
    _viewport = viewport;
    _reportGeneratorFactory = reportGeneratorFactory;
  }

  /**
   * @return JSON containing the structure of the viewport's portfolio and primitiave grids.  This doesn't include
   * the structure of any dependency graph grids
   */
  @Path("grid")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getGridStructure() {
    return new JSONObject(_viewport.getGridStructure()).toString();
  }

  /**
   * @return JSON containing the viewport's latest data
   */
  @Path("data")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getLatestData() {
    return new JSONObject(_viewport.getLatestResults()).toString();
  }

  /**
   * Pauses or unpauses the view.
   * @param run {@code true} to run the view, {@code false} to pause it
   * @return An empty response
   */
  // TODO is a query param the right way to do this? easy to implement but is that adequate justification?
  @POST
  @Path("running")
  public Response setRunning(@QueryParam("run") boolean run) {
    _viewport.setRunning(run);
    return Response.ok().build();
  }

  @Path("report")
  public ViewportReportResource getReport() {
    return new ViewportReportResource(_viewport, _reportGeneratorFactory);
  }
}
