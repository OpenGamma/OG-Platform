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
import com.opengamma.web.server.push.analytics.ViewportResults;
import com.opengamma.web.server.push.analytics.ViewportSpecification;

/**
 * REST resource superclass
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
                                @FormParam("expanded") boolean expanded) {
    long version = update(new ViewportSpecification(rows, columns, expanded));
    return new ViewportVersion(version);
  }

  public abstract long update(ViewportSpecification viewportSpecification);

  @DELETE
  public abstract void delete();

  @GET
  public abstract ViewportResults getData();
}
