/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup;

/**
 * Interface for providing a value from a security.
 * @param <T> The security type
 */
public interface SecurityValueProvider<T> {

  /**
   * Returns a value for a security.
   * @param security The security
   * @return The value
   */
  Object getValue(T security);
}
