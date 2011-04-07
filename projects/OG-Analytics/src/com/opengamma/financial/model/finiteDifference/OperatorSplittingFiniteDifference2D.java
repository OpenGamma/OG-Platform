/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

import com.opengamma.math.cube.Cube;
import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * <b>Note</b> this is for testing purposes and is not recommended for actual use 
 */
public class OperatorSplittingFiniteDifference2D implements ConvectionDiffusionPDESolver2D {

  private static final Decomposition<?> DCOMP = new LUDecompositionCommons();
  private static final double THETA = 0.5;

  @Override
  public double[][] solve(ConvectionDiffusion2DPDEDataBundle pdeData, int tSteps, int xSteps, int ySteps, double tMax, BoundaryCondition2D xLowerBoundary, BoundaryCondition2D xUpperBoundary,
      BoundaryCondition2D yLowerBoundary, BoundaryCondition2D yUpperBoundary) {
    return solve(pdeData, tSteps, xSteps, ySteps, tMax, xLowerBoundary, xUpperBoundary, yLowerBoundary, yUpperBoundary, null);
  }

  public double[][] solve(ConvectionDiffusion2DPDEDataBundle pdeData, final int tSteps, final int xSteps, final int ySteps, final double tMax, BoundaryCondition2D xLowerBoundary,
      BoundaryCondition2D xUpperBoundary, BoundaryCondition2D yLowerBoundary, BoundaryCondition2D yUpperBoundary, final Cube<Double, Double, Double, Double> freeBoundary) {

    double dt = tMax / (tSteps);
    double dx = (xUpperBoundary.getLevel() - xLowerBoundary.getLevel()) / (xSteps);
    double dy = (yUpperBoundary.getLevel() - yLowerBoundary.getLevel()) / (ySteps);
    double dtdx2 = dt / dx / dx;
    double dtdx = dt / dx;
    double dtdy2 = dt / dy / dy;
    double dtdy = dt / dy;
    double dtdxdy = dt / dy / dx;

    double[][] v = new double[xSteps + 1][ySteps + 1];
    double[] x = new double[xSteps + 1];
    double[] y = new double[ySteps + 1];
    final double[] q = new double[xSteps + 1];
    final double[] r = new double[ySteps + 1];
    final double[][] mx = new double[xSteps + 1][xSteps + 1];
    final double[][] my = new double[ySteps + 1][ySteps + 1];

    double currentX = 0;
    double currentY = 0;

    for (int j = 0; j <= ySteps; j++) {
      currentY = yLowerBoundary.getLevel() + j * dy;
      y[j] = currentY;
    }
    for (int i = 0; i <= xSteps; i++) {
      currentX = xLowerBoundary.getLevel() + i * dx;
      x[i] = currentX;
      for (int j = 0; j <= ySteps; j++) {
        v[i][j] = pdeData.getInitialValue(x[i], y[j]);
      }
    }

    double t = 0.0;
    double a, b, c, d, e, f, aa, bb, cc;

    for (int n = 0; n < tSteps; n++) {
      t += dt / 2;

      // stag 1 x terms and the cross
      for (int j = 1; j < ySteps; j++) {

        for (int i = 1; i < xSteps; i++) {

          a = pdeData.getA(t, x[i], y[j]);
          b = pdeData.getB(t, x[i], y[j]);
          c = pdeData.getC(t, x[i], y[j]);
          e = pdeData.getE(t - dt / 2, x[i], y[j]);
          aa = (dtdx2 * a - 0.5 * dtdx * b);
          bb = 1 - (2 * dtdx2 * a - dt * c);
          cc = (dtdx2 * a + 0.5 * dtdx * b);
          q[i] = v[i][j] - dtdxdy * e / 8.0 * (v[i + 1][j + 1] + v[i - 1][j - 1] - v[i + 1][j - 1] - v[i - 1][j + 1]);
          mx[i][i - 1] = aa;
          mx[i][i] = bb;
          mx[i][i + 1] = cc;
        }

        double[] temp = xLowerBoundary.getLeftMatrixCondition(pdeData, t, y[j]);
        for (int k = 0; k < temp.length; k++) {
          mx[0][k] = temp[k];
        }
        temp = xUpperBoundary.getLeftMatrixCondition(pdeData, t, y[j]);
        for (int k = 0; k < temp.length; k++) {
          mx[xSteps][xSteps - k] = temp[k];
        }

        temp = xLowerBoundary.getRightMatrixCondition(pdeData, t, y[j]);
        double sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * v[k][j];
        }
        q[0] = sum + xLowerBoundary.getConstant(pdeData, t, y[j], dx);

        temp = xUpperBoundary.getRightMatrixCondition(pdeData, t, y[j]);
        sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * v[xSteps - k][j];
        }
        q[xSteps] = sum + xUpperBoundary.getConstant(pdeData, t, y[j], dx);

        final DoubleMatrix2D mM = new DoubleMatrix2D(mx);
        final DecompositionResult res = DCOMP.evaluate(mM);
        double[] vNew = res.solve(q);
        for (int i = 0; i <= xSteps; i++) {
          v[i][j] = vNew[i];
        }

        // // SOR
        // final double omega = 1.0;
        // double scale = 1.0;
        // double errorSqr = Double.POSITIVE_INFINITY;
        // while (errorSqr / (scale + 1e-10) > 1e-18) {
        // errorSqr = 0.0;
        // scale = 0.0;
        // for (int l = 0; l <= xSteps; l++) {
        // sum = 0;
        // for (int k = 0; k <= xSteps; k++) {
        // sum += mx[l][k] * v[k][j];
        // }
        // double correction = omega / mx[l][l] * (q[l] - sum);
        // // if (freeBoundary != null) {
        // // correction = Math.max(correction, freeBoundary.getZValue(t, x[j]) - f[j]);
        // // }
        // errorSqr += correction * correction;
        // v[l][j] += correction;
        // scale += v[l][j] * v[l][j];
        // }
        // }

      }

      t += dt / 2;
      // stag 2 y terms and the cross
      for (int i = 1; i < xSteps; i++) {

        for (int j = 1; j < ySteps; j++) {

          d = pdeData.getD(t, x[i], y[j]);
          f = pdeData.getF(t, x[i], y[j]);
          e = pdeData.getE(t - dt / 2, x[i], y[j]);

          aa = (dtdy2 * d - 0.5 * dtdy * f);
          bb = 1 - (2 * dtdy2 * d);
          cc = (dtdy2 * d + 0.5 * dtdy * f);
          r[j] = v[i][j] - dtdxdy * e / 8.0 * (v[i + 1][j + 1] + v[i - 1][j - 1] - v[i + 1][j - 1] - v[i - 1][j + 1]);
          my[j][j - 1] = aa;
          my[j][j] = bb;
          my[j][j + 1] = cc;
        }

        double[] temp = yLowerBoundary.getLeftMatrixCondition(pdeData, t, x[i]);
        for (int k = 0; k < temp.length; k++) {
          my[0][k] = temp[k];
        }
        temp = yUpperBoundary.getLeftMatrixCondition(pdeData, t, x[i]);
        for (int k = 0; k < temp.length; k++) {
          my[ySteps][ySteps - k] = temp[k];
        }

        temp = yLowerBoundary.getRightMatrixCondition(pdeData, t, x[i]);
        double sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * v[i][k];
        }
        r[0] = sum + yLowerBoundary.getConstant(pdeData, t, x[i], dy);

        temp = yUpperBoundary.getRightMatrixCondition(pdeData, t, x[i]);
        sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * v[i][ySteps - k];
        }
        r[ySteps] = sum + yUpperBoundary.getConstant(pdeData, t, x[i], dy);

        final DoubleMatrix2D mM = new DoubleMatrix2D(mx);
        final DecompositionResult res = DCOMP.evaluate(mM);
        double[] vNew = res.solve(r);
        for (int j = 0; j <= ySteps; j++) {
          v[i][j] = vNew[j];
        }

        //
        // // SOR
        // final double omega = 1.0;
        // double scale = 1.0;
        // double errorSqr = Double.POSITIVE_INFINITY;
        // while (errorSqr / (scale + 1e-10) > 1e-18) {
        // errorSqr = 0.0;
        // scale = 0.0;
        // for (int l = 0; l <= ySteps; l++) {
        // sum = 0;
        // for (int k = 0; k <= ySteps; k++) {
        // sum += my[l][k] * v[i][k];
        // }
        // double correction = omega / my[l][l] * (r[l] - sum);
        // // if (freeBoundary != null) {
        // // correction = Math.max(correction, freeBoundary.getZValue(t, x[j]) - f[j]);
        // // }
        // errorSqr += correction * correction;
        // v[i][l] += correction;
        // scale += v[i][l] * v[i][l];
        // }
        // }

      }

    } // time loop
    return v;

  }

  // private double[][] solveSOR(double[][] m, double[][] v)
}
