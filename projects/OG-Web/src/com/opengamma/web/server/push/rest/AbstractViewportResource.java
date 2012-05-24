/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsResults;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.ViewportRequest;

/**
 * REST resource superclass
 */
public abstract class AbstractViewportResource {

  protected final AnalyticsView.GridType _gridType;
  protected final AnalyticsView _view;
  protected final String _viewportId;

  public AbstractViewportResource(AnalyticsView.GridType gridType, AnalyticsView view, String viewportId) {
    ArgumentChecker.notNull(gridType, "gridType");
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(viewportId, "viewportId");
    _gridType = gridType;
    _view = view;
    _viewportId = viewportId;
  }

  @POST
  public abstract void update(ViewportRequest viewportRequest);

  @DELETE
  public abstract void delete();

  @GET
  @Path("data")
  public abstract AnalyticsResults getData();
}
