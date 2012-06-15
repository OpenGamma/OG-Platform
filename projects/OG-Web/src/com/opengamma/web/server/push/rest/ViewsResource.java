/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsViewListener;
import com.opengamma.web.server.push.analytics.AnalyticsViewManager;
import com.opengamma.web.server.push.analytics.ViewRequest;

/**
 *
 */
@Path("views")
public class ViewsResource {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewsResource.class);
  private static final AtomicLong s_nextViewId = new AtomicLong(0);

  private final AnalyticsViewManager _viewManager;

  public ViewsResource(AnalyticsViewManager viewManager) {
    ArgumentChecker.notNull(viewManager, "viewManager");
    _viewManager = viewManager;
  }

  @Consumes(MediaType.APPLICATION_JSON)
  @POST
  public Response createView(@Context UriInfo uriInfo, ViewRequest viewRequest) {
    String viewId = Long.toString(s_nextViewId.getAndIncrement());
    URI portfolioGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path(ViewResource.class, "getPortfolioGrid")
        .path(AbstractGridResource.class, "getGridStructure").build();
    URI primitivesGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path(ViewResource.class, "getPrimitivesGrid")
        .path(AbstractGridResource.class, "getGridStructure").build();
    AnalyticsViewListener listener = null; // TODO this needs to connect to the async web stuff
    // TODO this is very obviously wrong - where can I get the user?
    UserPrincipal user = UserPrincipal.getTestUser();
    _viewManager.createView(viewRequest, user, listener, viewId, portfolioGridUri.toString(), primitivesGridUri.toString());
    URI uri = uriInfo.getAbsolutePathBuilder().path(viewId).build();
    return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, uri).build();
  }

  @Path("{viewId}")
  public ViewResource getView(@PathParam("viewId") String viewId) {
    return new ViewResource(_viewManager.getView(viewId), _viewManager);
  }
}
