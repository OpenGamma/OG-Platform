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
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ObjectComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Parses a string representation of a user expression into a {@link UserExpression}
 * object that can be evaluated.
 */
public abstract class UserExpressionParser {

  private static final ThreadLocal<ComputationTargetResolver.AtVersionCorrection> s_resolver = new ThreadLocal<ComputationTargetResolver.AtVersionCorrection>();

  /**
   * Returns the thread's resolver for the current expression evaluation. This allows static items to be registered with the parser but access services allowing deep resolution of referenced entities.
   *
   * @return the thread's resolver
   */
  public static ComputationTargetResolver.AtVersionCorrection getResolver() {
    return s_resolver.get();
  }

  /**
   * Sets the thread's resolver for the current expression evaluation. This allows static items to be registered with the parser but access services allowing deep resolution of referenced entities.
   *
   * @param resolver the thread's resolver
   */
  public static void setResolver(final ComputationTargetResolver.AtVersionCorrection resolver) {
    s_resolver.set(resolver);
  }

  /**
   * Resolves an identifier as part of expression evaluation using the thread's resolver. The identifier may be the object already resolved, a unique identifier, an external identifier, or an external
   * identifier bundle.
   *
   * @param <T> the resolved type
   * @param type the type to resolve to, not null
   * @param id the identifier to resolve
   * @return the resolved object or null if it could not be resolved
   */
  @SuppressWarnings("unchecked")
  public static <T extends UniqueIdentifiable> T resolve(final ObjectComputationTargetType<T> type, final Object id) {
    if (id == null) {
      return null;
    }
    if ((id instanceof UniqueIdentifiable) && type.isCompatible((UniqueIdentifiable) id)) {
      return (T) id;
    }
    ComputationTargetSpecification spec;
    if (id instanceof UniqueId) {
      spec = new ComputationTargetSpecification(type, (UniqueId) id);
    } else {
      ComputationTargetRequirement req;
      if (id instanceof ExternalId) {
        req = new ComputationTargetRequirement(type, (ExternalId) id);
      } else if (id instanceof ExternalIdBundle) {
        req = new ComputationTargetRequirement(type, (ExternalIdBundle) id);
      } else {
        throw new UnsupportedOperationException("Invalid " + type + " - " + id);
      }
      spec = getResolver().getSpecificationResolver().getTargetSpecification(req);
      if (spec == null) {
        return null;
      }
    }
    final ComputationTarget resolved = getResolver().resolve(spec);
    if (resolved == null) {
      return null;
    }
    return resolved.getValue(type);
  }

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
  @SuppressWarnings({"unchecked", "rawtypes" })
  public <T, S> void setSynthetic(final Class<T> object, final Class<S> type, final String name, final Function<T, S> method) {
    Map synthetics = _synthetics.get(object);
    if (synthetics == null) {
      synthetics = new HashMap();
      _synthetics.put(object, synthetics);
    }
    synthetics.put(name, Pairs.of(type, method));
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
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
      for (final Class iface : clazz.getInterfaces()) {
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
