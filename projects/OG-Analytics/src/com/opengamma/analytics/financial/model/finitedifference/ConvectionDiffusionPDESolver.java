/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import com.opengamma.analytics.math.surface.Surface;

/**
 * Solver for convection-diffusion type partial differential equations (PDEs), i.e.
 * $\frac{\partial f}{\partial t} + a(t,x) \frac{\partial^2 f}{\partial x^2} + b(t,x) \frac{\partial f}{\partial x} + (t,x)f = 0$
 * This follows the physical convention of time starting at zero and moving
 * forward to some desired point tMax. For the financial convention of 'time'
 * starting at maturity and moving backwards to zero, simply set tMax equal to
 * maturity and transform the PDE to be in terms of 'time-to-maturity'
 */
public interface ConvectionDiffusionPDESolver {

  /**
   * Solver for convection-diffusion type partial differential equations (PDEs), i.e.
   * $\frac{\partial f}{\partial t} + a(t,x) \frac{\partial^2 f}{\partial x^2} + b(t,x) \frac{\partial f}{\partial x} + (t,x)f = 0$
   * @param pdeData Data bundle holding a description of the PDE
   * @param tSteps Number of steps in the time direction (Note: The number of grid points in the time direction  will be tSteps + 1)
   * @param xSteps Number of steps in the spatial direction (Note: The number of grid points in the spatial direction  will be xSteps + 1)
   * @param tMax Time starts at zero (where the initial condition is set) and runs to tMax (where the solution is taken)
   * @param lowerBoundary Descriptor of the lower boundary in $x$
   * @param upperBoundary Descriptor of the upper boundary in $x$
   * @return An 2 by (xSteps + 1) array, with the first column giving the spatial (x) grid points and the second given the value of the fucntion $f$ at tMax and these grid points
   */
  PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary);

  /**
   * Solver for convection-diffusion type partial differential equations (PDEs), i.e.
   * $\frac{\partial f}{\partial t} + a(t,x) \frac{\partial^2 f}{\partial x^2} + b(t,x) \frac{\partial f}{\partial x} + (t,x)f = 0$
   * @param pdeData Data bundle holding a description of the PDE
   * @param tSteps Number of steps in the time direction (Note: The number of grid points in the time direction  will be tSteps + 1)
   * @param xSteps Number of steps in the spatial direction (Note: The number of grid points in the spatial direction  will be xSteps + 1)
   * @param tMax Time starts at zero (where the initial condition is set) and runs to tMax (where the solution is taken)
   * @param lowerBoundary Descriptor of the lower boundary in x
   * @param upperBoundary Descriptor of the upper boundary in x
   * @param freeBoundary A surface g(t,x) such that f+(t,x) = max(f-(t,x),g(t,x)), where f-(t,x) is the normal solution at (t,x) and f+(t,x) is the updated solution to be used
   * in the next time step. E.g. for a American put where x is the stock price, then g(t,x) = max(k-x,0), where k is the strike
   * @return An 2 by (xSteps + 1) array, with the first column  giving the spatial (x) grid points and the second given the value of the fucntion f at tMax and these grid points
   */
  PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary, final Surface<Double, Double, Double> freeBoundary);

  PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary);

  PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary,
      final Surface<Double, Double, Double> freeBoundary);
}
