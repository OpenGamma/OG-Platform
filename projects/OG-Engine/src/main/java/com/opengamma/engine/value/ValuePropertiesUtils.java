/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.util.Collections;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * Utilities classes for {@link ValueProperties}
 */
public class ValuePropertiesUtils {

  /**
   * Returns a copy of the original properties with the given property added and set to optional. If the new property is already present 
   * in the original and the property value is not equal to that in the original, throws an exception.
   * @param originalProperties The original properties, not null
   * @param propertyName The property name to add, not null
   * @param propertyValue The property value, not null
   * @return The new properties
   */
  public static ValueProperties.Builder addOptional(final ValueProperties originalProperties, final String propertyName, final String propertyValue) {
    ArgumentChecker.notNull(originalProperties, "original properties");
    ArgumentChecker.notNull(propertyName, "property name");
    ArgumentChecker.notNull(propertyValue, "property value");
    final Set<String> originalPropertyValues = originalProperties.getValues(propertyName);
    if (originalPropertyValues != null && !originalPropertyValues.equals(Collections.singleton(propertyValue))) {
      throw new IllegalStateException("Property " + propertyName + " already present, but value " + 
          propertyValue + " not equal to " + originalProperties.getValues(propertyName));
    }
    final ValueProperties.Builder newProperties = originalProperties.copy()
        .with(propertyName, propertyValue)
        .withOptional(propertyName);
    return newProperties;
  }
  
  /**
   * Returns a copy of the original properties with all new properties added and set to optional. If the properties to add input
   * is null or empty, returns an unchanged copy of the original. If the new properties contain a value for a property that is 
   * already present in the original and the property value is not equal to that in the original, throws an exception.
   * @param originalProperties The original properties, not null
   * @param propertiesToAdd The properties to add
   * @return The new properties
   * @throws IllegalStateException If the properties to add contains a value for a property name that is present in the original
   * properties but that is not equal to its value(s)
   */
  public static ValueProperties.Builder addAllOptional(final ValueProperties originalProperties, final ValueProperties propertiesToAdd) {
    ArgumentChecker.notNull(originalProperties, "original properties");
    if (propertiesToAdd == null || propertiesToAdd.isEmpty()) {
      return originalProperties.copy();
    }
    final ValueProperties.Builder newProperties = originalProperties.copy();
    for (final String propertyName : propertiesToAdd.getProperties()) {
      final Set<String> propertyValue = propertiesToAdd.getValues(propertyName);
      if (originalProperties.getValues(propertyName) != null && !propertyValue.equals(originalProperties.getValues(propertyName))) {
        throw new IllegalStateException("Property " + propertyName + " already present, but value " + 
            propertyValue + " not equal to " + originalProperties.getValues(propertyName));
      }
      if (propertyValue == null || propertyValue.isEmpty()) {
        newProperties.withAny(propertyName).withOptional(propertyName);
      } else {
        newProperties.with(propertyName, propertyValue).withOptional(propertyName);
      }
    }
    return newProperties;
  }
  
  /**
   * Adds all new properties to the original properties and sets them to optional. If the properties to add input
   * is null or empty, the original properties are unchanged. If the new properties contain a value for a property that is 
   * already present in the original and the property value is not equal to that in the original, throws an exception.
   * @param properties The original properties, not null
   * @param propertiesToAdd The properties to add
   * @throws IllegalStateException If the properties to add contains a value for a property name that is present in the original
   * properties but that is not equal to its value(s)
   */
  public static void withAllOptional(final ValueProperties.Builder properties, final ValueProperties propertiesToAdd) {
    ArgumentChecker.notNull(properties, "properties");
    if (propertiesToAdd == null || propertiesToAdd.isEmpty()) {
      return;
    }
    final ValueProperties originalProperties = properties.get();
    for (final String propertyName : propertiesToAdd.getProperties()) {
      final Set<String> propertyValue = propertiesToAdd.getValues(propertyName);
      if (originalProperties.getValues(propertyName) != null && !propertyValue.equals(originalProperties.getValues(propertyName))) {
        throw new IllegalStateException("Property " + propertyName + " already present, but value " + 
          propertyValue + " not equal to " + originalProperties.getValues(propertyName));
      }
      if (propertyValue == null || propertyValue.isEmpty()) {
        properties.withAny(propertyName).withOptional(propertyName);
      } else {
        properties.with(propertyName, propertyValue).withOptional(propertyName);
      }
    }
    return;
  }
  
  /**
   * Returns a copy of the original properties with all of the given properties removed if present.
   * @param originalProperties The original properties, not null
   * @param propertiesToRemove The properties to remove
   * @return A copy of the original properties with all given properties removed
   */
  public static ValueProperties.Builder removeAll(final ValueProperties originalProperties, final String... propertiesToRemove) {
    ArgumentChecker.notNull(originalProperties, "original properties");
    final ValueProperties.Builder newProperties = originalProperties.copy();
    if (propertiesToRemove == null || propertiesToRemove.length == 0) {
      return newProperties;
    }
    for (final String propertyName : propertiesToRemove) {
      newProperties.withoutAny(propertyName);
    }
    return newProperties;
  }
  
  /**
   * Returns properties that contain all of the properties that were set to optional in the original properties.
   * @param originalProperties The original properties, not null
   * @return Properties that contain only those values that were set to optional in the original properties
   */
  public static ValueProperties.Builder getAllOptional(final ValueProperties originalProperties) {
    ArgumentChecker.notNull(originalProperties, "original properties");
    final ValueProperties.Builder optionalProperties = ValueProperties.builder();
    for (String propertyName : originalProperties.getProperties()) {
      if (originalProperties.isOptional(propertyName)) {
        final Set<String> propertyValues = originalProperties.getValues(propertyName);
        if (propertyValues == null || propertyValues.isEmpty()) {
          optionalProperties.withAny(propertyName).withOptional(propertyName);
        } else {
          optionalProperties.with(propertyName, propertyValues).withOptional(propertyName);
        }
      }
    }
    return optionalProperties;
  }
}
