/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.GridCell;
import com.opengamma.web.server.push.analytics.ViewportDefinition;
import com.opengamma.web.server.push.analytics.ViewportResults;

/**
 * REST resource superclass for grid viewports. A viewport represents the part of the grid that is visible.
 */
public abstract class AbstractViewportResource {

  protected final AnalyticsView.GridType _gridType;
  protected final AnalyticsView _view;
  protected final int _viewportId;

  public AbstractViewportResource(AnalyticsView.GridType gridType, AnalyticsView view, int viewportId) {
    ArgumentChecker.notNull(gridType, "gridType");
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(viewportId, "viewportId");
    _gridType = gridType;
    _view = view;
    _viewportId = viewportId;
  }

  @PUT
  public ViewportVersion update(@FormParam("rows") List<Integer> rows,
                                @FormParam("columns") List<Integer> columns,
                                @FormParam("cells") List<GridCell> cells,
                                @FormParam("expanded") boolean expanded) {
    long version = update(ViewportDefinition.create(rows, columns, cells, expanded));
    return new ViewportVersion(version);
  }

  /**
   * Updates the viewport, e.g. in response to the user scrolling the grid and changing the visible area.
   * @param viewportDefinition The new viewport definition
   * @return The version ID of the updated viewport, allows the client to ensure that the data they receive for the
   * viewport was created for the correct version of the viewport
   */
  public abstract long update(ViewportDefinition viewportDefinition);

  /**
   * Deletes the viewport
   */
  @DELETE
  public abstract void delete();

  /**
   * @return The data to display in the viewport
   */
  @GET
  public abstract ViewportResults getData();
}
