package com.opengamma.math.minimization;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.function.Function;

/**
 * 
 * @author emcleod
 * 
 */

public interface Minimizer<S, T, U extends Function<S, T, W>, V, W extends Exception> {
  public double TOLERANCE = 1e-12;

  public V minimize(U f, T[] initialPoints) throws ConvergenceException, W;

}
