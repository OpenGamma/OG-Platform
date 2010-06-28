/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.util.ArgumentChecker;

/**
 * Represents the user and client details encoded in a UniqueIdentifier for a user-generated resource.
 */
public class UserResourceDetails {
  
  private final String _username;
  private final String _clientId;
  private final String _resourceType;
  
  public UserResourceDetails(String username, String clientId, String resourceType) {
    ArgumentChecker.notNull(username, "username");
    ArgumentChecker.notNull(clientId, "clientId");
    ArgumentChecker.notNull(resourceType, "resourceType");
    _username = username;
    _clientId = clientId;
    _resourceType = resourceType;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return _username;
  }

  /**
   * @return the clientId
   */
  public String getClientId() {
    return _clientId;
  }
  
  /**
   * @return the resource type
   */
  public String getResourceType() {
    return _resourceType;
  }
  
}
