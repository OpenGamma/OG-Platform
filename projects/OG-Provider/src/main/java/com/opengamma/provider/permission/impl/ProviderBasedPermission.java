/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import org.apache.shiro.authz.Permission;

import com.opengamma.core.user.UserPrincipals;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;

/**
 * An Apache Shiro permission that uses a {@code PermissionCheckProvider}.
 * <p>
 * This uses the underlying provider to check permissions.
 * See {@link ProviderBasedPermissionResolver} for public access.
 */
final class ProviderBasedPermission implements Permission {

  /**
   * The underlying provider.
   */
  private final PermissionCheckProvider _provider;
  /**
   * The permission string.
   */
  private final String _permissionString;

  /**
   * Creates an instance of the permission.
   * 
   * @param provider  the underlying permission check provider, not null
   * @param permissionString  the permission string, not null
   */
  ProviderBasedPermission(PermissionCheckProvider provider, String permissionString) {
    _provider = ArgumentChecker.notNull(provider, "provider");
    _permissionString = ArgumentChecker.notNull(permissionString, "permissionString");
  }

  //-------------------------------------------------------------------------
  // this permission is the permission I have
  // the other permission is the permission being checked
  @Override
  public boolean implies(Permission requestedPermission) {
    if (requestedPermission instanceof ProviderBasedPermission == false) {
      return false;
    }
    ProviderBasedPermission requestedPerm = (ProviderBasedPermission) requestedPermission;
    UserPrincipals user = (UserPrincipals) AuthUtils.getSubject().getSession().getAttribute(UserPrincipals.ATTRIBUTE_KEY);
    if (user == null) {
      return false;
    }
    return _provider.isPermitted(user.getAlternateIds(), user.getNetworkAddress(), requestedPerm._permissionString);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ProviderBasedPermission) {
      ProviderBasedPermission other = (ProviderBasedPermission) obj;
      return _permissionString.equals(other._permissionString);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _permissionString.hashCode();
  }

  @Override
  public String toString() {
    return _permissionString;
  }

}
