package com.opengamma.math.integration;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class Integrator1D<T, U, V> implements Integrator<T, Function1D<V, V>, V> {

  public T integrate(U f, V[] lower, V[] upper) {
    return integrate(f, lower[0], upper[0]);
  }

  public abstract T integrate(U f, V lower, V upper);

}
