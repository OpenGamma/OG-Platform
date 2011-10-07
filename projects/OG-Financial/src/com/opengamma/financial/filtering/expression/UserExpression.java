/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.filtering.expression;

/**
 * AST representation of a user expression.
 */
public abstract class UserExpression {

  /* package */UserExpression() {
  }

  private abstract static class Binary extends UserExpression {

    private final UserExpression _left;
    private final UserExpression _right;

    private Binary(final UserExpression left, final UserExpression right) {
      _left = left;
      _right = right;
    }

    protected UserExpression getLeft() {
      return _left;
    }

    protected UserExpression getRight() {
      return _right;
    }

  }

  private abstract static class Unary extends UserExpression {

    private final UserExpression _expr;

    private Unary(final UserExpression expr) {
      _expr = expr;
    }

    protected UserExpression getExpr() {
      return _expr;
    }

  }

  /**
   * 
   */
  public static final class And extends Binary {

    /* package */And(final UserExpression left, final UserExpression right) {
      super(left, right);
    }

    public <V, D> V accept(final UserExpressionVisitor<V, D> visitor, final D data) {
      return visitor.visitAnd(getLeft(), getRight(), data);
    }

  }

  /**
   * 
   */
  public static final class Eq extends Binary {

    /* package */Eq(final UserExpression left, final UserExpression right) {
      super(left, right);
    }

    public <V, D> V accept(final UserExpressionVisitor<V, D> visitor, final D data) {
      return visitor.visitEq(getLeft(), getRight(), data);
    }

  }

  /**
   * 
   */
  public static final class Gt extends Binary {

    /* package */Gt(final UserExpression left, final UserExpression right) {
      super(left, right);
    }

    public <V, D> V accept(final UserExpressionVisitor<V, D> visitor, final D data) {
      return visitor.visitGt(getLeft(), getRight(), data);
    }

  }

  /**
   * 
   */
  public static final class Gte extends Binary {

    /* package */Gte(final UserExpression left, final UserExpression right) {
      super(left, right);
    }

    public <V, D> V accept(final UserExpressionVisitor<V, D> visitor, final D data) {
      return visitor.visitGte(getLeft(), getRight(), data);
    }

  }

  /**
   * 
   */
  public static final class Identifier extends UserExpression {

    private final String _text;

    /* package */Identifier(final String text) {
      _text = text;
    }

    public <V, D> V accept(final UserExpressionVisitor<V, D> visitor, final D data) {
      return visitor.visitIdentifier(_text, data);
    }

  }

  /**
   * 
   */
  public static final class Literal extends UserExpression {

    private final Object _value;

    /* package */Literal(final Object value) {
      _value = value;
    }

    public <V, D> V accept(final UserExpressionVisitor<V, D> visitor, final D data) {
      return visitor.visitLiteral(_value, data);
    }
  }

  /**
   * 
   */
  public static final class Lt extends Binary {

    /* package */Lt(final UserExpression left, final UserExpression right) {
      super(left, right);
    }

    public <V, D> V accept(final UserExpressionVisitor<V, D> visitor, final D data) {
      return visitor.visitLt(getLeft(), getRight(), data);
    }

  }

  /**
   * 
   */
  public static final class Lte extends Binary {

    /* package */Lte(final UserExpression left, final UserExpression right) {
      super(left, right);
    }

    public <V, D> V accept(final UserExpressionVisitor<V, D> visitor, final D data) {
      return visitor.visitLte(getLeft(), getRight(), data);
    }

  }

  /**
   * 
   */
  public static final class Neq extends Binary {

    /* package */Neq(final UserExpression left, final UserExpression right) {
      super(left, right);
    }

    public <V, D> V accept(final UserExpressionVisitor<V, D> visitor, final D data) {
      return visitor.visitNeq(getLeft(), getRight(), data);
    }

  }

  /**
   * 
   */
  public static final class Not extends Unary {

    /* package */Not(final UserExpression expr) {
      super(expr);
    }

    public <V, D> V accept(final UserExpressionVisitor<V, D> visitor, final D data) {
      return visitor.visitNot(getExpr(), data);
    }

  }

  /**
   * 
   */
  public static final class Or extends Binary {

    /* package */Or(final UserExpression left, final UserExpression right) {
      super(left, right);
    }

    public <V, D> V accept(final UserExpressionVisitor<V, D> visitor, final D data) {
      return visitor.visitOr(getLeft(), getRight(), data);
    }

  }

  public abstract <V, D> V accept(UserExpressionVisitor<V, D> visitor, D data);

  @Override
  public String toString() {
    return accept(new UserExpressionVisitor<StringBuilder, StringBuilder>() {

      private StringBuilder visitBinary(final String op, final UserExpression left, final UserExpression right, final StringBuilder sb) {
        sb.append("`").append(op).append(" ");
        sb.append("(");
        left.accept(this, sb);
        sb.append(") (");
        right.accept(this, sb);
        return sb.append(")");
      }

      private StringBuilder visitUnary(final String op, final UserExpression expr, final StringBuilder sb) {
        sb.append("`").append(op).append(" (");
        expr.accept(this, sb);
        return sb.append(")");
      }

      @Override
      public StringBuilder visitAnd(final UserExpression left, final UserExpression right, final StringBuilder sb) {
        return visitBinary("AND", left, right, sb);
      }

      @Override
      public StringBuilder visitEq(final UserExpression left, final UserExpression right, final StringBuilder sb) {
        return visitBinary("EQ", left, right, sb);
      }

      @Override
      public StringBuilder visitGt(final UserExpression left, final UserExpression right, final StringBuilder sb) {
        return visitBinary("GT", left, right, sb);
      }

      @Override
      public StringBuilder visitGte(final UserExpression left, final UserExpression right, final StringBuilder sb) {
        return visitBinary("GTE", left, right, sb);
      }

      @Override
      public StringBuilder visitIdentifier(final String identifier, final StringBuilder sb) {
        return sb.append("`IDENTIFIER ").append(identifier);
      }

      @Override
      public StringBuilder visitLiteral(final Object value, final StringBuilder sb) {
        return sb.append("`LITERAL ").append(value);
      }

      @Override
      public StringBuilder visitLt(final UserExpression left, final UserExpression right, final StringBuilder sb) {
        return visitBinary("LT", left, right, sb);
      }

      @Override
      public StringBuilder visitLte(final UserExpression left, final UserExpression right, final StringBuilder sb) {
        return visitBinary("LTE", left, right, sb);
      }

      @Override
      public StringBuilder visitNeq(final UserExpression left, final UserExpression right, final StringBuilder sb) {
        return visitBinary("NEQ", left, right, sb);
      }

      @Override
      public StringBuilder visitNot(final UserExpression expr, final StringBuilder sb) {
        return visitUnary("NOT", expr, sb);
      }

      @Override
      public StringBuilder visitOr(final UserExpression left, final UserExpression right, final StringBuilder sb) {
        return visitBinary("OR", left, right, sb);
      }

    }, new StringBuilder()).toString();
  }

}
