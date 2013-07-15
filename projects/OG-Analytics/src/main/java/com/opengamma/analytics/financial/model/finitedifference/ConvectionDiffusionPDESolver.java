/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

/**
 * Solver for convection-diffusion type partial differential equations (PDEs), i.e.
 * $\frac{\partial f}{\partial t} + a(t,x) \frac{\partial^2 f}{\partial x^2} + b(t,x) \frac{\partial f}{\partial x} + (t,x)f = 0$
 * This follows the physical convention of time starting at zero and moving
 * forward to some desired point tMax. For the financial convention of 'time'
 * starting at maturity and moving backwards to zero, simply set tMax equal to
 * maturity and transform the PDE to be in terms of 'time-to-maturity'
 */
public interface ConvectionDiffusionPDESolver extends PDE1DSolver<ConvectionDiffusionPDE1DCoefficients> {

  @Override
  PDEResults1D solve(PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> pdeData);
}
