/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Partial implementation of {@link BinaryDataStore}.
 */
public abstract class AbstractBinaryDataStore implements BinaryDataStore {

  public Map<Long, byte[]> get(final Collection<Long> identifiers) {
    return get(this, identifiers);
  }

  public static Map<Long, byte[]> get(final BinaryDataStore dataStore, final Collection<Long> identifiers) {
    final Map<Long, byte[]> result = new HashMap<Long, byte[]>();
    for (Long identifier : identifiers) {
      final byte[] data = dataStore.get(identifier);
      if (data != null) {
        result.put(identifier, data);
      }
    }
    return result;
  }

  public void put(final Map<Long, byte[]> data) {
    put(this, data);
  }

  public static void put(final BinaryDataStore dataStore, final Map<Long, byte[]> data) {
    for (Map.Entry<Long, byte[]> pair : data.entrySet()) {
      dataStore.put(pair.getKey(), pair.getValue());
    }
  }

}
