/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.serialization;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Substitution representation of an inner class that is created as an invocation on the containing
 * class. Call with an object instance if a specific instance is needed for the invocation. If the
 * method is static or the containing object has no state pass the containing class. If an instance
 * is passed, care must be taken in its serialized form such that it does not include the contained
 * form (e.g. as an attribute) - doing so will create a loop in the graph which may prevent
 * {@link #readReplace} from executing correctly.
 */
public final class InvokedSerializedForm implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final Object[] EMPTY_ARRAY = new Object[0];

  private static final String[] s_methodPrefixes = new String[] {"as", "get", "to", "from"};

  private final Class<?> _outerClass;
  private final Object _outerInstance;
  private final String _method;
  private final Object[] _parameters;
  private Object _replacementInstance;

  public InvokedSerializedForm(final Object outer, final String method) {
    this(outer, method, EMPTY_ARRAY);
  }

  public InvokedSerializedForm(final Object outer, final String method, final Object... parameters) {
    ArgumentChecker.notNull(outer, "outer");
    ArgumentChecker.notNull(method, "method");
    if (outer instanceof Class<?>) {
      _outerClass = (Class<?>) outer;
      _outerInstance = null;
    } else {
      _outerClass = null;
      _outerInstance = outer;
      _replacementInstance = outer;
    }
    String preferredMethod = method;
    for (final String prefix : s_methodPrefixes) {
      if (Character.isUpperCase(method.charAt(prefix.length())) && method.startsWith(prefix)) {
        preferredMethod = method.substring(prefix.length());
        break;
      }
    }
    _method = preferredMethod;
    _parameters = parameters;
  }

  public Object getOuterInstance() {
    return _outerInstance;
  }

  public Class<?> getOuterClass() {
    return _outerClass;
  }

  public String getMethod() {
    return _method;
  }

  public Object[] getParameters() {
    return _parameters;
  }

  private Object tryMethod(final Method method) {
    final Class<?>[] args = method.getParameterTypes();
    final Object[] params = getParameters();
    if (args.length == params.length) {
      for (int i = 0; i < params.length; i++) {
        if (!args[i].isAssignableFrom(params[i].getClass())) {
          return null;
        }
      }
      try {
        if ((_replacementInstance == null) && !Modifier.isStatic(method.getModifiers())) {
          _replacementInstance = getOuterClass().newInstance();
        }
        return method.invoke(_replacementInstance, params);
      } catch (final Exception e) {
        throw new OpenGammaRuntimeException("Couldn't replace " + toString(), e);
      }
    }
    return null;
  }

  public Object readReplace() {
    final Class<?> clazz = (getOuterInstance() != null) ? getOuterInstance().getClass() : getOuterClass();
    final Method[] methods = clazz.getMethods();
    if (Character.isUpperCase(getMethod().charAt(0))) {
      for (final String prefix : s_methodPrefixes) {
        final String methodName = prefix + getMethod();
        for (final Method method : methods) {
          if (methodName.equals(method.getName())) {
            final Object r = tryMethod(method);
            if (r != null) {
              return r;
            }
          }
        }
      }
    } else {
      clazz.getMethods();
      for (final Method method : methods) {
        if (getMethod().equals(method.getName())) {
          final Object r = tryMethod(method);
          if (r != null) {
            return r;
          }
        }
      }
    }
    throw new OpenGammaRuntimeException("Couldn't replace " + toString());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("InvokedSerializedForm[");
    if (getOuterClass() != null) {
      sb.append(getOuterClass().getName());
    } else {
      sb.append(getOuterInstance().toString());
    }
    sb.append(", ").append(getMethod());
    for (final Object param : getParameters()) {
      sb.append(", ").append(param.toString());
    }
    sb.append(']');
    return sb.toString();
  }

}
