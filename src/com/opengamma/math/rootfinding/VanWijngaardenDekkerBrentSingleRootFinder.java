package com.opengamma.math.rootfinding;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function;

public class VanWijngaardenDekkerBrentSingleRootFinder implements DoubleSingleRootFinder<Exception> {

  @Override
  public Double getRoot(Function<Double, Double, Exception> function, Double xLow, Double xHigh, Double accuracy) throws Exception {
    if (accuracy == null)
      throw new IllegalArgumentException("Accuracy was null");
    double a = xLow;
    double b = xHigh;
    double c = b;
    double yA = function.evaluate(a);
    double yB = function.evaluate(b);
    if (Math.abs(yA) < accuracy)
      return xHigh;
    if (Math.abs(yB) < accuracy)
      return xLow;
    if (yA * yB >= 0)
      throw new MathException(xLow + " and " + xHigh + " do not bracket a root");
    double yC = yB;
    double d, e, p, q, r, s, tolerance, xMid, min1, min2;
    d = b - a;
    e = d;
    for (int i = 0; i < MAX_ATTEMPTS; i++) {
      if ((yB > 0 && yC > 0) || (yB < 0 && yC < 0)) {
        c = a;
        yC = yA;
        d = b - a;
        e = d;
      }
      if (Math.abs(yC) < Math.abs(yB)) {
        a = b;
        b = c;
        c = a;
        yA = yB;
        yB = yC;
        yC = yA;
      }
      tolerance = 2 * accuracy * Math.abs(b) + 0.5 * accuracy;
      xMid = 0.5 * (c - b);
      if (Math.abs(xMid) <= tolerance || Math.abs(yB) < ZERO)
        return b;
      if (Math.abs(e) >= tolerance && Math.abs(yA) > Math.abs(yB)) {
        s = yB / yA;
        if (Math.abs(a - c) < ZERO) {
          p = 2 * xMid * s;
          q = 1 - s;
        } else {
          q = yA / yC;
          r = yB / yC;
          p = s * (2 * xMid * q * (r - q) - (b - a) * (r - 1));
          q = (q - 1) * (r - 1) * (s - 1);
        }
        if (p > 0)
          q = -q;
        p = Math.abs(p);
        min1 = 3 * xMid * q - Math.abs(tolerance * q);
        min2 = Math.abs(e * q);
        if (2 * p < (min1 < min2 ? min1 : min2)) {
          e = d;
          d = p / q;
        } else {
          d = xMid;
          e = d;
        }
      } else {
        d = xMid;
        e = d;
      }
      a = b;
      yA = yB;
      if (Math.abs(d) > tolerance) {
        b += d;
      } else {
        b += adjustSign(tolerance, xMid);
        yB = function.evaluate(b);
      }
    }
    throw new ConvergenceException(CONVERGENCE_STRING);
  }

  private double adjustSign(double x, double y) {
    return Math.abs(x) * Math.signum(y);
  }
}
