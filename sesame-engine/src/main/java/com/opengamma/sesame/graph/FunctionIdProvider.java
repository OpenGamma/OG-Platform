/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.graph;

/**
 * Provides IDs for function instances.
 * <p>
 * If two functions are logically equal they will be assigned the same ID. This is used by the caching mechanism
 * to allow safe sharing of values calculated by different functions.
 * <p>
 * Two function instances are considered to be logically equal if their {@link FunctionModelNode} instances are
 * equal.
 */
public interface FunctionIdProvider {

  /**
   * Returns the ID of a function instance.
   *
   * @param fn a function
   * @return the function's ID
   * @throws IllegalArgumentException if the function has no known ID
   */
  FunctionId getFunctionId(Object fn);
}
