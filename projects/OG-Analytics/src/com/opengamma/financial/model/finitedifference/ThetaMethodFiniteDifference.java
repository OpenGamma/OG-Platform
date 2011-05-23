/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.math.surface.Surface;

/**
 * A theta (i.e. weighted between explicit and implicit time stepping) scheme using SOR algorithm to solve the matrix system at each time step 
 * This uses the exponentially fitted scheme of duffy 
 */
public class ThetaMethodFiniteDifference implements ConvectionDiffusionPDESolver {

  private final double _theta;
  private final boolean _showFullResults;

  /**
   * Sets up a standard Crank-Nicolson scheme 
   */
  public ThetaMethodFiniteDifference() {
    _theta = 0.5;
    _showFullResults = false;
  }

  /**
   * Sets up a scheme that is the weighted average of an explicit and an implicit scheme 
   * @param theta The weight. theta = 0 - fully explicit, theta = 0.5 - Crank-Nicolson, theta = 1.0 - fully implicit 
   */
  public ThetaMethodFiniteDifference(final double theta, final boolean showFullResults) {
    Validate.isTrue(theta >= 0 && theta <= 1.0, "theta must be in the range 0 to 1");
    _theta = theta;
    _showFullResults = showFullResults;
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary,
      final Surface<Double, Double, Double> freeBoundary) {
    Validate.notNull(pdeData, "pde data");
    Validate.notNull(grid, "need a grid");
    validateSetup(grid, lowerBoundary, upperBoundary);

    final int tNodes = grid.getNumTimeNodes();
    final int xNodes = grid.getNumSpaceNodes();

    final double[] f = new double[xNodes];
    double[][] full = null;
    if (_showFullResults) {
      full = new double[tNodes][xNodes];
    }
    final double[] q = new double[xNodes];
    final double[][] m = new double[xNodes][xNodes];

    double[] rho1 = new double[xNodes - 2];
    final double[] rho2 = new double[xNodes - 2];
    double[] a1 = new double[xNodes - 2];
    final double[] a2 = new double[xNodes - 2];
    double[] b1 = new double[xNodes - 2];
    final double[] b2 = new double[xNodes - 2];
    double[] c1 = new double[xNodes - 2];
    final double[] c2 = new double[xNodes - 2];

    double dt, t1, t2, x;
    double[] x1st, x2nd;

    for (int i = 0; i < xNodes; i++) {
      f[i] = pdeData.getInitialValue(grid.getSpaceNode(i));
    }
    if (_showFullResults) {
      full[0] = Arrays.copyOf(f, f.length);
    }

    for (int i = 0; i < xNodes - 2; i++) {
      x = grid.getSpaceNode(i + 1);
      a1[i] = pdeData.getA(0, x);
      b1[i] = pdeData.getB(0, x);
      c1[i] = pdeData.getC(0, x);
      rho1[i] = getFittingParameter(grid, a1[i], b1[i], i + 1);
    }

    for (int n = 1; n < tNodes; n++) {
      t1 = grid.getTimeNode(n - 1);
      t2 = grid.getTimeNode(n);
      dt = grid.getTimeStep(n - 1);

      for (int i = 1; i < xNodes - 1; i++) {
        x = grid.getSpaceNode(i);
        x1st = grid.getFirstDerivativeCoefficients(i);
        x2nd = grid.getSecondDerivativeCoefficients(i);

        q[i] = f[i];
        q[i] -= (1 - _theta) * dt * (x2nd[0] * rho1[i - 1] + x1st[0] * b1[i - 1]) * f[i - 1];
        q[i] -= (1 - _theta) * dt * (x2nd[1] * rho1[i - 1] + x1st[1] * b1[i - 1] + c1[i - 1]) * f[i];
        q[i] -= (1 - _theta) * dt * (x2nd[2] * rho1[i - 1] + x1st[2] * b1[i - 1]) * f[i + 1];

        a2[i - 1] = pdeData.getA(t2, x);
        b2[i - 1] = pdeData.getB(t2, x);
        c2[i - 1] = pdeData.getC(t2, x);
        rho2[i - 1] = getFittingParameter(grid, a2[i - 1], b2[i - 1], i);

        m[i][i - 1] = _theta * dt * (x2nd[0] * rho2[i - 1] + x1st[0] * b2[i - 1]);
        m[i][i] = 1 + _theta * dt * (x2nd[1] * rho2[i - 1] + x1st[1] * b2[i - 1] + c2[i - 1]);
        m[i][i + 1] = _theta * dt * (x2nd[2] * rho2[i - 1] + x1st[2] * b2[i - 1]);
      }

      double[] temp = lowerBoundary.getLeftMatrixCondition(pdeData, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[0][k] = temp[k];
      }

      temp = upperBoundary.getLeftMatrixCondition(pdeData, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes - 1][xNodes - temp.length + k] = temp[k];
      }

      temp = lowerBoundary.getRightMatrixCondition(pdeData, grid, t1);
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[0] = sum + lowerBoundary.getConstant(pdeData, t2);

      temp = upperBoundary.getRightMatrixCondition(pdeData, grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[xNodes - 1] = sum + upperBoundary.getConstant(pdeData, t2);

      sor(grid, freeBoundary, xNodes, f, q, m, t2);
      if (_showFullResults) {
        full[n] = Arrays.copyOf(f, f.length);
      }

      a1 = a2;
      b1 = b2;
      c1 = c2;
      rho1 = rho2;
    }

    PDEResults1D res;
    if (_showFullResults) {
      res = new PDEFullResults1D(grid, full);
    } else {
      res = new PDETerminalResults1D(grid, f);
    }
    return res;

  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary) {
    return solve(pdeData, tSteps, xSteps, tMax, lowerBoundary, upperBoundary, null);
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary,
      final Surface<Double, Double, Double> freeBoundary) {
    final PDEGrid1D grid = new PDEGrid1D(tSteps + 1, xSteps + 1, tMax, lowerBoundary.getLevel(), upperBoundary.getLevel());
    return solve(pdeData, grid, lowerBoundary, upperBoundary, freeBoundary);
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary) {
    return solve(pdeData, grid, lowerBoundary, upperBoundary, null);
  }

  /**
   * Checks that the lower and upper boundaries match up with the grid
   * @param grid
   * @param lowerBoundary
   * @param upperBoundary
   */
  private void validateSetup(final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary) {
    // TODO would like more sophistication that simply checking to the grid is consistent with the boundary level
    Validate.isTrue(Math.abs(grid.getSpaceNode(0) - lowerBoundary.getLevel()) < 1e-7, "space grid not consistent with boundary level");
    Validate.isTrue(Math.abs(grid.getSpaceNode(grid.getNumSpaceNodes() - 1) - upperBoundary.getLevel()) < 1e-7, "space grid not consistent with boundary level");
  }

  /**
   * @param grid
   * @param freeBoundary
   * @param xNodes
   * @param f
   * @param q
   * @param m
   * @param t2
   */
  private void sor(final PDEGrid1D grid, final Surface<Double, Double, Double> freeBoundary, final int xNodes, final double[] f, final double[] q, final double[][] m, final double t2) {
    double sum;
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
          correction = Math.max(correction, freeBoundary.getZValue(t2, grid.getSpaceNode(j)) - f[j]);
        }
        errorSqr += correction * correction;
        f[j] += correction;
        scale += f[j] * f[j];
      }
    }
  }

  /**
   * @param grid
   * @param a
   * @param b
   * @param i
   * @return
   */
  private double getFittingParameter(final PDEGrid1D grid, final double a, final double b, final int i) {
    double rho;
    final double[] x1st = grid.getFirstDerivativeCoefficients(i);
    final double[] x2nd = grid.getSecondDerivativeCoefficients(i);
    final double bdx1 = (b * grid.getSpaceStep(i - 1));
    final double bdx2 = (b * grid.getSpaceStep(i));

    // convection dominated
    if (Math.abs(bdx1) > 10 * Math.abs(a) || Math.abs(bdx2) > 10 * Math.abs(a)) {
      if (b > 0) {
        rho = -b * x1st[0] / x2nd[0];
      } else {
        rho = -b * x1st[2] / x2nd[2];
      }
    } else if (Math.abs(a) > 10 * Math.abs(bdx1) || Math.abs(a) > 10 * Math.abs(bdx2)) {
      rho = a; // diffusion dominated
    } else {
      final double expo1 = Math.exp(bdx1 / a);
      final double expo2 = Math.exp(-bdx2 / a);
      rho = -b * (x1st[0] * expo1 + x1st[1] + x1st[2] * expo2) / (x2nd[0] * expo1 + x2nd[1] + x2nd[2] * expo2);
    }
    return rho;
  }

}
