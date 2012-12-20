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
   * Callback interface for registering a source of dynamic variables to an evaluation context.
   */
  public interface DynamicVariables {

    /**
     * Returns the value of the variable, or NA if not defined.
     * 
     * @param name name of the variable
     * @return the value, or NA if not defined
     */
    Object getValue(String name);

  }

  /**
   * Callback interface for registering a source of dynamic attributes to an evaluation context.
   */
  public interface DynamicAttributes {

    /**
     * Returns the value of an attribute, or NA if not defined.
     * 
     * @param object object the attribute is declared on
     * @param name name of the attribute
     * @return the value, or NA if not defined
     */
    Object getValue(Object object, String name);

  }

  /**
   * Evaluation context for an expression.
   */
  public final class Evaluator {

    private Map<String, Object> _variables;
    private Map<Class<?>, Object> _contexts;
    private DynamicVariables _dynamicVariables;
    private DynamicAttributes _dynamicAttributes;

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

    public void setDynamicVariables(final DynamicVariables dynamicVariables) {
      _dynamicVariables = dynamicVariables;
    }

    public DynamicVariables getDynamicVariables() {
      return _dynamicVariables;
    }

    /**
     * Evaluates a variable's value. If the variable has been explicitly set, the set value is
     * used. If it is unset and a dynamic variable provider is registered, that is queried. Otherwise
     * NA is returned.
     * 
     * @param var name of the variable
     * @return the value or NA if undefined
     */
    public Object evaluateVariable(final String var) {
      assert var != null;
      final Object value = getVariable(var);
      if (value != NA) {
        return value;
      }
      if (_dynamicVariables != null) {
        return _dynamicVariables.getValue(var);
      }
      return NA;
    }

    public void setDynamicAttributes(final DynamicAttributes dynamicAttributes) {
      _dynamicAttributes = dynamicAttributes;
    }

    public DynamicAttributes getDynamicAttributes() {
      return _dynamicAttributes;
    }

    public Object evaluateAttribute(final Object object, final String attribute) {
      assert object != null;
      assert attribute != null;
      if (_dynamicAttributes != null) {
        return _dynamicAttributes.getValue(object, attribute);
      }
      return NA;
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
