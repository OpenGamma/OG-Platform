/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import java.net.URI;

import javax.ws.rs.core.UriInfo;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import com.opengamma.core.user.UserProfile;
import com.opengamma.util.ArgumentChecker;

/**
 * Wrapper of user and security information for Freemarker.
 */
public class WebUser {

  /**
   * The subject.
   */
  private final Subject _subject;
  /**
   * The URI information.
   */
  private final UriInfo _uriInfo;

  /**
   * Creates an instance.
   * 
   * @param uriInfo  the URI, not null
   */
  public WebUser(UriInfo uriInfo) {
    ArgumentChecker.notNull(uriInfo, "uriInfo");
    Subject subject = SecurityUtils.getSubject();
    _subject = subject;
    _uriInfo = uriInfo;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if security is enabled.
   * 
   * @return true if enabled, not null
   */
  public boolean isEnabled() {
    return (SecurityUtils.getSecurityManager().getClass().getName().contains("Permissive") == false);
  }

  /**
   * Gets the subject.
   * 
   * @return the subject, not null
   */
  public Subject getSubject() {
    return _subject;
  }

  /**
   * Gets the user profile.
   * 
   * @return the profile, null if profile not available
   */
  public UserProfile getProfile() {
    Session session = _subject.getSession(false);
    if (session == null) {
      return null;
    }
    return (UserProfile) session.getAttribute(UserProfile.class.getName());
  }

  /**
   * Gets the URI info.
   * 
   * @return the URI info, not null
   */
  public UriInfo getUriInfo() {
    return _uriInfo;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the login URI.
   * 
   * @return the login URI, not null
   */
  public URI getLoginUri() {
    return WebLoginResource.uri(_uriInfo);
  }

  /**
   * Gets the logout URI.
   * 
   * @return the logout URI, not null
   */
  public URI getLogoutUri() {
    return WebLogoutResource.uri(_uriInfo);
  }

  /**
   * Gets the registration URI.
   * 
   * @return the registration URI, not null
   */
  public URI getRegisterUri() {
    return WebRegisterResource.uri(_uriInfo);
  }

  /**
   * Gets the profile URI.
   * 
   * @return the profile URI, not null
   */
  public URI getProfileUri() {
    return WebProfileResource.uri(_uriInfo);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the user name.
   * 
   * @return the user name, not null
   */
  public String getUserName() {
    if (_subject.isAuthenticated() == false) {
      return null;
    }
    return (String) _subject.getPrincipal();
  }

  /**
   * Is the requested permission allowed for the subject.
   * 
   * @param permission  the permission, not null
   * @return true if permitted
   */
  public boolean isPermitted(String permission) {
    return _subject.isPermitted(permission);
  }

  /**
   * Is the subject logged in (authenticated).
   * 
   * @return true if logged in and authenticated
   */
  public boolean isLoggedIn() {
    return _subject.isAuthenticated();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return String.format("WebSecurity[%s]", isLoggedIn() ? getUserName() : "<anonymous>");
  }

}
