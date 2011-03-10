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
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class CrankNicolsonFiniteDifference {

  private static final Decomposition<?> DCOMP = new LUDecompositionCommons();

  private static final double THETA = 0.5;

  public double[] solve(ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final double xMin, final double xMax) {
    Validate.notNull(pdeData, "pde data");
    double dt = tMax / (tSteps);
    double dx = (xMax - xMin) / (xSteps);
    double nu1 = dt / dx / dx;
    double nu2 = dt / dx;

    double[] f = new double[xSteps + 1];
    double[] x = new double[xSteps + 1];
    double[] q = new double[xSteps + 1];
    double[][] m = new double[xSteps + 1][xSteps + 1];

    double currentX = xMin;

    double a, b, c, aa, bb, cc;

    for (int j = 0; j <= xSteps; j++) {
      currentX = xMin + j * dx;
      x[j] = currentX;
      double value = pdeData.getInitialValue(currentX);
      f[j] = value;
    }

    double t = 0.0;
    for (int i = 0; i < tSteps; i++) {

      for (int j = 1; j < xSteps; j++) {
        a = pdeData.getA(t, x[j]);
        b = pdeData.getB(t, x[j]);
        c = pdeData.getC(t, x[j]);
        aa = THETA * (-nu1 * a + 0.5 * nu2 * b);
        bb = 1 + THETA * (2 * nu1 * a - dt * c);
        cc = THETA * (-nu1 * a - 0.5 * nu2 * b);
        q[j] = aa * f[j - 1] + bb * f[j] + cc * f[j + 1];

        // TODO could store these
        a = pdeData.getA(t + dt, x[j]);
        b = pdeData.getB(t + dt, x[j]);
        c = pdeData.getC(t + dt, x[j]);
        aa = (-nu1 * a + 0.5 * nu2 * b);
        bb = (2 * nu1 * a - dt * c);
        cc = (-nu1 * a - 0.5 * nu2 * b);
        m[j][j - 1] = (THETA - 1) * aa;
        m[j][j] = 1 + (THETA - 1) * bb;
        m[j][j + 1] = (THETA - 1) * cc;
      }

      // make fNew[0] = f[0];
      q[0] = f[0];
      q[xSteps] = 0.0;
      m[0][0] = 1.0; // make fNew[0] = f[0];

      // zero second derivative condition
      m[xSteps][xSteps - 2] = 1;
      m[xSteps][xSteps - 1] = -2;
      m[xSteps][xSteps] = 1;
     // q[xSteps] += Math.exp(x[xSteps]);
      
      DoubleMatrix2D mM = new DoubleMatrix2D(m);
      DecompositionResult res = DCOMP.evaluate(mM);
      f = res.solve(q);
    }

    return f;

  }

}
