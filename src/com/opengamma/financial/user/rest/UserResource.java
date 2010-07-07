/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.ArgumentChecker;

/**
 * Temporary RESTful resource representing a user, at the moment purely to provide access to the resource representing
 * a user's clients.
 */
@Path("/users/{username}")
public class UserResource {

  private final UsersResource _usersResource;
  private final String _userName;
  private final ClientsResource _clients;
  
  public UserResource(final UsersResource usersResource, final String userName, final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(usersResource, "usersResource");
    _clients = new ClientsResource(this, fudgeContext);
    _usersResource = usersResource;
    _userName = userName;
  }
  
  public UsersResource getUsersResource() {
    return _usersResource;
  }
  
  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return getUsersResource().getUriInfo();
  }
  
  public String getUserName() {
    return _userName;
  }
  
  @Path("clients")
  public ClientsResource getClients() {
    return _clients;
  }
  
}
