/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.Path;

import com.opengamma.financial.user.ClientTracker;
import com.opengamma.financial.user.UserDataTracker;

/**
 * RESTful resource representing a user, at the moment purely to provide access to the resource representing
 * a user's clients.
 */
public class UserResource {

  private final String _userName;
  private final ClientsResource _clients;
  
  public UserResource(final String userName, final UsersResourceContext context, final ClientTracker clientTracker, final UserDataTracker userDataTracker) {
    _clients = new ClientsResource(userName, clientTracker, userDataTracker, context);
    _userName = userName;
  }
  
//  /**
//   * Gets the URI info.
//   * @return the uri info, not null
//   */
//  public UriInfo getUriInfo() {
//    return getUsersResource().getUriInfo();
//  }
  
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
