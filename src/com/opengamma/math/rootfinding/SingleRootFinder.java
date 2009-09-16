package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function;

/**
 * 
 * @author emcleod
 * 
 * @param <T>
 */
public interface SingleRootFinder<S, T, U, V extends Exception> {
  public static final int MAX_ATTEMPTS = 100;
  public static final double ZERO = 1e-12;
  public static final String CONVERGENCE_STRING = "Could not find root in " + MAX_ATTEMPTS + " attempts";

  public U getRoot(Function<S, T, V> function, T xLow, T xHigh, Double accuracy) throws V;
}
