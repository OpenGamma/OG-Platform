/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

/**
 * Provides key comparison methods for any implementation of
 * {@link AnalyticValueDefinition}.
 *
 * @author kirk
 */
public class AnalyticValueDefinitionComparator {
  
  // TODO kirk 2009-09-16 -- If we decide to have ordering, make this actually
  // implement Comparator<AnalyticValueDefinition>.

  public static <T> int hashCode(AnalyticValueDefinition<?> definition) {
    final int prime = 31;
    int result = 1;
    for(String key : definition.getKeys()) {
      result = prime * result + key.hashCode(); 
      for(Object value : definition.getValues(key)) {
        result = prime * result + value.hashCode();
      }
    }
    return result;
  }
  
  public static boolean equals(AnalyticValueDefinition<?> d1, AnalyticValueDefinition<?> d2) {
    Set<String> key1 = d1.getKeys();
    Set<String> key2 = d2.getKeys();
    if(!ObjectUtils.equals(key1, key2)) {
      return false;
    }
    for(String key : key1) {
      Set<Object> values1 = d1.getValues(key);
      Set<Object> values2 = d2.getValues(key);
      if(!ObjectUtils.equals(values1, values2)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Determine whether the actual key/value pairs are sufficient to satisfy the required
   * specification.
   *  
   * @param required The required key/value pairs
   * @param actual   The actual key/value pairs
   * @return {@code true} iff the actual is a strict superset of the required key/value pairs.
   */
  public static boolean matches(AnalyticValueDefinition<?> required, AnalyticValueDefinition<?> actual) {
    Set<String> requiredKeys = required.getKeys();
    Set<String> actualKeys = actual.getKeys();
    if(!actualKeys.containsAll(requiredKeys)) {
      return false;
    }
    for(String key : requiredKeys) {
      Set<Object> requiredValues = required.getValues(key);
      Set<Object> actualValues = actual.getValues(key);
      if(!ObjectUtils.equals(requiredValues, actualValues)) {
        return false;
      }
    }
    return true;
  }

}
