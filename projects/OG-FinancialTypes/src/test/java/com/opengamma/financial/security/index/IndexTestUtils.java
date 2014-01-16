/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.index;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class IndexTestUtils {

  /**
   * Gets all of the fields (excluding those from Object) of a class, including
   * private and inherited.
   * @param clazz The class
   * @return A list of fields
   */
  public static List<Field> getFields(final Class<?> clazz) {
    final List<Field> found = new ArrayList<>();
    Class<?> currentClass = clazz;
    while (currentClass != null && currentClass != Object.class) {
      for (final Field field : currentClass.getDeclaredFields()) {
        if (!field.isSynthetic()) {
          found.add(field);
        }
      }
      currentClass = currentClass.getSuperclass();
    }
    return found;
  }
}
