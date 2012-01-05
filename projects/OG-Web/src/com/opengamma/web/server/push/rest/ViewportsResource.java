/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.web.server.push.ConnectionManager;
import com.opengamma.web.server.push.Viewport;
import com.opengamma.web.server.push.ViewportDefinition;
import com.opengamma.web.server.push.reports.ReportFactory;
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
 * REST resource for creating and retrieving {@link Viewport} instances.
 * TODO catch RestRuntimeException and set the status on the response?
 * TODO would that be best done with a servlet filter? surely there's a standard way to do that?
 */
@Path("viewports")
public class ViewportsResource {

  // TODO better way of generating viewport IDs
  /** The next viewport ID.  This is 1-based to make sure the viewport and client IDs are different (for debugging) */
  private final AtomicLong _nextId = new AtomicLong(1);

  /** For creating and looking up {@link Viewport}s */
  private final ConnectionManager _restUpdateManager;

  /** For passing to {@link ViewportResource} instances */
  private final ReportFactory _reportGeneratorFactory;

  public ViewportsResource(ConnectionManager restUpdateManager, ReportFactory reportGeneratorFactory) {
    _restUpdateManager = restUpdateManager;
    _reportGeneratorFactory = reportGeneratorFactory;
  }

  /**
   * Creates a new viewport and sets up a subscription so the client will be notified when the viewport data or
   * structure changes.
   * @param viewportDefinition Details of the new viewport
   * @param clientId ID of the client connection
   * @param request HTTP request, used to get the ID of the logged-in user
   * @return URI of the new viewport
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String createViewport(ViewportDefinition viewportDefinition,
                               @QueryParam("clientId") String clientId, // TODO should this be optional? that would be consistent with the other REST methods
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

  /**
   * Returns an existing viewport.
   * @param clientId ID of the client connection that owns the viewport.
   * @param viewportId ID of the viewport.
   * @param request HTTP request, used to get the ID of the logged-in user
   * @return A resource wrapping the viewport
   * @throws DataNotFoundException If there is no viewport with the specified ID or it is not owned by the
   * specified client and user
   */
  @Path("{viewportId}")
  public ViewportResource findViewport(@QueryParam("clientId") String clientId, // TODO should this be optional? how? different method?
                                       @PathParam("viewportId") String viewportId,
                                       @Context HttpServletRequest request) {
    String userId = request.getRemoteUser();
    Viewport viewport = _restUpdateManager.getViewport(userId, clientId, viewportId);
    if (viewport != null) {
      return new ViewportResource(viewport, _reportGeneratorFactory);
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
