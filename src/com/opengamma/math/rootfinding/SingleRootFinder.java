package com.opengamma.math.rootfinding;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function;

/**
 * 
 * @author emcleod
 * 
 * @param <T>
 */
public interface SingleRootFinder<T> {
  public static final int MAX_ATTEMPTS = 100;
  public static final double ZERO = 1e-12;
  public static final String CONVERGENCE_STRING = "Could not find root in " + MAX_ATTEMPTS + " attempts";

  public T getRoot(Function<T, T> function, T xLow, T xHigh, T accuracy) throws MathException, ConvergenceException;
}
