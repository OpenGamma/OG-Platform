/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.convention;

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
import com.opengamma.core.convention.ConventionType;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.ClassUtils;

/**
 * Provides all supported convention types
 */
public final class ConventionTypesProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ConventionTypesProvider.class);
  /**
   * Singleton instance.
   */
  private static final ConventionTypesProvider s_instance = new ConventionTypesProvider();

  /**
   * Map of config types.
   */
  private final ImmutableSortedMap<String, Class<? extends ManageableConvention>> _conventionTypeMap;
  /**
   * Map of config descriptions.
   */
  private final ImmutableSortedMap<String, String> _conventionDescriptionMap;

  //-------------------------------------------------------------------------
  /**
   * Gets the singleton instance.
   * 
   * @return the provider, not null
   */
  public static ConventionTypesProvider getInstance() {
    return s_instance;
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor
   */
  private ConventionTypesProvider() {
    Map<String, Class<? extends ManageableConvention>> result = Maps.newHashMap();
    ImmutableSortedMap.Builder<String, String> descriptions = ImmutableSortedMap.naturalOrder();
    AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
    Set<Class<? extends ManageableConvention>> conventionClasses = reflector.getReflector().getSubTypesOf(ManageableConvention.class);
    for (Class<? extends ManageableConvention> conventionClass : conventionClasses) {
      // ensure this class is fully loaded, to force static initialization
      ClassUtils.initClass(conventionClass);
      // find type
      if (Modifier.isAbstract(conventionClass.getModifiers())) {
        continue;
      }
      ConventionType type;
      try {
        type = (ConventionType) conventionClass.getDeclaredField("TYPE").get(null);
      } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
        s_logger.warn("Convention class must declare a static variable 'TYPE' but none found: " + conventionClass.getName());
        continue;
      }
      // extract description
      String description = type.getName().replaceAll(
          String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
      // store
      Class<?> old = result.put(type.getName(), conventionClass);
      if (old != null) {
        s_logger.warn("Two classes exist with the same name: " + conventionClass.getSimpleName());
      }
      descriptions.put(type.getName(), description);
    }
    _conventionTypeMap = ImmutableSortedMap.copyOf(result);
    _conventionDescriptionMap = descriptions.build();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the set of convention keys.
   * 
   * @return the types, not null
   */
  public ImmutableSortedSet<String> getTypeSet() {
    return ImmutableSortedSet.copyOf(_conventionTypeMap.keySet());
  }

  /**
   * Gets the map of convention types by short key.
   * 
   * @return the map, not null
   */
  public ImmutableSortedMap<String, Class<? extends ManageableConvention>> getTypeMap() {
    return _conventionTypeMap;
  }

  /**
   * Gets the map of convention descriptions by short key.
   * 
   * @return the map, not null
   */
  public ImmutableSortedMap<String, String> getDescriptionMap() {
    return _conventionDescriptionMap;
  }

  /**
   * Gets the description for a type.
   * 
   * @param clazz  the convention class, not null
   * @return the description, not null
   */
  public String getDescription(Class<?> clazz) {
    String key = HashBiMap.create(_conventionTypeMap).inverse().get(clazz);
    String description = null;
    if (key != null) {
      description = _conventionDescriptionMap.get(key);
    }
    return (description != null ? description : clazz.getSimpleName());
  }

}
