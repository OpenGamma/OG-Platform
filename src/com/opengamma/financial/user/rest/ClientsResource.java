/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriInfo;

/**
 * Temporary RESTful resource representing a collection of a user's clients. If any client is requested then it will
 * magically exist and persist forever.
 * 
 * This is where clients would be deleted if heartbeats stop, automatically clearing out all client-related state. 
 */
@Path("/users/{username}/clients")
public class ClientsResource {

  private final UserResource _userResource;
  private final ConcurrentHashMap<String, ClientResource> _clientMap = new ConcurrentHashMap<String, ClientResource>();
  
  public ClientsResource(final UserResource userResource) {
    _userResource = userResource;
  }
  
  public UserResource getUserResource() {
    return _userResource;
  }
  
  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return getUserResource().getUriInfo();
  }
  
  @Path("{clientUid}")
  public ClientResource get(@PathParam("clientUid") String clientName) {
    ClientResource freshClient = new ClientResource(this, clientName);
    ClientResource actualClient = _clientMap.putIfAbsent(clientName, freshClient);
    if (actualClient == null) {
      actualClient = freshClient;
    }
    return actualClient;
  }
  
}
