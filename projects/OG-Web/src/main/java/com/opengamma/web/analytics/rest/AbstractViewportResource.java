/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.GridCell;
import com.opengamma.web.analytics.ViewportDefinition;
import com.opengamma.web.analytics.ViewportResults;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * REST resource superclass for grid viewports. A viewport represents the part of the grid that is visible.
 */
public abstract class AbstractViewportResource {

  protected final AnalyticsView.GridType _gridType;
  protected final AnalyticsView _view;
  protected final int _viewportId;

  /**
   * @param gridType The type of data in the grid (portfolio or primitives)
   * @param view The view that supplies data to the grid
   * @param viewportId This viewport's ID
   */
  public AbstractViewportResource(AnalyticsView.GridType gridType, AnalyticsView view, int viewportId) {
    ArgumentChecker.notNull(gridType, "gridType");
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(viewportId, "viewportId");
    _gridType = gridType;
    _view = view;
    _viewportId = viewportId;
  }

  /**
   * Updates the viewport, e.g. in response to the user scrolling the grid and changing the visible area.
   * @param rows Indices of rows in the viewport, can be empty if {@code cells} is non-empty
   * @param columns Indices of columns in the viewport, can be empty if {@code cells} is non-empty
   * @param cells Cells in the viewport, can be empty if {@code rows} and {@code columns} are non-empty
   * @param format Specifies the way the data should be formatted
   * @return Viewport version number, allows clients to ensure the data they receive for a viewport corresponds to
   * its current state
   */
  @PUT
  public void update(@FormParam("version") int version,
                     @FormParam("rows") List<Integer> rows,
                     @FormParam("columns") List<Integer> columns,
                     @FormParam("cells") List<GridCell> cells,
                     @FormParam("format") TypeFormatter.Format format,
                     @FormParam("enableLogging") Boolean enableLogging) {
    update(ViewportDefinition.create(version, rows, columns, cells, format, enableLogging));
  }

  /**
   * Updates the viewport, e.g. in response to the user scrolling the grid and changing the visible area.
   * @param viewportDefinition The new viewport definition
   * @return The version ID of the updated viewport, allows the client to ensure that the data they receive for the
   * viewport was created for the correct version of the viewport
   */
  public abstract void update(ViewportDefinition viewportDefinition);

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
