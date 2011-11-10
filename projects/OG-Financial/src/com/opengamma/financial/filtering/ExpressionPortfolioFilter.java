/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering;

import com.opengamma.core.position.Position;
import com.opengamma.financial.expression.UserExpression;
import com.opengamma.financial.expression.UserExpression.Evaluator;
import com.opengamma.financial.expression.deprecated.ExpressionParser;

/**
 * Filters a portfolio according to a provided {@link UserExpression}
 */
public class ExpressionPortfolioFilter extends AbstractFilteringFunction {

  private final UserExpression _expression;

  /**
   * Creates a new filter from a string expression (in the Expr.g form)
   * 
   * @param expression string expression
   * @deprecated Use the alternative constructor so that the parsing dialect is explicit
   */
  @Deprecated
  public ExpressionPortfolioFilter(final String expression) {
    this(new ExpressionParser().parse(expression));
  }

  /**
   * Creates a new filter from an arbitrary user expression
   * 
   * @param expression the parsed user expression
   */
  public ExpressionPortfolioFilter(final UserExpression expression) {
    super("User expression");
    _expression = expression;
  }

  private UserExpression getExpression() {
    return _expression;
  }

  @Override
  public boolean acceptPosition(final Position position) {
    final Evaluator eval = getExpression().evaluator();
    eval.setVariable("position", position);
    return Boolean.TRUE.equals(eval.evaluate());
  }

}
