package com.opengamma.math.rootfinding;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function;

/**
 * @author emcleod
 */

public class NewtonRaphsonSingleRootFinder implements DoubleSingleRootFinder<Exception> {
  private static final int MAX_ITER = 10000;

  @Override
  public Double getRoot(Function<Double, Double, Exception> function, Double xLow, Double xHigh, Double accuracy) throws Exception {
    if (accuracy == null)
      throw new IllegalArgumentException("Accuracy was null");
    double yLow = function.evaluate(xLow);
    double y = function.evaluate(xHigh);
    if (Math.abs(y) < accuracy)
      return xHigh;
    if (Math.abs(yLow) < accuracy)
      return xLow;
    if (yLow * y >= 0)
      throw new MathException(xLow + " and " + xHigh + " do not bracket a root");
    int i = 0;
    double y1, y2, dy, root = 0;
    do {
      y1 = function.evaluate(xLow);
      y2 = function.evaluate(xLow + accuracy);
      dy = (y1 - y2) / accuracy;
      root -= y1 / dy;
      if (Math.abs(dy) < accuracy) {
        return root;
      }
    } while (i < MAX_ITER);
    throw new ConvergenceException(CONVERGENCE_STRING);
  }
}
