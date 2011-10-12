/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering.expression;

import java.math.BigDecimal;

/**
 * Implementation of a visitor to evaluate expressions. Only the basic constructs are handled
 * as the meaning of identifiers will vary depending on how an expression is used. For example
 * if used as a portfolio filter, symbols like "Position" might refer to the currently considered
 * position as the filtering is run.
 * <p>
 * If the user expressions are extended to allow arbitrary code to be executed, then the full
 * set of OG functions might be available here - and the D might be a session context.
 * 
 * @param <D> evaluation context data
 */
public abstract class AbstractExpressionEvaluator<D> implements UserExpressionVisitor<Object, D> {

  @SuppressWarnings("unchecked")
  protected <T> T evaluate(final Class<T> expected, final UserExpression expr, final D data) {
    Object value = expr.accept(this, data);
    if (value == null) {
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

  @Override
  public Object visitAnd(final UserExpression left, final UserExpression right, final D data) {
    final Boolean leftValue = evaluate(Boolean.class, left, data);
    if ((leftValue == null) || !leftValue.booleanValue()) {
      return false;
    }
    final Boolean rightValue = evaluate(Boolean.class, right, data);
    if (rightValue == null) {
      return false;
    }
    return rightValue;
  }

  @Override
  public Object visitEq(final UserExpression left, final UserExpression right, final D data) {
    final Object leftValue = left.accept(this, data);
    if (leftValue == null) {
      return false;
    }
    final Object rightValue = coerce(leftValue, right.accept(this, data));
    if (rightValue == null) {
      return false;
    }
    return leftValue.equals(rightValue);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object visitGt(final UserExpression left, final UserExpression right, final D data) {
    final Comparable leftValue = evaluate(Comparable.class, left, data);
    if (leftValue == null) {
      return false;
    }
    final Object rightValue = coerce(leftValue, right.accept(this, data));
    if (rightValue == null) {
      return false;
    }
    return leftValue.compareTo(rightValue) > 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visitGte(final UserExpression left, final UserExpression right, final D data) {
    final Comparable leftValue = evaluate(Comparable.class, left, data);
    if (leftValue == null) {
      return false;
    }
    final Object rightValue = coerce(leftValue, right.accept(this, data));
    if (rightValue == null) {
      return false;
    }
    return leftValue.compareTo(rightValue) >= 0;
  }

  @Override
  public Object visitLiteral(final Object value, final D data) {
    return value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object visitLt(final UserExpression left, final UserExpression right, final D data) {
    final Comparable leftValue = evaluate(Comparable.class, left, data);
    if (leftValue == null) {
      return false;
    }
    final Object rightValue = coerce(leftValue, right.accept(this, data));
    if (rightValue == null) {
      return false;
    }
    return leftValue.compareTo(rightValue) < 0;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object visitLte(final UserExpression left, final UserExpression right, final D data) {
    final Comparable leftValue = evaluate(Comparable.class, left, data);
    if (leftValue == null) {
      return false;
    }
    final Object rightValue = coerce(leftValue, right.accept(this, data));
    if (rightValue == null) {
      return false;
    }
    return leftValue.compareTo(rightValue) <= 0;
  }

  @Override
  public Object visitNeq(final UserExpression left, final UserExpression right, final D data) {
    final Object leftValue = left.accept(this, data);
    if (leftValue == null) {
      return true;
    }
    final Object rightValue = coerce(leftValue, right.accept(this, data));
    if (rightValue == null) {
      return true;
    }
    return !leftValue.equals(rightValue);
  }

  @Override
  public Object visitNot(UserExpression expr, D data) {
    final Boolean exprValue = evaluate(Boolean.class, expr, data);
    if (exprValue == null) {
      return null;
    }
    return !exprValue;
  }

  @Override
  public Object visitOr(UserExpression left, UserExpression right, D data) {
    final Boolean leftValue = evaluate(Boolean.class, left, data);
    if ((leftValue != null) && leftValue.booleanValue()) {
      return true;
    }
    final Boolean rightValue = evaluate(Boolean.class, right, data);
    if (rightValue == null) {
      return false;
    }
    return rightValue.booleanValue();
  }

}
