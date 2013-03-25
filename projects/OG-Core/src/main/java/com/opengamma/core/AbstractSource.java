/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;

/**
 * A partial implementation of {@link Source}
 * 
 * @param <V> the type returned by the source
 */
public abstract class AbstractSource<V> implements Source<V> {

  public static <V> Map<UniqueId, V> get(Source<V> source, Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, V> result = newHashMap();
    for (final UniqueId uniqueId : uniqueIds) {
      try {
        final V object = source.get(uniqueId);
        result.put(uniqueId, object);
      } catch (final DataNotFoundException ex) {
        // do nothing
      }
    }
    return result;
  }

  // Source

  @Override
  public Map<UniqueId, V> get(Collection<UniqueId> uniqueIds) {
    return get(this, uniqueIds);
  }

}
