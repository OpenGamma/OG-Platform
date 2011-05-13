/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class RichardsonExstapolationFiniteDifference implements ConvectionDiffusionPDESolver {

  private final ConvectionDiffusionPDESolver _baseSolver;

  public RichardsonExstapolationFiniteDifference(ConvectionDiffusionPDESolver baseSolver) {
    Validate.notNull(baseSolver, "null baseSolver");
    _baseSolver = baseSolver;

  }

  @Override
  public PDEResults1D solve(ConvectionDiffusionPDEDataBundle pdeData, int tSteps, int xSteps, double tMax, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary) {
    return null;
  }

  @Override
  public PDEResults1D solve(ConvectionDiffusionPDEDataBundle pdeData, int tSteps, int xSteps, double tMax, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary,
      Surface<Double, Double, Double> freeBoundary) {
    return null;
  }

  @Override
  public PDEResults1D solve(ConvectionDiffusionPDEDataBundle pdeData, PDEGrid1D grid, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary) {
    return null;
  }

  @Override
  public PDEResults1D solve(ConvectionDiffusionPDEDataBundle pdeData, PDEGrid1D grid, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary, Surface<Double, Double, Double> freeBoundary) {
    // PDEGrid1D doubleTimeStep = new PDEGrid1D(grid., null)
    // res1 = _bas
    //
    return null;
  }

}
