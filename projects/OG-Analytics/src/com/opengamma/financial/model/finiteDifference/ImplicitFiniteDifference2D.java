/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

import com.opengamma.math.cube.Cube;

/**
 * <b>Note</b> this is for testing purposes and is not recommended for actual use 
 */
public class ImplicitFiniteDifference2D implements ConvectionDiffusionPDESolver2D {

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
    double[][] w = new double[size][9];

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

          w[index][0] = dtdxdy * e / 4.0;
          w[index][1] = (dtdy2 * d - 0.5 * dtdy * f);
          w[index][2] = -dtdxdy * e / 4.0;

          w[index][3] = (dtdx2 * a - 0.5 * dtdx * b);
          w[index][4] = 1 - (2 * dtdx2 * a - dt * c) - (2 * dtdy2 * d);
          w[index][5] = (dtdx2 * a + 0.5 * dtdx * b);

          w[index][6] = -dtdxdy * e / 4.0;
          w[index][7] = (dtdy2 * d + 0.5 * dtdy * f);
          w[index][8] = dtdxdy * e / 4.0;

          q[index] = u[index];
        }
      }

      // The y boundary conditions
      double[][][] yBoundary = new double[2][xSteps + 1][];

      for (int i = 0; i <= xSteps; i++) {
        yBoundary[0][i] = yLowerBoundary.getLeftMatrixCondition(pdeData, t, x[i]);
        yBoundary[1][i] = yUpperBoundary.getLeftMatrixCondition(pdeData, t, x[i]);

        double[] temp = yLowerBoundary.getRightMatrixCondition(pdeData, t, x[i]);
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
      double[][][] xBoundary = new double[2][ySteps - 1][];

      for (int j = 1; j < ySteps; j++) {
        xBoundary[0][j - 1] = xLowerBoundary.getLeftMatrixCondition(pdeData, t, y[j]);
        xBoundary[1][j - 1] = xUpperBoundary.getLeftMatrixCondition(pdeData, t, y[j]);

        double[] temp = xLowerBoundary.getRightMatrixCondition(pdeData, t, y[j]);
        int offset = j * (xSteps + 1);
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

      // SOR
      final double omega = 1.0;
      double scale = 1.0;
      double errorSqr = Double.POSITIVE_INFINITY;
      double sum;
      int l;
      while (errorSqr / (scale + 1e-10) > 1e-18) {
        errorSqr = 0.0;
        scale = 0.0;
        // solve for the innards first
        for (int i = 1; i < xSteps; i++) {
          for (int j = 1; j < ySteps; j++) {
            l = j * (xSteps + 1) + i;
            sum = 0;
            sum += w[l][0] * u[l - xSteps - 2];
            sum += w[l][1] * u[l - xSteps - 1];
            sum += w[l][2] * u[l - xSteps];
            sum += w[l][3] * u[l - 1];
            sum += w[l][4] * u[l];
            sum += w[l][5] * u[l + 1];
            sum += w[l][6] * u[l + xSteps];
            sum += w[l][7] * u[l + xSteps + 1];
            sum += w[l][8] * u[l + xSteps + 2];

            double correction = omega / w[l][4] * (q[l] - sum);
            // if (freeBoundary != null) {
            // correction = Math.max(correction, freeBoundary.getZValue(t, x[j]) - f[j]);
            // }
            errorSqr += correction * correction;
            u[l] += correction;
            scale += u[l] * u[l];
          }
        }

        // the lower y boundary
        for (int i = 0; i <= xSteps; i++) {
          sum = 0;
          l = i;
          double[] temp = yBoundary[0][i];
          for (int k = 0; k < temp.length; k++) {
            int offset = k * (xSteps + 1);
            sum += temp[k] * u[offset + i];
          }
          double correction = omega / temp[0] * (q[l] - sum);
          errorSqr += correction * correction;
          u[l] += correction;
          scale += u[l] * u[l];
        }

        // the upper y boundary
        for (int i = 0; i <= xSteps; i++) {
          sum = 0;
          l = (xSteps + 1) * ySteps + i;
          double[] temp = yBoundary[1][i];
          for (int k = 0; k < temp.length; k++) {
            int offset = (ySteps - k) * (xSteps + 1);
            sum += temp[k] * u[offset + i];
          }
          double correction = omega / temp[0] * (q[l] - sum);
          errorSqr += correction * correction;
          u[l] += correction;
          scale += u[l] * u[l];
        }

        // the lower x boundary
        for (int j = 1; j < ySteps; j++) {
          sum = 0;
          l = j * (xSteps + 1);
          double[] temp = xBoundary[0][j - 1];
          for (int k = 0; k < temp.length; k++) {
            sum += temp[k] * u[l + k];
          }
          double correction = omega / temp[0] * (q[l] - sum);
          errorSqr += correction * correction;
          u[l] += correction;
          scale += u[l] * u[l];
        }

        // the upper x boundary
        for (int j = 1; j < ySteps; j++) {
          sum = 0;
          l = (j + 1) * (xSteps + 1) - 1;
          double[] temp = xBoundary[1][j - 1];
          for (int k = 0; k < temp.length; k++) {
            sum += temp[k] * u[l - k];
          }
          double correction = omega / temp[0] * (q[l] - sum);
          errorSqr += correction * correction;
          u[l] += correction;
          scale += u[l] * u[l];
        }

      } // end of SOR

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
