/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * Utility methods for working with enums.
 * <p>
 * This is a thread-safe static utility class.
 */
public final class EnumUtils {

  /**
   * Restricted constructor.
   */
  private EnumUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Safely returns the enum instance for the specified name.
   * <p>
   * This operates as per {@link Enum#valueOf(Class, String)} but returns null
   * if the name or class cannot be found.
   * 
   * @param <T> the enum type
   * @param enumType  the enum class, null returns null
   * @param name  the enum name, null returns null
   * @return the corresponding enum, null if not found
   */
  public static <T extends Enum<T>> T safeValueOf(Class<T> enumType, String name) {
    if (enumType == null || name == null) {
      return null;
    }
    try {
      return Enum.valueOf(enumType, name.trim());
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

}
