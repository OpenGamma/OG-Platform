package com.opengamma.math.integration;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class Integrator1D<T, U, V, W extends Exception> implements Integrator<T, Function1D<V, V, W>, V, W> {

  public T integrate(U f, V[] lower, V[] upper) throws W {
    return integrate(f, lower[0], upper[0]);
  }

  public abstract T integrate(U f, V lower, V upper) throws W;

}
