/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierTemplate;
import com.opengamma.util.ArgumentChecker;

/**
 * Generates and parses features of user/client-specific {@link UniqueIdentifier}s. The generated identifiers look
 * remarkably similar to the URI under which the associated resource can be found in a RESTful manner and it is likely
 * that this will be replaced - see UTL-60.
 */
public class UserUniqueIdentifierUtils {
  
  /**
   * The scheme used in UniqueIdentifiers created on behalf of users.
   */
  private static final String SCHEME = "User"; 
  private static final String SEPARATOR = "/";
  
  /**
   * Generates a {@link UniqueIdentifierTemplate} for the given username and clientId.
   * 
   * @param details  details identifying the data source
   * @return  a template for generating {@link UniqueIdentifier}s which encode the given details.
   */
  public static UniqueIdentifierTemplate getTemplate(UserResourceDetails details) {
    return new UniqueIdentifierTemplate(SCHEME, getValuePrefix(details));
  }
  
  /**
   * Extracts the username and client ID back out of a {@link UniqueIdentifier}.
   * 
   * @param uid  the identifier
   * @return  the extracted details
   */
  public static UserResourceDetails getDetails(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    String[] valueParts = uid.getValue().split(SEPARATOR, 3);
    if (valueParts.length < 3) {
      throw new IllegalArgumentException("The specified UniqueIdentifier was not in the expected format");
    }
    String username = valueParts[0];
    String clientId = valueParts[1];
    String resourceType = valueParts[2];
    return new UserResourceDetails(username, clientId, resourceType); 
  }
  
  public static String getUserScheme() {
    return SCHEME;
  }
    
  private static String getValuePrefix(UserResourceDetails details) {
    if (details.getUsername().contains(SEPARATOR) || details.getClientId().contains(SEPARATOR) || details.getResourceType().contains(SEPARATOR)) {
      throw new IllegalArgumentException("Invalid character '" + SEPARATOR + "' found in the identifier details");
    }
    return details.getUsername() + SEPARATOR + details.getClientId() + SEPARATOR + details.getResourceType();
  }
  
}
