/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.config.Config;
import com.opengamma.util.ArgumentChecker;

/**
 * Trivial wrapper class around a map of strings which exists to allow the map to be stored in the config database.
 */
@Config
public class Mappings {

  /** The mappings. */
  private final Map<String, String> _mappings;

  public Mappings(Map<String, String> mappings) {
    ArgumentChecker.notNull(mappings, "mappingValues");
    _mappings = ImmutableMap.copyOf(mappings);
  }

  /**
   * @return The underlying mappings
   */
  public Map<String, String> getMappings() {
    return _mappings;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return _mappings.equals(((Mappings) o)._mappings);

  }

  @Override
  public int hashCode() {
    return _mappings.hashCode();
  }
}
