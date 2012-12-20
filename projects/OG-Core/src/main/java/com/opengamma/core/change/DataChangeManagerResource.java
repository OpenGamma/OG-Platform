/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a {@link ChangeManager}.
 * <p>
 * This class only handles JMS change managers.
 */
public class DataChangeManagerResource extends AbstractDataResource {

  /**
   * The change manager.
   */
  private final JmsChangeManager _changeManager;

  /**
   * Creates a new instance.
   * 
   * @param changeManager  the change manager, not null
   */
  public DataChangeManagerResource(ChangeManager changeManager) {
    if (changeManager instanceof JmsChangeManager == false) {
      throw new UnsupportedOperationException("Only JmsChangeManager can be published over REST");
    }
    _changeManager = (JmsChangeManager) changeManager; //TODO: implement other change managers?
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("topicName")
  public Response getTopicName() {
    return responseOk(_changeManager.getJmsConnector().getTopicName());
  }

  @GET
  @Path("brokerUri")
  public Response getBrokerUri() {
    return responseOk(_changeManager.getJmsConnector().getClientBrokerUri().toString());
  }

}
