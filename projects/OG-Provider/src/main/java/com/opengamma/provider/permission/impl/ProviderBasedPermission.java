/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.UnauthenticatedException;

import com.opengamma.core.user.UserPrincipals;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;
import com.opengamma.util.auth.ExtendedPermission;

/**
 * An Apache Shiro permission that uses a {@code PermissionCheckProvider}.
 * <p>
 * This uses the underlying provider to check permissions.
 * See {@link ProviderBasedPermissionResolver} for public access.
 */
final class ProviderBasedPermission implements ExtendedPermission {

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
  private String getPermissionString() {
    return _permissionString;
  }

  //-------------------------------------------------------------------------
  // this permission is the permission I have
  // the other permission is the permission being checked
  @Override
  public boolean implies(Permission requiredPermission) {
    if (requiredPermission instanceof ProviderBasedPermission == false) {
      return false;
    }
    ProviderBasedPermission requiredPerm = (ProviderBasedPermission) requiredPermission;
    UserPrincipals user = (UserPrincipals) AuthUtils.getSubject().getSession().getAttribute(UserPrincipals.ATTRIBUTE_KEY);
    if (user == null) {
      return false;
    }
    return _provider.isPermitted(user.getAlternateIds(), user.getNetworkAddress(), requiredPerm.getPermissionString());
  }

  @Override
  public boolean checkImplies(Permission requiredPermission) {
    if (requiredPermission instanceof ProviderBasedPermission == false) {
      return false;
    }
    ProviderBasedPermission requiredPerm = (ProviderBasedPermission) requiredPermission;
    UserPrincipals user = (UserPrincipals) AuthUtils.getSubject().getSession().getAttribute(UserPrincipals.ATTRIBUTE_KEY);
    if (user == null) {
      throw new UnauthenticatedException("Permission denied: User not logged in: " + requiredPermission);
    }
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(
        user.getAlternateIds(), user.getNetworkAddress(), requiredPerm.getPermissionString());
    PermissionCheckProviderResult result = _provider.isPermitted(request);
    result.checkErrors();
    return result.isPermitted(requiredPerm.getPermissionString());
  }

  @Override
  public Boolean checkImpliesAll(Collection<Permission> requiredPermissions, boolean exceptionsOnError) {
    if (requiredPermissions.isEmpty()) {
      return Boolean.TRUE;
    }
    Set<String> required = new HashSet<>();
    for (Permission requiredPermission : requiredPermissions) {
      if (requiredPermission instanceof ProviderBasedPermission == false) {
        return null;
      }
      required.add(((ProviderBasedPermission) requiredPermission).getPermissionString());
    }
    UserPrincipals user = (UserPrincipals) AuthUtils.getSubject().getSession().getAttribute(UserPrincipals.ATTRIBUTE_KEY);
    if (user == null) {
      if (exceptionsOnError) {
        throw new UnauthenticatedException("Permission denied: User not logged in: " + required);
      } else {
        return Boolean.FALSE;
      }
    }
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(
        user.getAlternateIds(), user.getNetworkAddress(), required);
    PermissionCheckProviderResult result = _provider.isPermitted(request);
    if (exceptionsOnError) {
      result.checkErrors();
    }
    return Boolean.valueOf(result.isPermittedAll(required));
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ProviderBasedPermission) {
      ProviderBasedPermission other = (ProviderBasedPermission) obj;
      return getPermissionString().equals(other.getPermissionString());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getPermissionString().hashCode();
  }

  @Override
  public String toString() {
    return getPermissionString();
  }

}
