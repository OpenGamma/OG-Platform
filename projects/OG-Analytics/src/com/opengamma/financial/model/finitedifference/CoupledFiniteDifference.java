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

  private final double _theta = 0.5;

  public double[][] solve(final ConvectionDiffusionPDEDataBundle pdeData1, final ConvectionDiffusionPDEDataBundle pdeData2, final double[] timeGrid, final double[] spaceGrid,
      final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary, final double lambda12, final double lambda21, final Surface<Double, Double, Double> freeBoundary) {
    Validate.notNull(pdeData1, "pde1 data");
    Validate.notNull(pdeData2, "pde2 data");
    PDEGrid1D grid = new PDEGrid1D(timeGrid, spaceGrid);
    int tNodes = timeGrid.length;
    int xNodes = spaceGrid.length;
    Validate.isTrue(tNodes > 1, "need at least 2 time nodes");
    Validate.isTrue(xNodes > 2, "need at least 3 space nodes");

    // TODO would like more sophistication that simply checking to the grid is consistent with the boundary level
    Validate.isTrue(Math.abs(spaceGrid[0] - lowerBoundary.getLevel()) < 1e-7, "space grid not consistent with boundary level");
    Validate.isTrue(Math.abs(spaceGrid[xNodes - 1] - upperBoundary.getLevel()) < 1e-7, "space grid not consistent with boundary level");

    double[] dt = new double[tNodes - 1];
    for (int n = 0; n < tNodes - 1; n++) {
      dt[n] = timeGrid[n + 1] - timeGrid[n];
      Validate.isTrue(dt[n] > 0, "time steps must be increasing");
    }

    double[] dx = new double[xNodes - 1];
    for (int i = 0; i < xNodes - 1; i++) {
      dx[i] = spaceGrid[i + 1] - spaceGrid[i];
      Validate.isTrue(dx[i] > 0, "space steps must be increasing");
    }

    // since the space grid is time independent, we can calculate the coefficients for derivatives once
    double[][] x1st = new double[xNodes - 2][3];
    double[][] x2nd = new double[xNodes - 2][3];
    for (int i = 0; i < xNodes - 2; i++) {
      x1st[i][0] = -dx[i + 1] / dx[i] / (dx[i] + dx[i + 1]);
      x1st[i][1] = (dx[i + 1] - dx[i]) / dx[i] / dx[i + 1];
      x1st[i][2] = dx[i] / dx[i + 1] / (dx[i] + dx[i + 1]);
      x2nd[i][0] = 2 / dx[i] / (dx[i] + dx[i + 1]);
      x2nd[i][1] = -2 / dx[i] / dx[i + 1];
      x2nd[i][2] = 2 / dx[i + 1] / (dx[i] + dx[i + 1]);
    }

    double[] f = new double[2 * xNodes];
    final double[] q = new double[2 * xNodes];
    final double[][] m = new double[2 * xNodes][2 * xNodes];

    double a, b, c, aa, bb, cc, sum;

    for (int i = 0; i < xNodes; i++) {
      f[i] = pdeData1.getInitialValue(spaceGrid[i]);
    }
    for (int i = 0; i < xNodes; i++) {
      f[i + xNodes] = pdeData1.getInitialValue(spaceGrid[i]);
    }

    for (int n = 1; n < tNodes; n++) {

      for (int i = 1; i < xNodes - 1; i++) {
        a = pdeData1.getA(timeGrid[n - 1], spaceGrid[i]);
        b = pdeData1.getB(timeGrid[n - 1], spaceGrid[i]);
        c = pdeData1.getC(timeGrid[n - 1], spaceGrid[i]);

        sum = 0;
        sum -= (1 - _theta) * dt[n - 1] * (x2nd[i - 1][0] * a + x1st[i - 1][0] * b) * f[i - 1];
        sum += (1 - (1 - _theta) * dt[n - 1] * (lambda12 + x2nd[i - 1][1] * a + x1st[i - 1][1] * b + c)) * f[i];
        sum -= (1 - _theta) * dt[n - 1] * (x2nd[i - 1][2] * a + x1st[i - 1][2] * b) * f[i + 1];
        sum += (1 - _theta) * dt[n - 1] * lambda12 * f[i + xNodes];
        q[i] = sum;

        a = pdeData2.getA(timeGrid[n - 1], spaceGrid[i]);
        b = pdeData2.getB(timeGrid[n - 1], spaceGrid[i]);
        c = pdeData2.getC(timeGrid[n - 1], spaceGrid[i]);

        sum = 0;
        sum -= (1 - _theta) * dt[n - 1] * (x2nd[i - 1][0] * a + x1st[i - 1][0] * b) * f[xNodes + i - 1];
        sum += (1 - (1 - _theta) * dt[n - 1] * (lambda21 + x2nd[i - 1][1] * a + x1st[i - 1][1] * b + c)) * f[xNodes + i];
        sum -= (1 - _theta) * dt[n - 1] * (x2nd[i - 1][2] * a + x1st[i - 1][2] * b) * f[xNodes + i + 1];
        sum += (1 - _theta) * dt[n - 1] * lambda21 * f[i];
        q[xNodes + i] = sum;

        // TODO could store these
        a = pdeData1.getA(timeGrid[n], spaceGrid[i]);
        b = pdeData1.getB(timeGrid[n], spaceGrid[i]);
        c = pdeData1.getC(timeGrid[n], spaceGrid[i]);
        aa = dt[n - 1] * (x2nd[i - 1][0] * a + x1st[i - 1][0] * b);
        bb = dt[n - 1] * (x2nd[i - 1][1] * a + x1st[i - 1][1] * b + c);
        cc = dt[n - 1] * (x2nd[i - 1][2] * a + x1st[i - 1][2] * b);
        m[i][i - 1] = _theta * aa;
        m[i][i] = 1 + dt[n - 1] * _theta * lambda12 + _theta * bb;
        m[i][i + 1] = _theta * cc;
        m[i][i + xNodes] = -dt[n - 1] * _theta * lambda12;

        a = pdeData2.getA(timeGrid[n], spaceGrid[i]);
        b = pdeData2.getB(timeGrid[n], spaceGrid[i]);
        c = pdeData2.getC(timeGrid[n], spaceGrid[i]);
        aa = dt[n - 1] * (x2nd[i - 1][0] * a + x1st[i - 1][0] * b);
        bb = dt[n - 1] * (x2nd[i - 1][1] * a + x1st[i - 1][1] * b + c);
        cc = dt[n - 1] * (x2nd[i - 1][2] * a + x1st[i - 1][2] * b);
        m[i + xNodes][i + xNodes - 1] = _theta * aa;
        m[i + xNodes][i + xNodes] = 1 + dt[n - 1] * _theta * lambda21 + _theta * bb;
        m[i + xNodes][i + xNodes + 1] = _theta * cc;
        m[i + xNodes][i] = -dt[n - 1] * _theta * lambda21;
      }

      double[] temp = lowerBoundary.getLeftMatrixCondition(pdeData1, grid, timeGrid[n]);
      for (int k = 0; k < temp.length; k++) {
        m[0][k] = temp[k];
      }

      temp = upperBoundary.getLeftMatrixCondition(pdeData1, grid, timeGrid[n]);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes - 1][xNodes - 1 - k] = temp[k];
      }

      temp = lowerBoundary.getLeftMatrixCondition(pdeData2, grid, timeGrid[n]);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes][xNodes + k] = temp[k];
      }

      temp = upperBoundary.getLeftMatrixCondition(pdeData2, grid, timeGrid[n]);
      for (int k = 0; k < temp.length; k++) {
        m[2 * xNodes - 1][2 * xNodes - 1 - k] = temp[k];
      }

      temp = lowerBoundary.getRightMatrixCondition(pdeData1, grid, timeGrid[n]);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[0] = sum + lowerBoundary.getConstant(pdeData1, timeGrid[n]);

      temp = upperBoundary.getRightMatrixCondition(pdeData1, grid, timeGrid[n]);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[xNodes - 1] = sum + upperBoundary.getConstant(pdeData1, timeGrid[n]);

      temp = lowerBoundary.getRightMatrixCondition(pdeData2, grid, timeGrid[n]);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[xNodes] = sum + lowerBoundary.getConstant(pdeData2, timeGrid[n]);

      temp = upperBoundary.getRightMatrixCondition(pdeData2, grid, timeGrid[n]);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[2 * xNodes - 1] = sum + upperBoundary.getConstant(pdeData2, timeGrid[n]);

      final DoubleMatrix2D mM = new DoubleMatrix2D(m);
      final DecompositionResult res = DCOMP.evaluate(mM);
      f = res.solve(q);

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

    final double[][] res = new double[3][];
    res[0] = spaceGrid;
    res[1] = new double[xNodes];
    res[2] = new double[xNodes];

    for (int i = 0; i < xNodes; i++) {
      res[1][i] = f[i];
      res[2][i] = f[i + xNodes];
    }
    return res;

  }
}
