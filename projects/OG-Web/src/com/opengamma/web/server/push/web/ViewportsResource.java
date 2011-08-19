/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.web.server.push.subscription.RestUpdateManager;
import com.opengamma.web.server.push.subscription.Viewport;
import com.opengamma.web.server.push.subscription.ViewportDefinition;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
  // TODO need a MessageBodyReader that produces ViewportDefinitions
  String createViewport(ViewportDefinition viewportDefinition, @QueryParam("clientId") String clientId) {
    String userId = null;
    // TODO should this be JSON?
    /* TODO this is awkward
    the connection needs the viewport URL to publish in the events
    but the URL needs to be created here using the viewportId (currently) generated in the update manager
    clearly the viewport ID needs to be generated here and the URL passed in
    */
    String viewportId = generateViewportId();
    String viewportUrl = null; // TODO there are JAX-RS helpers for this
    return _restUpdateManager.createViewport(userId, clientId, viewportDefinition, viewportUrl);
  }

  @Path("{viewportId}")
  ViewportResource findViewport(@PathParam("viewportId") String viewportId, @QueryParam("clientId") String clientId) {
    String userId = ;
    Viewport viewport = _restUpdateManager.getViewport(userId, clientId, viewportId);
    return new ViewportResource(viewport);
  }

  private String generateViewportId() {

  }
}
