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
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.AnalyticsViewManager;
import com.opengamma.web.analytics.ErrorInfo;

/**
 * @deprecated in favour of {@link WebUiResource}
 */
@Path("views/{viewId}")
@Deprecated
public class ViewResource {
  
  private static final Logger s_logger = LoggerFactory.getLogger(ViewResource.class);

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
    return new MainGridResource(AnalyticsView.GridType.PORTFOLIO, _view);
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
  @Path("pauseOrResume")
  public Response pauseOrResumeView(@FormParam("state") String state) {
   
    state = StringUtils.stripToNull(state);
    Response response = Response.status(Status.BAD_REQUEST).build();
    if (state != null) {
      ViewClientState currentState = _viewClient.getState();
      state = state.toUpperCase();
      switch (state) {
        case "PAUSE":
        case "P":
          if (currentState != ViewClientState.TERMINATED) {
            _viewClient.pause();
            response = Response.ok().build();
          }
          break;
        case "RESUME":
        case "R":
          if (currentState != ViewClientState.TERMINATED) {
            _viewClient.resume();
            response = Response.ok().build();
          }
          break;
        default:
          s_logger.warn("client {} requesting for invalid view client state change to {}", _viewId, state);
          response = Response.status(Status.BAD_REQUEST).build();
          break;
      }
    }
    return response;
  }

  @Path("errors")
  @GET
  public List<ErrorInfo> getErrors() {
    return _view.getErrors();
  }

  @Path("errors/{errorId}")
  @DELETE
  public void deleteError(@PathParam("errorId") long errorId) {
    _view.deleteError(errorId);
  }
}
