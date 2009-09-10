package com.opengamma.math.minimization;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public class BrentMinimizer1D implements Minimizer1D<Double, MathException> {
  private static final double GOLDEN = 0.61803399;
  private static final double COMPLEMENT = 1 - GOLDEN;
  private static final MinimumBracketer<Double, MathException> BRACKETER = new ParabolicMinimumBracketer();
  private static final int MAX_ITER = 10000;
  private static final double EPS = 1e-12;
  private static final double ZERO = 1e-20;

  @Override
  public Double minimize(Function1D<Double, Double, MathException> f, Double[] initialPoints) throws ConvergenceException, MathException {
    double a, b, etemp, fu, fv, fw, fx;
    double p, q, r, tol1, tol2, u, v, w, x, xm;
    double d = 0;
    double e = 0;
    double ax, bx, cx;
    Double[] bracketted = BRACKETER.getBracketedPoints(f, initialPoints[0], initialPoints[1]);
    ax = bracketted[0];
    bx = bracketted[1];
    cx = bracketted[2];
    a = ax < cx ? ax : cx;
    b = ax > cx ? ax : cx;
    x = w = v = bx;
    d = 0;
    fw = fv = fx = f.evaluate(x);
    for (int i = 0; i < MAX_ITER; i++) {
      xm = 0.5 * (a + b);
      tol1 = EPS * Math.abs(x) + ZERO;
      tol2 = 2 * tol1;
      if (Math.abs(x - xm) <= tol2 - 0.5 * (b - a)) {
        return x;
      }
      if (Math.abs(e) > tol1) {
        r = (x - w) * (fx - fv);
        q = (x - v) * (fx - fw);
        p = (x - v) * q - (x - w) * r;
        q = 2 * (q - r);
        if (q > 0)
          p = -p;
        q = Math.abs(q);
        etemp = e;
        e = d;
        if (Math.abs(p) >= Math.abs(0.5 * q * etemp) || p <= q * (a - x) || p >= q * (b - x)) {
          e = x >= xm ? a - x : b - x;
          d = COMPLEMENT * e;
        } else {
          d = p / q;
          u = x + d;
          if (u - a < tol2 || b - u < tol2) {
            d = Math.copySign(tol1, xm - x);
          }
        }
      } else {
        e = x >= xm ? a - x : b - x;
        d = COMPLEMENT * e;
      }
      u = Math.abs(d) >= tol1 ? x + d : x + Math.copySign(tol1, d);
      fu = f.evaluate(u);
      if (fu <= fx) {
        if (u >= x) {
          a = x;
        } else {
          b = x;
        }
        v = w;
        w = x;
        x = u;
        fv = fw;
        fw = fx;
        fx = fu;
      } else {
        if (u < x) {
          a = u;
        } else {
          b = u;
        }
        if (fu <= fw || w == x) {
          v = w;
          w = u;
          fv = fw;
          fw = fu;
        } else if (fu <= fv || v == x || v == w) {
          v = u;
          fv = fu;
        }
      }
    }
    throw new ConvergenceException();
  }
}
