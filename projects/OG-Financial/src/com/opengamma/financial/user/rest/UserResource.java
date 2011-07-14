/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

/**
 * RESTful resource representing a user, at the moment purely to provide access to the resource representing
 * a user's clients.
 */
public class UserResource {

  private final ClientsResource _clients;
  private final String _userName;
  private final UserResourceData _data;
  
  public UserResource(final String userName, final UserResourceData data) {
    _clients = new ClientsResource(userName, data);
    _userName = userName;
    _data = data;
  }
  
  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return _data.getUriInfo();
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
