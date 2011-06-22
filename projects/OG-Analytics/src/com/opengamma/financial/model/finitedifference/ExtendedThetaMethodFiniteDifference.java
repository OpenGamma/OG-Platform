/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class ExtendedThetaMethodFiniteDifference extends ThetaMethodFiniteDifference {

  public ExtendedThetaMethodFiniteDifference(final double theta, final boolean showFullResults) {
    super(theta, showFullResults);
  }

  public PDEFullResults1D solve(final ExtendedConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary) {

    Validate.notNull(pdeData, "pde data");
    Validate.notNull(grid, "need a grid");
    validateSetup(grid, lowerBoundary, upperBoundary);

    final int tNodes = grid.getNumTimeNodes();
    final int xNodes = grid.getNumSpaceNodes();
    final double theta = getTheta();
    final double[] f = new double[xNodes];
    final double[][] full = new double[tNodes][xNodes];

    final double[] q = new double[xNodes];
    final double[][] m = new double[xNodes][xNodes];

    double[] a1 = new double[xNodes - 2];
    final double[] a2 = new double[xNodes - 2];
    double[] b1 = new double[xNodes - 2];
    final double[] b2 = new double[xNodes - 2];
    double[] c1 = new double[xNodes - 2];
    final double[] c2 = new double[xNodes - 2];
    double[] alpha1 = new double[xNodes];
    final double[] alpha2 = new double[xNodes];
    double[] beta1 = new double[xNodes];
    final double[] beta2 = new double[xNodes];

    double dt, t1, t2, x;
    double[] x1st, x2nd;

    double omega = 1.5;
    boolean omegaIncrease = false;
    int oldCount = 0;

    for (int i = 0; i < xNodes; i++) {
      f[i] = pdeData.getInitialValue(grid.getSpaceNode(i));
    }

    full[0] = Arrays.copyOf(f, f.length);

    for (int i = 0; i < xNodes - 2; i++) {
      x = grid.getSpaceNode(i + 1);
      a1[i] = pdeData.getA(0, x);
      b1[i] = pdeData.getB(0, x);
      c1[i] = pdeData.getC(0, x);
    }

    for (int i = 0; i < xNodes; i++) {
      x = grid.getSpaceNode(i);
      alpha1[i] = pdeData.getAlpha(0, x);
      beta1[i] = pdeData.getBeta(0, x);
    }

    //    DecompositionResult decompRes = null;
    //    boolean first = true;

    for (int n = 1; n < tNodes; n++) {
      t1 = grid.getTimeNode(n - 1);
      t2 = grid.getTimeNode(n);
      dt = grid.getTimeStep(n - 1);

      for (int i = 0; i < xNodes; i++) {
        x = grid.getSpaceNode(i);
        alpha2[i] = pdeData.getAlpha(t2, x);
        beta2[i] = pdeData.getBeta(t2, x);
      }

      for (int i = 1; i < xNodes - 1; i++) {
        x = grid.getSpaceNode(i);
        x1st = grid.getFirstDerivativeCoefficients(i);
        x2nd = grid.getSecondDerivativeCoefficients(i);

        q[i] = f[i];
        q[i] -= (1 - theta) * dt * (x2nd[0] * a1[i - 1] * alpha1[i - 1] + x1st[0] * b1[i - 1] * beta1[i - 1]) * f[i - 1];
        q[i] -= (1 - theta) * dt * (x2nd[1] * a1[i - 1] * alpha1[i] + x1st[1] * b1[i - 1] * beta1[i] + c1[i - 1]) * f[i];
        q[i] -= (1 - theta) * dt * (x2nd[2] * a1[i - 1] * alpha1[i + 1] + x1st[2] * b1[i - 1] * beta1[i + 1]) * f[i + 1];

        a2[i - 1] = pdeData.getA(t2, x);
        b2[i - 1] = pdeData.getB(t2, x);
        c2[i - 1] = pdeData.getC(t2, x);

        m[i][i - 1] = theta * dt * (x2nd[0] * a2[i - 1] * alpha2[i - 1] + x1st[0] * b2[i - 1] * beta2[i - 1]);
        m[i][i] = 1 + theta * dt * (x2nd[1] * a2[i - 1] * alpha2[i] + x1st[1] * b2[i - 1] * beta2[i] + c2[i - 1]);
        m[i][i + 1] = theta * dt * (x2nd[2] * a2[i - 1] * alpha2[i + 1] + x1st[2] * b2[i - 1] * beta2[i + 1]);
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

      final int count = sor(omega, grid, null, xNodes, f, q, m, t2);
      if (oldCount > 0) {
        if ((omegaIncrease && count > oldCount) || (!omegaIncrease && count < oldCount)) {
          omega = Math.max(1.0, omega * 0.9);
          omegaIncrease = false;
        } else {
          omega = 1.1 * omega;
          omegaIncrease = true;
        }
      }
      oldCount = count;

      //      if (first) {
      //        final DoubleMatrix2D mM = new DoubleMatrix2D(m);
      //        decompRes = DCOMP.evaluate(mM);
      //        first = false;
      //      }
      //      f = decompRes.solve(q);

      full[n] = Arrays.copyOf(f, f.length);

      a1 = a2;
      b1 = b2;
      c1 = c2;
      alpha1 = Arrays.copyOf(alpha2, alpha2.length);
      beta1 = Arrays.copyOf(beta2, beta2.length);

    }

    final PDEFullResults1D res = new PDEFullResults1D(grid, full);

    return res;

  }
}
