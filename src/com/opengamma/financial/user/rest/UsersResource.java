/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * Temporary RESTful resource which isn't backed by any user objects, just to host /users. Any user requested will
 * magically exist.
 */
@Path("/users")
public class UsersResource {
   
  private final ConcurrentHashMap<String, UserResource> _userMap = new ConcurrentHashMap<String, UserResource>();
  /**
   * Information about the URI injected by JSR-311.
   */
  @Context
  private UriInfo _uriInfo;
  
  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return _uriInfo;
  }

  @Path("{username}")
  public UserResource getUser(@PathParam("username") String username) {
    UserResource freshUser = new UserResource(this, username);
    UserResource actualUser = _userMap.putIfAbsent(username, freshUser);
    if (actualUser == null) {
      actualUser = freshUser;
    }
    return actualUser;
  }
  
  /**
   * Temporary method to get all users. This should be on an underlying UserMaster, when it exists.
   * 
   * @return  a collection of all users, unmodifiable, not null
   */
  public Collection<UserResource> getAllUsers() {
    return Collections.unmodifiableCollection(_userMap.values());
  }
}
