/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.DataNotFoundException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.GridCell;
import com.opengamma.web.analytics.GridStructure;
import com.opengamma.web.analytics.ViewportDefinition;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * REST resource superclass for all analytics grids.
 * @deprecated in favour of {@link WebUiResource}
 */
@Deprecated
public abstract class AbstractGridResource {

  /**
   * For generating IDs for grids and viewports.
   */
  static final AtomicInteger s_nextId = new AtomicInteger(0);
  /**
   * The view whose data the grid displays.
   */
  private final AnalyticsView _view;
  /**
   * The type of data displayed in the grid (portfolio or primitives).
   */
  private final AnalyticsView.GridType _gridType;

  /**
   * @param gridType  the type of grid, not null
   * @param view  the view whose data the grid displays, not null
   */
  public AbstractGridResource(AnalyticsView.GridType gridType, AnalyticsView view) {
    ArgumentChecker.notNull(gridType, "gridType");
    ArgumentChecker.notNull(view, "view");
    _gridType = gridType;
    _view = view;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the view.
   * 
   * @return the view, not null
   */
  protected AnalyticsView getView() {
    return _view;
  }

  /**
   * Gets the grid type.
   * 
   * @return the grid type, not null
   */
  protected AnalyticsView.GridType getGridType() {
    return _gridType;
  }

  /**
   * @return The initial row and column structure of the grid
   * subsequent requests will need to be made to the viewport
   */
  @GET
  public abstract GridStructure getInitialGridStructure();

  /**
   * Creates a new viewport which represents a part of a grid that the user is viewing.
   * 
   * @param requestId  the request ID
   * @param version  the version
   * @param uriInfo  the details of the request URI
   * @param rows  the indices of rows in the viewport, can be empty if {@code cells} is non-empty
   * @param columns  the indices of columns in the viewport, can be empty if {@code cells} is non-empty
   * @param cells  the cells in the viewport, can be empty if {@code rows} and {@code columns} are non-empty
   * @param format  the way the data should be formatted
   * @param enableLogging  whether to enable logging
   * @return a response with the viewport's URL in the {@code Location} header
   */
  @POST
  @Path("viewports")
  // TODO need requestId for initial callback
  public Response createViewport(@Context UriInfo uriInfo,
      @FormParam("requestId") int requestId,
      @FormParam("version") int version,
      @FormParam("rows") List<Integer> rows,
      @FormParam("columns") List<Integer> columns,
      @FormParam("cells") List<GridCell> cells,
      @FormParam("format") TypeFormatter.Format format,
      @FormParam("enableLogging") Boolean enableLogging) {
    ViewportDefinition viewportDefinition = ViewportDefinition.create(version, rows, columns, cells, format, enableLogging);
    int viewportId = s_nextId.getAndIncrement();
    String viewportIdStr = Integer.toString(viewportId);
    UriBuilder viewportUriBuilder = uriInfo.getAbsolutePathBuilder().path(viewportIdStr);
    String callbackId = viewportUriBuilder.build().getPath();
    String structureCallbackId = viewportUriBuilder.path(AbstractViewportResource.class, "getGridStructure").build().getPath();
    createViewport(requestId, viewportId, callbackId, structureCallbackId, viewportDefinition);
    return Response.status(Response.Status.CREATED).build();
  }

  /**
   * Creates a viewport corresponding to a visible area of the grid.
   * @param viewportId Unique ID for the viewport
   * @param callbackId ID passed to listeners when the viewport data changes
   * @param structureCallbackId ID passed to listeners when the viewport structure changes
   * @param viewportDefinition Definition of the viewport
   * @return Viewport version number, allows clients to ensure the data they receive for a viewport corresponds to
   * its current state
   */
  /* package */abstract void createViewport(int requestId, int viewportId, String callbackId, String structureCallbackId, ViewportDefinition viewportDefinition);

  /**
   * Returns a resource for a viewport. If the ID is unknown a resource will be returned but a
   * {@link DataNotFoundException} will be thrown when it is used.
   * @param viewportId The viewport ID
   * @return A resource for the viewport with the given ID, not null
   */
  @Path("viewports/{viewportId}")
  public abstract AbstractViewportResource getViewport(@PathParam("viewportId") int viewportId);

}
