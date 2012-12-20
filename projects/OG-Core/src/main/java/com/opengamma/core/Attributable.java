/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.util.Map;

/**
 * Provides a common way to access a set of attributes on an object.
 * <p>
 * Attributes provide the ability to access and manipulate a map of strings on an object.
 * They are typically used to store additional information or to tag the object in some way.
 */
public interface Attributable {

  /**
   * Gets the entire set of attributes.
   * <p>
   * Attributes are used to tag the object with additional information.
   * 
   * @return the complete set of attributes, not null
   */
  Map<String, String> getAttributes();

  /**
   * Sets the entire set of attributes.
   * <p>
   * Attributes are used to tag the object with additional information.
   * 
   * @param attributes  the new set of attributes, not null
   */
  void setAttributes(Map<String, String> attributes);

  /**
   * Adds a key-value pair to the set of attributes
   * <p>
   * Attributes are used to tag the object with additional information.
   * 
   * @param key  the key to add, not null
   * @param value  the value to add, not null
   */
  void addAttribute(String key, String value);

}
