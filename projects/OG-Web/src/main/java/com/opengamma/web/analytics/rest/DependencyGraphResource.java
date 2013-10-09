/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import javax.ws.rs.DELETE;

import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.GridStructure;
import com.opengamma.web.analytics.ViewportDefinition;

/**
 * REST resource for a grid displaying the dependency graph showing the calculation steps for a cell's value.
 * @deprecated in favour of {@link WebUiResource}
 */
@Deprecated
public class DependencyGraphResource extends AbstractGridResource {

  private final int _graphId;

  /**
   * @param gridType The type of the main grid associated with this dependency graph
   * @param view The view whose data the grid displays.
   * @param graphId The ID of the dependency graph
   */
  public DependencyGraphResource(AnalyticsView.GridType gridType, AnalyticsView view, int graphId) {
    super(gridType, view);
    _graphId = graphId;
  }

  /**
   * @return The initial row and column structure of the grid
   * subsequent requests will need to be made to the viewport
   */
  public GridStructure getInitialGridStructure() {
    return getView().getInitialGridStructure(getGridType(), _graphId);
  }

  public GridStructure getGridStructure(int viewportId) {
    return getViewport(viewportId).getGridStructure();
  }

  @Override
  /* package */ void createViewport(int requestId, int viewportId, String callbackId, String structureCallbackId, ViewportDefinition viewportDefinition) {
    getView().createViewport(requestId, getGridType(), _graphId, viewportId, callbackId, structureCallbackId, viewportDefinition);
  }

  @Override
  public AbstractViewportResource getViewport(int viewportId) {
    return new DependencyGraphViewportResource(getGridType(), getView(), _graphId, viewportId);
  }

  /**
   * Closes the grid.
   */
  @DELETE
  public void close() {
    getView().closeDependencyGraph(getGridType(), _graphId);
  }
}
