/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

import org.apache.commons.lang.Validate;

import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class CrankNicolsonFiniteDifference {

  private static final double THETA = 0.5; // TODO investigate adjusting this (Douglas schemes)
  private static final Decomposition<?> DCOMP = new LUDecompositionCommons();

  public double[][] solve(ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, BoundaryCondition lowerBoundary,
      BoundaryCondition upperBoundary) {
    Validate.notNull(pdeData, "pde data");
    double dt = tMax / (tSteps);
    double dx = (upperBoundary.getLevel() - lowerBoundary.getLevel()) / (xSteps);
    double nu1 = dt / dx / dx;
    double nu2 = dt / dx;

    double[] f = new double[xSteps + 1];
    double[] x = new double[xSteps + 1];
    double[] q = new double[xSteps + 1];
    double[][] m = new double[xSteps + 1][xSteps + 1];

    double currentX = lowerBoundary.getLevel();

    double a, b, c, aa, bb, cc;

    for (int j = 0; j <= xSteps; j++) {
      currentX = lowerBoundary.getLevel() + j * dx;
      x[j] = currentX;
      double value = pdeData.getInitialValue(currentX);
      f[j] = value;
    }

    double t = 0.0;
    for (int i = 0; i < tSteps; i++) {
      t += dt;

      for (int j = 1; j < xSteps; j++) {
        a = pdeData.getA(t - dt, x[j]);
        b = pdeData.getB(t - dt, x[j]);
        c = pdeData.getC(t - dt, x[j]);
        aa = THETA * (-nu1 * a + 0.5 * nu2 * b);
        bb = 1 + THETA * (2 * nu1 * a - dt * c);
        cc = THETA * (-nu1 * a - 0.5 * nu2 * b);
        q[j] = aa * f[j - 1] + bb * f[j] + cc * f[j + 1];

        // TODO could store these
        a = pdeData.getA(t, x[j]);
        b = pdeData.getB(t, x[j]);
        c = pdeData.getC(t, x[j]);
        aa = (-nu1 * a + 0.5 * nu2 * b);
        bb = (2 * nu1 * a - dt * c);
        cc = (-nu1 * a - 0.5 * nu2 * b);
        m[j][j - 1] = (THETA - 1) * aa;
        m[j][j] = 1 + (THETA - 1) * bb;
        m[j][j + 1] = (THETA - 1) * cc;
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
      q[0] = sum + lowerBoundary.getConstant(pdeData, t);

      temp = upperBoundary.getRightMatrixCondition(pdeData, t);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xSteps - k];
      }
      q[xSteps] = sum + upperBoundary.getConstant(pdeData, t);

    

      DoubleMatrix2D mM = new DoubleMatrix2D(m);
      DecompositionResult res = DCOMP.evaluate(mM);
      f = res.solve(q);
    }
    
    double[][] res = new double[2][];
    res[0] = x;
    res[1] = f;

    return res;

  }

}
