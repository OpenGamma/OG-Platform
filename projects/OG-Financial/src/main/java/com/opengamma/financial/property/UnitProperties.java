/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import com.opengamma.engine.value.ValuePropertyNames;

/**
 * Defines property names that correspond to "units" of the value they are describing. Some operations may only be valid if the units of the operands match or are otherwise compatible.
 */
public final class UnitProperties {

  /**
   * Prevents instantiation.
   */
  private UnitProperties() {
  }

  // Standard unit names from ValuePropertyNames

  //CSOFF

  public static final String CURRENCY = ValuePropertyNames.CURRENCY;

  // OpenGamma specific unit names

  // TODO: ...

  //CSON

  /**
   * Returns all property names that correspond to units.
   * 
   * @return an array of all property names
   */
  public static String[] unitPropertyNames() {
    return new String[] {CURRENCY };
  }

}
