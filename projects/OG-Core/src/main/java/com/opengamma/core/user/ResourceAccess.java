/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newTreeSet;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * ResourceAccess types 
 */
public enum ResourceAccess {
  /**
   * read rights
   */
  READ('R'),
  /**
   * write rights
   */
  WRITE('W'),
  /**
   * create rights
   */
  CREATE('C'),
  /**
   * delete rights
   */
  DELETE('D'),
  /**
   * query rights
   */
  QUERY('Q');

  private char _code;
  private static Map<Character, ResourceAccess> s_index;

  public static ResourceAccess of(char code) {
    synchronized (ResourceAccess.class) {
      if (s_index == null) {
        s_index = newHashMap();
        for (ResourceAccess resourceAccess : ResourceAccess.values()) {
          s_index.put(resourceAccess._code, resourceAccess);
        }
      }
    }
    return s_index.get(code);
  }

  private ResourceAccess(char code) {
    _code = code;
  }

  @Override
  public String toString() {
    return "" + _code;
  }

  public static String toString(Set<ResourceAccess> accesses) {
    SortedSet<Character> accessChars = newTreeSet();
    for (ResourceAccess access : accesses) {
      accessChars.add(access._code);
    }

    StringBuilder sb = new StringBuilder();
    for (Character accessChar : accessChars) {
      sb.append(accessChar);
    }
    return sb.toString();
  }

}
