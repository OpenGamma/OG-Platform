/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.surface.Surface;

/**
 * Explicit solver for the PDE $\frac{\partial f}{\partial t} + a(t,x) \frac{\partial^2 f}{\partial x^2} + b(t,x) \frac{\partial f}{\partial x} + (t,x)f = 0$
 * @deprecated This is for testing purposes and is not recommended for actual use. 
 */
@Deprecated
public class ExplicitFiniteDifference implements ConvectionDiffusionPDESolver {

  @Override
  public PDEResults1D solve(ConvectionDiffusionPDEDataBundle pdeData, int tSteps, int xSteps, double tMax, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary) {
    // simple test code - doesn't use a PDEGrid1D
    PDEGrid1D grid = new PDEGrid1D(tSteps + 1, xSteps + 1, tMax, lowerBoundary.getLevel(), upperBoundary.getLevel());

    final double dt = tMax / (tSteps);
    final double dx = (upperBoundary.getLevel() - lowerBoundary.getLevel()) / (xSteps);
    final double nu1 = dt / dx / dx;
    final double nu2 = dt / dx;

    double[] f = new double[xSteps + 1];
    final double[] x = new double[xSteps + 1];

    double currentX = lowerBoundary.getLevel();

    for (int j = 0; j <= xSteps; j++) {
      currentX = lowerBoundary.getLevel() + j * dx;
      x[j] = currentX;
      final double value = pdeData.getInitialValue(currentX);
      f[j] = value;
    }

    double t = 0.0;
    for (int i = 0; i < tSteps; i++) {
      final double[] fNew = new double[xSteps + 1];
      for (int j = 1; j < xSteps; j++) {
        final double a = pdeData.getA(t, x[j]);
        final double b = pdeData.getB(t, x[j]);
        final double c = pdeData.getC(t, x[j]);
        final double aa = -nu1 * a + 0.5 * nu2 * b;
        final double bb = 2 * nu1 * a - dt * c + 1;
        final double cc = -nu1 * a - 0.5 * nu2 * b;
        fNew[j] = aa * f[j - 1] + bb * f[j] + cc * f[j + 1];
      }

      double[] temp = lowerBoundary.getRightMatrixCondition(pdeData, grid, t);
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      double q = sum + lowerBoundary.getConstant(pdeData, t);

      sum = 0;
      temp = lowerBoundary.getLeftMatrixCondition(pdeData, grid, t);
      for (int k = 1; k < temp.length; k++) {
        sum += temp[k] * fNew[k];
      }
      fNew[0] = (q - sum) / temp[0];

      temp = upperBoundary.getRightMatrixCondition(pdeData, grid, t);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xSteps + k + 1 - temp.length];
      }
      q = sum + upperBoundary.getConstant(pdeData, t);

      sum = 0;
      temp = upperBoundary.getLeftMatrixCondition(pdeData, grid, t);
      for (int k = 0; k < temp.length - 1; k++) {
        sum += temp[k] * fNew[xSteps + k + 1 - temp.length];
      }

      fNew[xSteps] = (q - sum) / temp[temp.length - 1];

      // TODO American payoff
      t += dt;
      f = fNew;
    }

    return new PDETerminalResults1D(grid, f);

  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary, final Surface<Double, Double, Double> freeBoundary) {
    throw new NotImplementedException();
  }

  @Override
  public PDEResults1D solve(ConvectionDiffusionPDEDataBundle pdeData, PDEGrid1D grid, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary) {
    throw new NotImplementedException();
  }

  @Override
  public PDEResults1D solve(ConvectionDiffusionPDEDataBundle pdeData, PDEGrid1D grid, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary, Surface<Double, Double, Double> freeBoundary) {
    throw new NotImplementedException();
  }
}
