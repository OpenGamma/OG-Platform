/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.function;

/**
 * A binary operator function.
 * <p>
 * This takes two object arguments and returns an object result, all of the same type.
 *
 * @param <T> the type of the operation
 */
public interface BinaryOperator<T> extends BiFunction<T, T, T> {

  /**
   * Applies the function.
   *
   * @param obj1  the first argument
   * @param obj2  the second argument
   * @return the result of the function
   */
  @Override
  T apply(T obj1, T obj2);

}
