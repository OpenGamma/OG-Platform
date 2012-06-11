/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsGridStructure;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.ViewportRequest;

/**
 * REST resource superclass for all analytics grids.
 */
public abstract class AbstractGridResource {

  /** The view whose data the grid displays. */
  protected final AnalyticsView _view;

  protected final AnalyticsView.GridType _gridType;

  /**
   * @param gridType The type of grid
   * @param view The view whose data the grid displays.
   */
  public AbstractGridResource(AnalyticsView.GridType gridType, AnalyticsView view) {
    ArgumentChecker.notNull(gridType, "gridType");
    ArgumentChecker.notNull(view, "view");
    _gridType = gridType;
    _view = view;
  }

  /**
   * @return
   */
  @GET
  @Path("grid")
  @Produces(MediaType.APPLICATION_JSON)
  public abstract AnalyticsGridStructure getGridStructure();

  @POST
  @Path("viewports")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createViewport(@Context UriInfo uriInfo, ViewportRequest viewportRequest) {
    String viewportId = createViewport(viewportRequest);
    return createdResponse(uriInfo, viewportId);
  }

  public abstract String createViewport(ViewportRequest viewportRequest);

  @Path("viewports/{viewportId}")
  public abstract AbstractViewportResource getViewport(@PathParam("viewportId") String viewportId);

  /* package */ static Response createdResponse(UriInfo uriInfo, String createdId) {
    URI uri = uriInfo.getAbsolutePathBuilder().path(createdId).build();
    return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, uri).build();
  }
}
