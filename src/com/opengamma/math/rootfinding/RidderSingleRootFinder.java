package com.opengamma.math.rootfinding;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function;

/**
 * 
 * @author emcleod
 * 
 */
public class RidderSingleRootFinder implements DoubleSingleRootFinder {

  @Override
  public Double getRoot(Function<Double, Double> function, Double xLow, Double xHigh, Double accuracy) {
    if (accuracy == null)
      throw new IllegalArgumentException("Accuracy was null");
    double x1 = xLow;
    double x2 = xHigh;
    double y1 = function.evaluate(x1);
    double y2 = function.evaluate(x2);
    if (Math.abs(y1) < accuracy)
      return xHigh;
    if (Math.abs(y2) < accuracy)
      return xLow;
    if (y1 * y2 >= 0)
      throw new MathException(xLow + " and " + xHigh + " do not bracket a root");
    double xMid, yMid, denom, xNew, yNew;
    for (int i = 0; i < MAX_ATTEMPTS; i++) {
      xMid = (x1 + x2) / 2;
      yMid = function.evaluate(xMid);
      denom = Math.sqrt(yMid * yMid - y1 * y2);
      if (Math.abs(denom) < ZERO)
        throw new MathException("Denominator of updating formula was zero");
      xNew = xMid + (xMid - x1) * ((y1 >= y2 ? 1 : -1) * yMid / denom);
      yNew = function.evaluate(xNew);
      if (Math.abs(yNew) < ZERO)
        return xNew;
      if (Math.abs(adjustSign(yMid, yNew) - yMid) > ZERO) {
        x1 = xMid;
        y1 = yMid;
        x2 = xNew;
        y2 = yNew;
      } else if (Math.abs(adjustSign(y1, yNew) - y1) > ZERO) {
        x2 = xNew;
        y2 = yNew;
      } else if (Math.abs(adjustSign(y2, yNew) - y2) > ZERO) {
        x1 = xNew;
        y1 = yNew;
      } else {
        throw new MathException("Should never reach here");
      }
      if (Math.abs(x2 - x1) < accuracy)
        return xNew;
    }
    throw new ConvergenceException(CONVERGENCE_STRING);
  }

  private double adjustSign(double x, double y) {
    return Math.abs(x) * Math.signum(y);
  }
}
