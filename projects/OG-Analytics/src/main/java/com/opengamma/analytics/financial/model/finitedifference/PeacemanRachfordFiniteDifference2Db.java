/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.cube.Cube;

/**
 *  Peaceman-Rachford splitting with boundary conditions applied at each of the 4 steps
 * <b>Note</b> this is for testing purposes and is not recommended for actual use
 */
@SuppressWarnings("deprecation")
public class PeacemanRachfordFiniteDifference2Db implements ConvectionDiffusionPDESolver2D {

  //private static final Decomposition<?> DCOMP = new LUDecompositionCommons();
  // Theta = 0 - explicit
  //private static final double THETA = 0.5;
  private static final int SOR_MAX = 5000;

  @Override
  public double[][] solve(final ConvectionDiffusion2DPDEDataBundle pdeData, final int tSteps, final int xSteps, final int ySteps, final double tMax, final BoundaryCondition2D xLowerBoundary,
      final BoundaryCondition2D xUpperBoundary, final BoundaryCondition2D yLowerBoundary, final BoundaryCondition2D yUpperBoundary) {
    return solve(pdeData, tSteps, xSteps, ySteps, tMax, xLowerBoundary, xUpperBoundary, yLowerBoundary, yUpperBoundary, null);
  }

  @Override
  public double[][] solve(final ConvectionDiffusion2DPDEDataBundle pdeData, final int tSteps, final int xSteps, final int ySteps, final double tMax, final BoundaryCondition2D xLowerBoundary,
      final BoundaryCondition2D xUpperBoundary, final BoundaryCondition2D yLowerBoundary, final BoundaryCondition2D yUpperBoundary, final Cube<Double, Double, Double, Double> freeBoundary) {

    final double dt = tMax / (tSteps);
    final double dx = (xUpperBoundary.getLevel() - xLowerBoundary.getLevel()) / (xSteps);
    final double dy = (yUpperBoundary.getLevel() - yLowerBoundary.getLevel()) / (ySteps);
    final double dtdx2 = dt / dx / dx;
    final double dtdx = dt / dx;
    final double dtdy2 = dt / dy / dy;
    final double dtdy = dt / dy;

    final double[][] v = new double[xSteps + 1][ySteps + 1];
    final double[][] vt = new double[xSteps + 1][ySteps + 1];

    final double[] x = new double[xSteps + 1];
    final double[] y = new double[ySteps + 1];

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

    double t, tStar;
    double a, b, c, d, f;

    for (int n = 0; n < tSteps; n++) {

      t = n * dt;
      tStar = t + 0.25 * dt;

      // stag 1 Explicit in y
      for (int i = 0; i <= xSteps; i++) {
        for (int j = 1; j < ySteps; j++) {
          c = pdeData.getC(t, x[i], y[j]);
          d = pdeData.getD(t, x[i], y[j]);
          f = pdeData.getF(t, x[i], y[j]);

          vt[i][j] = (1 - 0.25 * dt * c) * v[i][j];
          vt[i][j] -= 0.5 * dtdy2 * d * (v[i][j + 1] + v[i][j - 1] - 2 * v[i][j]);
          vt[i][j] -= 0.25 * dtdy * f * (v[i][j + 1] - v[i][j - 1]);
        }

        double[] temp = yLowerBoundary.getRightMatrixCondition(tStar, x[i]);
        double sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * v[i][k];
        }
        sum += yLowerBoundary.getConstant(tStar, x[i], dy);

        temp = yLowerBoundary.getLeftMatrixCondition(tStar, x[i]);
        for (int k = 1; k < temp.length; k++) {
          sum -= temp[k] * vt[i][k];
        }
        vt[i][0] = sum / temp[0];

        temp = yUpperBoundary.getRightMatrixCondition(tStar, x[i]);
        sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * v[i][ySteps - k];
        }
        sum += yUpperBoundary.getConstant(tStar, x[i], dy);

        temp = yUpperBoundary.getLeftMatrixCondition(tStar, x[i]);
        for (int k = 1; k < temp.length; k++) {
          sum -= temp[k] * vt[i][ySteps - k];
        }
        vt[i][ySteps] = sum / temp[0];
      }

      // stag 2 - Implicit in x
      t += 0.5 * dt;
      for (int j = 0; j <= ySteps; j++) {
        for (int i = 1; i < xSteps; i++) {
          a = pdeData.getA(t, x[i], y[j]);
          b = pdeData.getB(t, x[i], y[j]);
          c = pdeData.getC(t, x[i], y[j]);

          mx[i][i - 1] = 0.5 * (dtdx2 * a - 0.5 * dtdx * b);
          mx[i][i] = 1 + 0.5 * (-2 * dtdx2 * a + 0.5 * dt * c);
          mx[i][i + 1] = 0.5 * (dtdx2 * a + 0.5 * dtdx * b);

          q[i] = vt[i][j];
        }

        double[] temp = xLowerBoundary.getLeftMatrixCondition(t, y[j]);
        for (int k = 0; k < temp.length; k++) {
          mx[0][k] = temp[k];
        }
        temp = xUpperBoundary.getLeftMatrixCondition(t, y[j]);
        for (int k = 0; k < temp.length; k++) {
          mx[xSteps][xSteps - k] = temp[k];
        }

        temp = xLowerBoundary.getRightMatrixCondition(t, y[j]);
        double sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * v[k][j];
        }
        q[0] = sum + xLowerBoundary.getConstant(t, y[j], dx);

        temp = xUpperBoundary.getRightMatrixCondition(t, y[j]);
        sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * v[xSteps - k][j];
        }
        q[xSteps] = sum + xUpperBoundary.getConstant(t, y[j], dx);

        // SOR
        final double omega = 1.5;
        double scale = 1.0;
        double errorSqr = Double.POSITIVE_INFINITY;
        int min, max;
        int count = 0;
        while (errorSqr / (scale + 1e-10) > 1e-18 && count < SOR_MAX) {
          errorSqr = 0.0;
          scale = 0.0;
          for (int l = 0; l <= xSteps; l++) {
            min = (l == xSteps ? 0 : Math.max(0, l - 1));
            max = (l == 0 ? xSteps : Math.min(xSteps, l + 1));
            sum = 0;
            // for (int k = 0; k <= xSteps; k++) {
            for (int k = min; k <= max; k++) { // mx is tri-diagonal so only need 3 steps here
              sum += mx[l][k] * vt[k][j];
            }
            final double correction = omega / mx[l][l] * (q[l] - sum);
            // if (freeBoundary != null) {
            // correction = Math.max(correction, freeBoundary.getZValue(t, x[j]) - f[j]);
            // }
            errorSqr += correction * correction;
            vt[l][j] += correction;
            scale += vt[l][j] * vt[l][j];
          }
          count++;
        }
        Validate.isTrue(count < SOR_MAX, "SOR exceeded max interations");
      }

      // stag 3 explicit in x
      tStar = t + 0.25 * dt;
      for (int j = 0; j <= ySteps; j++) {
        for (int i = 1; i < xSteps; i++) {

          a = pdeData.getA(t, x[i], y[j]);
          b = pdeData.getB(t, x[i], y[j]);
          c = pdeData.getC(t, x[i], y[j]);

          v[i][j] = (1 - 0.25 * c) * vt[i][j];
          v[i][j] -= 0.5 * dtdx2 * a * (vt[i + 1][j] + vt[i - 1][j] - 2 * vt[i][j]);
          v[i][j] -= 0.25 * dtdx * b * (vt[i + 1][j] - vt[i - 1][j]);
        }

        double[] temp = xLowerBoundary.getRightMatrixCondition(tStar, y[j]);
        double sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * vt[k][j];
        }
        sum += xLowerBoundary.getConstant(tStar, y[j], dx);

        temp = xLowerBoundary.getLeftMatrixCondition(tStar, y[j]);
        for (int k = 1; k < temp.length; k++) {
          sum -= temp[k] * v[k][j];
        }
        v[0][j] = sum / temp[0];

        temp = xUpperBoundary.getRightMatrixCondition(tStar, y[j]);
        sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * vt[xSteps - k][j];
        }
        sum += xUpperBoundary.getConstant(tStar, y[j], dx);

        temp = xUpperBoundary.getLeftMatrixCondition(tStar, y[j]);
        for (int k = 1; k < temp.length; k++) {
          sum -= temp[k] * v[xSteps - k][j];
        }
        v[xSteps][j] = sum / temp[0];
      }

      // stag 4 - implicit in y
      t = (n + 1) * dt;
      for (int i = 0; i <= xSteps; i++) {
        for (int j = 1; j < ySteps; j++) {

          c = pdeData.getC(t, x[i], y[j]);
          d = pdeData.getD(t, x[i], y[j]);
          f = pdeData.getF(t, x[i], y[j]);

          my[j][j - 1] = 0.5 * (dtdy2 * d - 0.5 * dtdy * f);
          my[j][j] = 1 + 0.5 * (-2 * dtdy2 * d + 0.5 * dt * c);
          my[j][j + 1] = 0.5 * (dtdy2 * d + 0.5 * dtdy * f);

          r[j] = v[i][j];
        }

        double[] temp = yLowerBoundary.getLeftMatrixCondition(t, x[i]);
        for (int k = 0; k < temp.length; k++) {
          my[0][k] = temp[k];
        }
        temp = yUpperBoundary.getLeftMatrixCondition(t, x[i]);
        for (int k = 0; k < temp.length; k++) {
          my[ySteps][ySteps - k] = temp[k];
        }

        temp = yLowerBoundary.getRightMatrixCondition(t, x[i]);
        double sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * vt[i][k];
        }
        r[0] = sum + yLowerBoundary.getConstant(t, x[i], dy);

        temp = yUpperBoundary.getRightMatrixCondition(t, x[i]);
        sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * vt[i][ySteps - k];
        }
        r[ySteps] = sum + yUpperBoundary.getConstant(t, x[i], dy);

        // SOR
        final double omega = 1.5;
        double scale = 1.0;
        double errorSqr = Double.POSITIVE_INFINITY;
        int count = 0;
        while (errorSqr / (scale + 1e-10) > 1e-18 && count < SOR_MAX) {
          errorSqr = 0.0;
          scale = 0.0;
          int min, max;
          for (int l = 0; l <= ySteps; l++) {
            min = (l == ySteps ? 0 : Math.max(0, l - 1));
            max = (l == 0 ? ySteps : Math.min(ySteps, l + 1));
            sum = 0;
            // for (int k = 0; k <= ySteps; k++) {
            for (int k = min; k <= max; k++) {
              sum += my[l][k] * v[i][k];
            }
            final double correction = omega / my[l][l] * (r[l] - sum);
            // if (freeBoundary != null) {
            // correction = Math.max(correction, freeBoundary.getZValue(t, x[j]) - f[j]);
            // }
            errorSqr += correction * correction;
            v[i][l] += correction;
            scale += v[i][l] * v[i][l];
          }
          count++;
        }
        Validate.isTrue(count < SOR_MAX, "SOR exceeded max interations");
      }

    } // time loop
    return v;

  }

  // private double[][] solveSOR(double[][] m, double[][] v)
}
