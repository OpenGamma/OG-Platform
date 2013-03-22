/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CoupledFiniteDifference {
  private static final Decomposition<?> DCOMP = new LUDecompositionCommons();

  private final double _theta;
  private final boolean _showFullResults;

  /**
   * 
   */
  public CoupledFiniteDifference() {
    _theta = 0.5;
    _showFullResults = true;
  }

  public CoupledFiniteDifference(final double theta, final boolean showFullResults) {
    _theta = theta;
    _showFullResults = showFullResults;
  }

  public double getTheta() {
    return _theta;
  }

  public boolean showFullResults() {
    return false;
  }

  public Decomposition<?> getDecomposition() {
    return DCOMP;
  }

  public PDEResults1D[] solve(final CoupledPDEDataBundle pdeData1,
      final CoupledPDEDataBundle pdeData2) {
    Validate.notNull(pdeData1, "pde1 data");
    Validate.notNull(pdeData2, "pde2 data");

    final PDEGrid1D grid = pdeData1.getGrid();
    ArgumentChecker.isTrue(grid == pdeData2.getGrid(), "grids must be same object");
    final ConvectionDiffusionPDE1DCoupledCoefficients coeff1 = pdeData1.getCoefficients();
    final ConvectionDiffusionPDE1DCoupledCoefficients coeff2 = pdeData2.getCoefficients();
    final double[] initalCond1 = pdeData1.getInitialCondition();
    final double[] initalCond2 = pdeData2.getInitialCondition();
    final BoundaryCondition lower1 = pdeData1.getLowerBoundary();
    final BoundaryCondition lower2 = pdeData2.getLowerBoundary();
    final BoundaryCondition upper1 = pdeData1.getUpperBoundary();
    final BoundaryCondition upper2 = pdeData2.getUpperBoundary();
    final double lambda1 = coeff1.getLambda();
    final double lambda2 = coeff2.getLambda();

    final int tNodes = grid.getNumTimeNodes();
    final int xNodes = grid.getNumSpaceNodes();

    double[] f = new double[2 * xNodes];
    double[][] full1 = null;
    double[][] full2 = null;
    if (_showFullResults) {
      full1 = new double[tNodes][xNodes];
      full2 = new double[tNodes][xNodes];
    }
    final double[] q = new double[2 * xNodes];
    final double[][] m = new double[2 * xNodes][2 * xNodes];

    double[][] a1 = new double[2][xNodes - 2];
    final double[][] a2 = new double[2][xNodes - 2];
    double[][] b1 = new double[2][xNodes - 2];
    final double[][] b2 = new double[2][xNodes - 2];
    double[][] c1 = new double[2][xNodes - 2];
    final double[][] c2 = new double[2][xNodes - 2];

    //    final double omega = 1.5;
    //    final int oldCount = 0;
    //    final boolean omegaIncrease = false;

    double dt, t1, t2, x;
    double[] x1st, x2nd;

    System.arraycopy(initalCond1, 0, f, 0, xNodes);
    System.arraycopy(initalCond2, 0, f, xNodes, xNodes);

    if (_showFullResults) {
      if (full1 != null && full2 != null) {
        full1[0] = Arrays.copyOfRange(f, 0, xNodes);
        full2[0] = Arrays.copyOfRange(f, xNodes, 2 * xNodes);
      }
    }

    for (int i = 0; i < xNodes - 2; i++) {
      x = grid.getSpaceNode(i + 1);
      a1[0][i] = coeff1.getA(0, x);
      b1[0][i] = coeff1.getB(0, x);
      c1[0][i] = coeff1.getC(0, x);

      a1[1][i] = coeff2.getA(0, x);
      b1[1][i] = coeff2.getB(0, x);
      c1[1][i] = coeff2.getC(0, x);
    }

    final boolean first = true;
    DecompositionResult decompRes = null;

    for (int n = 1; n < tNodes; n++) {

      t1 = grid.getTimeNode(n - 1);
      t2 = grid.getTimeNode(n);
      dt = grid.getTimeStep(n - 1);

      for (int i = 1; i < xNodes - 1; i++) {

        x = grid.getSpaceNode(i);
        x1st = grid.getFirstDerivativeCoefficients(i);
        x2nd = grid.getSecondDerivativeCoefficients(i);

        q[i] = f[i];
        q[i] -= (1 - _theta) * dt * (x2nd[0] * a1[0][i - 1] + x1st[0] * b1[0][i - 1]) * f[i - 1];
        q[i] -= (1 - _theta) * dt * (x2nd[1] * a1[0][i - 1] + x1st[1] * b1[0][i - 1] + c1[0][i - 1]) * f[i];
        q[i] -= (1 - _theta) * dt * (x2nd[2] * a1[0][i - 1] + x1st[2] * b1[0][i - 1]) * f[i + 1];
        q[i] -= (1 - _theta) * dt * lambda1 * f[i + xNodes];

        q[xNodes + i] = f[xNodes + i];
        q[xNodes + i] -= (1 - _theta) * dt * (x2nd[0] * a1[1][i - 1] + x1st[0] * b1[1][i - 1]) * f[xNodes + i - 1];
        q[xNodes + i] -= (1 - _theta) * dt * (x2nd[1] * a1[1][i - 1] + x1st[1] * b1[1][i - 1] + c1[1][i - 1]) * f[xNodes + i];
        q[xNodes + i] -= (1 - _theta) * dt * (x2nd[2] * a1[1][i - 1] + x1st[2] * b1[1][i - 1]) * f[xNodes + i + 1];
        q[xNodes + i] -= (1 - _theta) * dt * lambda2 * f[i];

        a2[0][i - 1] = coeff1.getA(t2, x);
        b2[0][i - 1] = coeff1.getB(t2, x);
        c2[0][i - 1] = coeff1.getC(t2, x);

        a2[1][i - 1] = coeff2.getA(t2, x);
        b2[1][i - 1] = coeff2.getB(t2, x);
        c2[1][i - 1] = coeff2.getC(t2, x);

        m[i][i - 1] = _theta * dt * (x2nd[0] * a2[0][i - 1] + x1st[0] * b2[0][i - 1]);
        m[i][i] = 1 + _theta * dt * (x2nd[1] * a2[0][i - 1] + x1st[1] * b2[0][i - 1] + c2[0][i - 1]);
        m[i][i + 1] = _theta * dt * (x2nd[2] * a2[0][i - 1] + x1st[2] * b2[0][i - 1]);
        m[i][i + xNodes] = dt * _theta * lambda1;

        m[xNodes + i][xNodes + i - 1] = _theta * dt * (x2nd[0] * a2[1][i - 1] + x1st[0] * b2[1][i - 1]);
        m[xNodes + i][xNodes + i] = 1 + _theta * dt * (x2nd[1] * a2[1][i - 1] + x1st[1] * b2[1][i - 1] + c2[1][i - 1]);
        m[xNodes + i][xNodes + i + 1] = _theta * dt * (x2nd[2] * a2[1][i - 1] + x1st[2] * b2[1][i - 1]);
        m[xNodes + i][i] = dt * _theta * lambda2;
      }

      double[] temp = lower1.getLeftMatrixCondition(pdeData1.getCoefficients(), grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[0][k] = temp[k];
      }

      temp = upper1.getLeftMatrixCondition(pdeData1.getCoefficients(), grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes - 1][xNodes - temp.length + k] = temp[k];
      }

      temp = lower2.getLeftMatrixCondition(pdeData2.getCoefficients(), grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes][xNodes + k] = temp[k];
      }

      temp = upper2.getLeftMatrixCondition(pdeData2.getCoefficients(), grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[2 * xNodes - 1][2 * xNodes - temp.length + k] = temp[k];
      }

      temp = lower1.getRightMatrixCondition(pdeData1.getCoefficients(), grid, t1);
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[0] = sum + lower1.getConstant(pdeData1.getCoefficients(), t2);

      temp = upper1.getRightMatrixCondition(pdeData1.getCoefficients(), grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[xNodes - 1] = sum + upper1.getConstant(pdeData1.getCoefficients(), t2);

      temp = lower2.getRightMatrixCondition(pdeData2.getCoefficients(), grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[xNodes] = sum + lower2.getConstant(pdeData2.getCoefficients(), t2);

      temp = upper2.getRightMatrixCondition(pdeData2.getCoefficients(), grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[2 * xNodes - 1] = sum + upper2.getConstant(pdeData2.getCoefficients(), t2);

      //TODO work out why SOR does not converge here
      //      final DoubleMatrix2D mM = new DoubleMatrix2D(m);
      //      final DecompositionResult res = DCOMP.evaluate(mM);
      //      f = res.solve(q);

      //      // SOR
      //
      //      int count = sor(omega, grid, freeBoundary, xNodes, f, q, m, t2);
      //      if (oldCount > 0) {
      //        if ((omegaIncrease && count > oldCount) || (!omegaIncrease && count < oldCount)) {
      //          omega = Math.max(1.0, omega * 0.9);
      //          omegaIncrease = false;
      //        } else {
      //          omega = Math.min(1.99, 1.1 * omega);
      //          omegaIncrease = true;
      //        }
      //      }
      //      oldCount = count;

      if (first) {
        final DoubleMatrix2D mM = new DoubleMatrix2D(m);
        decompRes = DCOMP.evaluate(mM);

        // first = false;
      }

      f = decompRes.solve(q);

      a1 = a2;
      b1 = b2;
      c1 = c2;

      if (_showFullResults) {
        if (full1 != null && full2 != null) {
          full1[n] = Arrays.copyOfRange(f, 0, xNodes);
          full2[n] = Arrays.copyOfRange(f, xNodes, 2 * xNodes);
        }
      }

    }
    final PDEResults1D[] res = new PDEResults1D[2];

    if (_showFullResults) {
      res[0] = new PDEFullResults1D(grid, full1);
      res[1] = new PDEFullResults1D(grid, full2);
    } else {
      final double[] res1 = Arrays.copyOfRange(f, 0, xNodes);
      final double[] res2 = Arrays.copyOfRange(f, xNodes, 2 * xNodes);
      res[0] = new PDETerminalResults1D(grid, res1);
      res[1] = new PDETerminalResults1D(grid, res2);
    }

    return res;

  }

  @SuppressWarnings("unused")
  private int sor(final double omega, final PDEGrid1D grid, final Surface<Double, Double, Double> freeBoundary, final int xNodes, final double[] f, final double[] q, final double[][] m,
      final double t2) {
    double sum;
    int count = 0;
    double scale = 1.0;
    double errorSqr = Double.POSITIVE_INFINITY;
    while (errorSqr / (scale + 1e-10) > 1e-18) {
      errorSqr = 0.0;
      scale = 0.0;
      for (int j = 0; j < 2 * xNodes; j++) {
        sum = 0;
        for (int k = 0; k < 2 * xNodes; k++) {
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
      count++;
    }
    return count;
  }

}
