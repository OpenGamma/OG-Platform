/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import com.opengamma.math.function.Function;

/**
 * 
 * @author emcleod
 * 
 */

public interface Integrator<T, U extends Function<?, ?>, V> {

  public T integrate(U f, V[] lower, V[] upper);
}
