/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

/**
 * Wrapper for a hash map implementation that exposes the underlying entry so that the actual key value can be used.
 */
/* package */final class MapEx<K, V> extends HashedMap {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unchecked")
  public Map.Entry<K, V> getHashEntry(final K key) {
    return getEntry(key);
  }

}
