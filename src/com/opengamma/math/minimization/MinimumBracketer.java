package com.opengamma.math.minimization;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class MinimumBracketer<T, U extends Exception> {
  protected static final double GOLDEN = 0.61803399;

  public abstract T[] getBracketedPoints(Function1D<T, T, U> f, T x1, T x2) throws ConvergenceException;
}
