/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering.expression;

/**
 * Visitor for a {@link UserExpression} node.
 * 
 * @param <V> return type of the visitor
 * @param <D> data type
 */
public interface UserExpressionVisitor<V, D> {

  V visitAnd(UserExpression left, UserExpression right, D data);

  V visitEq(UserExpression left, UserExpression right, D data);

  V visitGt(UserExpression left, UserExpression right, D data);

  V visitGte(UserExpression left, UserExpression right, D data);

  V visitIdentifier(String identifier, D data);

  V visitLiteral(Object value, D data);

  V visitLt(UserExpression left, UserExpression right, D data);

  V visitLte(UserExpression left, UserExpression right, D data);

  V visitNeq(UserExpression left, UserExpression right, D data);

  V visitNot(UserExpression expr, D data);

  V visitOr(UserExpression left, UserExpression right, D data);

}
