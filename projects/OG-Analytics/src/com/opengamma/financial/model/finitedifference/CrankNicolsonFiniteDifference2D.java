/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.math.cube.Cube;

/**
 * <b>Note</b> this is for testing purposes and is not recommended for actual use 
 */
public class CrankNicolsonFiniteDifference2D implements ConvectionDiffusionPDESolver2D {

  private final double _theta;

  /**
   * Sets up a standard Crank-Nicolson scheme for 2-D (two spatial dimensions) PDEs 
   */
  public CrankNicolsonFiniteDifference2D() {
    _theta = 0.5;
  }

  /**
   * Sets up a scheme that is the weighted average of an explicit and an implicit scheme 
   * @param theta The weight. theta = 0 - fully explicit, theta = 0.5 - Crank-Nicolson, theta = 1.0 - fully implicit 
   */
  public CrankNicolsonFiniteDifference2D(final double theta) {
    Validate.isTrue(theta >= 0 && theta <= 1.0, "theta must be in the range 0 to 1");
    _theta = theta;
  }

  @Override
  public double[][] solve(final ConvectionDiffusion2DPDEDataBundle pdeData, final int tSteps, final int xSteps, final int ySteps, final double tMax, final BoundaryCondition2D xLowerBoundary,
      final BoundaryCondition2D xUpperBoundary,
      final BoundaryCondition2D yLowerBoundary, final BoundaryCondition2D yUpperBoundary) {
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
    final double dtdxdy = dt / dy / dx;
    final int size = (xSteps + 1) * (ySteps + 1);

    final double[][] v = new double[xSteps + 1][ySteps + 1];
    final double[] u = new double[size];
    final double[] x = new double[xSteps + 1];
    final double[] y = new double[ySteps + 1];
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
      final int offset = j * (xSteps + 1);
      for (int i = 0; i <= xSteps; i++) {
        u[offset + i] = pdeData.getInitialValue(x[i], currentY);
      }
    }

    double t = 0.0;
    double a, b, c, d, e, f;
    final double[][] w = new double[size][9];

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

          double sum = 0;
          if (_theta != 1.0) {
            sum -= a * dtdx2 * (u[index + 1] - 2 * u[index] + u[index - 1]);
            sum -= b * dtdx / 2 * (u[index + 1] - u[index - 1]);
            sum -= c * dt * u[index];
            sum -= d * dtdy2 * (u[index + xSteps + 1] - 2 * u[index] + u[index - xSteps - 1]);
            sum -= e * dtdxdy / 4 * (u[index + xSteps + 2] + u[index - xSteps - 2] - u[index - xSteps] - u[index + xSteps]);
            sum -= f * dtdy / 2 * (u[index + xSteps + 1] - u[index - xSteps - 1]);

            // sum += dtdxdy * e / 4.0 * u[index - xSteps - 2];
            // sum += (dtdy2 * d - 0.5 * dtdy * f) * u[index - xSteps - 1];
            // sum += -dtdxdy * e / 4.0 * u[index - xSteps];
            // sum += (dtdx2 * a - 0.5 * dtdx * b) * u[index - 1];
            // sum += -(2 * dtdx2 * a - dt * c) - (2 * dtdy2 * d) * u[index];
            // sum += (dtdx2 * a + 0.5 * dtdx * b) * u[index + 1];
            // sum += -dtdxdy * e / 4.0 * u[index + xSteps];
            // sum += (dtdy2 * d + 0.5 * dtdy * f) * u[index + xSteps + 1];
            // sum += dtdxdy * e / 4.0 * u[index + xSteps + 2];
            sum *= (1 - _theta);
          }
          sum += u[index];

          // sum = v[i][j];

          q[index] = sum;

          w[index][0] = _theta * dtdxdy * e / 4.0;
          w[index][1] = _theta * (dtdy2 * d - 0.5 * dtdy * f);
          w[index][2] = -_theta * dtdxdy * e / 4.0;

          w[index][3] = _theta * (dtdx2 * a - 0.5 * dtdx * b);
          w[index][4] = 1 - _theta * ((2 * dtdx2 * a - dt * c) + (2 * dtdy2 * d));
          w[index][5] = _theta * (dtdx2 * a + 0.5 * dtdx * b);

          w[index][6] = -_theta * dtdxdy * e / 4.0;
          w[index][7] = _theta * (dtdy2 * d + 0.5 * dtdy * f);
          w[index][8] = _theta * dtdxdy * e / 4.0;

        }
      }

      // The y boundary conditions
      final double[][][] yBoundary = new double[2][xSteps + 1][];

      for (int i = 0; i <= xSteps; i++) {
        yBoundary[0][i] = yLowerBoundary.getLeftMatrixCondition(pdeData, t, x[i]);
        yBoundary[1][i] = yUpperBoundary.getLeftMatrixCondition(pdeData, t, x[i]);

        double[] temp = yLowerBoundary.getRightMatrixCondition(pdeData, t, x[i]);
        double sum = 0;
        for (int k = 0; k < temp.length; k++) {
          final int offset = k * (xSteps + 1);
          sum += temp[k] * u[offset + i];
        }
        q[i] = sum + yLowerBoundary.getConstant(pdeData, t, x[i], dy);

        temp = yUpperBoundary.getRightMatrixCondition(pdeData, t, x[i]);
        sum = 0;
        for (int k = 0; k < temp.length; k++) {
          final int offset = (ySteps - k) * (xSteps + 1);
          sum += temp[k] * u[offset + i];
        }
        q[i + ySteps * (xSteps + 1)] = sum + xUpperBoundary.getConstant(pdeData, t, x[i], dy);
      }

      // The x boundary conditions
      final double[][][] xBoundary = new double[2][ySteps - 1][];

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

            final double correction = omega / w[l][4] * (q[l] - sum);
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
          final double[] temp = yBoundary[0][i];
          for (int k = 0; k < temp.length; k++) {
            final int offset = k * (xSteps + 1);
            sum += temp[k] * u[offset + i];
          }
          final double correction = omega / temp[0] * (q[l] - sum);
          errorSqr += correction * correction;
          u[l] += correction;
          scale += u[l] * u[l];
        }

        // the upper y boundary
        for (int i = 0; i <= xSteps; i++) {
          sum = 0;
          l = (xSteps + 1) * ySteps + i;
          final double[] temp = yBoundary[1][i];
          for (int k = 0; k < temp.length; k++) {
            final int offset = (ySteps - k) * (xSteps + 1);
            sum += temp[k] * u[offset + i];
          }
          final double correction = omega / temp[0] * (q[l] - sum);
          errorSqr += correction * correction;
          u[l] += correction;
          scale += u[l] * u[l];
        }

        // the lower x boundary
        for (int j = 1; j < ySteps; j++) {
          sum = 0;
          l = j * (xSteps + 1);
          final double[] temp = xBoundary[0][j - 1];
          for (int k = 0; k < temp.length; k++) {
            sum += temp[k] * u[l + k];
          }
          final double correction = omega / temp[0] * (q[l] - sum);
          errorSqr += correction * correction;
          u[l] += correction;
          scale += u[l] * u[l];
        }

        // the upper x boundary
        for (int j = 1; j < ySteps; j++) {
          sum = 0;
          l = (j + 1) * (xSteps + 1) - 1;
          final double[] temp = xBoundary[1][j - 1];
          for (int k = 0; k < temp.length; k++) {
            sum += temp[k] * u[l - k];
          }
          final double correction = omega / temp[0] * (q[l] - sum);
          errorSqr += correction * correction;
          u[l] += correction;
          scale += u[l] * u[l];
        }

      } // end of SOR

    } // time loop

    // unpack vector to matrix
    for (int j = 0; j <= ySteps; j++) {
      final int offset = j * (xSteps + 1);
      for (int i = 0; i <= xSteps; i++) {
        v[i][j] = u[offset + i];
      }
    }
    return v;

  }

  // private double[][] solveSOR(double[][] m, double[][] v)

}
