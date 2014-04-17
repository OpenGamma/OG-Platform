/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import java.net.URI;

import com.opengamma.master.user.ManageableRole;

/**
 * URIs for web-based roles.
 */
public class WebRoleUris {

  /**
   * The data.
   */
  private final WebRoleData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebRoleUris(WebRoleData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * @return the URI
   */
  public URI base() {
    return roles();
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI roles() {
    return WebRolesResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI role() {
    return WebRoleResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param roleName  the role name, not null
   * @return the URI
   */
  public URI role(final String roleName) {
    return WebRoleResource.uri(_data, roleName);
  }

  /**
   * Gets the URI.
   * @param role  the role, not null
   * @return the URI
   */
  public URI role(final ManageableRole role) {
    return WebRoleResource.uri(_data, role.getRoleName());
  }

  /**
   * Gets the URI.
   * @param userName  the user name, not null
   * @return the URI
   */
  public URI user(final String userName) {
    return WebUserResource.uri(new WebUserData(_data.getUriInfo()), userName);
  }

}
