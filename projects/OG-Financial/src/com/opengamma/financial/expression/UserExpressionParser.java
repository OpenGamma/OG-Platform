/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.expression;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;
import com.opengamma.util.tuple.Pair;

/**
 * Parses a string representation of a user expression into a {@link UserExpression}
 * object that can be evaluated.
 */
public abstract class UserExpressionParser {

  private final Map<Class<?>, Map<String, Pair<Class<?>, Function<?, ?>>>> _synthetics;

  protected UserExpressionParser() {
    _synthetics = new HashMap<Class<?>, Map<String, Pair<Class<?>, Function<?, ?>>>>();
  }

  /**
   * Registers a constant that should be replaced at parse time.
   * 
   * @param name name of the constant
   * @param value constant value
   */
  public abstract void setConstant(String name, Object value);

  /**
   * Registers a function. The function might appear as <name><object> (e.g. getSecurity)
   * or <object>:<name> (e.g. Security:get) depending on the parser.
   * 
   * @param object the object type being returned, e.g. Security
   * @param name the name of the operation, e.g. get
   * @param method the static method to invoke to evaluate this
   */
  public abstract void setFunction(String object, String name, Method method);

  /**
   * Registers a synthetic property to pass to all evaluation contexts created.
   * 
   * @param <T> class of object to declare the synthetic property on, e.g. Security
   * @param <S> class of the synthetic property, e.g. Currency
   * @param object class of object to declare the synthetic property on, e.g. Security
   * @param type class of synthetic property on, e.g. Currency
   * @param name name of the property, e.g. currency
   * @param method the function to supply the synthetic value
   */
  @SuppressWarnings("unchecked")
  public <T, S> void setSynthetic(final Class<T> object, final Class<S> type, final String name, final Function<T, S> method) {
    Map synthetics = _synthetics.get(object);
    if (synthetics == null) {
      synthetics = new HashMap();
      _synthetics.put(object, synthetics);
    }
    synthetics.put(name, Pair.of(type, method));
  }

  @SuppressWarnings("unchecked")
  protected Pair<Class<?>, Function<Object, Object>> getSynthetic(final Object value, final String name) {
    Class clazz = value.getClass();
    do {
      Map synthetics = _synthetics.get(clazz);
      if (synthetics != null) {
        final Object synthetic = synthetics.get(name);
        if (synthetic != null) {
          return (Pair) synthetic;
        }
      }
      for (Class iface : clazz.getInterfaces()) {
        synthetics = _synthetics.get(iface);
        if (synthetics != null) {
          final Object synthetic = synthetics.get(name);
          if (synthetic != null) {
            return (Pair) synthetic;
          }
        }
      }
      clazz = clazz.getSuperclass();
    } while (clazz != null);
    return null;
  }

  public abstract UserExpression parse(String source);

}
