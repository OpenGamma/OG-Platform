/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

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

  public static int hashCode(AnalyticValueDefinition definition) {
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
  
  public static boolean equals(AnalyticValueDefinition d1, AnalyticValueDefinition d2) {
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

}
