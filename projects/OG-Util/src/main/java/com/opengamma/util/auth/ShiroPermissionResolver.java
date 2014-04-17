/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.auth;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.PermissionResolver;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.opengamma.util.ArgumentChecker;

/**
 * An Apache Shiro {@code PermissionResolver} that resolves to OpenGamma permissions.
 */
public final class ShiroPermissionResolver implements PermissionResolver {

  /**
   * The cached permissions.
   */
  private final LoadingCache<String, Permission> _cache =
      CacheBuilder.newBuilder()
        .maximumSize(1000)
        .build(new CacheLoader<String, Permission>() {
          @Override
          public Permission load(String permissionStr) {
            return doResolvePermission(permissionStr);
          }
        });
  /**
   * A pluggable set of resolvers by prefix.
   */
  private final ConcurrentMap<String, PermissionResolver> _prefixResolvers = new ConcurrentHashMap<>();

  /**
   * Creates an instance.
   */
  public ShiroPermissionResolver() {
  }

  //-------------------------------------------------------------------------
  /**
   * Registers a prefix that maps to a different permission resolver.
   * 
   * @param prefix  the permission prefix, not null
   * @param resolver  the permission resolver, not null
   * @throws IllegalArgumentException if the prefix is already registered
   */
  public void registerPrefix(String prefix, PermissionResolver resolver) {
    ArgumentChecker.notNull(prefix, "prefix");
    ArgumentChecker.notNull(resolver, "resolver");
    PermissionResolver existing = _prefixResolvers.putIfAbsent(prefix, resolver);
    if (existing != null && existing.equals(resolver) == false) {
      throw new IllegalArgumentException("Prefix is already registered");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the permission.
   * <p>
   * This uses a cache to speed up comparisons.
   * 
   * @param permissionStr  the permission string, not null
   * @return the permission object, not null
   */
  @Override
  public Permission resolvePermission(String permissionStr) {
    ArgumentChecker.notNull(permissionStr, "permissionStr");
    try {
      return _cache.getUnchecked(permissionStr);
    } catch (UncheckedExecutionException ex) {
      Throwables.propagateIfPossible(ex.getCause());
      throw ex;
    }
  }

  /**
   * Resolves the permission.
   * 
   * @param permissionStr  the permission string, not null
   * @return the new permission object, not null
   */
  Permission doResolvePermission(String permissionStr) {
    for (String prefix : _prefixResolvers.keySet()) {
      if (permissionStr.startsWith(prefix)) {
        return _prefixResolvers.get(prefix).resolvePermission(permissionStr);
      }
    }
    return ShiroPermission.of(permissionStr);
  }

}
