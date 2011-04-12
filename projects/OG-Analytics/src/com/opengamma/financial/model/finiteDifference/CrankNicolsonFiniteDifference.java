/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.surface.Surface;

/**
 * **
 * Crank-Nicolson solver for the PDE {@latex.inline $\\frac{\\partial f}{\\partial t} + a(t,x) \\frac{\\partial^2 f}{\\partial x^2} + b(t,x) \\frac{\\partial f}{\\partial x} + (t,x)f = 0$}
 * <b>Note</b> this is using LU-decomposition to solve the matrix system so is not suitable for free-boundary problems (e.g. American options) 
 */
public class CrankNicolsonFiniteDifference implements ConvectionDiffusionPDESolver {

  private static final double THETA = 0.5; // TODO investigate adjusting this (Douglas schemes)
  private static final Decomposition<?> DCOMP = new LUDecompositionCommons();

  public double[][] solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary) {
    Validate.notNull(pdeData, "pde data");
    final double dt = tMax / (tSteps);
    final double dx = (upperBoundary.getLevel() - lowerBoundary.getLevel()) / (xSteps);
    final double nu1 = dt / dx / dx;
    final double nu2 = dt / dx;

    double[] f = new double[xSteps + 1];
    final double[] x = new double[xSteps + 1];
    final double[] q = new double[xSteps + 1];
    final double[][] m = new double[xSteps + 1][xSteps + 1];

    double currentX = lowerBoundary.getLevel();

    double a, b, c, aa, bb, cc;

    for (int i = 0; i <= xSteps; i++) {
      currentX = lowerBoundary.getLevel() + i * dx;
      x[i] = currentX;
      final double value = pdeData.getInitialValue(currentX);
      f[i] = value;
    }

    double t = 0.0;
    for (int n = 0; n < tSteps; n++) {
      t += dt;

      for (int i = 1; i < xSteps; i++) {
        a = pdeData.getA(t - dt, x[i]);
        b = pdeData.getB(t - dt, x[i]);
        c = pdeData.getC(t - dt, x[i]);
        aa = THETA * (-nu1 * a + 0.5 * nu2 * b);
        bb = 1 + THETA * (2 * nu1 * a - dt * c);
        cc = THETA * (-nu1 * a - 0.5 * nu2 * b);
        q[i] = aa * f[i - 1] + bb * f[i] + cc * f[i + 1];

        // TODO could store these
        a = pdeData.getA(t, x[i]);
        b = pdeData.getB(t, x[i]);
        c = pdeData.getC(t, x[i]);
        aa = (-nu1 * a + 0.5 * nu2 * b);
        bb = (2 * nu1 * a - dt * c);
        cc = (-nu1 * a - 0.5 * nu2 * b);
        m[i][i - 1] = (THETA - 1) * aa;
        m[i][i] = 1 + (THETA - 1) * bb;
        m[i][i + 1] = (THETA - 1) * cc;
      }

      double[] temp = lowerBoundary.getLeftMatrixCondition(pdeData, t);
      for (int k = 0; k < temp.length; k++) {
        m[0][k] = temp[k];
      }
      temp = upperBoundary.getLeftMatrixCondition(pdeData, t);
      for (int k = 0; k < temp.length; k++) {
        m[xSteps][xSteps - k] = temp[k];
      }

      temp = lowerBoundary.getRightMatrixCondition(pdeData, t);
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[0] = sum + lowerBoundary.getConstant(pdeData, t, dx);

      temp = upperBoundary.getRightMatrixCondition(pdeData, t);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xSteps - k];
      }
      q[xSteps] = sum + upperBoundary.getConstant(pdeData, t, dx);

      final DoubleMatrix2D mM = new DoubleMatrix2D(m);
      final DecompositionResult res = DCOMP.evaluate(mM);
      f = res.solve(q);
    }

    final double[][] res = new double[2][];
    res[0] = x;
    res[1] = f;

    return res;

  }

  /**
   * <b>NOTE</b> not implemented 
   */
  @Override
  public double[][] solve(ConvectionDiffusionPDEDataBundle pdeData, int tSteps, int xSteps, double tMax, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary,
      Surface<Double, Double, Double> freeBoundary) {
    throw new NotImplementedException();
  }

}
