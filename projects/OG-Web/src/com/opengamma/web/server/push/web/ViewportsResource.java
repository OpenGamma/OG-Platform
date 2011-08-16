/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.subscription.SubscriptionManager;
import com.opengamma.web.server.push.subscription.ViewportSubscriptionRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 */
@Path("viewport")
public class ViewportsResource {

  private final SubscriptionManager _subscriptionManager;

  public ViewportsResource(SubscriptionManager subscriptionManager) {
    _subscriptionManager = subscriptionManager;
  }

  /**
   * @param request Details of the viewport
   * @return URI of the new viewport
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN)
  String createViewport(/*TODO this arg is wrong (at least the name is wrong)*/ViewportSubscriptionRequest request) {
    //_subscriptionManager.
    throw new UnsupportedOperationException("TODO");
  }

  @GET
  @Path("{clientId}/{viewportId}")
  String getLatestViewportData(@PathParam("clientId") String clientId, @PathParam("viewportId") UniqueId viewportId) {
    throw new UnsupportedOperationException("TODO");
  }
}
