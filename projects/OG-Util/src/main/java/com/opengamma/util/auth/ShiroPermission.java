/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.auth;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.shiro.authz.Permission;

import com.google.common.collect.ImmutableSet;
import com.opengamma.util.ArgumentChecker;

/**
 * An Apache Shiro {@code Permission} that allows permission resolving to be extended.
 */
public final class ShiroPermission implements Permission {

  /**
   * The cached permissions.
   */
  private static final ConcurrentMap<String, ShiroPermission> s_cache = new ConcurrentHashMap<>();
  /**
   * The wildcard segment.
   */
  private static final ImmutableSet<String> WILDCARD_SEGMENT = ImmutableSet.of("*");

  /**
   * The permission segments.
   */
  private final List<Set<String>> _segments = new ArrayList<>();
  /**
   * The hash code.
   */
  private final int _hashCode;
  /**
   * The string form.
   */
  private final String _toString;

  /**
   * Creates an instance.
   * 
   * @param permissionStr  the permission string, not null
   * @return the permission object, not null
   */
  public static Permission of(String permissionStr) {
    try {
      ShiroPermission perm = s_cache.get(permissionStr);
      if (perm == null) {
        s_cache.putIfAbsent(permissionStr, new ShiroPermission(permissionStr));
        perm = s_cache.get(permissionStr);
      }
      return perm;
      
    } catch (NullPointerException ex) {
      // this is done to avoid null check in common case
      ArgumentChecker.notNull(permissionStr, "permissionStr");
      throw ex;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param permissionStr  the permission string, not null
   */
  private ShiroPermission(final String permissionStr) {
    String permStr = StringUtils.stripToNull(permissionStr);
    if (permStr == null) {
      throw new IllegalArgumentException("Permission string must not be blank: " + permissionStr);
    }
    // case insensitive
    permStr = permStr.toLowerCase(Locale.ROOT);
    // split once
    List<Set<String>> wildcardSegments = new ArrayList<>();
    String[] segmentStrs = StringUtils.splitPreserveAllTokens(permStr, ':');
    for (String segmentStr : segmentStrs) {
      String[] partStrs = StringUtils.splitPreserveAllTokens(segmentStr, ',');
      if (partStrs.length == 0) {
        throw new IllegalArgumentException("Permission string must not contain an empty segment: " + permissionStr);
      }
      Set<String> parts = new LinkedHashSet<>();
      for (String partStr : partStrs) {
        partStr = partStr.trim();
        if (partStr.isEmpty()) {
          throw new IllegalArgumentException("Permission string must not contain an empty part: " + permissionStr);
        }
        if (partStr.contains("*") && partStr.equals("*") == false) {
          throw new IllegalArgumentException("Permission string wildcard can only be applied to whole segment: " + permissionStr);
        }
        parts.add(partStr);
      }
      // simplify
      if (parts.contains("*")) {
        wildcardSegments.add(WILDCARD_SEGMENT);
      } else {
        _segments.addAll(wildcardSegments);
        wildcardSegments.clear();
        _segments.add(parts);
      }
    }
    _hashCode = _segments.hashCode();
    _toString = createToString(_segments);
  }

  private static String createToString(List<Set<String>> segments) {
    StrBuilder buf = new StrBuilder();
    for (Iterator<Set<String>> it1 = segments.iterator(); it1.hasNext();) {
      for (Iterator<String> it2 = it1.next().iterator(); it2.hasNext();) {
        buf.append(it2.next());
        if (it2.hasNext()) {
          buf.append(',');
        }
      }
      if (it1.hasNext()) {
        buf.append(':');
      }
    }
    return buf.toString();
  }

  //-------------------------------------------------------------------------
  // this permission is the permission I have
  // the other permission is the permission being checked
  @Override
  public boolean implies(Permission permission) {
    if (permission instanceof ShiroPermission == false) {
      return false;
    }
    ShiroPermission perm = (ShiroPermission) permission;
    List<Set<String>> thisSegments = _segments;
    List<Set<String>> otherSegments = perm._segments;
    if (thisSegments.size() > otherSegments.size()) {
      return false;
    }
    int commonLen = Math.min(thisSegments.size(), otherSegments.size());
    for (int i = 0; i < commonLen; i++) {
      Set<String> thisSegment = thisSegments.get(i);
      Set<String> otherSegment = otherSegments.get(i);
      if (thisSegment != WILDCARD_SEGMENT && thisSegment.containsAll(otherSegment) == false) {
        return false;
      }
    }
    return true;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ShiroPermission) {
      ShiroPermission other = (ShiroPermission) obj;
      return _segments.equals(other._segments);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }

  @Override
  public String toString() {
    return _toString;
  }

}
