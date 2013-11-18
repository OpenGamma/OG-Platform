/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * How the shift should be applied.
 */
public enum CurveShiftType implements GroovyAliasable {

  /**
   * Relative shift.
   */
  RELATIVE("Relative"), 
  
  /**
   * Absolute shift.
   */
  ABSOLUTE("Absolute");
  
  private static final ImmutableList<String> s_aliases;
  static {
    List<String> result = newArrayList();
    for (GroovyAliasable value : values()) {
      result.add(value.getGroovyAlias());
    }
    Collections.sort(result);
    s_aliases = ImmutableList.copyOf(result);
    
  }
  
  private final String _groovyAlias;
  
  private CurveShiftType(String groovyAlias) {
    _groovyAlias = groovyAlias;
  }

  /**
   * The alias to use in the groovy script
   * @return the alias
   */
  @Override
  public String getGroovyAlias() {
    return _groovyAlias;
  }
  
  /**
   * The list of available groovy aliases, sorted.
   * @return list of aliases.
   */
  public static ImmutableList<String> getAliasList() {
    return s_aliases;
  }

  
}
