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
import javax.ws.rs.Path;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.GridCell;
import com.opengamma.web.analytics.GridStructure;
import com.opengamma.web.analytics.ViewportDefinition;
import com.opengamma.web.analytics.ViewportResults;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * REST resource superclass for grid viewports. A viewport represents the part of the grid that is visible.
 * @deprecated in favour of {@link WebUiResource}
 */
@Deprecated
public abstract class AbstractViewportResource {

  private final AnalyticsView.GridType _gridType;
  private final AnalyticsView _view;
  private final int _viewportId;

  /**
   * Creates an instance.
   * 
   * @param gridType  the type of data in the grid (portfolio or primitives), not null
   * @param view  the view that supplies data to the grid, not null
   * @param viewportId  the ID of this viewport, not null
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
   * 
   * @param version  the version
   * @param rows Indices of rows in the viewport, can be empty if {@code cells} is non-empty
   * @param columns Indices of columns in the viewport, can be empty if {@code cells} is non-empty
   * @param cells Cells in the viewport, can be empty if {@code rows} and {@code columns} are non-empty
   * @param format Specifies the way the data should be formatted
   * @param enableLogging  whether to enable logging
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
   * 
   * @param viewportDefinition  the new viewport definition
   */
  public abstract void update(ViewportDefinition viewportDefinition);

  /**
   * Deletes the viewport
   */
  @DELETE
  public abstract void delete();

  /**
   * Gets the data to display in the viewport.
   * 
   * @return the data to display in the viewport
   */
  @GET
  public abstract ViewportResults getData();

  @GET
  @Path("structure")
  public abstract GridStructure getGridStructure();

    /**
     * Gets the grid type.
     *
     * @return the grid type, not null
     */
  protected AnalyticsView.GridType getGridType() {
    return _gridType;
  }

  /**
   * Gets the view.
   * 
   * @return the view, not null
   */
  protected AnalyticsView getView() {
    return _view;
  }

  /**
   * Gets the viewport ID.
   * 
   * @return the viewport ID, not null
   */
  protected int getViewportId() {
    return _viewportId;
  }

}
