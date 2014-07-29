/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.auth;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.InvalidPermissionStringException;
import org.apache.shiro.authz.permission.PermissionResolver;

/**
 * Resolves Apache Shiro {@code Permission} instances from strings.
 * <p>
 * This resolver acts as a factory for {@code Permission} instances.
 * Most permissions are resolved by {@link ShiroPermissionResolver} into
 * {@link ShiroWildcardPermission} instances. However, permissions that
 * have a registered prefix are resolved using this interface.
 */
public interface PrefixedPermissionResolver extends PermissionResolver {

  /**
   * Gets the prefix that this resolver matches.
   * 
   * @return the prefix for matched permission strings, not null
   */
  String getPrefix();

  /**
   * Resolves a single permission from the string form.
   *
   * @param permissionString  the string representation of a permission, not null
   * @return the equivalent permission object, not null
   * @throws InvalidPermissionStringException if the permission string is invalid
   */
  @Override
  Permission resolvePermission(String permissionString);

}
