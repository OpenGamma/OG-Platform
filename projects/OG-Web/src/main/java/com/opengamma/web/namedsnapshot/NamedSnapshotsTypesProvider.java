/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.namedsnapshot;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.AnnotationReflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.NamedSnapshot;

/**
 * Provides all supported snapshot types
 */
public final class NamedSnapshotsTypesProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(NamedSnapshotsTypesProvider.class);
  /**
   * Singleton instance.
   */
  private static final NamedSnapshotsTypesProvider s_instance = new NamedSnapshotsTypesProvider();

  /**
   * Map of snapshot types.
   */
  private final ImmutableSortedMap<String, Class<? extends NamedSnapshot>> _snapshotTypeMap;
  /**
   * Map of snapshot descriptions.
   */
  private final ImmutableSortedMap<String, String> _snapshotDescriptionMap;

  //-------------------------------------------------------------------------
  /**
   * Gets the singleton instance.
   * 
   * @return the provider, not null
   */
  public static NamedSnapshotsTypesProvider getInstance() {
    return s_instance;
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor
   */
  private NamedSnapshotsTypesProvider() {
    Map<String, Class<? extends NamedSnapshot>> result = Maps.newHashMap();
    ImmutableSortedMap.Builder<String, String> descriptions = ImmutableSortedMap.naturalOrder();
    AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
    Set<Class<? extends NamedSnapshot>> snapshotClasses = reflector.getReflector().getSubTypesOf(NamedSnapshot.class);
    for (Class<? extends NamedSnapshot> snapshotClass : snapshotClasses) {
      // find type
      if (Modifier.isAbstract(snapshotClass.getModifiers())) {
        continue;
      }
      // store
      String typeName = snapshotClass.getSimpleName();
      Class<?> old = result.put(typeName, snapshotClass);
      if (old != null) {
        s_logger.warn("Two classes exist with the same name: " + snapshotClass.getSimpleName());
      }
      //TODO what is this for?
      descriptions.put(typeName, toDescription(typeName));
    }
    _snapshotTypeMap = ImmutableSortedMap.copyOf(result);
    _snapshotDescriptionMap = descriptions.build();
  }

  /**
   * @param typeName
   * @return
   */
  private String toDescription(String typeName) {
    typeName = typeName.replaceAll("([A-Z])", " $1");
    return typeName.replaceAll("Snapshot", "");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the set of snapshot keys.
   * 
   * @return the types, not null
   */
  public ImmutableSortedSet<String> getTypeSet() {
    return ImmutableSortedSet.copyOf(_snapshotTypeMap.keySet());
  }

  /**
   * Gets the map of snapshot types by short key.
   * 
   * @return the map, not null
   */
  public ImmutableSortedMap<String, Class<? extends NamedSnapshot>> getTypeMap() {
    return _snapshotTypeMap;
  }

  /**
   * Gets the map of snapshot descriptions by short key.
   * 
   * @return the map, not null
   */
  public ImmutableSortedMap<String, String> getDescriptionMap() {
    return _snapshotDescriptionMap;
  }

  /**
   * Gets the description for a type.
   * 
   * @param clazz  the snapshot class, not null
   * @return the description, not null
   */
  public String getDescription(Class<?> clazz) {
    String key = HashBiMap.create(_snapshotTypeMap).inverse().get(clazz);
    String description = null;
    if (key != null) {
      description = _snapshotDescriptionMap.get(key);
    }
    return (description != null ? description : clazz.getSimpleName());
  }

}
