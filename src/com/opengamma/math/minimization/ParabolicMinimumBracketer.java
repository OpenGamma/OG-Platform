package com.opengamma.math.minimization;

import java.util.Arrays;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

import com.opengamma.math.ConvergenceException;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public class ParabolicMinimumBracketer extends MinimumBracketer<Double> {
  private static final int MAX_ITER = 100;
  private final Algebra ALGEBRA = new Algebra();

  // TODO rename x1, x2
  @Override
  public Double[] getBracketedPoints(Function1D<Double, Double> f, Double xLower, Double xUpper) {
    double x1 = xLower;
    double x2 = xUpper;
    double f1 = f.evaluate(x1);
    double f2 = f.evaluate(x2);
    double xTemp, fTemp;
    if (f2 > f1) {
      xTemp = x1;
      fTemp = f1;
      x1 = x2;
      f1 = f2;
      x2 = xTemp;
      f2 = fTemp;
    }
    double x3 = x1 + GOLDEN * (x2 - x1);
    double f3 = f.evaluate(x3);
    if (f3 < f1 && f3 < f2) {
      Double[] result = new Double[] { x1, x2, x3 };
      Arrays.sort(result);
      return result;
    }
    DoubleMatrix2D xMatrix;
    DoubleMatrix1D fMatrix, coefficients;
    int i = 0;
    while (i < MAX_ITER) {
      i++;
      xMatrix = DoubleFactory2D.dense.make(new double[][] { { x1 * x1, x1, 1 }, { x2 * x2, x2, 1 }, { x3 * x3, x3, 1 } });
      fMatrix = DoubleFactory1D.dense.make(new double[] { f1, f2, f3 });
      coefficients = ALGEBRA.mult(ALGEBRA.inverse(xMatrix), fMatrix);
      if (coefficients.get(0) > 0) {
        f1 = f.evaluate(x1);
        f2 = f.evaluate(x2);
        f3 = f.evaluate(x3);
        if (f3 < f1 && f3 < f2) {
          double parabolaMinimumX = -coefficients.get(1) / (2 * coefficients.get(0));
          Double[] result = new Double[] { x1, parabolaMinimumX, (parabolaMinimumX - x1) * (1 + GOLDEN) };
          Arrays.sort(result);
          return result;
        }
      }
      x1 += x2 < x1 ? (1 + GOLDEN) * Math.abs(x1) : -(1 + GOLDEN) * Math.abs(x1);
      x2 += x2 < x1 ? -(1 + GOLDEN) * Math.abs(x2) : (1 + GOLDEN) * Math.abs(x2);
    }
    throw new ConvergenceException("Could not find bracketting triplet in " + MAX_ITER + " attempts");
  }
}
