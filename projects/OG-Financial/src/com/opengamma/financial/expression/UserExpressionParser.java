/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression;

import java.lang.reflect.Method;


/**
 * Parses a string representation of a user expression into a {@link UserExpression}
 * object that can be evaluated.
 */
public abstract class UserExpressionParser {

  protected UserExpressionParser() {
  }

  public abstract void setConstant(String name, Object value);

  /**
   * Registers a function. The function might appear as <name><object> (e.g. getSecurity)
   * or <object>:<name> (e.g. Security:get) depending on the parser.
   * 
   * @param object the object type being returned, e.g. Security
   * @param name the name of the operation, e.g. get
   * @param method the static method to invoke to evaluate this
   */
  public abstract void setFunction(String object, final String name, final Method method);

  public abstract UserExpression parse(String source);

}
