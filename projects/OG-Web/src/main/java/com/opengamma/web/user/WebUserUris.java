/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import java.net.URI;

import com.opengamma.master.user.ManageableUser;

/**
 * URIs for web-based users.
 */
public class WebUserUris {

  /**
   * The data.
   */
  private final WebUserData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebUserUris(WebUserData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * @return the URI
   */
  public URI base() {
    return users();
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI users() {
    return WebUsersResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI userResetPassword() {
    return WebUserResource.uriResetPassword(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI userStatus() {
    return WebUserResource.uriStatus(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI user() {
    return WebUserResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param user  the user, not null
   * @return the URI
   */
  public URI user(final ManageableUser user) {
    return WebUserResource.uri(_data, user.getUserName());
  }

}
