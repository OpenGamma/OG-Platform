/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression;

import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a user expression.
 */
public abstract class UserExpression {

  /**
   * Special result produced by expressions which fail to evaluate.
   */
  public static final Object NA = new Object() {

    @Override
    public String toString() {
      return "N/A";
    }

  };

  /**
   * Evaluation context for an expression.
   */
  public final class Evaluator {

    private Map<String, Object> _variables;
    private Map<Class<?>, Object> _contexts;

    private Evaluator() {
    }

    public void setVariable(final String var, final Object value) {
      assert var != null;
      assert value != NA;
      if (_variables == null) {
        _variables = new HashMap<String, Object>();
      }
      _variables.put(var, value);
    }

    public void removeVariable(final String var) {
      assert var != null;
      if (_variables != null) {
        _variables.remove(var);
      }
    }

    public Object getVariable(final String var) {
      assert var != null;
      if (_variables == null) {
        return NA;
      }
      final Object result = _variables.get(var);
      if ((result == null) && !_variables.containsKey(var)) {
        return NA;
      }
      return result;
    }

    public Object evaluate() {
      return UserExpression.this.evaluate(this);
    }

    private Object getContext(final Class<?> clazz) {
      assert clazz != null;
      if (_contexts == null) {
        return null;
      }
      return _contexts.get(clazz);
    }

    private void setContext(final Class<?> clazz, final Object context) {
      assert clazz != null;
      assert context != null;
      if (_contexts == null) {
        _contexts = new HashMap<Class<?>, Object>();
      }
      _contexts.put(clazz, context);
    }

  }

  protected UserExpression() {
  }

  public Evaluator evaluator() {
    return new Evaluator();
  }

  protected Object createContext(Evaluator evaluator) {
    return null;
  }

  protected Object getContext(final Evaluator evaluator) {
    Object context = evaluator.getContext(getClass());
    if (context == null) {
      context = createContext(evaluator);
      evaluator.setContext(getClass(), context);
    }
    return context;
  }

  protected abstract Object evaluate(Evaluator evaluator);

}
