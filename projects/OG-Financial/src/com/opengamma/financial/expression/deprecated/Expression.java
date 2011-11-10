/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression.deprecated;

import java.math.BigDecimal;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.financial.expression.NavigablePortfolioNode;
import com.opengamma.financial.expression.UserExpression;

/**
 * AST representation of a user expression.
 */
/* package */abstract class Expression extends UserExpression {

  /* package */Expression() {
  }

  @SuppressWarnings("unchecked")
  protected <T> T evaluate(final Class<T> expected, final Expression expr, final Evaluator evaluator) {
    Object value = expr.evaluate(evaluator);
    if ((value == NA) || (value == null)) {
      return null;
    }
    if (!expected.isAssignableFrom(value.getClass())) {
      value = coerceByClass(expected, value);
      if (!expected.isAssignableFrom(value.getClass())) {
        throw new ClassCastException("Expected value of type " + expected + " from " + expr + "; got " + value);
      }
    }
    return (T) value;
  }

  /**
   * Coerce a source value to a matching type for the target value.
   * 
   * @param targetValue value of the type to coerce to
   * @param sourceValue original value
   * @return the coerced value or the original value if no coercion is possible
   */
  protected Object coerce(final Object targetValue, final Object sourceValue) {
    return coerceByClass(targetValue.getClass(), sourceValue);
  }

  /**
   * Coerce a source value to a specific class.
   * 
   * @param targetClass class to coerce to
   * @param sourceValue original value
   * @return the coerced value or the original value if no coercion is possible
   */
  protected Object coerceByClass(final Class<?> targetClass, final Object sourceValue) {
    if (targetClass == String.class) {
      return sourceValue.toString();
    } else if (targetClass == Integer.class) {
      if (sourceValue instanceof String) {
        try {
          return Integer.parseInt((String) sourceValue);
        } catch (NumberFormatException e) {
          // Ignore
        }
      } else if (sourceValue instanceof Number) {
        return ((Number) sourceValue).intValue();
      }
    } else if (targetClass == Double.class) {
      if (sourceValue instanceof String) {
        try {
          return Double.parseDouble((String) sourceValue);
        } catch (NumberFormatException e) {
          // Ignore
        }
      } else if (sourceValue instanceof Number) {
        return ((Number) sourceValue).doubleValue();
      }
    } else if (targetClass == BigDecimal.class) {
      if (sourceValue instanceof String) {
        return BigDecimal.valueOf(Double.parseDouble((String) sourceValue)); //TODO parse properly
      } else if (sourceValue instanceof Integer || sourceValue instanceof Long) {
        return BigDecimal.valueOf(((Number) sourceValue).longValue());
      } else if (sourceValue instanceof Double || sourceValue instanceof Float) {
        return BigDecimal.valueOf(((Number) sourceValue).doubleValue());
      }
    }
    return sourceValue;
  }

  private abstract static class Binary extends Expression {

    private final Expression _left;
    private final Expression _right;

    private Binary(final Expression left, final Expression right) {
      _left = left;
      _right = right;
    }

    protected Expression getLeft() {
      return _left;
    }

    protected Expression getRight() {
      return _right;
    }

    protected String toString(final String op) {
      return "`" + op + " (" + getLeft() + ") (" + getRight() + ")";
    }

  }

  private abstract static class Unary extends Expression {

    private final Expression _expr;

    private Unary(final Expression expr) {
      _expr = expr;
    }

    protected Expression getExpr() {
      return _expr;
    }

    protected String toString(final String op) {
      return "`" + op + " (" + getExpr() + ")";
    }

  }

  /**
   * 
   */
  public static final class And extends Binary {

    /* package */And(final Expression left, final Expression right) {
      super(left, right);
    }

    @Override
    protected Object evaluate(final Evaluator evaluator) {
      final Boolean leftValue = evaluate(Boolean.class, getLeft(), evaluator);
      if ((leftValue == null) || !leftValue.booleanValue()) {
        return false;
      }
      final Boolean rightValue = evaluate(Boolean.class, getRight(), evaluator);
      if (rightValue == null) {
        return false;
      }
      return rightValue;
    }

    @Override
    public String toString() {
      return toString("AND");
    }

  }

  /**
   * 
   */
  public static final class Eq extends Binary {

    /* package */Eq(final Expression left, final Expression right) {
      super(left, right);
    }

    @Override
    protected Object evaluate(final Evaluator evaluator) {
      final Object leftValue = getLeft().evaluate(evaluator);
      if (leftValue == null) {
        return false;
      }
      final Object rightValue = coerce(leftValue, getRight().evaluate(evaluator));
      if (rightValue == null) {
        return false;
      }
      return leftValue.equals(rightValue);
    }

    @Override
    public String toString() {
      return toString("EQ");
    }

  }

  /**
   * 
   */
  public static final class Gt extends Binary {

    /* package */Gt(final Expression left, final Expression right) {
      super(left, right);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object evaluate(final Evaluator evaluator) {
      final Comparable leftValue = evaluate(Comparable.class, getLeft(), evaluator);
      if (leftValue == null) {
        return false;
      }
      final Object rightValue = coerce(leftValue, getRight().evaluate(evaluator));
      if (rightValue == null) {
        return false;
      }
      return leftValue.compareTo(rightValue) > 0;
    }

    @Override
    public String toString() {
      return toString("GT");
    }

  }

  /**
   * 
   */
  public static final class Gte extends Binary {

    /* package */Gte(final Expression left, final Expression right) {
      super(left, right);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object evaluate(final Evaluator evaluator) {
      final Comparable leftValue = evaluate(Comparable.class, getLeft(), evaluator);
      if (leftValue == null) {
        return false;
      }
      final Object rightValue = coerce(leftValue, getRight().evaluate(evaluator));
      if (rightValue == null) {
        return false;
      }
      return leftValue.compareTo(rightValue) >= 0;
    }

    @Override
    public String toString() {
      return toString("GTE");
    }

  }

  /**
   * 
   */
  public static final class Identifier extends Expression {

    private final String _text;

    /* package */Identifier(final String text) {
      _text = text;
    }

    private static Object evaluateSecurityIdentifier(final String identifier, final Security security) {
      if (security == null) {
        return null;
      }
      if ("Name".equals(identifier)) {
        return security.getName();
      } else if ("Type".equals(identifier)) {
        return security.getSecurityType();
      }
      return null;
    }

    private static Object evaluatePositionAttribute(final String attribute, final Position position) {
      return position.getAttributes().get(attribute);
    }

    private static Object evaluatePositionIdentifier(final String identifier, final Position position) {
      if (identifier.startsWith("Attribute.")) {
        return evaluatePositionAttribute(identifier.substring(10), position);
      } else if ("Quantity".equals(identifier)) {
        return position.getQuantity();
      }
      return evaluatePositionAttribute(identifier, position);
    }

    private static Object evaluateTradeAttribute(final String attribute, final Position position) {
      for (Trade trade : position.getTrades()) {
        final String value = trade.getAttributes().get(attribute);
        if (value != null) {
          return value;
        }
      }
      return null;
    }

    private static Object evaluateTradeIdentifier(final String identifier, final Position position) {
      if (identifier.startsWith("Attribute.")) {
        return evaluateTradeAttribute(identifier.substring(10), position);
      } else if ("Counterparty".equals(identifier)) {
        for (Trade trade : position.getTrades()) {
          if (trade.getCounterparty() != null) {
            return trade.getCounterparty();
          }
        }
        return null;
      } else if ("Premium".equals(identifier)) {
        for (Trade trade : position.getTrades()) {
          if (trade.getPremium() != null) {
            return trade.getPremium();
          }
        }
        return null;
      } else if ("PremiumCurrency".equals(identifier)) {
        for (Trade trade : position.getTrades()) {
          if (trade.getPremiumCurrency() != null) {
            return trade.getPremiumCurrency();
          }
        }
        return null;
      } else if ("PremiumDate".equals(identifier)) {
        for (Trade trade : position.getTrades()) {
          if (trade.getPremiumDate() != null) {
            return trade.getPremiumDate();
          }
        }
        return null;
      } else if ("PremiumTime".equals(identifier)) {
        for (Trade trade : position.getTrades()) {
          if (trade.getPremiumTime() != null) {
            return trade.getPremiumTime();
          }
        }
        return null;
      }
      return evaluateTradeAttribute(identifier, position);
    }

    private static Object evaluateNodeIdentifier(final String identifier, final NavigablePortfolioNode node) {
      if ("Depth".equals(identifier)) {
        return node.getDepth();
      } else if ("Name".equals(identifier)) {
        return node.getName();
      } else if (identifier.startsWith("Parent.")) {
        final NavigablePortfolioNode parent = node.getNavigableParentNode();
        if (parent != null) {
          return evaluateNodeIdentifier(identifier.substring(7), parent);
        } else {
          return null;
        }
      } else if ("Positions".equals(identifier)) {
        return node.getPositions().size();
      } else if ("Nodes".equals(identifier)) {
        return node.getChildNodes().size();
      } else {
        return null;
      }
    }

    private static Object evaluatePositionOrTradeIdentifier(final String identifier, final Position position) {
      Object value;
      if (identifier.startsWith("Position.")) {
        return evaluatePositionIdentifier(identifier.substring(9), position);
      } else if (identifier.startsWith("Security.")) {
        return evaluateSecurityIdentifier(identifier.substring(9), position.getSecurity());
      } else if (identifier.startsWith("Trade.")) {
        return evaluateTradeIdentifier(identifier.substring(6), position);
      }
      value = evaluatePositionIdentifier(identifier, position);
      if (value != null) {
        return value;
      }
      value = evaluateSecurityIdentifier(identifier, position.getSecurity());
      if (value != null) {
        return value;
      }
      return evaluateTradeIdentifier(identifier, position);
    }

    private static Object nullForNA(final Object v) {
      if (v == NA) {
        return null;
      } else {
        return v;
      }
    }

    @Override
    protected Object evaluate(final Evaluator evaluator) {
      Position position = (Position) nullForNA(evaluator.getVariable("position"));
      NavigablePortfolioNode node = (NavigablePortfolioNode) nullForNA(evaluator.getVariable("node"));
      if ("isNode".equals(_text)) {
        return (position == null);
      } else if ("isPosition".equals(_text)) {
        return (position != null);
      } else if ((node != null) && _text.startsWith("Node.")) {
        return evaluateNodeIdentifier(_text.substring(5), node);
      } else if (position != null) {
        return evaluatePositionOrTradeIdentifier(_text, position);
      } else {
        return evaluateNodeIdentifier(_text, node);
      }
    }

    @Override
    public String toString() {
      return "`IDENTIFIER " + _text;
    }

  }

  /**
   * 
   */
  public static final class Literal extends Expression {

    private final Object _value;

    /* package */Literal(final Object value) {
      _value = value;
    }

    @Override
    protected Object evaluate(final Evaluator evaluator) {
      return _value;
    }

    @Override
    public String toString() {
      return "`LITERAL " + _value;
    }

  }

  /**
   * 
   */
  public static final class Lt extends Binary {

    /* package */Lt(final Expression left, final Expression right) {
      super(left, right);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object evaluate(final Evaluator evaluator) {
      final Comparable leftValue = evaluate(Comparable.class, getLeft(), evaluator);
      if (leftValue == null) {
        return false;
      }
      final Object rightValue = coerce(leftValue, getRight().evaluate(evaluator));
      if (rightValue == null) {
        return false;
      }
      return leftValue.compareTo(rightValue) < 0;
    }

    @Override
    public String toString() {
      return toString("LT");
    }

  }

  /**
   * 
   */
  public static final class Lte extends Binary {

    /* package */Lte(final Expression left, final Expression right) {
      super(left, right);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object evaluate(final Evaluator evaluator) {
      final Comparable leftValue = evaluate(Comparable.class, getLeft(), evaluator);
      if (leftValue == null) {
        return false;
      }
      final Object rightValue = coerce(leftValue, getRight().evaluate(evaluator));
      if (rightValue == null) {
        return false;
      }
      return leftValue.compareTo(rightValue) <= 0;
    }

    @Override
    public String toString() {
      return toString("LTE");
    }

  }

  /**
   * 
   */
  public static final class Neq extends Binary {

    /* package */Neq(final Expression left, final Expression right) {
      super(left, right);
    }

    @Override
    protected Object evaluate(final Evaluator evaluator) {
      final Object leftValue = getLeft().evaluate(evaluator);
      if (leftValue == null) {
        return true;
      }
      final Object rightValue = coerce(leftValue, getRight().evaluate(evaluator));
      if (rightValue == null) {
        return true;
      }
      return !leftValue.equals(rightValue);
    }

    @Override
    public String toString() {
      return toString("NEQ");
    }

  }

  /**
   * 
   */
  public static final class Not extends Unary {

    /* package */Not(final Expression expr) {
      super(expr);
    }

    @Override
    protected Object evaluate(final Evaluator evaluator) {
      final Boolean exprValue = evaluate(Boolean.class, getExpr(), evaluator);
      if (exprValue == null) {
        return null;
      }
      return !exprValue;
    }

    @Override
    public String toString() {
      return toString("NOT");
    }

  }

  /**
   * 
   */
  public static final class Or extends Binary {

    /* package */Or(final Expression left, final Expression right) {
      super(left, right);
    }

    @Override
    protected Object evaluate(Evaluator evaluator) {
      final Boolean leftValue = evaluate(Boolean.class, getLeft(), evaluator);
      if ((leftValue != null) && leftValue.booleanValue()) {
        return true;
      }
      final Boolean rightValue = evaluate(Boolean.class, getRight(), evaluator);
      if (rightValue == null) {
        return false;
      }
      return rightValue.booleanValue();
    }

    @Override
    public String toString() {
      return toString("OR");
    }

  }

}
