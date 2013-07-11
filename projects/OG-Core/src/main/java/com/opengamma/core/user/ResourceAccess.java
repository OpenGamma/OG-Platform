package com.opengamma.core.user;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newTreeSet;

/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
public enum ResourceAccess {
  READ('R'), WRITE('W'), CREATE('C'), DELETE('D'), QUERY('Q');

  private char _code;
  static private Map<Character, ResourceAccess> index;

  public static ResourceAccess of(char code) {
    synchronized (ResourceAccess.class) {
      if (index == null) {
        index = newHashMap();
        for (ResourceAccess resourceAccess : ResourceAccess.values()) {
          index.put(resourceAccess._code, resourceAccess);
        }
      }
    }
    return index.get(code);
  }

  private ResourceAccess(char code) {
    _code = code;
  }

  @Override
  public String toString() {
    return "" + _code;
  }

  public static String toString(Set<ResourceAccess> accesses){
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
