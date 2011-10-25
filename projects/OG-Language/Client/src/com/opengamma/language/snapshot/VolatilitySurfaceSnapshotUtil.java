/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.snapshot;

import com.opengamma.util.tuple.Pair;

/* package */final class VolatilitySurfaceSnapshotUtil {

  private VolatilitySurfaceSnapshotUtil() {
  }

  public static String toString(final Object key) {
    if (key.getClass().isEnum()) {
      return ((Enum<?>) key).name();
    } else if (key instanceof String) {
      return (String) key;
    } else if (key instanceof Pair<?, ?>) {
      return toString(((Pair<?, ?>) key).getFirst()) + ", " + toString(((Pair<?, ?>) key).getSecond());
    } else {
      return key.toString();
    }
  }

}
