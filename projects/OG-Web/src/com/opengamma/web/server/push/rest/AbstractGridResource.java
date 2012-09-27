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

import com.opengamma.DataNotFoundException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.GridCell;
import com.opengamma.web.server.push.analytics.GridStructure;
import com.opengamma.web.server.push.analytics.ViewportDefinition;

/**
 * REST resource superclass for all analytics grids.
 */
public abstract class AbstractGridResource {

  /** For generating IDs for grids and viewports. */
  protected static final AtomicInteger s_nextId = new AtomicInteger(0);

  /** The view whose data the grid displays. */
  protected final AnalyticsView _view;

  /** The type of data displayed in the grid (portfolio or primitives). */
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
   * @return The row and column structure of the grid
   */
  @GET
  public abstract GridStructure getGridStructure();

  /**
   * Creates a new viewport which represents a part of a grid that the user is viewing.
   * @param uriInfo Details of the request URI
   * @param rows Indices of rows in the viewport, can be empty if {@code cells} is non-empty
   * @param columns Indices of columns in the viewport, can be empty if {@code cells} is non-empty
   * @param cells Cells in the viewport, can be empty if {@code rows} and {@code columns} are non-empty
   * @param expanded Whether the full data should be returned (e.g. for display in a popup window) or if it should
   * be formatted to fit in a single grid cell
   * @return A response with the viewport's URL in the {@code Location} header
   */
  @POST
  @Path("viewports")
  public Response createViewport(@Context UriInfo uriInfo,
                                 @FormParam("rows") List<Integer> rows,
                                 @FormParam("columns") List<Integer> columns,
                                 @FormParam("cells") List<GridCell> cells,
                                 @FormParam("expanded") boolean expanded) {
    ViewportDefinition viewportDefinition = ViewportDefinition.create(rows, columns, cells, expanded);
    int viewportId = s_nextId.getAndIncrement();
    String viewportIdStr = Integer.toString(viewportId);
    URI viewportUri = uriInfo.getAbsolutePathBuilder().path(viewportIdStr).build();
    String callbackId = viewportUri.getPath();
    long version = createViewport(viewportId, callbackId, viewportDefinition);
    ViewportVersion viewportVersion = new ViewportVersion(version);
    return Response.status(Response.Status.CREATED).entity(viewportVersion).header(HttpHeaders.LOCATION,
                                                                                   viewportUri).build();
  }

  /**
   * Creates a viewport corresponding to a visible area of the grid.
   * @param viewportId Unique ID for the viewport
   * @param callbackId ID passed to listeners when the viewport data changes
   * @param viewportDefinition Definition of the viewport
   * @return Viewport version number, allows clients to ensure the data they receive for a viewport corresponds to
   * its current state
   */
  /* package */ abstract long createViewport(int viewportId, String callbackId, ViewportDefinition viewportDefinition);

  /**
   * Returns a resource for a viewport. If the ID is unknown a resource will be returned but a
   * {@link DataNotFoundException} will be thrown when it is used.
   * @param viewportId The viewport ID
   * @return A resource for the viewport with the given ID, not null
   */
  @Path("viewports/{viewportId}")
  public abstract AbstractViewportResource getViewport(@PathParam("viewportId") int viewportId);
}
