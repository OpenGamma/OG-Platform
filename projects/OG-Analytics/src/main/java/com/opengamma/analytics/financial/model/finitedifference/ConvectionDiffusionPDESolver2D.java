/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import com.opengamma.analytics.math.cube.Cube;

/**
 * Solver for 2D (i.e. 2  spatial dimensions and time) convection-diffusion
 * type partial differential equations (PDEs), i.e.
 * $\frac{\partial f}{\partial t} + a(t,x,y) \frac{\partial^2 f}{\partial x^2} + b(t,x,y) \frac{\partial f}{\partial x} + c(t,x,y)f
 * + d(t,x,y) \frac{\partial^2 f}{\partial y^2} + e(t,x,y) \frac{\partial^2 f}{\partial x \partial y} + f(t,x,y) \frac{\partial f}{\partial y} = 0$
 * This follows the physical convention of time starting at zero and moving
 * forward to some desired point tMax. For the financial convention of 'time'
 * starting at maturity and moving backwards to zero, simply set tMax equal to
 * maturity and transform the PDE to be in terms of 'time-to-maturity'
 */
@SuppressWarnings("deprecation")
public interface ConvectionDiffusionPDESolver2D {

  /**
   * Solver for 2D (i.e. 2  spatial dimensions and time) convection-diffusion
   * type partial differential equations (PDEs), i.e.
   * $\frac{\partial f}{\partial t} + a(t,x,y) \frac{\partial^2 f}{\partial x^2} + b(t,x,y) \frac{\partial f}{\partial x} + c(t,x,y)f
   * + d(t,x,y) \frac{\partial^2 f}{\partial y^2} + e(t,x,y) \frac{\partial^2 f}{\partial x \partial y} + f(t,x,y) \frac{\partial f}{\partial y} = 0$
   * @param pdeData Data bundle holding a description of the PDE
   * @param tSteps Number of steps in the time direction (Note: The number of grid points in the time direction  will be tSteps + 1)
   * @param xSteps Number of steps in the first spatial direction (Note: The number of grid points in this  direction  will be xSteps + 1)
   * @param ySteps Number of steps in the second spatial direction (Note: The number of grid points in this  direction  will be ySteps + 1)
   * @param tMax Time starts at zero (where the initial condition is set) and runs to tMax (where the solution is taken)
   * @param xLowerBoundary Descriptor of the lower boundary in $x$
   * @param xUpperBoundary Descriptor of the upper boundary in $x$
   * @param yLowerBoundary Descriptor of the lower boundary in $y$
   * @param yUpperBoundary Descriptor of the upper boundary in $y$
   * @return An array of the function value on the spatial grid at tMax
   */
  double[][] solve(ConvectionDiffusion2DPDEDataBundle pdeData, final int tSteps, final int xSteps, final int ySteps, final double tMax, BoundaryCondition2D xLowerBoundary,
      BoundaryCondition2D xUpperBoundary, BoundaryCondition2D yLowerBoundary, BoundaryCondition2D yUpperBoundary);

  /**
   * Solver for 2D (i.e. 2  spatial dimensions and time) convection-diffusion type partial differential equations (PDEs), i.e.
   * $\frac{\partial f}{\partial t} + a(t,x,y) \frac{\partial^2 f}{\partial x^2} + b(t,x,y) \frac{\partial f}{\partial x} + c(t,x,y)f
   * + d(t,x,y) \frac{\partial^2 f}{\partial y^2} + e(t,x,y) \frac{\partial^2 f}{\partial x \partial y} + f(t,x,y) \frac{\partial f}{\partial y} = 0$
   * @param pdeData Data bundle holding a description of the PDE
   * @param tSteps Number of steps in the time direction (Note: The number of grid points in the time direction  will be tSteps + 1)
   * @param xSteps Number of steps in the first spatial direction (Note: The number of grid points in this  direction  will be xSteps + 1)
   * @param ySteps Number of steps in the second spatial direction (Note: The number of grid points in this  direction  will be ySteps + 1)
   * @param tMax Time starts at zero (where the initial condition is set) and runs to tMax (where the solution is taken)
   * @param xLowerBoundary Descriptor of the lower boundary in x
   * @param xUpperBoundary Descriptor of the upper boundary in x
   * @param yLowerBoundary Descriptor of the lower boundary in y
   * @param yUpperBoundary Descriptor of the upper boundary in y
   * @param freeBoundary A cube g(t,x,y) such that f+(t,x,y) = max(f-(t,x,y),g(t,x,y)), where f-(t,x,y) is the normal solution at (t,x,y) and f+(t,x,y) is the updated solution to be used
   * in the next time step. E.g. for a American put under the Heston model where x is the stock price and y is the vol-squared, then g(t,x,y) = max(k-x,0), where k is the strike
   * @return An array of the function value on the spatial grid at tMax
   */
  double[][] solve(ConvectionDiffusion2DPDEDataBundle pdeData, final int tSteps, final int xSteps, final int ySteps, final double tMax, BoundaryCondition2D xLowerBoundary,
      BoundaryCondition2D xUpperBoundary, BoundaryCondition2D yLowerBoundary, BoundaryCondition2D yUpperBoundary, final Cube<Double, Double, Double, Double> freeBoundary);

}
