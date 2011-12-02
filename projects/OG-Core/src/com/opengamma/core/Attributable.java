/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.util.Map;

/**
 * Class implementing Attributable interface hold collection of String attributes.
 */
public interface Attributable {

    /**
   * Gets the general purpose attributes.
   * @return the value of the property
   */
  Map<String, String> getAttributes();

  /**
   * Sets the general purpose attributes.
   * @param attributes  the new value of the property
   */
  void setAttributes(Map<String, String> attributes);

  /**
   * Adds a key value pair to attributes
   *
   * @param key  the key to add, not null
   * @param value  the value to add, not null
   */
  void addAttribute(String key, String value);
}
