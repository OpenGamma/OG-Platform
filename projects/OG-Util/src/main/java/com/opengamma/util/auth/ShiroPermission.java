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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.InvalidPermissionStringException;
import org.apache.shiro.authz.permission.WildcardPermission;

import com.google.common.collect.ImmutableSet;

/**
 * An Apache Shiro {@code Permission} that allows permission resolving to be extended.
 * <p>
 * This is a faster version of {@link WildcardPermission}.
 * See {@link ShiroPermissionResolver} for public access.
 */
final class ShiroPermission implements Permission {

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
   * @param permissionString  the permission string, not null
   * @return the permission object, not null
   * @throws InvalidPermissionStringException if the permission string is invalid
   */
  static Permission of(String permissionString) {
    return new ShiroPermission(permissionString);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param permissionString  the permission string, not null
   * @throws InvalidPermissionStringException if the permission string is invalid
   */
  private ShiroPermission(final String permissionString) {
    String permStr = StringUtils.stripToNull(permissionString);
    if (permStr == null) {
      throw new InvalidPermissionStringException("Permission string must not be blank: " + permissionString, permissionString);
    }
    // case insensitive
    permStr = permStr.toLowerCase(Locale.ROOT);
    // split once
    List<Set<String>> wildcardSegments = new ArrayList<>();
    String[] segmentStrs = StringUtils.splitPreserveAllTokens(permStr, ':');
    for (String segmentStr : segmentStrs) {
      String[] partStrs = StringUtils.splitPreserveAllTokens(segmentStr, ',');
      if (partStrs.length == 0) {
        throw new InvalidPermissionStringException("Permission string must not contain an empty segment: " + permissionString, permissionString);
      }
      Set<String> parts = new LinkedHashSet<>();
      for (String partStr : partStrs) {
        partStr = partStr.trim();
        if (partStr.isEmpty()) {
          throw new InvalidPermissionStringException("Permission string must not contain an empty part: " + permissionString, permissionString);
        }
        if (partStr.contains("*") && partStr.equals("*") == false) {
          throw new InvalidPermissionStringException("Permission string wildcard can only be applied to whole segment: " + permissionString, permissionString);
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
