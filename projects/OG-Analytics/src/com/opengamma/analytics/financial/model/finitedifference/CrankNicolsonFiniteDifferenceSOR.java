/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.surface.Surface;

/**
 * Crank-Nicolson scheme using SOR algorithm to solve the matrix system at each time step 
 */
public class CrankNicolsonFiniteDifferenceSOR implements ConvectionDiffusionPDESolver {

  private final double _theta;

  /**
   * Sets up a standard Crank-Nicolson scheme 
   */
  public CrankNicolsonFiniteDifferenceSOR() {
    _theta = 0.5;
  }

  /**
   * Sets up a scheme that is the weighted average of an explicit and an implicit scheme 
   * @param theta The weight. theta = 0 - fully explicit, theta = 0.5 - Crank-Nicolson, theta = 1.0 - fully implicit 
   */
  public CrankNicolsonFiniteDifferenceSOR(final double theta) {
    Validate.isTrue(theta >= 0 && theta <= 1.0, "theta must be in the range 0 to 1");
    _theta = theta;
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary) {
    return solve(pdeData, tSteps, xSteps, tMax, lowerBoundary, upperBoundary, null);
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary, final Surface<Double, Double, Double> freeBoundary) {
    Validate.notNull(pdeData, "pde data");

    // simple test code - doesn't use a PDEGrid1D
    final PDEGrid1D grid = new PDEGrid1D(tSteps + 1, xSteps + 1, tMax, lowerBoundary.getLevel(), upperBoundary.getLevel());

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

        double rho = a;
        // double bdx = (b * dx / 2);
        // if (Math.abs(bdx) > 10 * Math.abs(a)) {
        // rho = Math.abs(bdx);
        // } else if (Math.abs(a) > 10 * Math.abs(bdx)) {
        // rho = a;
        // } else {
        // rho = bdx / Math.tanh(bdx / a);
        // }

        aa = (1 - _theta) * (-dtdx2 * rho + 0.5 * dtdx * b);
        bb = 1 + (1 - _theta) * (2 * dtdx2 * rho - dt * c);
        cc = (1 - _theta) * (-dtdx2 * rho - 0.5 * dtdx * b);
        q[i] = aa * f[i - 1] + bb * f[i] + cc * f[i + 1];

        // TODO could store these
        a = pdeData.getA(t, x[i]);
        b = pdeData.getB(t, x[i]);
        c = pdeData.getC(t, x[i]);

        rho = a;
        // bdx = (b * dx / 2);
        // if (Math.abs(bdx) > 10 * Math.abs(a)) {
        // rho = Math.abs(bdx);
        // } else if (Math.abs(a) > 10 * Math.abs(bdx)) {
        // rho = a;
        // } else {
        // rho = bdx / Math.tanh(bdx / a);
        // }

        aa = (-dtdx2 * rho + 0.5 * dtdx * b);
        bb = (2 * dtdx2 * rho - dt * c);
        cc = (-dtdx2 * rho - 0.5 * dtdx * b);
        m[i][i - 1] = -_theta * aa;
        m[i][i] = 1 - _theta * bb;
        m[i][i + 1] = -_theta * cc;
      }

      double[] temp = lowerBoundary.getLeftMatrixCondition(pdeData, grid, t);
      for (int k = 0; k < temp.length; k++) {
        m[0][k] = temp[k];
      }
      temp = upperBoundary.getLeftMatrixCondition(pdeData, grid, t);
      for (int k = 0; k < temp.length; k++) {
        m[xSteps][xSteps - k] = temp[k];
      }

      temp = lowerBoundary.getRightMatrixCondition(pdeData, grid, t);
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[0] = sum + lowerBoundary.getConstant(pdeData, t);

      temp = upperBoundary.getRightMatrixCondition(pdeData, grid, t);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xSteps - k];
      }
      q[xSteps] = sum + upperBoundary.getConstant(pdeData, t);

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

    return new PDETerminalResults1D(grid, f);
  }

  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final double[] timeGrid, final double[] spaceGrid, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary, final Surface<Double, Double, Double> freeBoundary) {
    Validate.notNull(pdeData, "pde data");

    final PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);

    final int tNodes = timeGrid.length;
    final int xNodes = spaceGrid.length;
    Validate.isTrue(tNodes > 1, "need at least 2 time nodes");
    Validate.isTrue(xNodes > 2, "need at least 3 space nodes");

    // TODO would like more sophistication that simply checking to the grid is consistent with the boundary level
    Validate.isTrue(Math.abs(spaceGrid[0] - lowerBoundary.getLevel()) < 1e-7, "space grid not consistent with boundary level");
    Validate.isTrue(Math.abs(spaceGrid[xNodes - 1] - upperBoundary.getLevel()) < 1e-7, "space grid not consistent with boundary level");

    final double[] dt = new double[tNodes - 1];
    for (int n = 0; n < tNodes - 1; n++) {
      dt[n] = timeGrid[n + 1] - timeGrid[n];
      Validate.isTrue(dt[n] > 0, "time steps must be increasing");
    }

    final double[] dx = new double[xNodes - 1];
    for (int i = 0; i < xNodes - 1; i++) {
      dx[i] = spaceGrid[i + 1] - spaceGrid[i];
      Validate.isTrue(dx[i] > 0, "space steps must be increasing");
    }

    final double[] f = new double[xNodes];
    final double[] q = new double[xNodes];
    final double[][] m = new double[xNodes][xNodes];

    double a, b, c, aa, bb, cc;

    for (int i = 0; i < xNodes; i++) {
      f[i] = pdeData.getInitialValue(spaceGrid[i]);
    }

    for (int n = 1; n < tNodes; n++) {

      for (int i = 1; i < xNodes - 1; i++) {
        a = pdeData.getA(timeGrid[n - 1], spaceGrid[i]);
        b = pdeData.getB(timeGrid[n - 1], spaceGrid[i]);
        c = pdeData.getC(timeGrid[n - 1], spaceGrid[i]);

        aa = (1 - _theta) * dt[n - 1] * (-2 / dx[i - 1] / (dx[i - 1] + dx[i]) * a + dx[i] / dx[i - 1] / (dx[i - 1] + dx[i]) * b);
        bb = 1 + (1 - _theta) * dt[n - 1] * (2 / dx[i - 1] / dx[i] * a - (dx[i] - dx[i - 1]) / dx[i - 1] / dx[i] * b - c);
        cc = (1 - _theta) * dt[n - 1] * (-2 / dx[i] / (dx[i - 1] + dx[i]) * a - dx[i - 1] / dx[i] / (dx[i - 1] + dx[i]) * b);
        q[i] = aa * f[i - 1] + bb * f[i] + cc * f[i + 1];

        // TODO could store these
        a = pdeData.getA(timeGrid[n], spaceGrid[i]);
        b = pdeData.getB(timeGrid[n], spaceGrid[i]);
        c = pdeData.getC(timeGrid[n], spaceGrid[i]);
        aa = dt[n - 1] * (-2 / dx[i - 1] / (dx[i - 1] + dx[i]) * a + dx[i] / dx[i - 1] / (dx[i - 1] + dx[i]) * b);
        bb = dt[n - 1] * (2 / dx[i - 1] / dx[i] * a - (dx[i] - dx[i - 1]) / dx[i - 1] / dx[i] * b - c);
        cc = dt[n - 1] * (-2 / dx[i] / (dx[i - 1] + dx[i]) * a - dx[i - 1] / dx[i] / (dx[i - 1] + dx[i]) * b);
        m[i][i - 1] = -_theta * aa;
        m[i][i] = 1 - _theta * bb;
        m[i][i + 1] = -_theta * cc;
      }

      double[] temp = lowerBoundary.getLeftMatrixCondition(pdeData, grid, timeGrid[n]);
      for (int k = 0; k < temp.length; k++) {
        m[0][k] = temp[k];
      }

      temp = upperBoundary.getLeftMatrixCondition(pdeData, grid, timeGrid[n]);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes - 1][xNodes - 1 - k] = temp[k];
      }
      // debug
      // m[xNodes - 1][xNodes - 3] = 2 / dx[xNodes - 3] / (dx[xNodes - 3] + dx[xNodes - 2]);
      // m[xNodes - 1][xNodes - 2] = -2 / dx[xNodes - 3] / dx[xNodes - 2];
      // m[xNodes - 1][xNodes - 1] = 2 / dx[xNodes - 2] / (dx[xNodes - 3] + dx[xNodes - 2]);

      temp = lowerBoundary.getRightMatrixCondition(pdeData, grid, timeGrid[n]);
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[0] = sum + lowerBoundary.getConstant(pdeData, timeGrid[n]); // TODO need to change how boundary are calculated - dx[0] wrong for non-constant grid

      temp = upperBoundary.getRightMatrixCondition(pdeData, grid, timeGrid[n]);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[xNodes - 1] = sum + upperBoundary.getConstant(pdeData, timeGrid[n]);

      // SOR
      final double omega = 1.0;
      double scale = 1.0;
      double errorSqr = Double.POSITIVE_INFINITY;
      while (errorSqr / (scale + 1e-10) > 1e-18) {
        errorSqr = 0.0;
        scale = 0.0;
        for (int j = 0; j < xNodes; j++) {
          sum = 0;
          for (int k = 0; k < xNodes; k++) {
            sum += m[j][k] * f[k];
          }
          double correction = omega / m[j][j] * (q[j] - sum);
          if (freeBoundary != null) {
            correction = Math.max(correction, freeBoundary.getZValue(timeGrid[n], spaceGrid[j]) - f[j]);
          }
          errorSqr += correction * correction;
          f[j] += correction;
          scale += f[j] * f[j];
        }
      }

    }

    return new PDETerminalResults1D(grid, f);

  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary) {
    throw new NotImplementedException();
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary,
      final Surface<Double, Double, Double> freeBoundary) {
    throw new NotImplementedException();
  }
}
