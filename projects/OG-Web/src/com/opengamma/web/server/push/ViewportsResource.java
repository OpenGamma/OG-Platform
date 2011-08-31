/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO catch RestRuntimeException and set the status on the response?
 * TODO would that be best done with a servlet filter? surely there's a standard way to do that?
 */
@Path("viewports")
public class ViewportsResource {

  // TODO better way of generating viewport IDs
  private final AtomicLong _nextId = new AtomicLong();
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
  @Produces(MediaType.APPLICATION_JSON) // TODO JSON? {viewportRestUrl: <url>}
  // TODO should clientId be a query param or part of the viewport def?
  public String createViewport(ViewportDefinition viewportDefinition,
                               @QueryParam("clientId") String clientId, // TODO should this be optional? is it? viewports without updates?
                               @Context HttpServletRequest request) {
    String userId = request.getRemoteUser();
    String viewportId = generateViewportId();
    String viewportUrl = generateViewportUrl(viewportId, request);
    String gridUrl = generateGridUrl(viewportId, viewportUrl);
    String dataUrl = generateDataUrl(viewportId, viewportUrl);
    _restUpdateManager.createViewport(userId, clientId, viewportDefinition, viewportId, dataUrl, gridUrl);
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("viewportUrl", viewportUrl);
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Unexpected exception creating JSON", e);
    }
    return jsonObject.toString();
  }

  @Path("{viewportId}")
  public ViewportResource findViewport(@QueryParam("clientId") String clientId, // TODO should this be optional? how?
                                       @PathParam("viewportId") String viewportId,
                                       @Context HttpServletRequest request) {
    String userId = request.getRemoteUser();
    Viewport viewport = _restUpdateManager.getViewport(userId, clientId, viewportId);
    if (viewport != null) {
      return new ViewportResource(viewport);
    } else {
      throw new DataNotFoundException("Unable to find viewport, userId: " + userId + ", clientId: " + clientId +
                                          ", viewportId: " + viewportId);
    }
  }

  private String generateViewportId() {
    return Long.toString(_nextId.getAndIncrement());
  }

  private String generateViewportUrl(String viewportId, HttpServletRequest request) {
    return UriBuilder.fromUri(request.getRequestURI())
        .path(ViewportsResource.class, "findViewport")
        .build(viewportId).toString();
  }

  private String generateDataUrl(String viewportId, String viewportUrl) {
    return UriBuilder.fromUri(viewportUrl)
        .path(ViewportResource.class, "getLatestData")
        .build(viewportId).toString();
  }

  private String generateGridUrl(String viewportId, String viewportUrl) {
    return UriBuilder.fromUri(viewportUrl)
        .path(ViewportResource.class, "getGridStructure")
        .build(viewportId).toString();
  }
}
