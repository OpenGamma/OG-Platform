/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.function;

/**
 * A supplier of values.
 *
 * @param <R> the type of the result
 */
public interface Supplier<R> {

  /**
   * Supplies a value.
   *
   * @return the result of the supplier
   */
  R get();

}
