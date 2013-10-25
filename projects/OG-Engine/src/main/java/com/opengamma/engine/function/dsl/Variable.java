/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.dsl;

/**
 * DSL representation of a variable.
 */
public class Variable {

  /**
   * The name.
   */
  private final String _name;

  /**
   * Creates an instance.
   * 
   * @param name  the name
   */
  public Variable(String name) {
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return _name;
  }

}
