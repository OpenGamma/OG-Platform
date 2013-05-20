/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.AnalyticsViewManager;

/**
 *
 */
@Path("views/{viewId}")
public class ViewResource {

  private final AnalyticsView _view;
  private final AnalyticsViewManager _viewManager;
  private final String _viewId;
  private final ViewClient _viewClient;

  public ViewResource(ViewClient viewClient, AnalyticsView view, AnalyticsViewManager viewManager, String viewId) {
    ArgumentChecker.notNull(viewManager, "viewManager");
    ArgumentChecker.notNull(view, "view");
    ArgumentChecker.notNull(viewId, "viewId");
    ArgumentChecker.notNull(viewClient, "viewClient");
    _viewManager = viewManager;
    _view = view;
    _viewId = viewId;
    _viewClient = viewClient;
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

  @PUT
  @Path("pause")
  public Response pauseView() {
    _viewClient.pause();
    return Response.ok().build();
  }
  
  @PUT
  @Path("resume")
  public Response resumeView() {
    _viewClient.resume();
    return Response.ok().build();
  }
    
}
