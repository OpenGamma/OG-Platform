/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsViewManager;
import com.opengamma.web.server.push.analytics.ViewRequest;

/**
 *
 */
@Path("views")
public class ViewsResource {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewsResource.class);

  private final AnalyticsViewManager _viewManager;

  public ViewsResource(AnalyticsViewManager viewManager) {
    ArgumentChecker.notNull(viewManager, "viewManager");
    _viewManager = viewManager;
  }

  @Consumes(MediaType.APPLICATION_JSON)
  @POST
  public Response createView(@Context UriInfo uriInfo, ViewRequest viewRequest) {
    String viewId = _viewManager.createView(viewRequest);
    URI uri = uriInfo.getAbsolutePathBuilder().path(viewId).build();
    return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, uri).build();
  }

  @Path("{viewId}")
  public ViewResource getView(@PathParam("viewId") String viewId) {
    return new ViewResource(_viewManager.getView(viewId), _viewManager);
  }
}
