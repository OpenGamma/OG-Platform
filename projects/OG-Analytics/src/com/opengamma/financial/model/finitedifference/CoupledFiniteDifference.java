/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class CoupledFiniteDifference {
  private static final Decomposition<?> DCOMP = new LUDecompositionCommons();

  private final double _theta;

  /**
   * 
   */
  public CoupledFiniteDifference() {
    _theta = 0.5;
  }

  public CoupledFiniteDifference(final double theta) {
    _theta = theta;
  }

  public PDEResults1D[] solve(final ConvectionDiffusionPDEDataBundle pdeData1, final ConvectionDiffusionPDEDataBundle pdeData2, final PDEGrid1D grid, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary, final double lambda12, final double lambda21, final Surface<Double, Double, Double> freeBoundary) {
    Validate.notNull(pdeData1, "pde1 data");
    Validate.notNull(pdeData2, "pde2 data");

    validateSetup(grid, lowerBoundary, upperBoundary);

    int tNodes = grid.getNumTimeNodes();
    int xNodes = grid.getNumSpaceNodes();

    double[] f = new double[2 * xNodes];
    final double[] q = new double[2 * xNodes];
    final double[][] m = new double[2 * xNodes][2 * xNodes];

    double[][] rho1 = new double[2][xNodes - 2];
    double[][] rho2 = new double[2][xNodes - 2];
    double[][] a1 = new double[2][xNodes - 2];
    double[][] a2 = new double[2][xNodes - 2];
    double[][] b1 = new double[2][xNodes - 2];
    double[][] b2 = new double[2][xNodes - 2];
    double[][] c1 = new double[2][xNodes - 2];
    double[][] c2 = new double[2][xNodes - 2];

    double dt, t1, t2, x;
    double[] x1st, x2nd;

    for (int i = 0; i < xNodes; i++) {
      f[i] = pdeData1.getInitialValue(grid.getSpaceNode(i));
    }
    for (int i = 0; i < xNodes; i++) {
      f[i + xNodes] = pdeData1.getInitialValue(grid.getSpaceNode(i));
    }

    for (int i = 0; i < xNodes - 2; i++) {
      x = grid.getSpaceNode(i + 1);
      a1[0][i] = pdeData1.getA(0, x);
      b1[0][i] = pdeData1.getB(0, x);
      c1[0][i] = pdeData1.getC(0, x);
      rho1[0][i] = getFittingParameter(grid, a1[0][i], b1[0][i], i + 1);
      a1[1][i] = pdeData2.getA(0, x);
      b1[1][i] = pdeData2.getB(0, x);
      c1[1][i] = pdeData2.getC(0, x);
      rho1[1][i] = getFittingParameter(grid, a1[1][i], b1[1][i], i + 1);
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
        q[i] -= (1 - _theta) * dt * (x2nd[0] * rho1[0][i - 1] + x1st[0] * b1[0][i - 1]) * f[i - 1];
        q[i] -= (1 - _theta) * dt * (x2nd[1] * rho1[0][i - 1] + x1st[1] * b1[0][i - 1] + c1[0][i - 1] + lambda12) * f[i];
        q[i] -= (1 - _theta) * dt * (x2nd[2] * rho1[0][i - 1] + x1st[2] * b1[0][i - 1]) * f[i + 1];
        q[i] += (1 - _theta) * dt * lambda12 * f[i + xNodes];

        q[xNodes + i] = f[xNodes + i];
        q[xNodes + i] -= (1 - _theta) * dt * (x2nd[0] * rho1[1][i - 1] + x1st[0] * b1[1][i - 1]) * f[xNodes + i - 1];
        q[xNodes + i] -= (1 - _theta) * dt * (x2nd[1] * rho1[1][i - 1] + x1st[1] * b1[1][i - 1] + c1[1][i - 1] + lambda21) * f[xNodes + i];
        q[xNodes + i] -= (1 - _theta) * dt * (x2nd[2] * rho1[1][i - 1] + x1st[2] * b1[1][i - 1]) * f[xNodes + i + 1];
        q[xNodes + i] += (1 - _theta) * dt * lambda21 * f[i];

        a2[0][i - 1] = pdeData1.getA(t2, x);
        b2[0][i - 1] = pdeData1.getB(t2, x);
        c2[0][i - 1] = pdeData1.getC(t2, x);
        rho2[0][i - 1] = getFittingParameter(grid, a2[0][i - 1], b2[0][i - 1], i);
        a2[1][i - 1] = pdeData2.getA(t2, x);
        b2[1][i - 1] = pdeData2.getB(t2, x);
        c2[1][i - 1] = pdeData2.getC(t2, x);
        rho2[1][i - 1] = getFittingParameter(grid, a2[1][i - 1], b2[1][i - 1], i);

        m[i][i - 1] = _theta * dt * (x2nd[0] * rho2[0][i - 1] + x1st[0] * b2[0][i - 1]);
        m[i][i] = 1 + _theta * dt * (x2nd[1] * rho2[0][i - 1] + x1st[1] * b2[0][i - 1] + c2[0][i - 1] + lambda12);
        m[i][i + 1] = _theta * dt * (x2nd[2] * rho2[0][i - 1] + x1st[2] * b2[0][i - 1]);
        m[i][i + xNodes] = -dt * _theta * lambda12;

        m[xNodes + i][xNodes + i - 1] = _theta * dt * (x2nd[0] * rho2[1][i - 1] + x1st[0] * b2[1][i - 1]);
        m[xNodes + i][xNodes + i] = 1 + _theta * dt * (x2nd[1] * rho2[1][i - 1] + x1st[1] * b2[1][i - 1] + c2[1][i - 1] + lambda21);
        m[xNodes + i][xNodes + i + 1] = _theta * dt * (x2nd[2] * rho2[1][i - 1] + x1st[2] * b2[1][i - 1]);
        m[xNodes + i][i] = -dt * _theta * lambda21;
      }

      double[] temp = lowerBoundary.getLeftMatrixCondition(pdeData1, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[0][k] = temp[k];
      }

      temp = upperBoundary.getLeftMatrixCondition(pdeData1, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes - 1][xNodes - 1 - k] = temp[k];
      }

      temp = lowerBoundary.getLeftMatrixCondition(pdeData2, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes][xNodes + k] = temp[k];
      }

      temp = upperBoundary.getLeftMatrixCondition(pdeData2, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[2 * xNodes - 1][2 * xNodes - 1 - k] = temp[k];
      }

      temp = lowerBoundary.getRightMatrixCondition(pdeData1, grid, t1);
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[0] = sum + lowerBoundary.getConstant(pdeData1, t2);

      temp = upperBoundary.getRightMatrixCondition(pdeData1, grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[xNodes - 1] = sum + upperBoundary.getConstant(pdeData1, t2);

      temp = lowerBoundary.getRightMatrixCondition(pdeData2, grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[xNodes] = sum + lowerBoundary.getConstant(pdeData2, t2);

      temp = upperBoundary.getRightMatrixCondition(pdeData2, grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[2 * xNodes - 1] = sum + upperBoundary.getConstant(pdeData2, t2);

      final DoubleMatrix2D mM = new DoubleMatrix2D(m);
      final DecompositionResult res = DCOMP.evaluate(mM);
      f = res.solve(q);

      a1 = a2;
      b1 = b2;
      c1 = c2;
      rho1 = rho2;

      // SOR
      // final double omega = 1.0;
      // double scale = 1.0;
      // double errorSqr = Double.POSITIVE_INFINITY;
      // while (errorSqr / (scale + 1e-10) > 1e-18) {
      // errorSqr = 0.0;
      // scale = 0.0;
      // for (int j = 0; j < 2 * xNodes; j++) {
      // sum = 0;
      // for (int k = 0; k < 2 * xNodes; k++) {
      // sum += m[j][k] * f[k];
      // }
      // double correction = omega / m[j][j] * (q[j] - sum);
      // // if (freeBoundary != null) {
      // // correction = Math.max(correction, freeBoundary.getZValue(timeGrid[n], spaceGrid[j]) - f[j]);
      // // }
      // errorSqr += correction * correction;
      // f[j] += correction;
      // scale += f[j] * f[j];
      // }
      // }

    }

    final PDEResults1D[] res = new PDEResults1D[2];

    double[] res1 = new double[xNodes];
    double[] res2 = new double[xNodes];

    for (int i = 0; i < xNodes; i++) {
      res1[i] = f[i];
      res2[i] = f[i + xNodes];
    }
    res[0] = new PDETerminalResults1D(grid, res1);
    res[1] = new PDETerminalResults1D(grid, res2);
    return res;
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

  private double getFittingParameter(final PDEGrid1D grid, double a, double b, int i) {
    double rho;
    double[] x1st = grid.getFirstDerivativeCoefficients(i);
    double[] x2nd = grid.getSecondDerivativeCoefficients(i);
    double bdx1 = (b * grid.getSpaceStep(i - 1));
    double bdx2 = (b * grid.getSpaceStep(i));

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
      double expo1 = Math.exp(bdx1 / a);
      double expo2 = Math.exp(-bdx2 / a);
      rho = -b * (x1st[0] * expo1 + x1st[1] + x1st[2] * expo2) / (x2nd[0] * expo1 + x2nd[1] + x2nd[2] * expo2);
    }
    return rho;
  }
}
