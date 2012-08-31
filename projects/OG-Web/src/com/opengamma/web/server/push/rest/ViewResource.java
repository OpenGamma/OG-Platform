/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.AnalyticsViewManager;

/**
 *
 */
public class ViewResource {

  private final AnalyticsView _view;
  private final AnalyticsViewManager _viewManager;
  private final String _viewId;

  public ViewResource(AnalyticsView view, AnalyticsViewManager viewManager, String viewId) {
    ArgumentChecker.notNull(viewManager, "viewManager");
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(viewId, "viewId");
    _viewManager = viewManager;
    _view = view;
    _viewId = viewId;
  }

  @Path("portfolio")
  public MainGridResource getPortfolioGrid() {
    return new MainGridResource(AnalyticsView.GridType.PORTFORLIO, _view);
  }

  @Path("primitives")
  public MainGridResource getPrimitivesGrid() {
    return new MainGridResource(AnalyticsView.GridType.PRIMITIVES, _view);
  }

  @DELETE
  public void deleteView() {
    _viewManager.deleteView(_viewId);
  }

  @POST
  public void pauseUnpauseView(boolean paused) {
    // TODO implement
  }
}
