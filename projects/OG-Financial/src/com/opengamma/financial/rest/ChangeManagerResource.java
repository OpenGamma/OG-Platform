/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.JmsChangeManager;


/**
 * RESTful resource for a {@link ChangeManager}.
 * Changes are published via JMS only at the moment
 */
public class ChangeManagerResource {
  private final JmsChangeManager _changeManager;

  public ChangeManagerResource(ChangeManager changeManager) {
    if (!(changeManager instanceof JmsChangeManager)) {
      throw new UnsupportedOperationException("This change manager cannot be published over rest");
    }
    _changeManager = (JmsChangeManager) changeManager; //TODO: implement other change managers?
  }

  @GET
  @Path("topicName")
  public Response getTopicName() {
    return Response.ok(_changeManager.getTopic()).build();
  }
}
