/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.web.server.push.subscription.RestUpdateManager;
import com.opengamma.web.server.push.subscription.Viewport;
import com.opengamma.web.server.push.subscription.ViewportDefinition;
import com.sun.jersey.api.core.HttpContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 *
 */
@Path("viewports")
public class ViewportsResource {

  private final RestUpdateManager _restUpdateManager;

  public ViewportsResource(RestUpdateManager restUpdateManager) {
    _restUpdateManager = restUpdateManager;
  }

  /**
   * @param request Details of the viewport
   * @return URI of the new viewport
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN) // TODO JSON? {viewportRestUrl: <url>}
  public String createViewport(ViewportDefinition viewportDefinition,
                               @QueryParam("clientId") String clientId,
                               @Context HttpServletRequest request) {
    String userId = request.getRemoteUser();
    // TODO should this be JSON?
    String viewportUrl = generateViewportUrl();
    _restUpdateManager.createViewport(userId, clientId, viewportDefinition, viewportUrl);
    return viewportUrl;
  }

  @Path("{viewportId}")
  public ViewportResource findViewport(@QueryParam("clientId") String clientId,
                                       @Context HttpContext httpContext,
                                       @Context HttpServletRequest request) {
    String viewportUrl = httpContext.getUriInfo().getPath(); // TODO is this right?
    String userId = request.getRemoteUser();
    Viewport viewport = _restUpdateManager.getViewport(userId, clientId, viewportUrl);
    return new ViewportResource(viewport);
  }

  // TODO there are JAX-RS helpers for this. but does it need to be a full URL?
  private String generateViewportUrl() {
    throw new UnsupportedOperationException();
  }
}
