package com.opengamma.math.rootfinding;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function;

/**
 * 
 * @author emcleod
 * 
 */
public class BisectionSingleRootFinder implements SingleRootFinder<Double, Double, Double> {

  @Override
  public Double getRoot(Function<Double, Double> function, Double xLow, Double xHigh, Double accuracy) {
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
    double dx, xRoot, xMid;
    if (yLow < 0) {
      dx = xHigh - xLow;
      xRoot = xLow;
    } else {
      dx = xLow - xHigh;
      xRoot = xHigh;
    }
    for (int i = 0; i < MAX_ATTEMPTS; i++) {
      dx *= 0.5;
      xMid = xRoot + dx;
      y = function.evaluate(xMid);
      if (y <= 0)
        xRoot = xMid;
      if (Math.abs(dx) < accuracy || Math.abs(y) < ZERO)
        return xRoot;
    }
    throw new ConvergenceException(CONVERGENCE_STRING);
  }
}
