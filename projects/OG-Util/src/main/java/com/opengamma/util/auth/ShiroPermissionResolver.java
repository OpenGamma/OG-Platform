/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.InvalidPermissionStringException;
import org.apache.shiro.authz.permission.PermissionResolver;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.opengamma.util.ArgumentChecker;

/**
 * An Apache Shiro {@code PermissionResolver} that resolves to OpenGamma permissions.
 * <p>
 * This resolver supports extended permission systems by registering a prefix for permissions.
 * If the requested permission matches a prefix then the associated registered resolver is used.
 * Otherwise, the standard permission is used.
 * <p>
 * For example, this could be used to check permission to access ticking data on an equity.
 * The user would be given the permission 'Data.BigDataProvider'.
 * The data would be given the permission 'Data.BigDataProvider.AnEquityIdentifier'.
 * A special permission resolver would then be registered for the 'Data.MyBigDataProvider' prefix.
 * When the prefix is seen, the resolver would return a different {@link Permission} implementation
 * that is capable of dynamically checking access to the specific equity identifier, which usually
 * requires contacting the big data provider.
 */
public final class ShiroPermissionResolver implements PermissionResolver {

  /**
   * The cached permissions.
   * ConcurrentHashMap cannot restrict size, so use LoadingCache.
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
   * Registration should occur only during startup, but still need concurrent map.
   */
  private final ConcurrentMap<String, PrefixedPermissionResolver> _prefixed = new ConcurrentHashMap<>(16, 0.75f, 1);

  /**
   * Creates an instance.
   */
  public ShiroPermissionResolver() {
  }

  //-------------------------------------------------------------------------
  /**
   * Registers a factory that handles permissions with a specific prefix.
   * <p>
   * This allows different implementations of the {@code Permission} interface
   * to be created based on a prefix.
   * 
   * @param resolver  the permission resolver, not null
   * @throws IllegalArgumentException if the prefix is already registered
   */
  public void register(PrefixedPermissionResolver resolver) {
    ArgumentChecker.notNull(resolver, "resolver");
    ArgumentChecker.notNull(resolver.getPrefix(), "resovler.prefix");
    PrefixedPermissionResolver existing = _prefixed.putIfAbsent(resolver.getPrefix(), resolver);
    if (existing != null && existing.equals(resolver) == false) {
      throw new IllegalArgumentException("Prefix is already registered");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the permission from string to object form.
   * <p>
   * This uses a cache to speed up comparisons.
   * 
   * @param permissionString  the permission string, not null
   * @return the permission object, not null
   * @throws InvalidPermissionStringException if the permission string is invalid
   */
  @Override
  public Permission resolvePermission(String permissionString) {
    ArgumentChecker.notNull(permissionString, "permissionString");
    try {
      return _cache.getUnchecked(permissionString);
    } catch (UncheckedExecutionException ex) {
      // cache annoyingly wraps underlying runtime exceptions, so unwrap and rethrow
      Throwables.propagateIfPossible(ex.getCause());
      throw ex;
    }
  }

  /**
   * Resolves a set of permissions from string to object form.
   * <p>
   * The returned set of permissions may be smaller than the input set.
   * 
   * @param permissionStrings  the set of permission strings, not null
   * @return the set of permission objects, not null
   * @throws InvalidPermissionStringException if the permission string is invalid
   */
  public List<Permission> resolvePermissions(String... permissionStrings) {
    ArgumentChecker.notNull(permissionStrings, "permissionStrings");
    List<Permission> permissions = new ArrayList<>(permissionStrings.length);
    for (String permissionString : permissionStrings) {
      permissions.add(resolvePermission(permissionString));
    }
    return permissions;
  }

  /**
   * Resolves a set of permissions from string to object form.
   * <p>
   * The returned set of permissions may be smaller than the input set.
   * 
   * @param permissionStrings  the set of permission strings, not null
   * @return the set of permission objects, not null
   * @throws InvalidPermissionStringException if the permission string is invalid
   */
  public Set<Permission> resolvePermissions(Collection<String> permissionStrings) {
    ArgumentChecker.notNull(permissionStrings, "permissionStrings");
    Set<Permission> permissions = new HashSet<>();
    for (String permissionString : permissionStrings) {
      permissions.add(resolvePermission(permissionString));
    }
    return permissions;
  }

  //-------------------------------------------------------------------------
  /**
   * Resolves the permission.
   * <p>
   * If the requested permission matches a prefix then the associated resolver is used.
   * Otherwise, the standard permission is used.
   * <p>
   * This is called directly from the cache.
   * 
   * @param permissionString  the permission string, not null
   * @return the new permission object, not null
   * @throws InvalidPermissionStringException if the permission string is invalid
   */
  private Permission doResolvePermission(String permissionString) {
    for (PrefixedPermissionResolver prefixedResolver : _prefixed.values()) {
      if (permissionString.startsWith(prefixedResolver.getPrefix())) {
        return prefixedResolver.resolvePermission(permissionString);
      }
    }
    return ShiroWildcardPermission.of(permissionString);
  }

}
