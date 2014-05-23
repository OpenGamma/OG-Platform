/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.PermissionResolver;

import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;

/**
 * An Apache Shiro permission resolver that uses a {@code PermissionCheckProvider}.
 * <p>
 * Instances of this class are registered using {@code ShiroPermissionResolver#registerPrefix}
 * accessed via {@link AuthUtils#getPermissionResolver()}.
 */
public final class ProviderBasedPermissionResolver implements PermissionResolver {

  /**
   * The underlying provider.
   */
  private final PermissionCheckProvider _provider;

  /**
   * Creates an instance.
   * 
   * @param provider  the underlying permission check provider, not null
   */
  public ProviderBasedPermissionResolver(PermissionCheckProvider provider) {
    _provider = ArgumentChecker.notNull(provider, "provider");
  }

  //-------------------------------------------------------------------------
  @Override
  public Permission resolvePermission(String permissionString) {
    return new ProviderBasedPermission(_provider, permissionString);
  }

}
