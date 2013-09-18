/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.GridStructure;
import com.opengamma.web.analytics.ViewportDefinition;
import com.opengamma.web.analytics.ViewportResults;

/**
 * REST resource for a viewport on a dependency graph grid. The viewport represents the visible part of the grid.
 * @deprecated in favour of {@link WebUiResource}
 */
@Deprecated
public class DependencyGraphViewportResource extends AbstractViewportResource {

  private final int _graphId;

  public DependencyGraphViewportResource(AnalyticsView.GridType gridType, AnalyticsView view, int graphId, int viewportId) {
    super(gridType, view, viewportId);
    _graphId = graphId;
  }

  @Override
  public void update(ViewportDefinition viewportDefinition) {
    getView().updateViewport(getGridType(), _graphId, getViewportId(), viewportDefinition);
  }

  @Override
  public void delete() {
    getView().deleteViewport(getGridType(), _graphId, getViewportId());
  }

  @Override
  public ViewportResults getData() {
    return getView().getData(getGridType(), _graphId, getViewportId());
  }

  public GridStructure getGridStructure() {
    GridStructure gs = getView().getGridStructure(getGridType(), _graphId, getViewportId());
    return gs;
  }
}
