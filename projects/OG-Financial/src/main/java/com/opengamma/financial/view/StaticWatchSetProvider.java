/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;

/**
 * A {@link WatchSetProvider} initialized from static data.
 */
public class StaticWatchSetProvider implements WatchSetProvider {

  private final Map<ObjectId, Collection<ObjectId>> _map;

  private static ObjectId toObjectId(final Object value) {
    if (value instanceof String) {
      return ObjectId.parse((String) value);
    } else if (value instanceof ObjectIdentifiable) {
      return ((ObjectIdentifiable) value).getObjectId();
    } else {
      throw new IllegalArgumentException("Not an ObjectId - " + value);
    }
  }

  /**
   * Creates a new provider. This is intended to be called from Spring configuration.
   * 
   * @param map the map of object identifiers (either strings or {@link ObjectId} instances) to watch identifiers (either strings, {@code ObjectId} instances, or collections of either)
   */
  @SuppressWarnings("rawtypes")
  public StaticWatchSetProvider(final Map map) {
    _map = Maps.newHashMapWithExpectedSize(map.size());
    for (Object entry : map.entrySet()) {
      final ObjectId key = toObjectId(((Map.Entry) entry).getKey());
      final Object value = ((Map.Entry) entry).getValue();
      if (value instanceof Collection) {
        final Collection valueCollection = (Collection) value;
        final Collection<ObjectId> values = new ArrayList<ObjectId>(valueCollection.size());
        for (Object valueEntry : valueCollection) {
          values.add(toObjectId(valueEntry));
        }
        _map.put(key, values);
      } else {
        _map.put(key, Collections.singleton(toObjectId(value)));
      }
    }
  }

  @Override
  public Set<ObjectId> getAdditionalWatchSet(final Set<ObjectId> watchSet) {
    final Set<ObjectId> result = new HashSet<ObjectId>();
    for (ObjectId watch : watchSet) {
      Collection<ObjectId> mapped = _map.get(watch);
      if (mapped != null) {
        result.addAll(mapped);
      }
    }
    return result;
  }

}
