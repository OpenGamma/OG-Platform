/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.cube.Cube;

/**
 *  Operating splitting (as in Duffy chapter 22) with boundary conditions applied at each of the 4 steps
 * <b>Note</b> this is for testing purposes and is not recommended for actual use
 */
@SuppressWarnings("deprecation")
public class OperatorSplittingFiniteDifference2D implements ConvectionDiffusionPDESolver2D {

  // private static final Decomposition<?> DCOMP = new LUDecompositionCommons();
  // Theta = 0 - explicit
  // private static final double THETA = 0.5;
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
    final double dtdxdy = dt / dx / dy;

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

    double t;
    double a, b, c, d, e, f;

    for (int n = 0; n < tSteps; n++) {

      t = n * dt;
      // stag 1 Explicit in the cross
      for (int i = 1; i < xSteps; i++) {
        for (int j = 1; j < ySteps; j++) {
          e = pdeData.getE(t, x[i], y[j]);
          vt[i][j] = v[i][j];
          vt[i][j] -= 0.125 * dtdxdy * e * (v[i + 1][j + 1] + v[i - 1][j - 1] - v[i + 1][j - 1] - v[i - 1][j + 1]);
        }
        // the explicit intermediate stag vt is missed the boundary
        vt[i][0] = v[i][0];
        vt[i][ySteps] = v[i][ySteps];
      }

      // stag 2 - Implicit in x
      t += 0.5 * dt;
      for (int j = 0; j <= ySteps; j++) {
        for (int i = 1; i < xSteps; i++) {
          a = pdeData.getA(t, x[i], y[j]);
          b = pdeData.getB(t, x[i], y[j]);
          c = pdeData.getC(t, x[i], y[j]);

          mx[i][i - 1] = (dtdx2 * a - 0.5 * dtdx * b);
          mx[i][i] = 1 + (-2 * dtdx2 * a + dt * c);
          mx[i][i + 1] = (dtdx2 * a + 0.5 * dtdx * b);

          q[i] = vt[i][j];
        }

        // it is not clear that these boundary conditions apply in the intermediate stage of operator splitting
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

      for (int j = 1; j < ySteps; j++) {
        for (int i = 1; i < xSteps; i++) {
          e = pdeData.getE(t, x[i], y[j]);
          v[i][j] = vt[i][j];
          v[i][j] -= 0.125 * dtdxdy * e * (vt[i + 1][j + 1] + vt[i - 1][j - 1] - vt[i + 1][j - 1] - vt[i - 1][j + 1]);
        }
        // again now v on the boundary is undefined
        v[0][j] = vt[0][j];
        v[xSteps][j] = vt[xSteps][j];
      }

      // stag 4 - implicit in y
      t = (n + 1) * dt;
      for (int i = 0; i <= xSteps; i++) {
        for (int j = 1; j < ySteps; j++) {

          d = pdeData.getD(t, x[i], y[j]);
          f = pdeData.getF(t, x[i], y[j]);

          my[j][j - 1] = (dtdy2 * d - 0.5 * dtdy * f);
          my[j][j] = 1 + (-2 * dtdy2 * d);
          my[j][j + 1] = (dtdy2 * d + 0.5 * dtdy * f);

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
