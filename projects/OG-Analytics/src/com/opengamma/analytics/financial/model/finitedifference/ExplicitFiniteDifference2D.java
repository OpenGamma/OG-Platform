/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.cube.Cube;

/**
 * <b>Note</b> this is for testing purposes and is not recommended for actual use 
 */
public class ExplicitFiniteDifference2D implements ConvectionDiffusionPDESolver2D {

  @Override
  public double[][] solve(ConvectionDiffusion2DPDEDataBundle pdeData, int tSteps, int xSteps, int ySteps, double tMax, BoundaryCondition2D xLowerBoundary, BoundaryCondition2D xUpperBoundary,
      BoundaryCondition2D yLowerBoundary, BoundaryCondition2D yUpperBoundary) {
    return solve(pdeData, tSteps, xSteps, ySteps, tMax, xLowerBoundary, xUpperBoundary, yLowerBoundary, yUpperBoundary, null);
  }

  @Override
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

    double sum;
    double t = 0.0;
    for (int k = 0; k < tSteps; k++) {
      double[][] vNew = new double[xSteps + 1][ySteps + 1];
      for (int i = 1; i < xSteps; i++) {
        for (int j = 1; j < ySteps; j++) {
          double a = pdeData.getA(t, x[i], y[j]);
          double b = pdeData.getB(t, x[i], y[j]);
          double c = pdeData.getC(t, x[i], y[j]);
          double d = pdeData.getD(t, x[i], y[j]);
          double e = pdeData.getE(t, x[i], y[j]);
          double f = pdeData.getF(t, x[i], y[j]);

          sum = v[i][j];
          sum -= a * dtdx2 * (v[i + 1][j] - 2 * v[i][j] + v[i - 1][j]);
          sum -= b * dtdx / 2 * (v[i + 1][j] - v[i - 1][j]);
          sum -= c * dt * v[i][j];
          sum -= d * dtdy2 * (v[i][j + 1] - 2 * v[i][j] + v[i][j - 1]);
          sum -= e * dtdxdy / 4 * (v[i + 1][j + 1] + v[i - 1][j - 1] - v[i + 1][j - 1] - v[i - 1][j + 1]);
          sum -= f * dtdy / 2 * (v[i][j + 1] - v[i][j - 1]);

          vNew[i][j] = sum;
        }
      }

      // The y = ymin and y = ymax boundaries
      for (int i = 1; i < xSteps; i++) {
        double[] temp = yLowerBoundary.getRightMatrixCondition(pdeData, t, x[i]);
        sum = 0;
        for (int n = 0; n < temp.length; n++) {
          sum += temp[n] * v[i][n];
        }
        double q = sum + yLowerBoundary.getConstant(pdeData, t, x[i], dy);
        sum = 0;
        temp = yLowerBoundary.getLeftMatrixCondition(pdeData, x[i], t);
        Validate.isTrue(temp[0] != 0.0);
        for (int k1 = 1; k1 < temp.length; k1++) {
          sum += temp[k1] * vNew[i][k1];
        }
        vNew[i][0] = (q - sum) / temp[0];

        temp = yUpperBoundary.getRightMatrixCondition(pdeData, t, x[i]);
        sum = 0;
        for (int n = 0; n < temp.length; n++) {
          sum += temp[n] * v[i][ySteps + n + 1 - temp.length];
        }
        q = sum + yUpperBoundary.getConstant(pdeData, t, x[i], dy);
        sum = 0;
        temp = yUpperBoundary.getLeftMatrixCondition(pdeData, t, x[i]);
        for (int k1 = 0; k1 < temp.length - 1; k1++) {
          sum += temp[k1] * vNew[i][ySteps + k1 + 1 - temp.length];
        }
        vNew[i][ySteps] = (q - sum) / temp[temp.length - 1];
      }

      // The x = xmin and x = xmax boundaries
      for (int j = 0; j <= ySteps; j++) {
        double[] temp = xLowerBoundary.getRightMatrixCondition(pdeData, y[j], t);
        sum = 0;
        for (int n = 0; n < temp.length; n++) {
          sum += temp[n] * v[n][j];
        }
        double q = sum + xLowerBoundary.getConstant(pdeData, t, y[j], dx);
        sum = 0;
        temp = xLowerBoundary.getLeftMatrixCondition(pdeData, t, y[j]);
        for (int k1 = 1; k1 < temp.length; k1++) {
          sum += temp[k1] * vNew[k1][j];
        }
        vNew[0][j] = (q - sum) / temp[0];

        temp = xUpperBoundary.getRightMatrixCondition(pdeData, t, y[j]);
        sum = 0;
        for (int n = 0; n < temp.length; n++) {
          sum += temp[n] * v[xSteps + n + 1 - temp.length][j];
        }
        q = sum + xUpperBoundary.getConstant(pdeData, t, y[j], dx);
        sum = 0;
        temp = xUpperBoundary.getLeftMatrixCondition(pdeData, t, y[j]);
        for (int k1 = 0; k1 < temp.length - 1; k1++) {
          sum += temp[k1] * vNew[xSteps + k1 + 1 - temp.length][j];
        }

        vNew[xSteps][j] = (q - sum) / temp[temp.length - 1];
      }

      // //average to find corners
      // vNew[0][0] = (vNew[0][1]+vNew[1][0])/2;

      // TODO American payoff
      t += dt;
      v = vNew;
    }

    return v;

  }

}
