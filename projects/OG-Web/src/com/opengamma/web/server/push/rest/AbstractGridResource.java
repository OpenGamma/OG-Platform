/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.http.HttpHeaders;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.GridStructure;
import com.opengamma.web.server.push.analytics.ViewportSpecification;

/**
 * REST resource superclass for all analytics grids.
 */
public abstract class AbstractGridResource {

  /** For generating IDs for grids and viewports. */
  protected static final AtomicInteger s_nextId = new AtomicInteger(0);

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
  @Path("/")
  public abstract GridStructure getGridStructure();

  @POST
  @Path("viewports")
  public Response createViewport(@Context UriInfo uriInfo,
                                 @FormParam("rows") List<Integer> rows,
                                 @FormParam("columns") List<Integer> columns,
                                 @FormParam("expanded") boolean expanded) {
    ViewportSpecification viewportSpecification = new ViewportSpecification(rows, columns, expanded);
    int viewportId = s_nextId.getAndIncrement();
    String viewportIdStr = Integer.toString(viewportId);
    URI viewportUri = uriInfo.getAbsolutePathBuilder().path(viewportIdStr).build();
    URI dataUri = uriInfo.getAbsolutePathBuilder().path(viewportIdStr).path(AbstractViewportResource.class, "getData").build();
    String dataId = dataUri.getPath();
    long version = createViewport(viewportId, dataId, viewportSpecification);
    ViewportVersion viewportVersion = new ViewportVersion(version);
    return Response.status(Response.Status.CREATED).entity(viewportVersion).header(HttpHeaders.LOCATION, viewportUri).build();
  }

  public abstract long createViewport(int viewportId, String dataId, ViewportSpecification viewportSpec);

  @Path("viewports/{viewportId}")
  public abstract AbstractViewportResource getViewport(@PathParam("viewportId") int viewportId);

}
