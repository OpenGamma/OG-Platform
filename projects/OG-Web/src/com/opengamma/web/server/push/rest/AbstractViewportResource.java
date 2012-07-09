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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.ViewportResults;
import com.opengamma.web.server.push.analytics.ViewportSpecification;

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
  public void update(@FormParam("rows") List<Integer> rows,
                     @FormParam("columns") List<Integer> columns,
                     @FormParam("expanded") boolean expanded) {
    update(new ViewportSpecification(rows, columns, expanded));
  }

  public abstract void update(ViewportSpecification viewportSpecification);

  @DELETE
  public abstract void delete();

  @GET
  @Path("data")
  @Produces(MediaType.APPLICATION_JSON)
  public abstract ViewportResults getData();
}
