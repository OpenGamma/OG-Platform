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
 * REST resource for a viewport on one of the main grids displaying analytics data. The viewport represents the
 * visible part of the grid.
 * @deprecated in favour of {@link WebUiResource}
 */
@Deprecated
public class MainGridViewportResource extends AbstractViewportResource {

  public MainGridViewportResource(AnalyticsView.GridType gridType, AnalyticsView view, int viewportId) {
    super(gridType, view, viewportId);
  }

  @Override
  public void update(ViewportDefinition viewportSpec) {
    getView().updateViewport(getGridType(), getViewportId(), viewportSpec);
  }

  @Override
  public void delete() {
    getView().deleteViewport(getGridType(), getViewportId());
  }

  @Override
  public ViewportResults getData() {
    return getView().getData(getGridType(), getViewportId());
  }

  public GridStructure getGridStructure() {
    return getView().getGridStructure(getGridType(), getViewportId());
  }
}
