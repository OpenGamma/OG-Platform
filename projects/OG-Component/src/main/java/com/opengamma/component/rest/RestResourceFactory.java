/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.rest;

import java.lang.reflect.Constructor;

import com.opengamma.util.ReflectionUtils;

/**
 * Base class that can integrate per-request resources into JaxRs.
 * <p>
 * Singleton instances can be easily integrated with JaxRs, but non-singletons require a factory.
 * This class needs to be extended for each JaxRs system, such as Jersey.
 */
public abstract class RestResourceFactory {

  /**
   * The constructor.
   */
  private final Constructor<?> _constructor;
  /**
   * The supplier that can create the class.
   */
  private final Object[] _arguments;

  /**
   * Creates an instance of the factory.
   * <p>
   * The factory will find and use a public constructor on the class.
   * The class should be annotated with {@code @Path}.
   * 
   * @param type  the type to create, not null
   * @param arguments  the arguments, not null, may contain nulls
   */
  public RestResourceFactory(Class<?> type, Object... arguments) {
    super();
    _constructor = ReflectionUtils.findConstructorByArguments(type, arguments);
    _arguments = arguments;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the type of the resource being created.
   * 
   * @return the resource type, not null
   */
  public Class<?> getType() {
    return _constructor.getDeclaringClass();
  }

  /**
   * Creates the new instance using the constructor.
   * 
   * @return the new instance, not null
   */
  public Object createInstance() {
    return ReflectionUtils.newInstance(_constructor, _arguments);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return _constructor.toString();
  }

}
