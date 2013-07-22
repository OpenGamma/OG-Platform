/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.config;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.AnnotationReflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.opengamma.core.config.Config;

/**
 * Provides all supported configuration types
 */
public final class ConfigTypesProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ConfigTypesProvider.class);
  /**
   * Singleton instance.
   */
  private static final ConfigTypesProvider s_instance = new ConfigTypesProvider();

  /**
   * Map of config types.
   */
  private final Map<String, Class<?>> _configTypeMap;

  /**
   * Restricted constructor
   */
  private ConfigTypesProvider() {
    _configTypeMap = getConfigValue();
  }

  public static ConfigTypesProvider getInstance() {
    return s_instance;
  }

  private Map<String, Class<?>> getConfigValue() {
    Map<String, Class<?>> result = Maps.newHashMap();
    AnnotationReflector reflector = AnnotationReflector.getDefaultReflector();
    Set<Class<?>> configClasses = reflector.getReflector().getTypesAnnotatedWith(Config.class);
    for (Class<?> configClass : configClasses) {
      Annotation annotation = configClass.getAnnotation(Config.class);
      if (annotation instanceof Config) {
        Config configValueAnnotation = (Config) annotation;
        Class<?> configType = configValueAnnotation.searchType();
        if (configType == Object.class) {
          configType = configClass;
        }
        Class<?> old = result.put(configType.getSimpleName(), configType);
        if (old != null) {
          s_logger.warn("Two classes exist with the same name: " + configType.getSimpleName());
        }
      }
    }
    return ImmutableMap.copyOf(result);
  }

  public Set<String> getConfigTypes() {
    return ImmutableSortedSet.copyOf(_configTypeMap.keySet());
  }

  public Map<String, Class<?>> getConfigTypeMap() {
    return _configTypeMap;
  }

}
