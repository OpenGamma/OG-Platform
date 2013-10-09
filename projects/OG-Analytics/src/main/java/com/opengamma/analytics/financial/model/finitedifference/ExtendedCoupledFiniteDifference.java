/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.DecompositionResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.surface.Surface;

/**
 *
 */
@SuppressWarnings("deprecation")
public class ExtendedCoupledFiniteDifference extends CoupledFiniteDifference {

  public ExtendedCoupledFiniteDifference(final double theta) {
    super(theta, true);
  }

  public PDEFullResults1D[] solve(final ExtendedCoupledPDEDataBundle pdeData1, final ExtendedCoupledPDEDataBundle pdeData2, final PDEGrid1D grid, final BoundaryCondition lowerBoundary1,
      final BoundaryCondition upperBoundary1, final BoundaryCondition lowerBoundary2, final BoundaryCondition upperBoundary2,
      @SuppressWarnings("unused") final Surface<Double, Double, Double> freeBoundary) {

    Validate.notNull(pdeData1, "pde1 data");
    Validate.notNull(pdeData2, "pde2 data");
    final int tNodes = grid.getNumTimeNodes();
    final int xNodes = grid.getNumSpaceNodes();
    final double theta = getTheta();
    final Decomposition<?> dcomp = getDecomposition();

    double[] f = new double[2 * xNodes];
    final double[][] full1 = new double[tNodes][xNodes];
    final double[][] full2 = new double[tNodes][xNodes];

    final double[] q = new double[2 * xNodes];
    final double[][] m = new double[2 * xNodes][2 * xNodes];

    final double[][] a1 = new double[2][xNodes - 2];
    final double[][] a2 = new double[2][xNodes - 2];
    final double[][] b1 = new double[2][xNodes - 2];
    final double[][] b2 = new double[2][xNodes - 2];
    final double[][] c1 = new double[2][xNodes - 2];
    final double[][] c2 = new double[2][xNodes - 2];
    final double[][] alpha1 = new double[2][xNodes];
    final double[][] alpha2 = new double[2][xNodes];
    final double[][] beta1 = new double[2][xNodes];
    final double[][] beta2 = new double[2][xNodes];

    final double lambda1 = pdeData1.getCoupling();
    final double lambda2 = pdeData2.getCoupling();

    //    final double omega = 1.5;
    //    final int oldCount = 0;
    //    final boolean omegaIncrease = false;

    double dt, t1, t2, x;
    double[] x1st, x2nd;

    for (int i = 0; i < xNodes; i++) {
      f[i] = pdeData1.getInitialCondition(grid.getSpaceNode(i));
    }
    for (int i = 0; i < xNodes; i++) {
      f[i + xNodes] = pdeData2.getInitialCondition(grid.getSpaceNode(i));
    }

    full1[0] = Arrays.copyOfRange(f, 0, xNodes);
    full2[0] = Arrays.copyOfRange(f, xNodes, 2 * xNodes);

    for (int i = 0; i < xNodes - 2; i++) {
      x = grid.getSpaceNode(i + 1);
      a1[0][i] = pdeData1.getA(0, x);
      b1[0][i] = pdeData1.getB(0, x);
      c1[0][i] = pdeData1.getC(0, x);
      a1[1][i] = pdeData2.getA(0, x);
      b1[1][i] = pdeData2.getB(0, x);
      c1[1][i] = pdeData2.getC(0, x);
    }

    for (int i = 0; i < xNodes; i++) {
      x = grid.getSpaceNode(i);
      alpha1[0][i] = pdeData1.getAlpha(0, x);
      beta1[0][i] = pdeData1.getBeta(0, x);
      alpha1[1][i] = pdeData2.getAlpha(0, x);
      beta1[1][i] = pdeData2.getBeta(0, x);
    }

    final boolean first = true;
    DecompositionResult decompRes = null;

    for (int n = 1; n < tNodes; n++) {

      t1 = grid.getTimeNode(n - 1);
      t2 = grid.getTimeNode(n);
      dt = grid.getTimeStep(n - 1);

      for (int i = 0; i < xNodes; i++) {
        x = grid.getSpaceNode(i);
        alpha2[0][i] = pdeData1.getAlpha(t2, x);
        beta2[0][i] = pdeData1.getBeta(t2, x);
        alpha2[1][i] = pdeData2.getAlpha(t2, x);
        beta2[1][i] = pdeData2.getBeta(t2, x);
      }

      for (int i = 1; i < xNodes - 1; i++) {
        x = grid.getSpaceNode(i);
        x1st = grid.getFirstDerivativeCoefficients(i);
        x2nd = grid.getSecondDerivativeCoefficients(i);

        q[i] = f[i];
        q[i] -= (1 - theta) * dt * (x2nd[0] * a1[0][i - 1] * alpha1[0][i - 1] + x1st[0] * b1[0][i - 1] * beta1[0][i - 1]) * f[i - 1];
        q[i] -= (1 - theta) * dt * (x2nd[1] * a1[0][i - 1] * alpha1[0][i] + x1st[1] * b1[0][i - 1] * beta1[0][i] + c1[0][i - 1]) * f[i];
        q[i] -= (1 - theta) * dt * (x2nd[2] * a1[0][i - 1] * alpha1[0][i + 1] + x1st[2] * b1[0][i - 1] * beta1[0][i + 1]) * f[i + 1];
        q[i] -= (1 - theta) * dt * lambda1 * f[i + xNodes];

        q[xNodes + i] = f[xNodes + i];
        q[xNodes + i] -= (1 - theta) * dt * (x2nd[0] * a1[1][i - 1] * alpha1[1][i - 1] + x1st[0] * b1[1][i - 1] * beta1[1][i - 1]) * f[xNodes + i - 1];
        q[xNodes + i] -= (1 - theta) * dt * (x2nd[1] * a1[1][i - 1] * alpha1[1][i] + x1st[1] * b1[1][i - 1] * beta1[1][i] + c1[1][i - 1]) * f[xNodes + i];
        q[xNodes + i] -= (1 - theta) * dt * (x2nd[2] * a1[1][i - 1] * alpha1[1][i + 1] + x1st[2] * b1[1][i - 1] * beta1[1][i + 1]) * f[xNodes + i + 1];
        q[xNodes + i] -= (1 - theta) * dt * lambda2 * f[i];

        a2[0][i - 1] = pdeData1.getA(t2, x);
        b2[0][i - 1] = pdeData1.getB(t2, x);
        c2[0][i - 1] = pdeData1.getC(t2, x);
        a2[1][i - 1] = pdeData2.getA(t2, x);
        b2[1][i - 1] = pdeData2.getB(t2, x);
        c2[1][i - 1] = pdeData2.getC(t2, x);

        m[i][i - 1] = theta * dt * (x2nd[0] * a2[0][i - 1] * alpha2[0][i - 1] + x1st[0] * b2[0][i - 1] * beta2[0][i - 1]);
        m[i][i] = 1 + theta * dt * (x2nd[1] * a2[0][i - 1] * alpha2[0][i] + x1st[1] * b2[0][i - 1] * beta2[0][i] + c2[0][i - 1]);
        m[i][i + 1] = theta * dt * (x2nd[2] * a2[0][i - 1] * alpha2[0][i + 1] + x1st[2] * b2[0][i - 1] * beta2[0][i + 1]);
        m[i][i + xNodes] = dt * theta * lambda1;

        m[xNodes + i][xNodes + i - 1] = theta * dt * (x2nd[0] * a2[1][i - 1] * alpha2[1][i - 1] + x1st[0] * b2[1][i - 1] * beta2[1][i - 1]);
        m[xNodes + i][xNodes + i] = 1 + theta * dt * (x2nd[1] * a2[1][i - 1] * alpha2[1][i] + x1st[1] * b2[1][i - 1] * beta2[1][i] + c2[1][i - 1]);
        m[xNodes + i][xNodes + i + 1] = theta * dt * (x2nd[2] * a2[1][i - 1] * alpha2[1][i + 1] + x1st[2] * b2[1][i - 1] * beta2[1][i + 1]);
        m[xNodes + i][i] = dt * theta * lambda2;
      }

      double[] temp = lowerBoundary1.getLeftMatrixCondition(null, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[0][k] = temp[k];
      }

      temp = upperBoundary1.getLeftMatrixCondition(null, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes - 1][xNodes - temp.length + k] = temp[k];
      }

      temp = lowerBoundary2.getLeftMatrixCondition(null, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[xNodes][xNodes + k] = temp[k];
      }

      temp = upperBoundary2.getLeftMatrixCondition(null, grid, t2);
      for (int k = 0; k < temp.length; k++) {
        m[2 * xNodes - 1][2 * xNodes - temp.length + k] = temp[k];
      }

      temp = lowerBoundary1.getRightMatrixCondition(null, grid, t1);
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[0] = sum + lowerBoundary1.getConstant(null, t2);

      temp = upperBoundary1.getRightMatrixCondition(null, grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[xNodes - 1] = sum + upperBoundary1.getConstant(null, t2);

      temp = lowerBoundary2.getRightMatrixCondition(null, grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      q[xNodes] = sum + lowerBoundary2.getConstant(null, t2);

      temp = upperBoundary2.getRightMatrixCondition(null, grid, t1);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xNodes - 1 - k];
      }

      q[2 * xNodes - 1] = sum + upperBoundary2.getConstant(null, t2);

      if (first) {
        final DoubleMatrix2D mM = new DoubleMatrix2D(m);
        decompRes = dcomp.evaluate(mM);
        // first = false;
      }
      f = decompRes.solve(q);

      a1[0] = Arrays.copyOf(a2[0], xNodes - 2);
      b1[0] = Arrays.copyOf(b2[0], xNodes - 2);
      c1[0] = Arrays.copyOf(c2[0], xNodes - 2);
      alpha1[0] = Arrays.copyOf(alpha2[0], xNodes);
      beta1[0] = Arrays.copyOf(beta2[0], xNodes);
      a1[1] = Arrays.copyOf(a2[1], xNodes - 2);
      b1[1] = Arrays.copyOf(b2[1], xNodes - 2);
      c1[1] = Arrays.copyOf(c2[1], xNodes - 2);
      alpha1[1] = Arrays.copyOf(alpha2[1], xNodes);
      beta1[1] = Arrays.copyOf(beta2[1], xNodes);

      full1[n] = Arrays.copyOfRange(f, 0, xNodes);
      full2[n] = Arrays.copyOfRange(f, xNodes, 2 * xNodes);
    }
    final PDEFullResults1D[] res = new PDEFullResults1D[2];
    res[0] = new PDEFullResults1D(grid, full1);
    res[1] = new PDEFullResults1D(grid, full2);
    return res;
  }
}
