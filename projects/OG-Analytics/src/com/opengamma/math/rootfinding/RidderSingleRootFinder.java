/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class RidderSingleRootFinder extends RealSingleRootFinder {
  private final double _accuracy;
  private static final int MAX_ITER = 10000;
  private static final double ZERO = 1e-16;

  public RidderSingleRootFinder() {
    this(1e-15);
  }

  public RidderSingleRootFinder(final double accuracy) {
    _accuracy = Math.abs(accuracy);
  }

  @Override
  public Double getRoot(final Function1D<Double, Double> function, final Double xLow, final Double xHigh) {
    checkInputs(function, xLow, xHigh);
    double x1 = xLow;
    double x2 = xHigh;
    double y1 = function.evaluate(x1);
    double y2 = function.evaluate(x2);
    if (Math.abs(y1) < _accuracy) {
      return xHigh;
    }
    if (Math.abs(y2) < _accuracy) {
      return x1;
    }
    if (y1 * y2 >= 0) {
      throw new MathException(x1 + " and " + xHigh + " do not bracket a root");
    }
    double xMid, yMid, denom, xNew, yNew;
    for (int i = 0; i < MAX_ITER; i++) {
      xMid = (x1 + x2) / 2;
      yMid = function.evaluate(xMid);
      denom = Math.sqrt(yMid * yMid - y1 * y2);
      if (Math.abs(denom) < ZERO) {
        throw new MathException("Denominator of updating formula was zero");
      }
      xNew = xMid + (xMid - x1) * (y1 >= y2 ? 1 : -1) * yMid / denom;
      yNew = function.evaluate(xNew);
      if (Math.abs(yNew) < ZERO) {
        return xNew;
      }
      if (Math.abs(Math.copySign(yMid, yNew) - yMid) > ZERO) {
        x1 = xMid;
        y1 = yMid;
        x2 = xNew;
        y2 = yNew;
      } else if (Math.abs(Math.copySign(y1, yNew) - y1) > ZERO) {
        x2 = xNew;
        y2 = yNew;
      } else if (Math.abs(Math.copySign(y2, yNew) - y2) > ZERO) {
        x1 = xNew;
        y1 = yNew;
      } else {
        throw new MathException("Should never reach here");
      }
      if (Math.abs(x2 - x1) < _accuracy) {
        return xNew;
      }
    }
    throw new MathException("Could not find root in " + MAX_ITER + " attempts");
  }
}
