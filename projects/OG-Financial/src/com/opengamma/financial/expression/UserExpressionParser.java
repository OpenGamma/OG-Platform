/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression;


/**
 * Parses a string representation of a user expression into a {@link UserExpression}
 * object that can be evaluated.
 */
public abstract class UserExpressionParser {

  protected UserExpressionParser() {
  }

  // TODO: functions/macros ?

  public abstract void setConstant(String name, Object value);

  public abstract UserExpression parse(String source);

}
