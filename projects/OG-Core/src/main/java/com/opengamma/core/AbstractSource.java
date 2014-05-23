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
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * A partial implementation of {@link Source}
 * 
 * @param <V> the type returned by the source
 */
public abstract class AbstractSource<V>
    implements Source<V> {

  /**
   * Bulk helper method that loops around the input collection calling the single search method serially.
   * 
   * @param <V>  the result type
   * @param source  the source to search on, not null
   * @param uniqueIds  the unique identifiers, not null
   * @return the map of results, not null
   */
  public static <V> Map<UniqueId, V> get(Source<V> source, Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, V> result = newHashMap();
    for (final UniqueId uniqueId : uniqueIds) {
      try {
        final V object = source.get(uniqueId);
        result.put(uniqueId, object);
      } catch (final DataNotFoundException ex) {
        // ignore objects that are not found
      }
    }
    return result;
  }

  /**
   * Bulk helper method that loops around the input collection calling the single search method serially.
   * 
   * @param <V>  the result type
   * @param source  the source to search on, not null
   * @param objectIds  the object identifiers, not null
   * @param versionCorrection  the version-correction, not null
   * @return the map of results, not null
   */
  public static <V> Map<ObjectId, V> get(final Source<V> source, final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    final Map<ObjectId, V> result = newHashMap();
    for (final ObjectId objectId : objectIds) {
      try {
        final V object = source.get(objectId, versionCorrection);
        result.put(objectId, object);
      } catch (final DataNotFoundException ex) {
        // ignore objects that are not found
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<UniqueId, V> get(Collection<UniqueId> uniqueIds) {
    return get(this, uniqueIds);
  }

  @Override
  public Map<ObjectId, V> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
    return get(this, objectIds, versionCorrection);
  }

}
