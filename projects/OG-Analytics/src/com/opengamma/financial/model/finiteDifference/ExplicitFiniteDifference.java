/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;


/**
 * Explicit solver for the PDE $\frac{\partial f}{\partial t} + a(t,x) \frac{\partial^2 f}{\partial x^2}$ + b(t,x) \frac{\partial f}{\partial x} + (t,x)V = 0$
 */
public class ExplicitFiniteDifference {

  public double[] solve(ConvectionDiffusionPDEDataBundle pdeData, final int timeSteps, final int priceSteps, final double tMax, final double xMin, final double xMax) {

    double dt = tMax / (timeSteps);
    double dx = (xMax - xMin) / (priceSteps);
    double nu1 = dt / dx / dx;
    double nu2 = dt / dx;

    double[] f = new double[priceSteps + 1];
    double[] x = new double[priceSteps + 1];

    double currentX = xMin;

    for (int j = 0; j <= priceSteps; j++) {
      currentX = xMin + j * dx;
      x[j] = currentX;
      double value = pdeData.getInitialValue(currentX);
      f[j] = value;
    }

    double t = 0.0;
    for (int i = 0; i < timeSteps; i++) {
      double[] fNew = new double[priceSteps + 1];
      for (int j = 1; j < priceSteps; j++) {
        double a = pdeData.getA(t, x[j]);
        double b = pdeData.getB(t, x[j]);
        double c = pdeData.getC(t, x[j]);
        double aa = -nu1 * a + 0.5 * nu2 * b;
        double bb = 2 * nu1 * a - dt * c + 1;
        double cc = -nu1 * a - 0.5 * nu2 * b;
        fNew[j] = aa * f[j - 1] + bb * f[j] + cc * f[j + 1];
      }

      fNew[0] = f[0];
      fNew[priceSteps] = 2 * fNew[priceSteps - 1] - fNew[priceSteps - 2]; // TODO proper handling of boundary conditions
      // TODO American payoff
      t += dt;
      f = fNew;
    }

    return f;

  }
}
