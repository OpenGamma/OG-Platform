/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import org.apache.shiro.authz.Permission;

import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;
import com.opengamma.util.auth.PrefixedPermissionResolver;
import com.opengamma.util.auth.ShiroPermissionResolver;

/**
 * An Apache Shiro permission resolver that uses a {@code PermissionCheckProvider}.
 * <p>
 * Instances of this class are registered using {@link ShiroPermissionResolver#register(PrefixedPermissionResolver)}
 * accessed via {@link AuthUtils#getPermissionResolver()}.
 */
public final class ProviderBasedPermissionResolver implements PrefixedPermissionResolver {

  /**
   * The permission prefix.
   */
  private final String _prefix;
  /**
   * The underlying provider.
   */
  private final PermissionCheckProvider _provider;

  /**
   * Creates an instance.
   * 
   * @param prefix  the permission prefix, not null
   * @param provider  the underlying permission check provider, not null
   */
  public ProviderBasedPermissionResolver(String prefix, PermissionCheckProvider provider) {
    _prefix = ArgumentChecker.notNull(prefix, "prefix");
    _provider = ArgumentChecker.notNull(provider, "provider");
  }

  //-------------------------------------------------------------------------
  @Override
  public String getPrefix() {
    return _prefix;
  }

  @Override
  public Permission resolvePermission(String permissionString) {
    return new ProviderBasedPermission(_provider, permissionString);
  }

}
