/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

import com.opengamma.math.cube.Cube;
import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.LUDecompositionCommons;

/**
 * <b>Note</b> this is for testing purposes and is not recommended for actual use 
 */
public class ImplicitFiniteDifference2D implements ConvectionDiffusionPDESolver2D {

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
    int size = (xSteps + 1) * (ySteps + 1);

    double[][] v = new double[xSteps + 1][ySteps + 1];
    double[] u = new double[size];
    double[] x = new double[xSteps + 1];
    double[] y = new double[ySteps + 1];
    final double[] q = new double[size];

    final double[][] mx = new double[size][size];

    double currentX = 0;
    double currentY = 0;
    int index;

    for (int i = 0; i <= xSteps; i++) {
      currentX = xLowerBoundary.getLevel() + i * dx;
      x[i] = currentX;
    }
    for (int j = 0; j <= ySteps; j++) {
      currentY = yLowerBoundary.getLevel() + j * dy;
      y[j] = currentY;
      int offset = j * (xSteps + 1);
      for (int i = 0; i <= xSteps; i++) {
        u[offset + i] = pdeData.getInitialValue(x[i], currentY);
      }
    }

    double t = 0.0;
    double a, b, c, d, e, f;
    double[] w = new double[9];

    for (int n = 0; n < tSteps; n++) {
      t += dt;

      for (int i = 1; i < xSteps; i++) {
        for (int j = 1; j < ySteps; j++) {
          index = j * (xSteps + 1) + i;
          a = pdeData.getA(t, x[i], y[j]);
          b = pdeData.getB(t, x[i], y[j]);
          c = pdeData.getC(t, x[i], y[j]);
          d = pdeData.getD(t, x[i], y[j]);
          e = pdeData.getE(t, x[i], y[j]);
          f = pdeData.getF(t, x[i], y[j]);
          w[0] = 1 - (2 * dtdx2 * a - dt * c) - (2 * dtdy2 * d);
          w[1] = dtdxdy * e / 4.0;
          w[2] = (dtdx2 * a + 0.5 * dtdx * b);
          w[3] = -dtdxdy * e / 4.0;
          w[4] = (dtdy2 * d - 0.5 * dtdy * f);
          w[5] = (dtdy2 * d + 0.5 * dtdy * f);
          w[6] = -dtdxdy * e / 4.0;
          w[7] = (dtdx2 * a - 0.5 * dtdx * b);
          w[8] = dtdxdy * e / 4.0;

          mx[index][index] = w[0];
          mx[index][index - xSteps] = w[1];
          mx[index][index + 1] = w[2];
          mx[index][index + 2 + xSteps] = w[3];
          mx[index][index - xSteps - 1] = w[4];
          mx[index][index + xSteps + 1] = w[5];
          mx[index][index - xSteps - 2] = w[6];
          mx[index][index - 1] = w[7];
          mx[index][index + xSteps] = w[8];

          q[index] = u[index];
        }
      }

      // The y boundary conditions
      for (int i = 0; i <= xSteps; i++) {
        double[] temp = yLowerBoundary.getLeftMatrixCondition(pdeData, t, x[i]);
        for (int k = 0; k < temp.length; k++) {
          int offset = k * (xSteps + 1);
          mx[i][offset + i] = temp[k];
        }

        temp = yUpperBoundary.getLeftMatrixCondition(pdeData, t, x[i]);
        for (int k = 0; k < temp.length; k++) {
          int offset = (ySteps - k) * (xSteps + 1);
          mx[i + ySteps * (xSteps + 1)][offset + i] = temp[k];
        }

        temp = yLowerBoundary.getRightMatrixCondition(pdeData, t, x[i]);
        double sum = 0;
        for (int k = 0; k < temp.length; k++) {
          int offset = k * (xSteps + 1);
          sum += temp[k] * u[offset + i];
        }
        q[i] = sum + yLowerBoundary.getConstant(pdeData, t, x[i], dy);

        temp = yUpperBoundary.getRightMatrixCondition(pdeData, t, x[i]);
        sum = 0;
        for (int k = 0; k < temp.length; k++) {
          int offset = (ySteps - k) * (xSteps + 1);
          sum += temp[k] * u[offset + i];
        }
        q[i + ySteps * (xSteps + 1)] = sum + xUpperBoundary.getConstant(pdeData, t, x[i], dy);
      }

      // The x boundary conditions

      for (int j = 1; j < ySteps; j++) {
        double[] temp = xLowerBoundary.getLeftMatrixCondition(pdeData, t, y[j]);
        int offset = j * (xSteps + 1);
        for (int k = 0; k < temp.length; k++) {
          mx[offset][offset + k] = temp[k];
        }

        temp = xUpperBoundary.getLeftMatrixCondition(pdeData, t, y[j]);
        offset = (j + 1) * (xSteps + 1) - 1;
        for (int k = 0; k < temp.length; k++) {
          mx[offset][offset - k] = temp[k];
        }

        temp = xLowerBoundary.getRightMatrixCondition(pdeData, t, y[j]);
        offset = j * (xSteps + 1);
        double sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * u[offset + k];
        }
        q[offset] = sum + xLowerBoundary.getConstant(pdeData, t, y[j], dx);

        temp = xUpperBoundary.getRightMatrixCondition(pdeData, t, y[j]);
        offset = (j + 1) * (xSteps + 1) - 1;
        sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * u[offset - k];
        }
        q[offset] = sum + xLowerBoundary.getConstant(pdeData, t, y[j], dx);
      }

      // debug
      // for (int i = 0; i < size; i++) {
      // for (int j = 0; j < size; j++) {
      // System.out.print(mx[i][j] + "\t");
      // }
      // System.out.print("\n");
      // }

      // SOR
      final double omega = 1.0;
      double scale = 1.0;
      double errorSqr = Double.POSITIVE_INFINITY;
      double sum;
      while (errorSqr / (scale + 1e-10) > 1e-18) {
        errorSqr = 0.0;
        scale = 0.0;
        for (int l = 0; l < size; l++) {
          sum = 0;
          for (int k = 0; k < size; k++) {
            sum += mx[l][k] * u[k];
          }
          double correction = omega / mx[l][l] * (q[l] - sum);
          // if (freeBoundary != null) {
          // correction = Math.max(correction, freeBoundary.getZValue(t, x[j]) - f[j]);
          // }
          errorSqr += correction * correction;
          u[l] += correction;
          scale += u[l] * u[l];
        }
      }

      // DecompositionResult dcompResult = DCOMP.evaluate(new DoubleMatrix2D(mx));
      // u = dcompResult.solve(q);
    } // time loop

    // unpack vector to matrix
    for (int j = 0; j <= ySteps; j++) {
      int offset = j * (xSteps + 1);
      for (int i = 0; i <= xSteps; i++) {
        v[i][j] = u[offset + i];
      }
    }
    return v;

  }

  // private double[][] solveSOR(double[][] m, double[][] v)

}
