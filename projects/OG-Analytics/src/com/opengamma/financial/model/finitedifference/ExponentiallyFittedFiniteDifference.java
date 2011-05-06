/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.math.surface.Surface;

/**
 * Exponentially fitted scheme (see Duffy, A Critique of the Crank Nicolson Scheme, 2004) using SOR algorithm to solve the matrix system at each time step 
 */
public class ExponentiallyFittedFiniteDifference implements ConvectionDiffusionPDESolver {

  @Override
  public double[][] solve(ConvectionDiffusionPDEDataBundle pdeData, int tSteps, int xSteps, double tMax, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary) {
    return solve(pdeData, tSteps, xSteps, tMax, lowerBoundary, upperBoundary, null);
  }

  @Override
  public double[][] solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary, final Surface<Double, Double, Double> freeBoundary) {
    Validate.notNull(pdeData, "pde data");
    final double dt = tMax / (tSteps);
    final double dx = (upperBoundary.getLevel() - lowerBoundary.getLevel()) / (xSteps);
    final double dtdx2 = dt / dx / dx;
    final double dtdx = dt / dx;

    final double[] f = new double[xSteps + 1];
    final double[] x = new double[xSteps + 1];
    final double[] q = new double[xSteps + 1];
    final double[][] m = new double[xSteps + 1][xSteps + 1];
    // double[] coefficients = new double[3];

    double currentX = lowerBoundary.getLevel();

    double a, b, c;

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

        q[i] = f[i];

        // TODO could store these
        a = pdeData.getA(t, x[i]);
        b = pdeData.getB(t, x[i]);
        c = pdeData.getC(t, x[i]);

        double rho;
        double bdx = (b * dx / 2);
        if (Math.abs(bdx) > 10 * Math.abs(a)) {
          rho = Math.abs(bdx);
        } else if (Math.abs(a) > 10 * Math.abs(bdx)) {
          rho = a;
        } else {
          rho = bdx / Math.tanh(bdx / a);
        }

        m[i][i - 1] = (dtdx2 * rho - 0.5 * dtdx * b);
        m[i][i] = (-2 * dtdx2 * rho + dt * c);
        m[i][i + 1] = (dtdx2 * rho - +0.5 * dtdx * b);
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

      // SOR
      final double omega = 1.0;
      double scale = 1.0;
      double errorSqr = Double.POSITIVE_INFINITY;
      while (errorSqr / (scale + 1e-10) > 1e-18) {
        errorSqr = 0.0;
        scale = 0.0;
        for (int j = 0; j <= xSteps; j++) {
          sum = 0;
          for (int k = 0; k <= xSteps; k++) {
            sum += m[j][k] * f[k];
          }
          double correction = omega / m[j][j] * (q[j] - sum);
          if (freeBoundary != null) {
            correction = Math.max(correction, freeBoundary.getZValue(t, x[j]) - f[j]);
          }
          errorSqr += correction * correction;
          f[j] += correction;
          scale += f[j] * f[j];
        }
      }

    }

    final double[][] res = new double[2][];
    res[0] = x;
    res[1] = f;

    return res;

  }

}
