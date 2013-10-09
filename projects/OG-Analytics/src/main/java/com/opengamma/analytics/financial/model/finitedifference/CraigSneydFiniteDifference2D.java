/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.cube.Cube;

/**
 * Craig-Sneyd splitting
 * <b>Note</b> this is for testing purposes and is not recommended for actual use
 *
 */
@SuppressWarnings("deprecation")
public class CraigSneydFiniteDifference2D implements ConvectionDiffusionPDESolver2D {

  // private static final Decomposition<?> DCOMP = new LUDecompositionCommons();
  // Theta = 0 - explicit
  private static final double THETA = 0.5;

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
    initializeMatrices(pdeData, xSteps, ySteps, xLowerBoundary, yLowerBoundary, dx, dy, v, x, y);

    double t = 0.0;
    double a, b, c, d, e, f;

    for (int n = 0; n < tSteps; n++) {

      // stag 1 full Explicit
      for (int i = 1; i < xSteps; i++) {
        for (int j = 1; j < ySteps; j++) {
          a = pdeData.getA(t, x[i], y[j]);
          b = pdeData.getB(t, x[i], y[j]);
          c = pdeData.getC(t, x[i], y[j]);
          d = pdeData.getD(t, x[i], y[j]);
          e = pdeData.getE(t, x[i], y[j]);
          f = pdeData.getF(t, x[i], y[j]);

          vt[i][j] = (1 - dt * (1 - 0.5 * THETA) * c) * v[i][j];
          vt[i][j] -= dtdx2 * a * (1 - THETA) * (v[i + 1][j] + v[i - 1][j] - 2 * v[i][j]);
          vt[i][j] -= 0.5 * dtdx * b * (1 - THETA) * (v[i + 1][j] - v[i - 1][j]);
          vt[i][j] -= dtdy2 * d * (v[i][j + 1] + v[i][j - 1] - 2 * v[i][j]);
          // upwind
          // if (f > 0) {
          // vt[i][j] -= dtdy * f * (v[i][j] - v[i][j - 1]);
          // } else if (f < 0) {
          // vt[i][j] -= dtdy * f * (v[i][j + 1] - v[i][j]);
          // }
          vt[i][j] -= 0.5 * dtdy * f * (v[i][j + 1] - v[i][j - 1]);
          vt[i][j] -= 0.25 * dtdxdy * e * (v[i + 1][j + 1] + v[i - 1][j - 1] - v[i + 1][j - 1] - v[i - 1][j + 1]);
        }

        // really not sure what to do with boundary conditions in these intermediate steps
        vt[i][0] = v[i][0];
        vt[i][ySteps] = v[i][ySteps];
      }

      // for (int i = 0; i <= xSteps; i++) {
      // double[] temp = yLowerBoundary.getRightMatrixCondition(pdeData, t, x[i]);
      // double sum = 0;
      // for (int k = 0; k < temp.length; k++) {
      // sum += temp[k] * v[i][k];
      // }
      // sum += yLowerBoundary.getConstant(pdeData, t, x[i], dy);
      //
      // temp = yLowerBoundary.getLeftMatrixCondition(pdeData, t, x[i]);
      // for (int k = 1; k < temp.length; k++) {
      // sum -= temp[k] * vt[i][k];
      // }
      // vt[i][0] = sum / temp[0];
      //
      // temp = yUpperBoundary.getRightMatrixCondition(pdeData, t, x[i]);
      // sum = 0;
      // for (int k = 0; k < temp.length; k++) {
      // sum += temp[k] * v[i][ySteps - k];
      // }
      // sum += yUpperBoundary.getConstant(pdeData, t, x[i], dy);
      //
      // temp = yUpperBoundary.getLeftMatrixCondition(pdeData, t, x[i]);
      // for (int k = 1; k < temp.length; k++) {
      // sum -= temp[k] * vt[i][ySteps - k];
      // }
      // vt[i][ySteps] = sum / temp[0];
      // }
      //
      // for (int j = 1; j < ySteps; j++) {
      // double[] temp = xLowerBoundary.getRightMatrixCondition(pdeData, t, y[j]);
      // double sum = 0;
      // for (int k = 0; k < temp.length; k++) {
      // sum += temp[k] * v[k][j];
      // }
      // sum += xLowerBoundary.getConstant(pdeData, t, y[j], dx);
      //
      // temp = xLowerBoundary.getLeftMatrixCondition(pdeData, t, y[j]);
      // for (int k = 1; k < temp.length; k++) {
      // sum -= temp[k] * vt[k][j];
      // }
      // vt[0][j] = sum / temp[0];
      //
      // temp = xUpperBoundary.getRightMatrixCondition(pdeData, t, y[j]);
      // sum = 0;
      // for (int k = 0; k < temp.length; k++) {
      // sum += temp[k] * v[xSteps - k][j];
      // }
      // sum += xUpperBoundary.getConstant(pdeData, t, y[j], dx);
      //
      // temp = xUpperBoundary.getLeftMatrixCondition(pdeData, t, y[j]);
      // for (int k = 1; k < temp.length; k++) {
      // sum -= temp[k] * vt[xSteps - k][j];
      // }
      // vt[xSteps][j] = sum / temp[0];
      // }

      // stag 2 implicit in x
      t += dt / 2;
      for (int j = 0; j <= ySteps; j++) {
        for (int i = 1; i < xSteps; i++) {
          a = pdeData.getA(t, x[i], y[j]);
          b = pdeData.getB(t, x[i], y[j]);
          c = pdeData.getC(t, x[i], y[j]);

          mx[i][i - 1] = THETA * (dtdx2 * a - 0.5 * dtdx * b);
          mx[i][i] = 1 + THETA * (-2 * dtdx2 * a + 0.5 * dt * c);
          mx[i][i + 1] = THETA * (dtdx2 * a + 0.5 * dtdx * b);

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
        final int count = sor(xSteps, vt, q, mx, j, omega);
        Validate.isTrue(count < 1000, "SOR exceeded max iterations");
      }

      // stag 3 explicit in y
      for (int i = 0; i <= xSteps; i++) {
        for (int j = 1; j < ySteps; j++) {

          c = pdeData.getC(t, x[i], y[j]);
          d = pdeData.getD(t, x[i], y[j]);
          f = pdeData.getF(t, x[i], y[j]);

          vt[i][j] += THETA * 0.5 * dt * c * v[i][j];
          vt[i][j] += THETA * dtdy2 * d * (v[i][j + 1] + v[i][j - 1] - 2 * v[i][j]);

          // upwind
          // if (f > 0) {
          // vt[i][j] += THETA * dtdy * f * (v[i][j] - v[i][j - 1]);
          // } else if (f < 0) {
          // vt[i][j] += THETA * dtdy * f * (v[i][j + 1] - v[i][j]);
          // }
          vt[i][j] += THETA * 0.5 * dtdy * f * (v[i][j + 1] - v[i][j - 1]);
        }

        // double[] temp = yLowerBoundary.getRightMatrixCondition(pdeData, t, x[i]);
        // double sum = 0;
        // for (int k = 0; k < temp.length; k++) {
        // sum += temp[k] * v[i][k];
        // }
        // sum += yLowerBoundary.getConstant(pdeData, t, x[i], dy);
        //
        // temp = yLowerBoundary.getLeftMatrixCondition(pdeData, t, x[i]);
        // for (int k = 1; k < temp.length; k++) {
        // sum -= temp[k] * vt[i][k];
        // }
        // vt[i][0] = sum / temp[0];
        //
        // temp = yUpperBoundary.getRightMatrixCondition(pdeData, t, x[i]);
        // sum = 0;
        // for (int k = 0; k < temp.length; k++) {
        // sum += temp[k] * v[i][ySteps - k];
        // }
        // sum += yUpperBoundary.getConstant(pdeData, t, x[i], dy);
        //
        // temp = yUpperBoundary.getLeftMatrixCondition(pdeData, t, x[i]);
        // for (int k = 1; k < temp.length; k++) {
        // sum -= temp[k] * vt[i][ySteps - k];
        // }
        // vt[i][ySteps] = sum / temp[0];
      }

      // The y = 0 and y = yStep boundary values are assumed the same as the previous sub-step
      // Again we could apply the y boundary conditions here

      // stag 4 implicit in y
      for (int i = 0; i <= xSteps; i++) {
        for (int j = 1; j < ySteps; j++) {

          c = pdeData.getC(t, x[i], y[j]);
          d = pdeData.getD(t, x[i], y[j]);
          f = pdeData.getF(t, x[i], y[j]);

          // upwind
          // if (f > 0) {
          // my[j][j - 1] = THETA * (dtdy2 * d - dtdy * f);
          // my[j][j] = 1 + THETA * (-2 * dtdy2 * d + dtdy * f + 0.5 * dt * c);
          // my[j][j + 1] = THETA * (dtdy2 * d);
          // } else if (f < 0) {
          // my[j][j - 1] = THETA * (dtdy2 * d);
          // my[j][j] = 1 + THETA * (-2 * dtdy2 * d - dtdy * f + 0.5 * dt * c);
          // my[j][j + 1] = THETA * (dtdy2 * d + dtdy * f);
          // }
          my[j][j - 1] = THETA * (dtdy2 * d - 0.5 * dtdy * f);
          my[j][j] = 1 + THETA * (-2 * dtdy2 * d + 0.5 * dt * c);
          my[j][j + 1] = THETA * (dtdy2 * d + 0.5 * dtdy * f);

          r[j] = vt[i][j];
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
          sum += temp[k] * v[i][k];
        }
        r[0] = sum + yLowerBoundary.getConstant(t, x[i], dy);

        temp = yUpperBoundary.getRightMatrixCondition(t, x[i]);
        sum = 0;
        for (int k = 0; k < temp.length; k++) {
          sum += temp[k] * v[i][ySteps - k];
        }
        r[ySteps] = sum + yUpperBoundary.getConstant(t, x[i], dy);

        // SOR
        final double omega = 1.5;
        double scale = 1.0;
        double errorSqr = Double.POSITIVE_INFINITY;
        int count = 0;
        while (errorSqr / (scale + 1e-10) > 1e-18 && count < 1000) {
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
        Validate.isTrue(count < 1000, "SOR exceeded max interations");
      }

    } // time loop
    return v;

  }

  // TODO there is almost identical code lines 297-319
  private int sor(final int steps, final double[][] v, final double[] q, final double[][] mx, final int j, final double omega) {
    double sum;
    int min;
    int max;
    int count = 0;
    double scale = 1.0;
    double errorSqr = Double.POSITIVE_INFINITY;
    while (errorSqr / (scale + 1e-10) > 1e-18 && count < 1000) {
      errorSqr = 0.0;
      scale = 0.0;
      for (int l = 0; l <= steps; l++) {
        min = (l == steps ? 0 : Math.max(0, l - 1));
        max = (l == 0 ? steps : Math.min(steps, l + 1));
        sum = 0;
        // for (int k = 0; k <= xSteps; k++) {
        for (int k = min; k <= max; k++) { // mx is tri-diagonal so only need 3 steps here
          sum += mx[l][k] * v[k][j];
        }
        final double correction = omega / mx[l][l] * (q[l] - sum);
        // if (freeBoundary != null) {
        // correction = Math.max(correction, freeBoundary.getZValue(t, x[j]) - f[j]);
        // }
        errorSqr += correction * correction;
        v[l][j] += correction;
        scale += v[l][j] * v[l][j];
      }
      count++;
    }
    return count;
  }

  private void initializeMatrices(final ConvectionDiffusion2DPDEDataBundle pdeData, final int xSteps, final int ySteps, final BoundaryCondition2D xLowerBoundary,
      final BoundaryCondition2D yLowerBoundary, final double dx, final double dy, final double[][] v, final double[] x, final double[] y) {
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
  }

  // private double[][] solveSOR(double[][] m, double[][] v)
}
