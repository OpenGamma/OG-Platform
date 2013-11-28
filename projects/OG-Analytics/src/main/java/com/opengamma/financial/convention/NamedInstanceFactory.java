/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.List;

import com.opengamma.util.ClassUtils;

/**
 * An interface for named instances.
 * <p>
 * A named instance is a type where each instance is uniquely identified by a name.
 * This factory provides access to all the instances.
 * <p>
 * Implementations should typically be singletons with a public static factory instance
 * named 'INSTANCE' accessible using {@link ClassUtils#singletonInstance(Class)}.
 * 
 * @param <T> type of objects returned
 */
public interface NamedInstanceFactory<T extends NamedInstance> {

  /* 
  static T of(String name)
   */

  /**
   * Returns a list of available instances.
   * 
   * @return the unmodifiable list of available named instances, not null
   */
  List<T> values();

}
