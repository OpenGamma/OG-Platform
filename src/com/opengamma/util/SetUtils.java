/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 *
 * @author kirk
 */
public final class SetUtils {
  private SetUtils() {
  }
  
  public static <T> Set<T> asSet(T... elements) {
    Set<T> result = new HashSet<T>();
    for(T element : elements) {
      result.add(element);
    }
    return result;
  }

}
