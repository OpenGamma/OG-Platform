/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import java.net.URI;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
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
import com.opengamma.web.server.push.analytics.ViewportSpecification;

/**
 * REST resource superclass for all analytics grids.
 */
public abstract class AbstractGridResource {

  /** For generating IDs for grids and viewports. */
  protected static final AtomicLong s_nextId = new AtomicLong(0);

  /** The view whose data the grid displays. */
  protected final AnalyticsView _view;

  protected final AnalyticsView.GridType _gridType;

  /**
   * @param gridType The type of grid
   * @param view The view whose data the grid displays
   */
  public AbstractGridResource(AnalyticsView.GridType gridType, AnalyticsView view) {
    ArgumentChecker.notNull(gridType, "gridType");
    ArgumentChecker.notNull(view, "view");
    _gridType = gridType;
    _view = view;
  }

  /**
   * @return The structure of the grid
   */
  @GET
  @Path("grid")
  @Produces(MediaType.APPLICATION_JSON)
  public abstract AnalyticsGridStructure getGridStructure();

  @POST
  @Path("viewports")
  public Response createViewport(@Context UriInfo uriInfo,
                                 @FormParam("rows") List<Integer> rows,
                                 @FormParam("columns") List<Integer> columns) {
    ViewportSpecification viewportSpecification = new ViewportSpecification(rows, columns);
    String viewportId = Long.toString(s_nextId.getAndIncrement());
    URI viewportUri = uriInfo.getAbsolutePathBuilder().path(viewportId).build();
    URI dataUri = uriInfo.getAbsolutePathBuilder().path(viewportId).path(AbstractViewportResource.class, "getData").build();
    String dataId = dataUri.toString();
    createViewport(viewportId, dataId, viewportSpecification);
    return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, viewportUri).build();
  }

  public abstract void createViewport(String viewportId, String dataId, ViewportSpecification viewportSpec);

  @Path("viewports/{viewportId}")
  public abstract AbstractViewportResource getViewport(@PathParam("viewportId") String viewportId);

}
