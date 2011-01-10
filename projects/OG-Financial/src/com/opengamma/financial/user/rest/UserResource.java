/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import com.opengamma.util.ArgumentChecker;

/**
 * RESTful resource representing a user, at the moment purely to provide access to the resource representing
 * a user's clients.
 */
public class UserResource {

  private final UsersResource _usersResource;
  private final String _userName;
  private final ClientsResource _clients;
  
  public UserResource(final UsersResource usersResource, final String userName, final UsersResourceContext context) {
    ArgumentChecker.notNull(usersResource, "usersResource");
    _clients = new ClientsResource(this, context);
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
  
  public void deleteClients(final long timestamp) {
    getClients().deleteClients(timestamp);
  }

  public long getLastAccessed() {
    return getClients().getLastAccessed();
  }

}
