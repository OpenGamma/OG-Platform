/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


/**
 * RESTful resource for a {@link ChangeManager}.
 * Changes are published via JMS only at the moment
 */
public class ChangeManagerResource {

  /**
   * The change manager.
   */
  private final JmsChangeManager _changeManager;

  /**
   * Creates a new instance.
   * 
   * @param changeManager  the change manager, not null
   */
  public ChangeManagerResource(ChangeManager changeManager) {
    if (changeManager instanceof JmsChangeManager == false) {
      throw new UnsupportedOperationException("Only JmsChangeManager can be published over REST");
    }
    _changeManager = (JmsChangeManager) changeManager; //TODO: implement other change managers?
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("topicName")
  public Response getTopicName() {
    return Response.ok(_changeManager.getJmsConnector().getTopicName()).build();
  }

}
