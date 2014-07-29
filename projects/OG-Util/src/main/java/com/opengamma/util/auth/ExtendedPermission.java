/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.auth;

import java.util.Collection;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;

/**
 * An extension to the Apache Shiro {@code Permission} interface.
 * <p>
 * This extension supports the check operation which can throw exceptions.
 * This allows implementations to throw meaningful exceptions.
 * See {@link ShiroPermissionResolver} for public access to permissions.
 * <p>
 * This extension also supports bulk checking of permissions for efficiency.
 * This is most useful if a remote call is involved.
 */
public interface ExtendedPermission extends Permission {

  /**
   * Checks that this permission implies the required permission.
   * <p>
   * This object will be the permission of the subject user.
   * The specified permission is the one that is required.
   * This only differs from {@link #implies(Permission)} in that this
   * method is allowed to throw an exception if there was a problem
   * determining the permission status.
   * 
   * @param requiredPermission  the required permission, not null
   * @return true if implied, false if not implied or type not recognized
   * @throws AuthorizationException if an exception occurred while determining the result
   */
  boolean checkImplies(Permission requiredPermission);

  /**
   * Checks that this permission implies all of the required permissions.
   * <p>
   * This object will be the permission of the subject user.
   * The specified permissions are the ones that are required.
   * <p>
   * The optional exception is only thrown if there was a problem
   * determining the permission status.
   * A normal permission denied result is indicated by returning false.
   * 
   * @param requiredPermissions  the required permissions, not null
   * @param exceptionsOnError  whether to allow meaningful exceptions
   * @return true if all permissions checked and implied,
   *  false if all permissions checked and at least one is not implied,
   *  null if permissions not fully checked
   * @throws AuthorizationException if an exception occurred while determining the result,
   *  only thrown if {@code allowExceptions} is true
   */
  Boolean checkImpliesAll(Collection<Permission> requiredPermissions, boolean exceptionsOnError);

}
