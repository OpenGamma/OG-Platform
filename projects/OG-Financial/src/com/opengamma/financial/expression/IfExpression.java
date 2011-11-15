/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression;

/**
 * Representation of an "if" expression. Returns the evaluation of the "result" if the
 * condition evaluates to true. Otherwise returns NA.
 */
public class IfExpression extends UserExpression {

  private final UserExpression _condition;
  private final UserExpression _result;

  public IfExpression(final UserExpression condition, final UserExpression result) {
    _condition = condition;
    _result = result;
  }

  @Override
  protected Object evaluate(final Evaluator evaluator) {
    final Object condition = _condition.evaluate(evaluator);
    if (Boolean.TRUE.equals(condition)) {
      return _result.evaluate(evaluator);
    } else {
      return NA;
    }
  }

}
