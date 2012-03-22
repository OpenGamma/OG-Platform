/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.hibernate.types.enums;

/**
 * Utility class designed to allow dynamic fiddling and manipulation of Enum 
 * instances which hold a string value.
 */
public interface StringValuedEnum {

  /**
   * Current string value stored in the enum.
   * 
   * @return string value.
   */
  String getValue();

}
