/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * Utility methods for working with enums.
 */
public final class EnumUtils {
  
  /**
   * Restricted constructor.
   */
  private EnumUtils() {
  }

  /**
   * A method that returns enum type from its value
   * 
   * @param <T> Enum type
   * @param enumType the enum class, not null
   * @param name the enum name
   * @return corresponding enum, or null
   */
  public static <T extends Enum<T>> T getEnumFromString(Class<T> enumType, String name) {
    T result = null;
    if (enumType != null && name != null) {
      try {
        result = Enum.valueOf(enumType, name.trim());
      } catch (IllegalArgumentException ex) {
        //do nothing
      }
    }
    return result;
  }

}
