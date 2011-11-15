/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.analysis.interpolation.BicubicSplineInterpolatingFunction;

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

  public PDEResults1D[] solve(final CoupledPDEDataBundle pdeData1, final CoupledPDEDataBundle pdeData2, final PDEGrid1D grid, final BoundaryCondition lowerBoundary1,
      final BoundaryCondition upperBoundary1, final BoundaryCondition lowerBoundary2, final BoundaryCondition upperBoundary2, final Surface<Double, Double, Double> freeBoundary) {
    Validate.notNull(pdeData1, "pde1 data");
    Validate.notNull(pdeData2, "pde2 data");

    validateSetup(grid, lowerBoundary1, upperBoundary1);
    validateSetup(grid, lowerBoundary2, upperBoundary2);

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

    double[][] rho1 = new double[2][xNodes - 2];
    final double[][] rho2 = new double[2][xNodes - 2];
    double[][] a1 = new double[2][xNodes - 2];
    final double[][] a2 = new double[2][xNodes - 2];
    double[][] b1 = new double[2][xNodes - 2];
    final double[][] b2 = new double[2][xNodes - 2];
    double[][] c1 = new double[2][xNodes - 2];
    final double[][] c2 = new double[2][xNodes - 2];
    final double lambda1 = pdeData1.getCoupling();
    final double lambda2 = pdeData2.getCoupling();

    //    final double omega = 1.5;
    //    final int oldCount = 0;
    //    final boolean omegaIncrease = false;

    double dt, t1, t2, x;
    double[] x1st, x2nd;

    for (int i = 0; i < xNodes; i++) {
      f[i] = pdeData1.getInitialValue(grid.getSpaceNode(i));
    }
    for (int i = 0; i < xNodes; i++) {
      f[i + xNodes] = pdeData2.getInitialValue(grid.getSpaceNode(i));
    }

    if (_showFullResults) {
      full1[0] = Arrays.copyOfRange(f, 0, xNodes);
      full2[0] = Arrays.copyOfRange(f, xNodes, 2 * xNodes);
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
        q[i] -= (1 - _theta) * dt * (x2nd[0] * rho1[0][i - 1] + x1st[0] * b1[0][i - 1]) * f[i - 1];
        q[i] -= (1 - _theta) * dt * (x2nd[1] * rho1[0][i - 1] + x1st[1] * b1[0][i - 1] + c1[0][i - 1]) * f[i];
        q[i] -= (1 - _theta) * dt * (x2nd[2] * rho1[0][i - 1] + x1st[2] * b1[0][i - 1]) * f[i + 1];
        q[i] -= (1 - _theta) * dt * lambda1 * f[i + xNodes];

        q[xNodes + i] = f[xNodes + i];
        q[xNodes + i] -= (1 - _theta) * dt * (x2nd[0] * rho1[1][i - 1] + x1st[0] * b1[1][i - 1]) * f[xNodes + i - 1];
        q[xNodes + i] -= (1 - _theta) * dt * (x2nd[1] * rho1[1][i - 1] + x1st[1] * b1[1][i - 1] + c1[1][i - 1]) * f[xNodes + i];
        q[xNodes + i] -= (1 - _theta) * dt * (x2nd[2] * rho1[1][i - 1] + x1st[2] * b1[1][i - 1]) * f[xNodes + i + 1];
        q[xNodes + i] -= (1 - _theta) * dt * lambda2 * f[i];

        a2[0][i - 1] = pdeData1.getA(t2, x);
        b2[0][i - 1] = pdeData1.getB(t2, x);
        c2[0][i - 1] = pdeData1.getC(t2, x);
        rho2[0][i - 1] = getFittingParameter(grid, a2[0][i - 1], b2[0][i - 1], i);
        a2[1][i - 1] = pdeData2.getA(t2, x);
        b2[1][i - 1] = pdeData2.getB(t2, x);
        c2[1][i - 1] = pdeData2.getC(t2, x);
        rho2[1][i - 1] = getFittingParameter(grid, a2[1][i - 1], b2[1][i - 1], i);

        m[i][i - 1] = _theta * dt * (x2nd[0] * rho2[0][i - 1] + x1st[0] * b2[0][i - 1]);
        m[i][i] = 1 + _theta * dt * (x2nd[1] * rho2[0][i - 1] + x1st[1] * b2[0][i - 1] + c2[0][i - 1]);
        m[i][i + 1] = _theta * dt * (x2nd[2] * rho2[0][i - 1] + x1st[2] * b2[0][i - 1]);
        m[i][i + xNodes] = dt * _theta * lambda1;

        m[xNodes + i][xNodes + i - 1] = _theta * dt * (x2nd[0] * rho2[1][i - 1] + x1st[0] * b2[1][i - 1]);
        m[xNodes + i][xNodes + i] = 1 + _theta * dt * (x2nd[1] * rho2[1][i - 1] + x1st[1] * b2[1][i - 1] + c2[1][i - 1]);
        m[xNodes + i][xNodes + i + 1] = _theta * dt * (x2nd[2] * rho2[1][i - 1] + x1st[2] * b2[1][i - 1]);
        m[xNodes + i][i] = dt * _theta * lambda2;
      }


      
      double[] temp = lowerBoundary1.getLeftMatrixCondition(pdeData1, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[0][k] = temp[k];
      }

      temp = upperBoundary1.getLeftMatrixCondition(pdeData1, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes - 1][xNodes - temp.length + k] = temp[k];
      }

      temp = lowerBoundary2.getLeftMatrixCondition(pdeData2, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes][xNodes + k] = temp[k];
      }

      temp = upperBoundary2.getLeftMatrixCondition(pdeData2, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[2 * xNodes - 1][2 * xNodes - temp.length + k] = temp[k];
      }

      temp = lowerBoundary1.getRightMatrixCondition(pdeData1, grid, t1);
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[0] = sum + lowerBoundary1.getConstant(pdeData1, t2);

      temp = upperBoundary1.getRightMatrixCondition(pdeData1, grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[xNodes - 1] = sum + upperBoundary1.getConstant(pdeData1, t2);

      temp = lowerBoundary2.getRightMatrixCondition(pdeData2, grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[xNodes] = sum + lowerBoundary2.getConstant(pdeData2, t2);

      temp = upperBoundary2.getRightMatrixCondition(pdeData2, grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[2 * xNodes - 1] = sum + upperBoundary2.getConstant(pdeData2, t2);

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
      rho1 = rho2;

      if (_showFullResults) {
        full1[n] = Arrays.copyOfRange(f, 0, xNodes);
        full2[n] = Arrays.copyOfRange(f, xNodes, 2 * xNodes);
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
    //debug
    //  System.out.println(count + " " + omega);
    return count;
  }

  /**
   * Checks that the lower and upper boundaries match up with the grid
   * @param grid The grid
   * @param lowerBoundary The lower boundary
   * @param upperBoundary The upper boundary
   */
  protected void validateSetup(final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary) {
    // TODO would like more sophistication that simply checking to the grid is consistent with the boundary level
    Validate.isTrue(Math.abs(grid.getSpaceNode(0) - lowerBoundary.getLevel()) < 1e-7, "space grid not consistent with boundary level");
    Validate.isTrue(Math.abs(grid.getSpaceNode(grid.getNumSpaceNodes() - 1) - upperBoundary.getLevel()) < 1e-7, "space grid not consistent with boundary level");
  }

  private double getFittingParameter(final PDEGrid1D grid, final double a, final double b, final int i) {
    double rho;
    final double[] x1st = grid.getFirstDerivativeCoefficients(i);
    final double[] x2nd = grid.getSecondDerivativeCoefficients(i);
    final double bdx1 = (b * grid.getSpaceStep(i - 1));
    final double bdx2 = (b * grid.getSpaceStep(i));

    // convection dominated
    if (Math.abs(bdx1) > 10 * Math.abs(a) || Math.abs(bdx2) > 10 * Math.abs(a) || a == 0.0) {
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
