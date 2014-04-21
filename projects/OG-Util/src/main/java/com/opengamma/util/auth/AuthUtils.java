/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.auth;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.subject.Subject;

/**
 * Main entry point to the authentication and authorization system.
 * <p>
 * This class is used instead of the standard Apache Shiro {@code SecurityUtils}.
 */
public final class AuthUtils extends SecurityUtils {

  /**
   * Initializes the authentication and authorization system to permissive mode.
   * Permissive mode has a logged on user with all permissions granted.
   */
  public static void initPermissive() {
    setSecurityManager(new PermissiveSecurityManager());
  }

  /**
   * Checks if the authentication and authorization system is in permissive mode.
   * Permissive mode has a logged on user with all permissions granted.
   * 
   * @return true if permissive
   * @throws UnavailableSecurityManagerException if no security manager is installed
   */
  public static boolean isPermissive() {
    return getSecurityManager() instanceof PermissiveSecurityManager;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the {@code Subject} available to the calling code, typically based on a thread-local.
   * <p>
   * This provides access to the {@code Subject}, which is the main class used to interact with
   * the authentication and authorization system. A {@code Subject} represents the 'user', which
   * may or may not be logged on, and may be an application rather than a human.
   * <p>
   * Methods are provided to login, logout, check permissions and check roles.
   * In addition, the {@code Subject} provides a session similar to {@code HttpSession}.
   *
   * @return the appropriate {@code Subject}, not null
   * @throws IllegalStateException if the subject or security manager cannot be obtained,
   *  which is a configuration error
   */
  public static Subject getSubject() {
    return SecurityUtils.getSubject();
  }

  /**
   * Gets the user name from the {@code Subject} if logged in.
   * <p>
   * This returns the user name if the user has been logged in.
   *
   * @return the user name, null if not logged on
   * @throws IllegalStateException if the subject or security manager cannot be obtained,
   *  which is a configuration error
   * @throws ClassCastException if the primary principal of the subject is not a user name
   */
  public static String getUserName() {
    return (String) SecurityUtils.getSubject().getPrincipal();
  }

}
