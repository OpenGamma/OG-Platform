/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for building Spring factories for singleton objects.
 * @param <T> the type of the factory bean
 */
public abstract class SingletonFactoryBean<T> implements FactoryBean<T>, InitializingBean {

  /**
   * The singleton instance.
   */
  private T _instance;

  /**
   * Override this to create the instance.
   * @return the created object
   */
  protected abstract T createObject();

  @Override
  public final T getObject() {
    //System.out.println ("getObject on " + getClass () + " returning " + _instance);
    return _instance;
  }

  @Override
  public final Class<?> getObjectType() {
    T obj = getObject();
    if (obj == null) {
      return null;
    } else {
      return obj.getClass();
    }
  }

  @Override
  public final boolean isSingleton() {
    return true;
  }

  @Override
  public void afterPropertiesSet() {
    _instance = createObject();
    //System.out.println ("afterPropertiesSet called on " + getClass ());
  }

}
