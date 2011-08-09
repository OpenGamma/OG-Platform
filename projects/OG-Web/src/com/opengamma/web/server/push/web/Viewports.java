/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.subscription.SubscriptionManager;
import com.opengamma.web.server.push.subscription.SubscriptionRequest;

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
@Path("viewports")
public class Viewports {

  // TODO this needs to be initialized by Spring
  private final SubscriptionManager _subscriptionManager;

  public Viewports(SubscriptionManager subscriptionManager) {
    _subscriptionManager = subscriptionManager;
  }

  /**
   * @param subscriptionRequest Details of the subscription
   * @return URI of the new viewport
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_PLAIN)
  String createViewport(SubscriptionRequest subscriptionRequest) {
    //_subscriptionManager.
    throw new UnsupportedOperationException("TODO");
  }

  @GET
  @Path("{viewportId}")
  String getLatestViewportData(@PathParam("viewportId") UniqueId viewportId) {
    throw new UnsupportedOperationException("TODO");
  }

}
