/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.financial.position.memory.InMemoryPositionMaster;
import com.opengamma.financial.position.rest.PortfoliosResource;

/**
 * Temporary RESTful resource representing a user's client session.
 */
@Path("/users/{username}/clients/{clientUid}")
public class ClientResource {
  
  /**
   * The path used to retrieve user portfolios
   */
  public static final String PORTFOLIOS_PATH = "portfolios";
  
  private final ClientsResource _clientsResource;
  private final ManagablePositionMaster _positionMaster;
  
  public ClientResource(ClientsResource clientsResource, String clientName) {
    _clientsResource = clientsResource;
    String uidScheme = clientsResource.getUserResource().getUserName() + "-" + clientName;
    _positionMaster = new InMemoryPositionMaster(uidScheme);
  }
  
  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return getClientsResource().getUriInfo();
  }
  
  public ClientsResource getClientsResource() {
    return _clientsResource;
  }
  
  @Path(PORTFOLIOS_PATH)
  public PortfoliosResource getPositionMaster() {
    return new PortfoliosResource(getUriInfo(), _positionMaster);
  }
  
}
