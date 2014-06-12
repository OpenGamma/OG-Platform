/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.auth;

import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;

import com.opengamma.util.ArgumentChecker;

/**
 * Main entry point to the authentication and authorization system.
 * <p>
 * This class is used instead of the standard Apache Shiro {@code SecurityUtils}.
 */
public final class AuthUtils {

  /**
   * The singleton permission resolver.
   */
  private static final ShiroPermissionResolver s_permissionResolver = new ShiroPermissionResolver();

  // always setup a security manager
  static {
    SecurityUtils.setSecurityManager(PermissiveSecurityManager.DEFAULT);
  }

  /**
   * Restricted constructor.
   */
  private AuthUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the {@code PermissionResolver} that creates authorization {@code Permission} instances.
   * <p>
   * The authorization system is based on {@link Permission} instances.
   * A {@link PermissionResolver} is used as a factory to create {@code Permission} instances.
   * <p>
   * The permission resolver returned here is a singleton that provides the ability
   * to switch the permission implementation based on a prefix.
   *
   * @return the singleton {@code PermissionResolver}, not null
   */
  public static ShiroPermissionResolver getPermissionResolver() {
    return s_permissionResolver;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the single shared security manager.
   * <p>
   * The shared security manager is used as a fallback when there is no manager
   * in the {@link ThreadContext}.
   * This will return a permissive security manager by default.
   * 
   * @return the security manager, not null
   */
  public static SecurityManager getSecurityManager() {
    return SecurityUtils.getSecurityManager();
  }

  /**
   * Sets the single shared security manager.
   * <p>
   * The shared security manager is used as a fallback when there is no manager
   * in the {@link ThreadContext}.
   * This method can only be called if the current manager is permissive.
   * 
   * @param securityManager  the new security manager, not null
   */
  public static void initSecurityManager(SecurityManager securityManager) {
    if (isPermissive()) {
      SecurityUtils.setSecurityManager(securityManager);
    } else {
      throw new ShiroException("A security manager cannot be changed once set");
    }
  }

  /**
   * Initializes the authentication and authorization system to permissive mode.
   * Permissive mode has a logged on user with all permissions granted.
   * <p>
   * This is used during startup to deliberately select a mode of operation
   * where security is switched off.
   */
  public static void initSecurityManagerPermissive() {
    initSecurityManager(new PermissiveSecurityManager());
  }

  /**
   * Checks if the authentication and authorization system is in permissive mode.
   * Permissive mode has a logged on user with all permissions granted.
   * 
   * @return true if permissive
   */
  public static boolean isPermissive() {
    return getSecurityManager() instanceof PermissiveSecurityManager;
  }

  /**
   * Checks if the authentication and authorization system is still in the
   * default permissive mode.
   * 
   * @return true if the security manager has not been set
   */
  public static boolean isDefault() {
    return getSecurityManager() == PermissiveSecurityManager.DEFAULT;
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

  //-------------------------------------------------------------------------
  /**
   * Checks that the user has all the permissions necessary to see the entity.
   * 
   * @param permissionable  the entity to be checked, not null
   * @return true if permitted
   */
  public static boolean isPermitted(Permissionable permissionable) {
    ArgumentChecker.notNull(permissionable, "entity");
    Set<Permission> requiredPermissions = AuthUtils.getPermissionResolver().resolvePermissions(permissionable.getRequiredPermissions());
    return AuthUtils.getSubject().isPermittedAll(requiredPermissions);
  }

  /**
   * Checks that the user has all the permissions necessary to see the entity.
   * 
   * @param permissionable  the entity to be checked, not null
   * @throws AuthorizationException if the user does not have permission
   */
  public static void checkPermissions(Permissionable permissionable) {
    ArgumentChecker.notNull(permissionable, "entity");
    Set<Permission> requiredPermissions = AuthUtils.getPermissionResolver().resolvePermissions(permissionable.getRequiredPermissions());
    AuthUtils.getSubject().checkPermissions(requiredPermissions);
  }

}
